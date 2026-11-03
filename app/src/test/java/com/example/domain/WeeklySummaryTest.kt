package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** Matriz filas 14–15: el resumen semanal coincide con los registros reales. */
class WeeklySummaryTest {

    private val bogota = TimeZone.getTimeZone("America/Bogota")

    private data class Reg(val tide: String, val at: Long)

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Long {
        val c = Calendar.getInstance(bogota)
        c.clear()
        c.set(year, month - 1, day, hour, minute)
        return c.timeInMillis
    }

    // Miércoles 4 de noviembre de 2026, 10:00
    private val now = at(2026, 11, 4, 10)

    @Test
    fun `la semana empieza el lunes a medianoche`() {
        assertEquals(at(2026, 11, 2, 0), WeeklySummary.weekStart(now, bogota))
        // Un domingo pertenece a la semana que empezó el lunes anterior
        assertEquals(at(2026, 11, 2, 0), WeeklySummary.weekStart(at(2026, 11, 8, 23, 59), bogota))
    }

    @Test
    fun `registros de la semana pasada no se mezclan con esta semana`() {
        val regs = listOf(
            Reg("TORMENTA", at(2026, 10, 26, 9)),   // lunes de la semana pasada
            Reg("TRANQUILA", at(2026, 11, 2, 8)),   // lunes de esta semana
            Reg("MOVIDA", at(2026, 11, 3, 18))      // martes de esta semana
        )
        val week = WeeklySummary.build(regs, now, bogota) { it.at }
        assertEquals(2, week.totalEntries)
        assertEquals(listOf("TRANQUILA"), week.days[0].entries.map { it.tide })
        assertEquals(listOf("MOVIDA"), week.days[1].entries.map { it.tide })
    }

    @Test
    fun `un dia sin registros queda vacio y no se rellena con ninguna marea`() {
        val week = WeeklySummary.build(listOf(Reg("ALTA", at(2026, 11, 2, 8))), now, bogota) { it.at }
        assertTrue(week.days[2].isEmpty)
        assertNull(WeeklySummary.dominant(week.days[2].entries, { it.tide }, { it.at }))
    }

    @Test
    fun `varios registros el mismo dia se cuentan todos`() {
        val regs = listOf(
            Reg("MOVIDA", at(2026, 11, 3, 8)),
            Reg("ALTA", at(2026, 11, 3, 12)),
            Reg("MOVIDA", at(2026, 11, 3, 20))
        )
        val week = WeeklySummary.build(regs, now, bogota) { it.at }
        assertEquals(3, week.days[1].entries.size)
        assertEquals("MOVIDA", WeeklySummary.dominant(week.days[1].entries, { it.tide }, { it.at }))
    }

    @Test
    fun `en empate gana la marea mas reciente`() {
        val regs = listOf(Reg("TRANQUILA", at(2026, 11, 3, 8)), Reg("TORMENTA", at(2026, 11, 3, 21)))
        val week = WeeklySummary.build(regs, now, bogota) { it.at }
        assertEquals("TORMENTA", WeeklySummary.dominant(week.days[1].entries, { it.tide }, { it.at }))
    }

    @Test
    fun `con menos de 3 registros se avisa que hay pocos datos`() {
        val two = listOf(Reg("ALTA", at(2026, 11, 2, 8)), Reg("ALTA", at(2026, 11, 3, 8)))
        assertFalse(WeeklySummary.build(two, now, bogota) { it.at }.hasEnoughData)
        val three = two + Reg("NO_SE", at(2026, 11, 4, 7))
        assertTrue(WeeklySummary.build(three, now, bogota) { it.at }.hasEnoughData)
    }
}
