package com.example.domain

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PIN del perfil del niño (decisión provisional del 7 nov 2026):
 * - Lo crea el niño: 4 a 6 dígitos, sin secuencias obvias.
 * - Se guarda solo como hash con sal (PBKDF2), en el teléfono.
 * - Tras 5 intentos fallidos hay esperas crecientes. Si lo olvida, el adulto genera un código nuevo.
 */
object PinRules {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 6
    const val FREE_ATTEMPTS = 5
    private const val ITERATIONS = 20_000
    private const val BASE_LOCK_MS = 30_000L
    private const val MAX_LOCK_MS = 15 * 60_000L

    fun isValidFormat(pin: String): Boolean = pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it in '0'..'9' }

    /** 1111, 1234, 9876… son fáciles de adivinar. */
    fun isTooSimple(pin: String): Boolean {
        if (!isValidFormat(pin)) return false
        val d = pin.map { it - '0' }
        val allSame = d.all { it == d[0] }
        val ascending = d.zipWithNext().all { (a, b) -> b == a + 1 }
        val descending = d.zipWithNext().all { (a, b) -> b == a - 1 }
        return allSame || ascending || descending
    }

    /** Mensaje para el niño, o null si el PIN sirve. */
    fun problem(pin: String, confirmation: String): String? = when {
        !isValidFormat(pin) -> "Usa entre $MIN_LENGTH y $MAX_LENGTH números."
        isTooSimple(pin) -> "Ese PIN es fácil de adivinar. Prueba con otros números."
        pin != confirmation -> "Los dos PIN no coinciden."
        else -> null
    }

    fun newSalt(random: SecureRandom = SecureRandom()): ByteArray = ByteArray(16).also { random.nextBytes(it) }

    // PBKDF2WithHmacSHA1 existe en todos los Android desde la API 24 (SHA256 solo desde la 26).
    fun hash(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        spec.clearPassword()
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(pin: String, salt: ByteArray, expectedHash: String): Boolean {
        val actual = hash(pin, salt)
        // Comparación en tiempo constante
        if (actual.length != expectedHash.length) return false
        var diff = 0
        for (i in actual.indices) diff = diff or (actual[i].code xor expectedHash[i].code)
        return diff == 0
    }

    /** Espera obligatoria después de [failedAttempts] intentos fallidos seguidos. */
    fun lockoutMillis(failedAttempts: Int): Long {
        if (failedAttempts < FREE_ATTEMPTS) return 0
        val steps = failedAttempts - FREE_ATTEMPTS
        return (BASE_LOCK_MS shl steps.coerceAtMost(10)).coerceAtMost(MAX_LOCK_MS)
    }
}
