package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.MareaClubApp
import com.example.data.MareaRepository
import com.example.ui.theme.MareaClubTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Los 8 casos de la tabla de pruebas del Documento Técnico §17, de punta a punta en la interfaz.
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

    private fun startApp() {
        tag("welcome_start_button").performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun goToMareas() {
        tag("tab_marea").performClick()
        compose.waitForIdle()
    }

    private fun saveTide(tide: String) {
        tag("tide_card_$tide").performScrollTo().performClick()
        tag("save_marea_screen_btn").performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun `instalacion nueva muestra la bienvenida con informacion de privacidad`() {
        tag("welcome_start_button").assertExists()
        compose.onNodeWithText("Tus datos se quedan contigo", substring = true).assertExists()
    }

    @Test
    fun `17-1 registro de marea guardado y reflejado en el historial`() {
        startApp()
        goToMareas()
        tag("marea_choose_hint").assertExists()
        saveTide("movida")
        tag("save_marea_screen_btn").assertIsNotEnabled()
        tag("menu_dots_button").performClick()
        tag("menu_archivo_button").performClick()
        val saved = viewModel.logs.value.single()
        tag("delete_log_${saved.id}").assertExists()
    }

    @Test
    fun `17-2 opcion de incertidumbre permite continuar sin marea concreta ni contexto`() {
        startApp()
        goToMareas()
        saveTide("no_se")
        compose.onAllNodesWithText("Marea guardada en tu registro").onFirst().assertExists()
        assert(viewModel.logs.value.single().situation.isEmpty())
    }

    @Test
    fun `fila 5 - tormenta muestra rutas de apoyo y adulto de confianza`() {
        startApp()
        goToMareas()
        tag("tide_card_tranquila").performScrollTo().performClick()
        tag("tide_support_card").assertDoesNotExist()
        tag("tide_card_tormenta").performScrollTo().performClick()
        tag("tide_support_card").assertExists()
        tag("tide_support_open_directory").performScrollTo().performClick()
        compose.onAllNodesWithText("Línea Amiga Saludable", substring = true).onFirst().assertExists()
    }

    @Test
    fun `17-3 actividad - salir sin penalizacion y retomar`() {
        startApp()
        tag("home_activity_act_diario").performScrollTo().performClick()
        tag("close_activity_button").performClick()
        compose.waitForIdle()
        assert(viewModel.calmMomentsCount.value == 0)
        tag("home_activity_act_diario").performScrollTo().performClick()
        tag("sea_diary_input").assertExists()
    }

    @Test
    fun `17-4 progreso de isla - el cambio se relaciona con una actividad identificable`() {
        startApp()
        tag("home_activity_act_diario").performScrollTo().performClick()
        tag("sea_diary_input").performTextInput("Hoy me sentí en paz en el parque")
        tag("complete_diary_button").performScrollTo().performClick()
        tag("reward_dialog").assertExists()
        compose.onNodeWithText("Apareció en tu isla porque completaste «Diario del Mar».").assertExists()
        tag("feedback_ayudo").performClick()
        tag("go_to_island_reward_button").performClick()
        compose.waitForIdle()
        assert("sofa_azul" in viewModel.islandState.value.unlockedElementIds)
        assert(viewModel.repository.completions.value.first().feedback?.name == "AYUDO")
        // El diario quedó guardado
        assert(viewModel.reflections.value.single().text.contains("parque"))
    }

    @Test
    fun `17-5 exploracion - los elementos abren detalle y permiten regresar`() {
        startApp()
        viewModel.repository.completeActivity("act_respiracion")
        tag("tab_island").performClick()
        tag("island_element_flores_coral").performScrollTo().performClick()
        compose.onNodeWithText("Apareció en tu isla porque completaste «Respiración del Océano».").assertExists()
        tag("island_element_back_button").performClick()
        tag("island_element_back_button").assertDoesNotExist()
        // Al abrirlo deja de ser "Nuevo"
        assert("flores_coral" in viewModel.islandState.value.seenElementIds)
    }

    @Test
    fun `17-6 resumen semanal coincide con registros y senala datos insuficientes`() {
        startApp()
        goToMareas()
        saveTide("alta")
        tag("tab_progress").performClick()
        tag("week_low_data_text").performScrollTo().assertExists()
        compose.onNodeWithText("Hay pocos datos esta semana (1 de 3 registros)", substring = true).assertExists()
    }

    @Test
    fun `17-7 navegacion - cada pestana abre su pantalla`() {
        startApp()
        tag("tab_marea").performClick(); tag("marea_choose_hint").assertExists()
        tag("tab_island").performClick(); tag("island_interactive_viewport").assertExists()
        tag("tab_progress").performClick(); tag("weekly_chart_card").assertExists()
        tag("tab_home").performClick(); tag("marea_hub_card").assertExists()
    }

    @Test
    fun `17-7 consulta anonima se identifica como demostracion con ayuda a un toque`() {
        startApp()
        tag("menu_dots_button").performClick()
        tag("menu_anonymous_button").performClick()
        tag("anonymous_demo_banner").assertExists()
        tag("anonymous_call_106").assertExists().assertHasClickAction()
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
