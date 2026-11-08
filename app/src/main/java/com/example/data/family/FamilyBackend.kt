package com.example.data.family

/** Quién usa la app en este teléfono. */
enum class DeviceRole { CHILD, ADULT }

/** Rol que otorga un código de vínculo. */
enum class LinkRole(val wire: String) { CHILD("child"), ADULT("adult") }

/** La familia tal como está en Firestore. No contiene datos emocionales. */
data class FamilyInfo(
    val id: String,
    val ownerUid: String,
    val adultUids: List<String>,
    val adultNames: Map<String, String>,
    val childUid: String?,
    val childName: String
) {
    val hasChild: Boolean get() = childUid != null
    val canInviteSecondAdult: Boolean get() = adultUids.size < 2
    fun adultNameList(): List<String> = adultUids.mapNotNull { adultNames[it] }
}

data class AdultAccount(val uid: String, val displayName: String)

/** Perfil guardado en users/{uid}. */
data class RemoteProfile(val role: DeviceRole, val familyId: String)

/** Resultado de usar un código. */
sealed interface LinkResult {
    data class Linked(val familyId: String) : LinkResult
    /** No existe, ya se usó, venció o es de otro rol: el servidor no explica cuál por seguridad. */
    data object InvalidCode : LinkResult
    data object NoConnection : LinkResult
}

/** Cancela una suscripción en tiempo real. */
fun interface Subscription {
    fun cancel()
}

/**
 * Acceso a Firebase (Auth + Firestore). La implementación real es [FirebaseFamilyBackend];
 * las pruebas usan un doble en memoria que aplica las mismas reglas.
 *
 * Todas las funciones lanzan [BackendUnavailable] si no hay conexión.
 */
interface FamilyBackend {
    val currentUid: String?
    val isSignedInWithGoogle: Boolean
    /** Primer nombre de la cuenta de Google, si hay sesión. */
    val currentDisplayName: String?

    suspend fun signInWithGoogle(idToken: String): AdultAccount
    suspend fun signInAnonymously(): String
    fun signOut()

    suspend fun loadProfile(): RemoteProfile?

    /** Crea la familia del adulto con su perfil. Devuelve el id de la familia. */
    suspend fun createFamily(adultName: String, childName: String): String

    /** Publica el hash de un código nuevo. El servidor fija la hora de creación. */
    suspend fun publishLinkCode(familyId: String, role: LinkRole, codeHash: String)

    /** Usa un código: marca el código como usado, entra a la familia y crea el perfil, todo junto. */
    suspend fun redeemLinkCode(codeHash: String, role: LinkRole, displayName: String): LinkResult

    fun observeFamily(familyId: String, onChange: (FamilyInfo?) -> Unit): Subscription

    suspend fun unlinkChild(familyId: String)
    suspend fun removeSecondAdult(familyId: String, ownerUid: String)
    suspend fun childLeaves(familyId: String)

    // Mis salvavidas
    /** Un pedido por cada adulto elegido (uno o ambos), en una sola operación. */
    suspend fun sendCompanionRequest(familyId: String, toUids: List<String>, reason: CompanionReason?): List<String>
    /** Niño: sus pedidos. Adulto: los pedidos dirigidos a él. Más recientes primero. */
    fun observeRequests(familyId: String, asChild: Boolean, onChange: (List<CompanionRequest>) -> Unit): Subscription
    suspend fun respondToRequest(familyId: String, requestId: String, status: RequestStatus)
    suspend fun childUpdateRequest(familyId: String, requestId: String, status: RequestStatus)

    // Compartir mi semana
    suspend fun shareWeek(familyId: String, week: SharedWeek)
    suspend fun stopSharingWeek(familyId: String)
    fun observeSharedWeek(familyId: String, onChange: (SharedWeek?) -> Unit): Subscription
}

/** Qué necesita, opcional y general: nunca texto libre ni emociones (Prompt Maestro §10). */
enum class CompanionReason(val wire: String, val label: String) {
    HABLAR("hablar", "Hablar"),
    COMPANIA("compania", "Compañía"),
    AYUDA("ayuda", "Ayuda práctica"),
    ESTAR("estar", "Simplemente estar acompañado");

    companion object {
        /** null = el niño prefirió no decirlo. */
        fun fromWire(w: String?) = entries.firstOrNull { it.wire == w }
    }
}

/** Estados reales del pedido. "Sin respuesta" no se guarda: se calcula a los 15 minutos. */
enum class RequestStatus(val wire: String) {
    ENVIADA("enviada"), VISTA("vista"), VOY("voy"), NO_PUEDO("no_puedo"), CANCELADA("cancelada"), CERRADA("cerrada");

    val isOpen: Boolean get() = this == ENVIADA || this == VISTA || this == VOY || this == NO_PUEDO

    companion object {
        fun fromWire(w: String?) = entries.firstOrNull { it.wire == w } ?: ENVIADA
    }
}

data class CompanionRequest(
    val id: String,
    val childUid: String,
    val toUid: String,
    val reason: CompanionReason?,
    val createdAtMillis: Long,
    val status: RequestStatus,
    val respondedAtMillis: Long?
)

/** Lo único que se comparte de la semana, y solo si el niño lo activa. */
data class SharedWeek(val weekStart: String, val counts: Map<String, Int>, val daysWithRecord: Int, val updatedAtMillis: Long)

class BackendUnavailable(cause: Throwable? = null) : Exception("Sin conexión con el servidor", cause)

/** Permite cambiar el servidor real por un doble en las pruebas. */
object FamilyBackends {
    @Volatile
    var factory: () -> FamilyBackend = { FirebaseFamilyBackend() }
}
