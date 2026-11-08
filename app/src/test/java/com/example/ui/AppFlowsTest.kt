package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.MareaClubApp
import com.example.data.MareaRepository
import com.example.model.MareaType
import com.example.ui.theme.MareaClubTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Casos de prueba del Documento Técnico §17 y del Prompt Maestro (entregas 1 y 2), de punta a punta.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AppFlowsTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var viewModel: MareaViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = MareaViewModel(app)
        compose.setContent { MareaClubTheme { MareaClubApp(viewModel = viewModel) } }
    }

    private fun tag(t: String) = compose.onNodeWithTag(t, useUnmergedTree = true)

    private fun click(t: String) {
        tag(t).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun startApp() = click("welcome_start_button")

    private fun goToMareas() {
        tag("tab_marea").performClick()
        compose.waitForIdle()
    }

    private fun saveTide(tide: String) {
        click("tide_card_$tide")
        click("save_marea_screen_btn")
    }

    /** Recorre "Mi mapa de situaciones" hasta la conclusión. */
    private fun runMapaDeSituaciones() {
        click("guided_start")
        click("option_examen"); click("guided_next")
        click("guided_skip") // ¿Dónde? es opcional
        click("option_ALTA"); click("guided_next")
        click("option_hablo"); click("guided_next")
        tag("summary_card").assertExists()
        click("guided_next")
    }

    @Test
    fun `instalacion nueva muestra la bienvenida con informacion de privacidad`() {
        tag("welcome_start_button").assertExists()
        compose.onNodeWithText("Tus datos se quedan contigo", substring = true).assertExists()
    }

    @Test
    fun `navegacion - las cuatro pestanas del nino abren su pantalla`() {
        startApp()
        tag("home_register_marea").assertExists()
        tag("tab_marea").performClick(); tag("marea_choose_hint").assertExists()
        tag("tab_activities").performClick(); tag("guided_act_mapa_situaciones").assertExists()
        tag("tab_island").performClick(); tag("island_scene").assertExists()
        tag("tab_home").performClick(); tag("home_salvavidas").assertExists()
    }

    @Test
    fun `17-1 registro de marea guardado y reflejado en el historial`() {
        startApp()
        click("home_register_marea")
        tag("marea_choose_hint").assertExists()
        saveTide("movida")
        tag("save_marea_screen_btn").assertIsNotEnabled()
        tag("menu_dots_button").performClick()
        tag("menu_archivo_button").performClick()
        val saved = viewModel.logs.value.single()
        tag("delete_log_${saved.id}").assertExists()
    }

    @Test
    fun `17-2 No se permite decir que no encuentra una emocion sin contexto`() {
        startApp()
        goToMareas()
        click("tide_card_no_se")
        click("uncertainty_sin_palabra")
        click("save_marea_screen_btn")
        compose.onAllNodesWithText("Marea guardada en tu registro").onFirst().assertExists()
        val log = viewModel.logs.value.single()
        assert(log.type == MareaType.NO_SE)
        assert(log.uncertainty == "SIN_PALABRA")
        assert(log.situation.isEmpty())
    }

    @Test
    fun `fila 5 - tormenta muestra apoyo y lleva a Mis salvavidas`() {
        startApp()
        goToMareas()
        click("tide_card_tranquila")
        tag("tide_support_card").assertDoesNotExist()
        click("tide_card_tormenta")
        tag("tide_support_card").assertExists()
        click("tide_support_open_directory")
        tag("salvavidas_call_106").assertExists().assertHasClickAction()
    }

    @Test
    fun `mi semana vive dentro de Mareas y senala datos insuficientes`() {
        startApp()
        goToMareas()
        saveTide("alta")
        click("marea_segment_week")
        tag("weekly_chart_card").assertExists()
        tag("week_low_data_text").performScrollTo().assertExists()
        compose.onNodeWithText("Hay pocos datos esta semana (1 de 3 registros)", substring = true).assertExists()
    }

    @Test
    fun `17-3 herramienta para calmarme - salir sin penalizacion y retomar`() {
        startApp()
        tag("tab_activities").performClick()
        click("tool_act_diario")
        tag("close_activity_button").performClick()
        compose.waitForIdle()
        assert(viewModel.completions.value.isEmpty())
        click("tool_act_diario")
        tag("sea_diary_input").assertExists()
    }

    @Test
    fun `herramienta para calmarme - termina con te ayudo y no cambia la isla`() {
        startApp()
        tag("tab_activities").performClick()
        click("tool_act_diario")
        tag("sea_diary_input").performTextInput("Hoy me sentí en paz en el parque")
        click("complete_diary_button")
        tag("closing_dialog").assertExists()
        tag("feedback_ayudo").performClick()
        compose.waitForIdle()
        assert(viewModel.completions.value.first().feedback?.name == "AYUDO")
        assert(viewModel.islandState.value.placed.isEmpty())
        assert(viewModel.reflections.value.single().text.contains("parque"))
    }

    @Test
    fun `17-4 actividad con proposito - elegir un objeto y verlo en la isla con su origen`() {
        startApp()
        tag("tab_activities").performClick()
        click("guided_act_mapa_situaciones")
        runMapaDeSituaciones()
        click("guided_finish")
        tag("island_offer_dialog").assertExists()
        tag("offer_confirm").assertIsNotEnabled()
        tag("offer_faro").performClick()
        tag("offer_confirm").performClick()
        compose.waitForIdle()
        assert(viewModel.islandState.value.placed.single().objectId == "faro")
        // La respuesta no se guarda en el diario si el niño no lo eligió
        assert(viewModel.reflections.value.isEmpty())
        tag("tab_island").performClick()
        click("island_row_faro")
        tag("island_object_origin").assertTextContains("Mi mapa de situaciones", substring = true)
    }

    @Test
    fun `actividad con proposito - ahora no deja la isla igual y guardar en diario es opcional`() {
        startApp()
        tag("tab_activities").performClick()
        click("guided_act_mapa_situaciones")
        runMapaDeSituaciones()
        click("guided_save_diary")
        click("guided_finish")
        click("offer_skip")
        assert(viewModel.islandState.value.placed.isEmpty())
        assert(viewModel.reflections.value.single().prompt == "Mi mapa de situaciones")
    }

    @Test
    fun `actividad con proposito - salir a la mitad no guarda nada`() {
        startApp()
        tag("tab_activities").performClick()
        click("guided_act_mapa_situaciones")
        click("guided_start")
        click("option_examen")
        tag("guided_close").performClick()
        compose.waitForIdle()
        assert(viewModel.completions.value.isEmpty())
        tag("guided_act_mapa_situaciones").assertExists()
    }

    @Test
    fun `mis salvavidas - sin adulto vinculado el boton no simula un envio`() {
        startApp()
        click("home_salvavidas")
        tag("salvavidas_request_button").assertIsNotEnabled()
        tag("salvavidas_not_linked").assertExists()
        click("salvavidas_all_lines")
        compose.onAllNodesWithText("Línea Amiga Saludable", substring = true).onFirst().assertExists()
    }

    @Test
    fun `17-8 reinicio vuelve al estado inicial sin datos`() {
        startApp()
        goToMareas()
        saveTide("tranquila")
        tag("menu_dots_button").performClick()
        tag("menu_reset_button").performScrollTo().performClick()
        tag("reset_confirm_button").assertIsNotEnabled()
        tag("reset_understood_checkbox").performClick()
        tag("reset_confirm_button").performClick()
        compose.waitForIdle()
        tag("welcome_start_button").assertExists()
        assert(viewModel.logs.value.isEmpty())
    }
}
