package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Bitácora del acompañamiento y Conexión familiar: estructura pedida por el Prompt Maestro §11–§12. */
class AdultGuideTest {

    private val forbidden = Regex("puntaje|puntos|nivel|correcto|incorrecto|buen padre|mal padre|mala madre|buena madre|diagn", RegexOption.IGNORE_CASE)

    @Test
    fun `cuatro recorridos y cinco experiencias, cada recorrido con al menos una`() {
        assertEquals(listOf("Leer la marea", "Bajo la superficie", "Regular el propio rumbo", "Volver a encontrar la corriente"), AdultGuide.routes.map { it.title })
        assertEquals(
            listOf("Lo que cambió antes de la reacción", "Más de una explicación", "Mi reacción también cuenta", "Cuando la rabia aparece", "Después del conflicto"),
            AdultGuide.experiences.map { it.title }
        )
        AdultGuide.routes.forEach { assertTrue(it.title, AdultGuide.experiencesFor(it.id).isNotEmpty()) }
    }

    @Test
    fun `cada experiencia tiene objetivo, situacion, reflexion, aprendizaje y aplicacion cotidiana`() {
        AdultGuide.experiences.forEach { e ->
            listOf(e.objective, e.situation, e.learning, e.dailyApplication).forEach { assertTrue(e.title, it.length > 20) }
            assertTrue(e.reflections.isNotEmpty())
            e.reflections.forEach { r ->
                assertTrue(r.options.size >= 3)
                r.options.forEach { o -> assertTrue("${e.title}/${o.label} sin comentario", o.note.isNotBlank()) }
            }
        }
    }

    @Test
    fun `no hay puntajes, clasificaciones de padres ni diagnosticos`() {
        AdultGuide.experiences.forEach { e ->
            val all = listOf(e.objective, e.situation, e.learning, e.dailyApplication) + e.reflections.flatMap { r -> listOf(r.prompt) + r.options.map { it.note } }
            all.forEach { assertFalse("${e.title}: $it", forbidden.containsMatchIn(it)) }
        }
    }

    @Test
    fun `conexion familiar - cinco guias voluntarias de 5 a 15 minutos con nota de cuidado`() {
        assertEquals(
            listOf("Dos perspectivas, una situación", "Lo que me gustaría que supieras", "Un acuerdo para nuestra semana", "Tiempo sin preguntas", "Nuestra señal de apoyo"),
            FamilyConnection.guides.map { it.title }
        )
        FamilyConnection.guides.forEach { g ->
            assertTrue(g.minutes, Regex("\\d+").findAll(g.minutes).all { it.value.toInt() in 5..15 })
            assertTrue(g.steps.size >= 3)
            assertTrue(g.careNote.isNotBlank())
        }
        assertTrue(FamilyConnection.byId("con_senal_apoyo")!!.careNote.contains("123"))
    }
}
