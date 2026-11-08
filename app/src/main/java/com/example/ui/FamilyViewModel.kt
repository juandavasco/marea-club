package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.family.*
import com.example.domain.LinkCode
import com.example.domain.PinRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Código recién generado por el adulto; solo vive en la memoria de su teléfono. */
data class ActiveCode(val code: String, val role: LinkRole, val createdAtMillis: Long)

sealed interface PinCheck {
    data object Ok : PinCheck
    data class Wrong(val attemptsBeforeWait: Int) : PinCheck
    data class Locked(val untilMillis: Long) : PinCheck
}

/**
 * Cuentas y vínculo familiar (entrega 3).
 * Las respuestas emocionales nunca pasan por aquí: solo el vínculo y, más adelante, Mis salvavidas.
 */
class FamilyViewModel @JvmOverloads constructor(
    application: Application,
    private val clock: () -> Long = { System.currentTimeMillis() },
    backendFactory: () -> FamilyBackend = FamilyBackends.factory
) : AndroidViewModel(application) {

    private val store = FamilyStore(application)
    private var adultName: String = "Adulto"
    private val backend: FamilyBackend by lazy { backendFactory() }
    private var subscriptions: List<Subscription> = emptyList()

    private val _role = MutableStateFlow(store.role)
    val role: StateFlow<DeviceRole?> = _role.asStateFlow()

    private val _familyId = MutableStateFlow(store.familyId)
    val familyId: StateFlow<String?> = _familyId.asStateFlow()

    private val _family = MutableStateFlow<FamilyInfo?>(null)
    val family: StateFlow<FamilyInfo?> = _family.asStateFlow()

    private val _adultSignedIn = MutableStateFlow(false)
    val adultSignedIn: StateFlow<Boolean> = _adultSignedIn.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _activeCode = MutableStateFlow<ActiveCode?>(null)
    val activeCode: StateFlow<ActiveCode?> = _activeCode.asStateFlow()

    /** Niño: sus pedidos de Mis salvavidas. Adulto: los que le enviaron. */
    private val _requests = MutableStateFlow<List<CompanionRequest>>(emptyList())
    val requests: StateFlow<List<CompanionRequest>> = _requests.asStateFlow()

    private val _sharedWeek = MutableStateFlow<SharedWeek?>(null)
    val sharedWeek: StateFlow<SharedWeek?> = _sharedWeek.asStateFlow()

    private val _shareWeek = MutableStateFlow(store.shareWeek)
    val shareWeek: StateFlow<Boolean> = _shareWeek.asStateFlow()

    private val _hasPin = MutableStateFlow(store.hasPin)
    val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()

    /** Después de vincularse (o de recuperar el PIN) el niño debe crear un PIN. */
    private val _needsPinSetup = MutableStateFlow(false)
    val needsPinSetup: StateFlow<Boolean> = _needsPinSetup.asStateFlow()

    /** El PIN se pide una vez por sesión (al abrir la app). */
    private val _unlocked = MutableStateFlow(!store.hasPin)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    init {
        // Quien ya usaba la app antes de la entrega 3 era el niño: no se le pregunta de nuevo.
        if (_role.value == null && store.isOnboarded) setRole(DeviceRole.CHILD)
        _familyId.value?.let { startObserving(it) }
        if (_role.value == DeviceRole.ADULT) {
            _adultSignedIn.value = runCatching { backend.isSignedInWithGoogle }.getOrDefault(false)
            runCatching { backend.currentDisplayName }.getOrNull()?.let { adultName = it }
        }
    }

    val myUid: String? get() = runCatching { backend.currentUid }.getOrNull()

    /** El niño está vinculado si la familia existe y lo reconoce como su niño. */
    fun isChildLinked(info: FamilyInfo?): Boolean = info != null && info.childUid != null && info.childUid == myUid

    fun clearMessage() {
        _message.value = null
    }

    fun setRole(role: DeviceRole?) {
        store.role = role
        _role.value = role
    }

    // ------------------------------------------------------------------ adulto

    fun onGoogleIdToken(idToken: String) = work {
        val account = backend.signInWithGoogle(idToken)
        adultName = account.displayName
        _adultSignedIn.value = true
        val profile = backend.loadProfile()
        if (profile != null && profile.role == DeviceRole.ADULT) linkedTo(profile.familyId)
    }

    fun onGoogleError(text: String) {
        _message.value = text
    }


    fun createFamily(childAlias: String) = work {
        val id = backend.createFamily(adultName, childAlias.trim().ifBlank { "Navegante" })
        linkedTo(id)
    }

    fun joinAsSecondAdult(rawCode: String) = redeem(rawCode, LinkRole.ADULT, adultName)

    fun generateCode(role: LinkRole) = work {
        val fid = _familyId.value ?: return@work
        val code = LinkCode.generate()
        backend.publishLinkCode(fid, role, LinkCode.hash(code))
        _activeCode.value = ActiveCode(code, role, clock())
    }

    fun discardCode() {
        _activeCode.value = null
    }

    fun unlinkChild() = work { _familyId.value?.let { backend.unlinkChild(it) } }

    fun removeSecondAdult() = work {
        val info = _family.value ?: return@work
        backend.removeSecondAdult(info.id, info.ownerUid)
    }

    fun signOutAdult() {
        stopObserving()
        runCatching { backend.signOut() }
        store.familyId = null
        _familyId.value = null
        _family.value = null
        _activeCode.value = null
        _adultSignedIn.value = false
    }

    // ------------------------------------------------------------------ niño

    /** Usa el código que dio el adulto. Si [resettingPin], al terminar se pide un PIN nuevo. */
    fun childRedeem(rawCode: String, displayName: String, resettingPin: Boolean = false) {
        redeem(rawCode, LinkRole.CHILD, displayName) {
            if (resettingPin) {
                store.clearPin()
                _hasPin.value = false
                _unlocked.value = true
            }
            if (!store.hasPin) _needsPinSetup.value = true
        }
    }

    fun requestPinSetup() {
        _needsPinSetup.value = true
    }

    fun childLeave() = work {
        _familyId.value?.let { backend.childLeaves(it) }
        store.shareWeek = false
        _shareWeek.value = false
        stopObserving()
        store.familyId = null
        _familyId.value = null
        _family.value = null
    }

    fun savePin(pin: String, confirmation: String): String? {
        PinRules.problem(pin, confirmation)?.let { return it }
        val salt = PinRules.newSalt()
        store.savePin(PinRules.hash(pin, salt), salt)
        _hasPin.value = true
        _unlocked.value = true
        _needsPinSetup.value = false
        return null
    }

    fun checkPin(pin: String): PinCheck {
        val now = clock()
        if (store.lockedUntil > now) return PinCheck.Locked(store.lockedUntil)
        val hash = store.pinHash
        val salt = store.pinSalt
        if (hash == null || salt == null) {
            _unlocked.value = true
            return PinCheck.Ok
        }
        if (PinRules.matches(pin, salt, hash)) {
            store.failedAttempts = 0
            store.lockedUntil = 0
            _unlocked.value = true
            return PinCheck.Ok
        }
        val failed = store.failedAttempts + 1
        store.failedAttempts = failed
        val wait = PinRules.lockoutMillis(failed)
        return if (wait > 0) {
            store.lockedUntil = now + wait
            PinCheck.Locked(now + wait)
        } else {
            PinCheck.Wrong(PinRules.FREE_ATTEMPTS - failed)
        }
    }

    // ------------------------------------------------------------------ común

    /** "Borrar todos mis datos": el niño sale de la familia y se cierra la sesión. */
    fun onLocalReset() {
        val fid = _familyId.value
        val wasChild = _role.value == DeviceRole.CHILD && isChildLinked(_family.value)
        stopObserving()
        if (wasChild && fid != null) viewModelScope.launch { runCatching { backend.childLeaves(fid) }; runCatching { backend.signOut() } }
        else runCatching { backend.signOut() }
        _role.value = null
        _familyId.value = null
        _family.value = null
        _activeCode.value = null
        _adultSignedIn.value = false
        _hasPin.value = false
        _unlocked.value = true
        _needsPinSetup.value = false
        _shareWeek.value = false
    }

    private fun redeem(rawCode: String, role: LinkRole, displayName: String, onLinked: () -> Unit = {}) = work {
        val normalized = LinkCode.normalize(rawCode)
        if (normalized == null) {
            _message.value = "El código tiene 8 letras y números, por ejemplo K7QX-M2PA."
            return@work
        }
        if (role == LinkRole.CHILD) backend.signInAnonymously()
        when (val result = backend.redeemLinkCode(LinkCode.hash(normalized), role, displayName)) {
            is LinkResult.Linked -> {
                linkedTo(result.familyId)
                onLinked()
            }
            LinkResult.InvalidCode -> _message.value =
                "Ese código no sirve: puede que esté mal escrito, que ya se haya usado o que hayan pasado más de 10 minutos. Pide uno nuevo."
            LinkResult.NoConnection -> _message.value = NO_CONNECTION
        }
    }

    private fun linkedTo(familyId: String) {
        store.familyId = familyId
        _familyId.value = familyId
        startObserving(familyId)
    }

    private fun startObserving(familyId: String) {
        stopObserving()
        val asChild = _role.value != DeviceRole.ADULT
        subscriptions = listOfNotNull(
            runCatching { backend.observeFamily(familyId) { _family.value = it } }.getOrNull(),
            runCatching { backend.observeRequests(familyId, asChild) { _requests.value = it } }.getOrNull(),
            runCatching { backend.observeSharedWeek(familyId) { _sharedWeek.value = it } }.getOrNull()
        )
    }

    private fun stopObserving() {
        subscriptions.forEach { it.cancel() }
        subscriptions = emptyList()
        _requests.value = emptyList()
        _sharedWeek.value = null
    }

    // ------------------------------------------------------------------ Mis salvavidas

    /** El niño envía un pedido; si no hay conexión se avisa y no queda nada en cola. */
    fun sendRequest(toUid: String, reason: CompanionReason) = work {
        val fid = _familyId.value ?: return@work
        backend.sendCompanionRequest(fid, toUid, reason)
    }

    fun childSetRequestStatus(requestId: String, status: RequestStatus) = work {
        _familyId.value?.let { backend.childUpdateRequest(it, requestId, status) }
    }

    fun respond(requestId: String, status: RequestStatus) = work {
        _familyId.value?.let { backend.respondToRequest(it, requestId, status) }
    }

    // ------------------------------------------------------------------ Compartir mi semana

    /** El niño activa o desactiva compartir su semana. Al desactivar se borra de la nube. */
    fun setShareWeek(enabled: Boolean, week: SharedWeek?) = work {
        val fid = _familyId.value ?: return@work
        if (enabled && week != null) backend.shareWeek(fid, week) else backend.stopSharingWeek(fid)
        store.shareWeek = enabled
        _shareWeek.value = enabled
    }

    /** Actualiza lo compartido cuando cambian los registros (silencioso si no hay conexión). */
    fun syncWeek(week: SharedWeek) {
        val fid = _familyId.value ?: return
        if (!_shareWeek.value || !isChildLinked(_family.value)) return
        val current = _sharedWeek.value
        if (current != null && current.weekStart == week.weekStart && current.counts == week.counts && current.daysWithRecord == week.daysWithRecord) return
        viewModelScope.launch { runCatching { backend.shareWeek(fid, week) } }
    }

    private fun work(block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true
        _message.value = null
        viewModelScope.launch {
            try {
                block()
            } catch (e: BackendUnavailable) {
                _message.value = NO_CONNECTION
            } catch (e: Exception) {
                _message.value = "Algo no salió bien. Intenta de nuevo en un momento."
            } finally {
                _busy.value = false
            }
        }
    }

    override fun onCleared() {
        stopObserving()
    }

    companion object {
        const val NO_CONNECTION = "No hay conexión a internet. Revisa el wifi o los datos e intenta de nuevo."
    }
}
