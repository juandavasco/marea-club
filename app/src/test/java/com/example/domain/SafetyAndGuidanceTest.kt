package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Matriz filas 5, 37 y 39. */
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

    @Test
    fun `la orientacion no produce puntajes, solo un orden de sugerencias`() {
        assertEquals(SelfCheckGuidance.Focus.CUIDADO_COTIDIANO, SelfCheckGuidance.focus(listOf(0, 0, 1, 0)))
        assertEquals(SelfCheckGuidance.Focus.CONVERSAR, SelfCheckGuidance.focus(listOf(1, 1, 1, 0)))
        assertEquals(SelfCheckGuidance.Focus.BUSCAR_APOYO, SelfCheckGuidance.focus(listOf(2, 2, 1, 2)))
        assertEquals(SelfCheckGuidance.Focus.CUIDADO_COTIDIANO, SelfCheckGuidance.focus(emptyList()))
    }
}
