package com.example.domain

import java.util.Calendar
import java.util.TimeZone

/**
 * Resumen semanal basado en fechas reales (Documento Técnico §6).
 *
 * - La semana va de lunes 00:00 a domingo 23:59 en la zona horaria del teléfono.
 * - Los registros de otras semanas no se mezclan.
 * - Un día sin registros queda vacío (no se rellena con ninguna marea).
 * - Con menos de [MIN_ENTRIES_FOR_SUMMARY] registros se avisa que hay pocos datos.
 */
object WeeklySummary {

    const val MIN_ENTRIES_FOR_SUMMARY = 3

    val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    data class Day<T>(
        /** 0 = lunes … 6 = domingo. */
        val index: Int,
        val name: String,
        val startMillis: Long,
        val entries: List<T>
    ) {
        val isEmpty: Boolean get() = entries.isEmpty()
    }

    data class Week<T>(
        val startMillis: Long,
        val endMillisExclusive: Long,
        val days: List<Day<T>>
    ) {
        val totalEntries: Int get() = days.sumOf { it.entries.size }
        val hasEnoughData: Boolean get() = totalEntries >= MIN_ENTRIES_FOR_SUMMARY
    }

    /** Lunes 00:00 de la semana que contiene [nowMillis]. */
    fun weekStart(nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone)
        cal.timeInMillis = nowMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dow = cal.get(Calendar.DAY_OF_WEEK) // domingo = 1 … sábado = 7
        val daysSinceMonday = (dow + 5) % 7
        cal.add(Calendar.DAY_OF_MONTH, -daysSinceMonday)
        return cal.timeInMillis
    }

    /**
     * Agrupa [items] (cualquier tipo) en los 7 días de la semana actual.
     * [timestampOf] devuelve el instante de cada registro.
     */
    fun <T> build(
        items: List<T>,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
        timestampOf: (T) -> Long
    ): Week<T> {
        val start = weekStart(nowMillis, timeZone)
        val dayStarts = (0..7).map { offset ->
            val cal = Calendar.getInstance(timeZone)
            cal.timeInMillis = start
            cal.add(Calendar.DAY_OF_MONTH, offset)
            cal.timeInMillis
        }
        val end = dayStarts[7]
        val days = (0 until 7).map { i ->
            val from = dayStarts[i]
            val to = dayStarts[i + 1]
            val inDay = items.filter { val t = timestampOf(it); t >= from && t < to }
                .sortedBy(timestampOf)
            Day(index = i, name = dayNames[i], startMillis = from, entries = inDay)
        }
        return Week(startMillis = start, endMillisExclusive = end, days = days)
    }

    /**
     * Valor más frecuente de un día; en empate gana el más reciente.
     * Devuelve null si el día está vacío.
     */
    fun <T, K> dominant(entries: List<T>, keyOf: (T) -> K, timestampOf: (T) -> Long): K? {
        if (entries.isEmpty()) return null
        val counts = entries.groupingBy(keyOf).eachCount()
        val max = counts.values.max()
        val tied = counts.filterValues { it == max }.keys
        return entries.sortedByDescending(timestampOf).first { keyOf(it) in tied }.let(keyOf)
    }

    /** Lo único que se comparte de la semana si el niño lo activa: conteos por marea y días con registro. */
    data class Shareable(val weekStart: String, val counts: Map<String, Int>, val daysWithRecord: Int)

    fun <T> shareable(
        items: List<T>,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
        typeName: (T) -> String,
        timestampOf: (T) -> Long
    ): Shareable {
        val week = build(items, nowMillis, timeZone, timestampOf)
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = week.startMillis }
        val start = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val counts = week.days.flatMap { it.entries }.groupingBy(typeName).eachCount()
        return Shareable(start, counts, week.days.count { !it.isEmpty })
    }
}
