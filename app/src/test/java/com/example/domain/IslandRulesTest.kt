package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Matriz filas 16–18 y 21: cada acción desbloquea un elemento identificable. */
class IslandRulesTest {

    @Test
    fun `cada uno de los 9 elementos tiene exactamente una accion que lo desbloquea`() {
        val elementIds = IslandRules.unlocks.map { it.elementId }
        assertEquals(9, elementIds.size)
        assertEquals("ningún elemento repetido", elementIds.size, elementIds.toSet().size)
        val sourceIds = IslandRules.unlocks.map { it.sourceId }
        assertEquals("ninguna acción repetida", sourceIds.size, sourceIds.toSet().size)
    }

    @Test
    fun `las 6 actividades del catalogo y las 2 herramientas tienen elemento`() {
        val sources = listOf(
            "act_respiracion", "act_diario", "act_emociones", "act_autoestima",
            "act_conchas", "act_dibujo", "act_burbujas", "act_sonidos", "reflexion_semanal"
        )
        sources.forEach { assertNotNull("sin elemento: $it", IslandRules.unlockForSource(it)) }
    }

    @Test
    fun `completar una actividad desbloquea su elemento y lo explica`() {
        val outcome = IslandRules.apply(emptySet(), "act_diario")
        assertTrue(outcome.newlyUnlocked)
        assertEquals("sofa_azul", outcome.unlock!!.elementId)
        assertEquals("Diario del Mar", outcome.unlock!!.sourceTitle)
        assertTrue("sofa_azul" in outcome.unlockedAfter)
    }

    @Test
    fun `repetir una actividad no duplica ni quita elementos`() {
        val first = IslandRules.apply(emptySet(), "act_respiracion")
        val second = IslandRules.apply(first.unlockedAfter, "act_respiracion")
        assertFalse(second.newlyUnlocked)
        assertEquals(first.unlockedAfter, second.unlockedAfter)
    }

    @Test
    fun `una accion desconocida no cambia la isla`() {
        val outcome = IslandRules.apply(setOf("flores_coral"), "registrar_marea_tormenta")
        assertNull(outcome.unlock)
        assertEquals(setOf("flores_coral"), outcome.unlockedAfter)
    }

    @Test
    fun `el nivel depende solo de los elementos desbloqueados`() {
        assertEquals(1, IslandRules.progress(emptySet()).level)
        assertEquals(0, IslandRules.progress(emptySet()).percentToNext)
        val three = IslandRules.unlocks.take(3).map { it.elementId }.toSet()
        assertEquals(2, IslandRules.progress(three).level)
        assertEquals("Mi Refugio", IslandRules.progress(three).levelName)
        val four = IslandRules.unlocks.take(4).map { it.elementId }.toSet()
        assertEquals(33, IslandRules.progress(four).percentToNext)
        val all = IslandRules.unlocks.map { it.elementId }.toSet()
        assertEquals(4, IslandRules.progress(all).level)
        assertEquals(100, IslandRules.progress(all).percentToNext)
    }

    @Test
    fun `la siguiente sugerencia es un elemento aun bloqueado`() {
        val unlocked = setOf("flores_coral")
        val next = IslandRules.nextSuggestion(unlocked)!!
        assertFalse(next.elementId in unlocked)
        assertNull(IslandRules.nextSuggestion(IslandRules.unlocks.map { it.elementId }.toSet()))
    }
}
