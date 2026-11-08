package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** QA-09: elegir objetos sin monedas, niveles, tienda, ranking, rachas ni candados. */
class IslandObjectsTest {

    @Test
    fun `el catalogo tiene los 4 objetos del documento con su significado`() {
        assertEquals(listOf("hamaca", "jardin", "puente", "faro"), IslandObjects.catalog.map { it.id })
        assertTrue(IslandObjects.catalog.all { it.meaning.isNotBlank() })
    }

    @Test
    fun `se ofrecen como maximo 3 objetos y nunca uno ya colocado`() {
        val offers = IslandObjects.offersFor(listOf("hamaca", "jardin", "puente", "faro"), setOf("jardin"))
        assertEquals(listOf("hamaca", "puente", "faro"), offers.map { it.id })
        assertTrue(IslandObjects.offersFor(listOf("faro"), setOf("faro")).isEmpty())
    }

    @Test
    fun `colocar un objeto no quita ni bloquea los demas`() {
        val placed = IslandObjects.place(setOf("hamaca"), "faro")
        assertEquals(setOf("hamaca", "faro"), placed)
        assertEquals(setOf("hamaca", "faro"), IslandObjects.place(placed, "faro"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `no se pueden colocar objetos que no existen`() {
        IslandObjects.place(emptySet(), "moneda")
    }

    @Test
    fun `no existe ninguna mecanica de recompensa en el modelo`() {
        val forbidden = listOf("level", "nivel", "coin", "moneda", "point", "punto", "streak", "racha", "lock", "candado", "rank")
        val members = IslandObjects::class.java.declaredMethods.map { it.name.lowercase() } +
            IslandObjects::class.java.declaredFields.map { it.name.lowercase() }
        forbidden.forEach { word -> assertTrue("aparece $word", members.none { it.contains(word) }) }
    }
}
