package com.example.data.family

/**
 * Servidor en memoria con las MISMAS reglas que firebase/firestore.rules
 * (código de un solo uso, 10 minutos, rol correcto, máximo dos adultos).
 * Las reglas reales se prueban aparte contra el emulador de Firestore.
 */
class FakeServer(var now: () -> Long = { System.currentTimeMillis() }) {
    data class Code(val familyId: String, val role: LinkRole, val createdAt: Long, var used: Boolean = false)

    val families = mutableMapOf<String, FamilyInfo>()
    val codes = mutableMapOf<String, Code>()
    val profiles = mutableMapOf<String, RemoteProfile>()
    val requests = mutableMapOf<String, MutableList<CompanionRequest>>()
    val weeks = mutableMapOf<String, SharedWeek>()
    private val requestListeners = mutableListOf<() -> Unit>()
    private val weekListeners = mutableListOf<() -> Unit>()

    fun requestsChanged() = requestListeners.toList().forEach { it() }
    fun weekChanged() = weekListeners.toList().forEach { it() }
    fun onRequests(l: () -> Unit): Subscription { requestListeners += l; l(); return Subscription { requestListeners -= l } }
    fun onWeek(l: () -> Unit): Subscription { weekListeners += l; l(); return Subscription { weekListeners -= l } }
    private val listeners = mutableMapOf<String, MutableList<(FamilyInfo?) -> Unit>>()
    var online = true
    private var counter = 0

    fun nextId(prefix: String) = "$prefix${++counter}"

    fun update(id: String, change: (FamilyInfo) -> FamilyInfo) {
        val updated = change(families.getValue(id))
        families[id] = updated
        listeners[id].orEmpty().toList().forEach { it(updated) }
    }

    fun listen(id: String, l: (FamilyInfo?) -> Unit): Subscription {
        listeners.getOrPut(id) { mutableListOf() }.add(l)
        l(families[id])
        return Subscription { listeners[id]?.remove(l) }
    }
}

class FakeFamilyBackend(
    private val server: FakeServer,
    private val googleName: String = "Juan"
) : FamilyBackend {
    private var uid: String? = null
    private var google = false

    override val currentUid: String? get() = uid
    override val isSignedInWithGoogle: Boolean get() = google && uid != null
    override val currentDisplayName: String? get() = if (google) googleName else null

    private fun requireOnline() {
        if (!server.online) throw BackendUnavailable()
    }

    override suspend fun signInWithGoogle(idToken: String): AdultAccount {
        requireOnline()
        uid = "google_$idToken"
        google = true
        return AdultAccount(uid!!, googleName)
    }

    override suspend fun signInAnonymously(): String {
        requireOnline()
        if (uid == null) uid = server.nextId("anon_")
        return uid!!
    }

    override fun signOut() {
        uid = null
        google = false
    }

    override suspend fun loadProfile(): RemoteProfile? {
        requireOnline()
        return uid?.let { server.profiles[it] }
    }

    override suspend fun createFamily(adultName: String, childName: String): String {
        requireOnline()
        val me = uid ?: error("sin sesión")
        check(google) { "solo adultos con Google crean familias" }
        val id = server.nextId("fam_")
        server.families[id] = FamilyInfo(id, me, listOf(me), mapOf(me to adultName), null, childName)
        server.profiles[me] = RemoteProfile(DeviceRole.ADULT, id)
        return id
    }

    override suspend fun publishLinkCode(familyId: String, role: LinkRole, codeHash: String) {
        requireOnline()
        val f = server.families.getValue(familyId)
        check(uid in f.adultUids)
        if (role == LinkRole.ADULT) check(f.ownerUid == uid && f.adultUids.size < 2)
        server.codes[codeHash] = FakeServer.Code(familyId, role, server.now())
    }

    override suspend fun redeemLinkCode(codeHash: String, role: LinkRole, displayName: String): LinkResult {
        if (!server.online) return LinkResult.NoConnection
        val me = uid ?: return LinkResult.InvalidCode
        val code = server.codes[codeHash] ?: return LinkResult.InvalidCode
        val expired = server.now() >= code.createdAt + 10 * 60_000L
        if (code.used || expired || code.role != role) return LinkResult.InvalidCode
        val f = server.families[code.familyId] ?: return LinkResult.InvalidCode
        if (role == LinkRole.ADULT && (!google || f.adultUids.size >= 2)) return LinkResult.InvalidCode
        code.used = true
        server.update(f.id) {
            if (role == LinkRole.CHILD) it.copy(childUid = me)
            else it.copy(adultUids = it.adultUids + me, adultNames = it.adultNames + (me to displayName))
        }
        server.profiles[me] = RemoteProfile(if (role == LinkRole.CHILD) DeviceRole.CHILD else DeviceRole.ADULT, f.id)
        return LinkResult.Linked(f.id)
    }

    override fun observeFamily(familyId: String, onChange: (FamilyInfo?) -> Unit): Subscription =
        server.listen(familyId) { info ->
            // Como en las reglas: solo los miembros pueden leer la familia.
            val member = info != null && (uid in info.adultUids || info.childUid == uid)
            onChange(if (member) info else null)
        }

    override suspend fun unlinkChild(familyId: String) {
        requireOnline()
        server.weeks.remove(familyId); server.weekChanged()
        server.update(familyId) { it.copy(childUid = null) }
    }

    override suspend fun removeSecondAdult(familyId: String, ownerUid: String) {
        requireOnline()
        server.update(familyId) { it.copy(adultUids = listOf(ownerUid)) }
    }

    override suspend fun childLeaves(familyId: String) {
        requireOnline()
        server.weeks.remove(familyId); server.weekChanged()
        server.update(familyId) { it.copy(childUid = null) }
    }

    // ------------------------------------------------------------------ Mis salvavidas

    override suspend fun sendCompanionRequest(familyId: String, toUids: List<String>, reason: CompanionReason?): List<String> {
        requireOnline()
        val f = server.families.getValue(familyId)
        check(f.childUid == uid && toUids.isNotEmpty() && toUids.all { it in f.adultUids })
        val created = toUids.map { CompanionRequest(server.nextId("req_"), uid!!, it, reason, server.now(), RequestStatus.ENVIADA, null) }
        server.requests.getOrPut(familyId) { mutableListOf() }.addAll(0, created)
        server.requestsChanged()
        return created.map { it.id }
    }

    override fun observeRequests(familyId: String, asChild: Boolean, onChange: (List<CompanionRequest>) -> Unit): Subscription =
        server.onRequests {
            val all = server.requests[familyId].orEmpty()
            onChange(all.filter { if (asChild) it.childUid == uid else it.toUid == uid })
        }

    private fun change(familyId: String, id: String, f: (CompanionRequest) -> CompanionRequest) {
        val list = server.requests.getValue(familyId)
        val i = list.indexOfFirst { it.id == id }
        list[i] = f(list[i])
        server.requestsChanged()
    }

    override suspend fun respondToRequest(familyId: String, requestId: String, status: RequestStatus) {
        requireOnline()
        change(familyId, requestId) {
            check(it.toUid == uid && it.status.isOpen && it.status != RequestStatus.CANCELADA)
            it.copy(status = status, respondedAtMillis = server.now())
        }
    }

    override suspend fun childUpdateRequest(familyId: String, requestId: String, status: RequestStatus) {
        requireOnline()
        change(familyId, requestId) { check(it.childUid == uid); it.copy(status = status) }
    }

    // ------------------------------------------------------------------ Compartir mi semana

    override suspend fun shareWeek(familyId: String, week: SharedWeek) {
        requireOnline()
        check(server.families.getValue(familyId).childUid == uid)
        server.weeks[familyId] = week.copy(updatedAtMillis = server.now())
        server.weekChanged()
    }

    override suspend fun stopSharingWeek(familyId: String) {
        requireOnline()
        server.weeks.remove(familyId)
        server.weekChanged()
    }

    override fun observeSharedWeek(familyId: String, onChange: (SharedWeek?) -> Unit): Subscription =
        server.onWeek { onChange(server.weeks[familyId]) }
}
