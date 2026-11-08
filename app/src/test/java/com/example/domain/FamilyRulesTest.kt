package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class FamilyRulesTest {

    // ---------------------------------------------------------------- códigos

    @Test
    fun `los codigos son aleatorios, de 8 caracteres y sin caracteres confusos`() {
        val codes = (1..500).map { LinkCode.generate() }.toSet()
        assertEquals("no se repiten", 500, codes.size)
        codes.forEach { code ->
            assertEquals(8, code.length)
            assertTrue(code.none { it in "01IOL" })
        }
    }

    @Test
    fun `normalizar acepta minusculas, espacios y guiones y rechaza lo invalido`() {
        assertEquals("K7QXM2PA", LinkCode.normalize(" k7qx-m2pa "))
        assertNull(LinkCode.normalize("K7QX-M2P"))
        assertNull("la O no existe en el alfabeto", LinkCode.normalize("K7QX-M2PO"))
        assertEquals("K7QX-M2PA", LinkCode.format("K7QXM2PA"))
    }

    @Test
    fun `el hash es SHA-256 en hexadecimal y no contiene el codigo`() {
        val h = LinkCode.hash("K7QXM2PA")
        assertEquals(64, h.length)
        assertFalse(h.contains("K7QX", ignoreCase = true))
        assertEquals(h, LinkCode.hash("K7QXM2PA"))
        assertNotEquals(h, LinkCode.hash("K7QXM2PB"))
    }

    @Test
    fun `el codigo vence a los 10 minutos`() {
        assertEquals(600_000L, LinkCode.remainingMillis(0, 0))
        assertEquals(0L, LinkCode.remainingMillis(0, 600_000))
        assertEquals(0L, LinkCode.remainingMillis(0, 9_999_999))
    }

    // ---------------------------------------------------------------- PIN

    @Test
    fun `el PIN tiene de 4 a 6 numeros y no puede ser obvio`() {
        assertNull(PinRules.problem("2580", "2580"))
        assertNull(PinRules.problem("739215", "739215"))
        assertEquals("Usa entre 4 y 6 números.", PinRules.problem("123", "123"))
        assertEquals("Usa entre 4 y 6 números.", PinRules.problem("12a4", "12a4"))
        assertTrue(PinRules.isTooSimple("1111"))
        assertTrue(PinRules.isTooSimple("1234"))
        assertTrue(PinRules.isTooSimple("6543"))
        assertEquals("Los dos PIN no coinciden.", PinRules.problem("2580", "2581"))
    }

    @Test
    fun `el PIN se guarda como hash con sal y se verifica`() {
        val salt = PinRules.newSalt(SecureRandom())
        val h = PinRules.hash("2580", salt)
        assertFalse(h.contains("2580"))
        assertTrue(PinRules.matches("2580", salt, h))
        assertFalse(PinRules.matches("2581", salt, h))
        assertNotEquals("otra sal, otro hash", h, PinRules.hash("2580", PinRules.newSalt()))
    }

    @Test
    fun `tras 5 intentos fallidos hay esperas crecientes con tope`() {
        (0..4).forEach { assertEquals(0L, PinRules.lockoutMillis(it)) }
        assertEquals(30_000L, PinRules.lockoutMillis(5))
        assertEquals(60_000L, PinRules.lockoutMillis(6))
        assertEquals(120_000L, PinRules.lockoutMillis(7))
        assertEquals(15 * 60_000L, PinRules.lockoutMillis(50))
    }
}
