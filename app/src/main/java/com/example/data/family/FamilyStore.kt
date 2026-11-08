package com.example.data.family

import android.content.Context
import android.util.Base64
import com.example.data.MareaRepository

/**
 * Lo que este teléfono recuerda del vínculo familiar. Vive en el mismo archivo de preferencias
 * que el resto de la app, así "Borrar todos mis datos" también lo borra.
 * El PIN se guarda solo como hash con sal (ver [com.example.domain.PinRules]).
 */
class FamilyStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE)

    var role: DeviceRole?
        get() = prefs.getString(KEY_ROLE, null)?.let { runCatching { DeviceRole.valueOf(it) }.getOrNull() }
        set(value) = prefs.edit().apply { if (value == null) remove(KEY_ROLE) else putString(KEY_ROLE, value.name) }.apply()

    var familyId: String?
        get() = prefs.getString(KEY_FAMILY, null)
        set(value) = prefs.edit().apply { if (value == null) remove(KEY_FAMILY) else putString(KEY_FAMILY, value) }.apply()

    val isOnboarded: Boolean get() = prefs.getBoolean("is_onboarded", false)

    val pinHash: String? get() = prefs.getString(KEY_PIN_HASH, null)
    val pinSalt: ByteArray? get() = prefs.getString(KEY_PIN_SALT, null)?.let { Base64.decode(it, Base64.NO_WRAP) }
    val hasPin: Boolean get() = pinHash != null && pinSalt != null

    fun savePin(hash: String, salt: ByteArray) {
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putInt(KEY_PIN_FAILED, 0)
            .putLong(KEY_PIN_LOCKED_UNTIL, 0)
            .apply()
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN_HASH).remove(KEY_PIN_SALT).remove(KEY_PIN_FAILED).remove(KEY_PIN_LOCKED_UNTIL).apply()
    }

    var failedAttempts: Int
        get() = prefs.getInt(KEY_PIN_FAILED, 0)
        set(value) = prefs.edit().putInt(KEY_PIN_FAILED, value).apply()

    var lockedUntil: Long
        get() = prefs.getLong(KEY_PIN_LOCKED_UNTIL, 0)
        set(value) = prefs.edit().putLong(KEY_PIN_LOCKED_UNTIL, value).apply()

    companion object {
        private const val KEY_ROLE = "device_role"
        private const val KEY_FAMILY = "family_id"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_FAILED = "pin_failed_attempts"
        private const val KEY_PIN_LOCKED_UNTIL = "pin_locked_until"
    }
}
