package com.example.data

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

/**
 * Cifra los datos emocionales antes de guardarlos (Documento Técnico §15).
 *
 * Usa Tink (Google, gratuito y de código abierto) con AES-256-GCM. La clave se guarda
 * protegida por el Android Keystore del teléfono; los datos nunca salen del dispositivo.
 */
interface TextCipher {
    fun encrypt(plain: String): String
    fun decrypt(encoded: String): String
}

class TinkTextCipher(context: Context) : TextCipher {

    private val appContext = context.applicationContext

    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(appContext, KEYSET_NAME, KEYSET_PREFS)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    override fun encrypt(plain: String): String {
        val bytes = aead.encrypt(plain.toByteArray(Charsets.UTF_8), ASSOCIATED_DATA)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun decrypt(encoded: String): String {
        val bytes = aead.decrypt(Base64.decode(encoded, Base64.NO_WRAP), ASSOCIATED_DATA)
        return String(bytes, Charsets.UTF_8)
    }

    private companion object {
        const val KEYSET_NAME = "marea_keyset"
        const val KEYSET_PREFS = "marea_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://marea_master_key"
        val ASSOCIATED_DATA = "marea-club-v1".toByteArray(Charsets.UTF_8)
    }
}
