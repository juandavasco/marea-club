package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.IslandRules
import com.example.model.ActivityFeedback
import com.example.model.MareaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

/**
 * Pruebas del almacenamiento contra la matriz de trazabilidad (Fase 0).
 * Los números de fila de la matriz aparecen en cada prueba.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MareaRepositoryTest {

    private lateinit var context: Context
    private var now = 0L

    private fun at(year: Int, month: Int, day: Int, hour: Int): Long {
        val c = Calendar.getInstance(TimeZone.getDefault())
        c.clear()
        c.set(year, month - 1, day, hour, 0)
        return c.timeInMillis
    }

    private fun repo(cipher: TextCipher = FakeCipher()) = MareaRepository(context, cipher) { now }

    private fun prefsDump(): String =
        context.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).all.toString()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        now = at(2026, 11, 4, 10) // miércoles
    }

    @Test
    fun `fila 32 - una instalacion nueva empieza sin datos y muestra la bienvenida`() {
        val r = repo()
        assertFalse(r.isOnboarded.value)
        assertTrue(r.logs.value.isEmpty())
        assertTrue(r.islandState.value.unlockedElementIds.isEmpty())
        assertEquals(0, r.calmMomentsCount.value)
    }

    @Test
    fun `17-1 registro de marea guardado y reflejado en historial y resumen`() {
        val r = repo()
        r.recordMarea(MareaType.MOVIDA, "Escuela / Tareas", "Tarde", "En el cole", "examen")
        assertEquals(1, r.logs.value.size)
        assertEquals(MareaType.MOVIDA, r.activeMarea.value)
        val wednesday = r.weeklyOverview.value[2]
        assertEquals(MareaType.MOVIDA, wednesday.mareaType)
        assertEquals(1, wednesday.entries.size)
        // Persistencia: otra instancia lee lo mismo
        assertEquals(1, repo().logs.value.size)
    }

    @Test
    fun `17-2 y filas 2-3 - no se sabe que siente y no elige contexto`() {
        val r = repo()
        val entry = r.recordMarea(MareaType.NO_SE, "", "", "", "")
        assertEquals("", entry.situation)
        assertEquals("", entry.timeOfDay)
        assertEquals("", entry.location)
        assertEquals(MareaType.NO_SE, repo().logs.value.first().type)
    }

    @Test
    fun `fila 21 - registrar emociones dificiles no cambia la isla`() {
        val r = repo()
        r.recordMarea(MareaType.TORMENTA, "", "", "", "")
        assertEquals(1, r.islandState.value.level)
        assertEquals(0, r.islandState.value.progressPercent)
    }

    @Test
    fun `17-4 y filas 16-17 - actividad desbloquea su elemento y lo explica`() {
        val r = repo()
        val reward = r.completeActivity("act_diario")!!
        assertTrue(reward.newlyUnlocked)
        assertEquals("Diario del Mar", reward.sourceTitle)
        assertEquals("Sillón de Arena Azul", reward.elementName)
        assertTrue("sofa_azul" in r.islandState.value.unlockedElementIds)
        // El elemento anunciado existe en la isla
        assertNotNull(r.allIslandElements.firstOrNull { it.name == reward.elementName })
    }

    @Test
    fun `fila 16 - cada actividad del catalogo anuncia el elemento que realmente desbloquea`() {
        val r = repo()
        r.availableActivities.forEach { act ->
            val expected = IslandRules.unlockForSource(act.id)!!
            assertEquals(expected.elementName, act.rewardTitle)
            val element = r.allIslandElements.first { it.id == expected.elementId }
            assertEquals(expected.elementName, element.name)
        }
    }

    @Test
    fun `fila 16 - burbujas y sonidos desbloquean su propio elemento`() {
        val r = repo()
        assertEquals("Farol de la Calma", r.completeActivity("act_burbujas")!!.elementName)
        assertEquals("Estanque Cristalino", r.completeActivity("act_sonidos")!!.elementName)
        assertFalse("flores_coral" in r.islandState.value.unlockedElementIds)
    }

    @Test
    fun `fila 9 - la respuesta te ayudo es opcional y se guarda`() {
        val r = repo()
        val reward = r.completeActivity("act_respiracion")!!
        r.recordActivityFeedback(reward.completionId, ActivityFeedback.AYUDO)
        assertEquals(ActivityFeedback.AYUDO, repo().completions.value.first().feedback)
        val second = r.completeActivity("act_conchas")!!
        assertNull(r.completions.value.first { it.id == second.completionId }.feedback)
    }

    @Test
    fun `contadores reales - momentos de calma y actividades distintas`() {
        val r = repo()
        r.completeActivity("act_respiracion")
        r.completeActivity("act_respiracion")
        r.completeActivity("act_dibujo")
        assertEquals(3, r.calmMomentsCount.value)
        assertEquals(2, r.activitiesExploredCount.value)
    }

    @Test
    fun `fila 13 - el diario se guarda y se puede borrar`() {
        val r = repo()
        r.addDiaryEntry("¿Qué te dio paz hoy?", "Caminar con mi perro")
        val entry = repo().reflections.value.single()
        assertTrue(entry.text.contains("perro"))
        r.deleteReflection(entry.id)
        assertTrue(repo().reflections.value.isEmpty())
    }

    @Test
    fun `reflexion semanal desbloquea el muelle`() {
        val r = repo()
        val reward = r.addReflection("Respirar antes del examen me ayudó")!!
        assertEquals("Muelle del Horizonte", reward.elementName)
    }

    @Test
    fun `17-6 y filas 14-15 - resumen por fecha real, sin mezclar semanas y con aviso de pocos datos`() {
        val r = repo()
        now = at(2026, 10, 27, 9) // martes de la semana pasada
        r.recordMarea(MareaType.TORMENTA, "", "", "", "")
        now = at(2026, 11, 3, 9) // martes de esta semana
        r.recordMarea(MareaType.TRANQUILA, "", "", "", "")
        now = at(2026, 11, 4, 10)
        val fresh = repo()
        val tuesday = fresh.weeklyOverview.value[1]
        assertEquals(1, tuesday.entries.size)
        assertEquals(MareaType.TRANQUILA, tuesday.mareaType)
        assertNull("un día vacío no se rellena", fresh.weeklyOverview.value[0].mareaType)
        assertFalse(fresh.weekHasEnoughData.value)
    }

    @Test
    fun `fila 15 - borrar un registro lo quita del resumen`() {
        val r = repo()
        val entry = r.recordMarea(MareaType.ALTA, "", "", "", "")
        r.deleteLog(entry.id)
        assertTrue(r.logs.value.isEmpty())
        assertNull(r.weeklyOverview.value[2].mareaType)
        assertNull(r.activeMarea.value)
    }

    @Test
    fun `17-8 y fila 31 - reiniciar deja la app como recien instalada`() {
        val r = repo()
        r.completeOnboarding()
        r.recordMarea(MareaType.ALTA, "Familia", "", "", "nota")
        r.completeActivity("act_dibujo")
        r.addDiaryEntry("p", "texto")
        r.submitAnonymousConsultation("hola")
        r.toggleFavoriteRoute("route_linea_amiga")
        r.resetAllData()
        assertFalse(r.isOnboarded.value)
        assertTrue(r.logs.value.isEmpty())
        assertTrue(r.reflections.value.isEmpty())
        assertTrue(r.consultations.value.isEmpty())
        assertTrue(r.favoriteRouteIds.value.isEmpty())
        assertTrue(r.islandState.value.unlockedElementIds.isEmpty())
        assertEquals(0, r.calmMomentsCount.value)
        assertFalse(repo().isOnboarded.value)
    }

    @Test
    fun `fila 33 - los textos emocionales no quedan legibles en el almacenamiento`() {
        val r = repo(TinkTextCipher(context))
        r.recordMarea(MareaType.TORMENTA, "Familia", "", "", "me siento muy triste hoy")
        r.addDiaryEntry("p", "secreto del diario")
        r.submitAnonymousConsultation("mensaje privado de prueba")
        val dump = prefsDump()
        assertFalse(dump.contains("triste"))
        assertFalse(dump.contains("secreto"))
        assertFalse(dump.contains("privado"))
        assertFalse(dump.contains("TORMENTA"))
        // Y se recupera con la misma clave
        val again = repo(TinkTextCipher(context))
        assertEquals("me siento muy triste hoy", again.logs.value.single().note)
    }

    @Test
    fun `fila 33 - se eliminan los datos sin cifrar de la version anterior`() {
        context.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("saved_logs", "[{\"note\":\"texto plano viejo\"}]")
            .putString("family_link_code", "MAR-482")
            .commit()
        repo()
        val dump = prefsDump()
        assertFalse(dump.contains("texto plano viejo"))
        assertFalse(dump.contains("MAR-482"))
    }

    @Test
    fun `filas 38-39 - la consulta anonima siempre ofrece ayuda humana y dice que es demostracion`() {
        val r = repo()
        val normal = r.submitAnonymousConsultation("Tuve un mal día en el colegio")
        assertTrue(normal.muniResponse.contains("106"))
        assertTrue(normal.muniResponse.contains("demostración"))
        assertFalse(normal.isEmergencyFlagged)
        val risk = r.submitAnonymousConsultation("Ya no aguanto más, quiero hacerme daño")
        assertTrue(risk.isEmergencyFlagged)
        assertTrue(risk.muniResponse.contains("123"))
    }

    @Test
    fun `fila 37 - la autoevaluacion no muestra puntajes y siempre sugiere rutas`() {
        val r = repo()
        val q = r.questionnaires.first()
        val allHigh = q.questions.indices.associateWith { 2 }
        val allLow = q.questions.indices.associateWith { 0 }
        listOf(allHigh, allLow).forEach { answers ->
            val result = r.evaluateQuestionnaire(q.id, answers)
            assertTrue(result.suggestedRouteIds.contains("route_linea_amiga"))
            assertFalse(result.message.contains(Regex("\\d+ ?/ ?\\d+|puntaje|nivel", RegexOption.IGNORE_CASE)))
        }
    }

    @Test
    fun `fila 40 - solo se muestran rutas verificadas con fuente, enlace y fecha`() {
        val r = repo()
        val routes = r.psychologicalRoutesMedellin
        assertTrue(routes.isNotEmpty())
        routes.forEach {
            assertTrue(it.verified)
            assertTrue(it.verificationUrl.startsWith("https://"))
            assertTrue(it.verificationDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }
        assertTrue(routes.none { it.id == "route_consultorio_udea" })
    }

    @Test
    fun `fila 35 - ya no existe vinculo de acudiente que acepte codigos`() {
        val methods = MareaRepository::class.java.declaredMethods.map { it.name }
        assertTrue(methods.none { it.contains("link", ignoreCase = true) && it.contains("Family", ignoreCase = true) })
    }
}
