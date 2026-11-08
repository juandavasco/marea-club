package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Matriz filas 5 y 39. */
class SafetyAndGuidanceTest {

    @Test
    fun `alta y tormenta muestran rutas de apoyo`() {
        assertTrue(SafetyRules.showsSupportFor("TORMENTA"))
        assertTrue(SafetyRules.showsSupportFor("ALTA"))
        assertFalse(SafetyRules.showsSupportFor("TRANQUILA"))
    }

    @Test
    fun `detecta expresiones de riesgo sin importar tildes ni mayusculas`() {
        assertTrue(SafetyRules.mentionsRisk("A veces pienso en hacerme DAÑO"))
        assertTrue(SafetyRules.mentionsRisk("ya no aguanto más"))
        assertFalse(SafetyRules.mentionsRisk("Hoy me fue bien en el examen"))
    }
}
