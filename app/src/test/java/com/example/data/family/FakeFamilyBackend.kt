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
        server.update(familyId) { it.copy(childUid = null) }
    }

    override suspend fun removeSecondAdult(familyId: String, ownerUid: String) {
        requireOnline()
        server.update(familyId) { it.copy(adultUids = listOf(ownerUid)) }
    }

    override suspend fun childLeaves(familyId: String) {
        requireOnline()
        server.update(familyId) { it.copy(childUid = null) }
    }
}
