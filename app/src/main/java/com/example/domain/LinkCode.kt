package com.example.domain

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Códigos de vínculo familiar (Prompt Maestro §8).
 *
 * - Aleatorios (SecureRandom), 8 caracteres sin letras ni números que se confunden (0/O, 1/I/L).
 * - Duran 10 minutos y se usan una sola vez: eso lo valida el servidor con las reglas de Firestore.
 * - En la nube solo se guarda su SHA-256; el código en claro nunca sale del teléfono del adulto.
 */
object LinkCode {
    const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
    const val LENGTH = 8
    const val VALID_MINUTES = 10

    fun generate(random: SecureRandom = SecureRandom()): String =
        (1..LENGTH).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")

    /** Acepta minúsculas, espacios y guiones. Devuelve null si no puede ser un código válido. */
    fun normalize(input: String): String? {
        val clean = input.uppercase().filter { it.isLetterOrDigit() }
        if (clean.length != LENGTH || clean.any { it !in ALPHABET }) return null
        return clean
    }

    /** "K7QXM2PA" → "K7QX-M2PA" para leerlo y dictarlo más fácil. */
    fun format(code: String): String = code.chunked(4).joinToString("-")

    /** Identificador del documento en Firestore. */
    fun hash(normalized: String): String =
        MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun remainingMillis(createdAtMillis: Long, nowMillis: Long): Long =
        (createdAtMillis + VALID_MINUTES * 60_000L - nowMillis).coerceAtLeast(0)
}
