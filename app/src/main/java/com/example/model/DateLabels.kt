package com.example.model

import java.util.Calendar
import java.util.TimeZone

/** Etiquetas de fecha en español, sin depender de la configuración regional del teléfono. */
object DateLabels {
    private val days = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    private val months = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    /** Ej.: "Lunes 2 nov". */
    fun dayLabel(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val c = Calendar.getInstance(timeZone)
        c.timeInMillis = timestamp
        return "${days[c.get(Calendar.DAY_OF_WEEK) - 1]} ${c.get(Calendar.DAY_OF_MONTH)} ${months[c.get(Calendar.MONTH)]}"
    }
}
