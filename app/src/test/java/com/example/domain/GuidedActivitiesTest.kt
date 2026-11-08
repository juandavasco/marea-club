package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Prompt Maestro §5.3 y Documento Técnico §6: requisitos comunes de las 6 actividades. QA-08. */
class GuidedActivitiesTest {

    private val all = GuidedActivities.all

    @Test
    fun `son exactamente las 6 actividades del documento`() {
        assertEquals(
            listOf(
                "Mi mapa de situaciones", "Dos mareas al mismo tiempo", "Antes de responder",
                "Lo que necesito y lo que puedo pedir", "Lo que no se ve de mí", "Mi propio ritmo"
            ),
            all.map { it.title }
        )
    }

    @Test
    fun `cada actividad tiene objetivo, explicacion, conclusion aplicable y pasos interactivos`() {
        all.forEach { a ->
            assertTrue(a.title, a.objective.isNotBlank() && a.intro.isNotBlank())
            assertTrue(a.title, a.conclusion.isNotBlank() && a.dailyTip.isNotBlank())
            assertTrue(a.title, a.steps.size >= 3)
        }
    }

    @Test
    fun `ninguna actividad es funcionalmente identica a otra`() {
        val signatures = all.map { GuidedActivities.interactionSignature(it) }
        assertEquals(signatures.size, signatures.toSet().size)
    }

    @Test
    fun `no hay puntajes, respuestas correctas ni comparaciones en los textos`() {
        val forbidden = Regex("puntaje|puntos|correcta|incorrecta|bien hecho|ganaste|nivel|mejor que|peor que", RegexOption.IGNORE_CASE)
        all.forEach { a ->
            val texts = mutableListOf(a.objective, a.intro, a.conclusion, a.dailyTip)
            a.steps.forEach { s ->
                texts += s.prompt; texts += s.help
                if (s is GuidedActivities.Choice) texts += s.options.flatMap { listOf(it.label, it.detail) }
                if (s is GuidedActivities.TwoTides) texts += s.options.map { it.label }
            }
            texts.forEach { t ->
                // "No hay una respuesta correcta para todos" es justamente lo contrario a calificar
                val clean = t.replace("No hay una respuesta correcta para todos.", "")
                assertFalse("${a.title}: \"$t\"", forbidden.containsMatchIn(clean))
            }
        }
    }

    @Test
    fun `cada actividad ofrece al menos un objeto de la isla que existe`() {
        all.forEach { a ->
            assertTrue(a.title, a.islandOffers.isNotEmpty())
            a.islandOffers.forEach { assertTrue(it, IslandObjects.byId(it) != null) }
        }
    }

    @Test
    fun `antes de responder muestra consecuencias para todas las respuestas posibles`() {
        val choice = GuidedActivities.antesDeResponder.stepById("respuesta") as GuidedActivities.Choice
        assertTrue(choice.options.all { it.detail.isNotBlank() })
    }

    @Test
    fun `dos mareas pide exactamente dos emociones y arma la frase con ambas`() {
        val a = GuidedActivities.dosMareasAlMismoTiempo
        val answers = GuidedActivities.Answers(choices = mapOf("escena" to listOf("fiesta"), "dos" to listOf("alegria", "nervios")))
        val text = GuidedActivities.diaryText(a, answers)
        assertTrue(text, text.contains("alegría y también nervios"))
    }

    @Test
    fun `la frase para pedir cambia segun la necesidad elegida`() {
        val a = GuidedActivities.loQueNecesito
        val phrase = a.steps.filterIsInstance<GuidedActivities.Phrase>().single()
        val espacio = phrase.build(GuidedActivities.Answers(choices = mapOf("necesidad" to listOf("espacio"))))
        val limite = phrase.build(GuidedActivities.Answers(choices = mapOf("necesidad" to listOf("limite"))))
        assertTrue(espacio != limite)
    }

    @Test
    fun `el texto que se guarda en el diario usa la frase editada por el nino`() {
        val a = GuidedActivities.loQueNecesito
        val answers = GuidedActivities.Answers(
            choices = mapOf("necesidad" to listOf("ayuda")),
            texts = mapOf("frase" to "Mami, ¿me ayudas con la tarea de sociales?")
        )
        assertTrue(GuidedActivities.diaryText(a, answers).contains("sociales"))
    }
}
