package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MareaRepository
import com.example.model.MareaType
import com.example.model.SkyMode
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Marea Club`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Marea Club", appName)
    }

    @Test
    fun `repository logs marea and updates progress`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        val initialCount = repo.logs.value.size

        repo.recordMarea(
            type = MareaType.TRANQUILA,
            situation = "Lectura relajante",
            timeOfDay = "Tarde",
            location = "En casa",
            note = "Me sentí en serenidad"
        )

        assertEquals(initialCount + 1, repo.logs.value.size)
        assertEquals(MareaType.TRANQUILA, repo.logs.value.first().type)
        assertEquals("Lectura relajante", repo.logs.value.first().situation)
    }

    @Test
    fun `repository completes activity and unlocks island reward`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        val initialActivities = repo.activitiesExploredCount.value
        repo.completeActivity("act_respiracion")

        assertEquals(initialActivities + 1, repo.activitiesExploredCount.value)
        assertTrue(repo.islandState.value.unlockedElementIds.contains("flores_coral"))
    }

    @Test
    fun `repository handles anonymous consultation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        val consultation = repo.submitAnonymousConsultation("Tengo dudas sobre cómo calmarme en época de exámenes")
        assertNotNull(consultation.id)
        assertTrue(consultation.muniResponse.isNotEmpty())
        assertTrue(repo.consultations.value.isNotEmpty())
        assertFalse(consultation.isEmergencyFlagged)
    }

    @Test
    fun `repository flags emergency keywords in anonymous consultation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        val consultation = repo.submitAnonymousConsultation("Siento que no quiero vivir y estoy en crisis")
        assertTrue(consultation.isEmergencyFlagged)
        assertTrue(consultation.muniResponse.contains("Línea Amiga"))
    }

    @Test
    fun `psychological routes favorites toggle correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        val routeId = "route_linea_amiga"
        assertFalse(repo.favoriteRouteIds.value.contains(routeId))

        repo.toggleFavoriteRoute(routeId)
        assertTrue(repo.favoriteRouteIds.value.contains(routeId))

        repo.toggleFavoriteRoute(routeId)
        assertFalse(repo.favoriteRouteIds.value.contains(routeId))
    }

    @Test
    fun `questionnaires evaluate into non-clinical levels`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        assertEquals(7, repo.questionnaires.size)

        // All 0s -> ESTABLE
        val resultLow = repo.evaluateQuestionnaire("quest_bienestar", mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0))
        assertEquals(com.example.model.EvaluationLevel.ESTABLE, resultLow.level)

        // All 2s -> APOYO_RECOMENDADO
        val resultHigh = repo.evaluateQuestionnaire("quest_bienestar", mapOf(0 to 2, 1 to 2, 2 to 2, 3 to 2))
        assertEquals(com.example.model.EvaluationLevel.APOYO_RECOMENDADO, resultHigh.level)
        assertNotNull(resultHigh.suggestedRouteId)
    }

    @Test
    fun `repository saves reflection entry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        val initialCount = repo.reflections.value.size
        repo.addReflection("Hoy aprendí que respirar profundo cambia mi perspectiva.")

        assertEquals(initialCount + 1, repo.reflections.value.size)
        assertEquals("Hoy aprendí que respirar profundo cambia mi perspectiva.", repo.reflections.value.first().text)
    }

    @Test
    fun `island sky mode updates correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MareaRepository(context)

        repo.setIslandSkyMode(SkyMode.ATARDECER)
        assertEquals(SkyMode.ATARDECER, repo.islandState.value.skyMode)

        repo.setIslandSkyMode(SkyMode.NOCHE)
        assertEquals(SkyMode.NOCHE, repo.islandState.value.skyMode)
    }
}
