package com.example.domain

import java.text.Normalizer

/**
 * Reglas de seguridad del menor (Documento Técnico §5 y §15).
 *
 * La detección por palabras es solo un APOYO: nunca decide si se muestran las rutas
 * de ayuda. Las rutas urgentes están siempre visibles en la consulta anónima y al
 * elegir marea Alta o Tormenta.
 */
object SafetyRules {

    /** Mareas en las que se muestran rutas de apoyo y adulto de confianza junto al registro. */
    val tidesThatShowSupport: Set<String> = setOf("ALTA", "TORMENTA")

    fun showsSupportFor(tideName: String): Boolean = tideName in tidesThatShowSupport

    private val riskStems = listOf(
        "suicid", "morir", "matarme", "me quiero matar", "no quiero vivir", "quitarme la vida",
        "hacerme dano", "lastimarme", "cortarme", "me corto", "desaparecer para siempre",
        "abuso", "abusan", "me pegan", "me tocan", "emergencia", "desesperad", "no aguanto mas"
    )

    private fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    /** true si el texto contiene alguna expresión de riesgo (sin tildes ni mayúsculas). */
    fun mentionsRisk(text: String): Boolean {
        val clean = normalize(text)
        return riskStems.any { clean.contains(it) }
    }
}
