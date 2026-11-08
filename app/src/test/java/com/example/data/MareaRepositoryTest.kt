package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ActivityFeedback
import com.example.model.MareaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertTrue(r.islandState.value.placed.isEmpty())
        assertTrue(r.completions.value.isEmpty())
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
    fun `mareas - No se guarda si no encuentra emocion o palabra`() {
        val r = repo()
        r.recordMarea(MareaType.NO_SE, "", "", "", "", uncertainty = "SIN_PALABRA")
        assertEquals("SIN_PALABRA", repo().logs.value.first().uncertainty)
        // En otras mareas la incertidumbre no aplica
        r.recordMarea(MareaType.ALTA, "", "", "", "", uncertainty = "NO_SE")
        assertEquals("", r.logs.value.first().uncertainty)
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
        assertTrue(r.islandState.value.placed.isEmpty())
    }

    @Test
    fun `isla - una actividad significativa ofrece objetos y el nino elige uno`() {
        val r = repo()
        val offer = r.completeGuidedActivity("act_mapa_situaciones")!!
        assertEquals(listOf("jardin", "faro"), offer.objectIds)
        assertTrue(r.placeIslandObject(offer, "faro"))
        val placed = repo().islandState.value.placed.single()
        assertEquals("faro", placed.objectId)
        assertEquals("Mi mapa de situaciones", placed.sourceTitle)
    }

    @Test
    fun `isla - no se puede colocar un objeto que no fue ofrecido ni repetirlo`() {
        val r = repo()
        val offer = r.completeGuidedActivity("act_dos_mareas")!!
        assertFalse(r.placeIslandObject(offer, "puente"))
        assertTrue(r.placeIslandObject(offer, "hamaca"))
        assertFalse(r.placeIslandObject(offer, "hamaca"))
        // La siguiente oferta ya no incluye la hamaca
        val next = r.completeGuidedActivity("act_dos_mareas")!!
        assertEquals(listOf("jardin"), next.objectIds)
    }

    @Test
    fun `isla - decir ahora no deja la isla igual`() {
        val r = repo()
        r.completeGuidedActivity("act_mi_ritmo")
        assertTrue(r.islandState.value.placed.isEmpty())
        assertEquals(1, r.completions.value.size)
    }

    @Test
    fun `isla - las herramientas para calmarme no cambian la isla`() {
        val r = repo()
        r.calmTools.forEach { r.completeCalmTool(it.id) }
        assertTrue(r.islandState.value.placed.isEmpty())
        assertEquals(r.calmTools.size, r.completions.value.size)
    }

    @Test
    fun `isla - la reflexion de un objeto queda cifrada y se recupera`() {
        val r = repo(TinkTextCipher(context))
        val offer = r.completeGuidedActivity("act_mapa_situaciones")!!
        r.placeIslandObject(offer, "jardin")
        r.setIslandReflection("jardin", "me recuerda cuidar mis plantas")
        assertFalse(prefsDump().contains("plantas"))
        assertEquals("me recuerda cuidar mis plantas", repo(TinkTextCipher(context)).islandState.value.placed.single().reflection)
    }

    @Test
    fun `fila 9 - la respuesta te ayudo es opcional y se guarda`() {
        val r = repo()
        val closing = r.completeCalmTool("act_respiracion")
        r.recordActivityFeedback(closing.completionId, ActivityFeedback.AYUDO)
        assertEquals(ActivityFeedback.AYUDO, repo().completions.value.first().feedback)
        val second = r.completeCalmTool("act_conchas")
        assertNull(r.completions.value.first { it.id == second.completionId }.feedback)
    }

    @Test
    fun `actividad - el diario solo guarda lo que el nino elige`() {
        val r = repo()
        r.completeGuidedActivity("act_lo_que_necesito")
        assertTrue(r.reflections.value.isEmpty())
        r.saveActivityToDiary("Lo que necesito", "Necesito que me escuchen")
        assertEquals("Lo que necesito", repo().reflections.value.single().prompt)
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
    fun `reflexion semanal se guarda y no cambia la isla`() {
        val r = repo()
        r.addReflection("Respirar antes del examen me ayudó")
        assertEquals(1, r.reflections.value.size)
        assertTrue(r.islandState.value.placed.isEmpty())
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
        r.completeCalmTool("act_dibujo")
        r.placeIslandObject(r.completeGuidedActivity("act_mapa_situaciones")!!, "faro")
        r.addDiaryEntry("p", "texto")
        r.toggleFavoriteRoute("route_linea_amiga")
        r.resetAllData()
        assertFalse(r.isOnboarded.value)
        assertTrue(r.logs.value.isEmpty())
        assertTrue(r.reflections.value.isEmpty())
        assertTrue(r.favoriteRouteIds.value.isEmpty())
        assertTrue(r.islandState.value.placed.isEmpty())
        assertTrue(r.completions.value.isEmpty())
        assertFalse(repo().isOnboarded.value)
    }

    @Test
    fun `fila 33 - los textos emocionales no quedan legibles en el almacenamiento`() {
        val r = repo(TinkTextCipher(context))
        r.recordMarea(MareaType.TORMENTA, "Familia", "", "", "me siento muy triste hoy")
        r.addDiaryEntry("p", "secreto del diario")
        val dump = prefsDump()
        assertFalse(dump.contains("triste"))
        assertFalse(dump.contains("secreto"))
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
            .putString("unlocked_elements", "sofa_azul")
            .commit()
        repo()
        val dump = prefsDump()
        assertFalse(dump.contains("texto plano viejo"))
        assertFalse(dump.contains("MAR-482"))
        assertFalse(dump.contains("sofa_azul"))
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
