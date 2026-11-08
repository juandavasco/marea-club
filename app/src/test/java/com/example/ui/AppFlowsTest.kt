package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.MareaClubApp
import com.example.data.MareaRepository
import com.example.data.family.FakeFamilyBackend
import com.example.data.family.FakeServer
import com.example.data.family.LinkRole
import com.example.domain.LinkCode
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
    private lateinit var family: FamilyViewModel
    private val server = FakeServer()

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = MareaViewModel(app)
        family = FamilyViewModel(app, backendFactory = { FakeFamilyBackend(server) })
        compose.setContent { MareaClubTheme { MareaClubApp(viewModel = viewModel, family = family) } }
    }

    private fun tag(t: String) = compose.onNodeWithTag(t, useUnmergedTree = true)

    private fun click(t: String) {
        val node = tag(t)
        // Algunos controles están en diálogos o encabezados que no se desplazan
        runCatching { node.performScrollTo() }
        node.performClick()
        compose.waitForIdle()
    }

    private fun startApp() {
        click("welcome_start_button")
        click("role_child")
    }

    /** El teléfono del adulto, simulado fuera de la interfaz. */
    private val adultPhone = FakeFamilyBackend(server, "Juan")
    private var adultFamilyId = ""

    /** Un adulto (en otro "teléfono") crea la familia y genera un código. Devuelve el código. */
    private fun adultCreatesCode(): String = kotlinx.coroutines.runBlocking {
        adultPhone.signInWithGoogle("juan")
        adultFamilyId = adultPhone.createFamily("Juan", "Navegante")
        val code = LinkCode.generate()
        adultPhone.publishLinkCode(adultFamilyId, LinkRole.CHILD, LinkCode.hash(code))
        code
    }

    /** Recorre en la interfaz: vincularse con el código y crear el PIN 2580. */
    private fun linkChildThroughUi() {
        val code = adultCreatesCode()
        startApp()
        tag("menu_dots_button").performClick()
        click("menu_family_button")
        tag("child_code_input").performTextInput(code)
        click("child_link_button")
        tag("pin_new").performTextInput("2580")
        tag("pin_confirm").performTextInput("2580")
        click("pin_save")
        click("header_back_button")
    }

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
        tag("salvavidas_request_button").assertDoesNotExist()
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

    // ------------------------------------------------------------------ entrega 3

    @Test
    fun `instalacion nueva pregunta quien usa el telefono`() {
        click("welcome_start_button")
        tag("role_child").assertExists()
        click("role_adult")
        tag("adult_google_button").assertExists()
        tag("privacy_what_adult_sees").assertExists()
        click("header_back_button")
        tag("role_child").assertExists()
    }

    @Test
    fun `el nino se vincula con el codigo del adulto, crea su PIN y puede ver con quien`() {
        val code = adultCreatesCode()
        startApp()
        tag("menu_dots_button").performClick()
        click("menu_family_button")
        tag("child_code_input").performTextInput(LinkCode.format(code))
        click("child_link_button")
        // Debe crear su PIN antes de seguir
        tag("pin_new").performTextInput("1234")
        tag("pin_confirm").performTextInput("1234")
        click("pin_save")
        tag("pin_problem").assertExists()
        tag("pin_new").performTextClearance(); tag("pin_new").performTextInput("2580")
        tag("pin_confirm").performTextClearance(); tag("pin_confirm").performTextInput("2580")
        click("pin_save")
        tag("child_linked_card").assertExists()
        compose.onNodeWithText("Vinculado con Juan").assertExists()
        assert(server.families.values.single().childUid != null)
    }

    @Test
    fun `codigo invalido muestra una explicacion`() {
        startApp()
        tag("menu_dots_button").performClick()
        click("menu_family_button")
        tag("child_code_input").performTextInput("K7QX-M2PA")
        click("child_link_button")
        tag("family_message").assertExists()
        tag("child_linked_card").assertDoesNotExist()
    }

    @Test
    fun `sin adulto vinculado, Mis salvavidas lleva a vincularse`() {
        startApp()
        click("home_salvavidas")
        tag("salvavidas_request_button").assertDoesNotExist()
        click("salvavidas_open_family")
        tag("child_code_input").assertExists()
    }

    // ------------------------------------------------------------------ entrega 4

    @Test
    fun `mis salvavidas - el nino vinculado pide compania y ve la respuesta real del adulto`() {
        linkChildThroughUi()
        click("home_salvavidas")
        // Decir qué necesita es opcional: el botón ya está activo
        tag("salvavidas_request_button").assertIsEnabled()
        click("salvavidas_reason_compania")
        click("salvavidas_request_button")
        tag("salvavidas_status_enviada").assertExists()
        val req = server.requests.getValue(adultFamilyId).single()
        assert(req.reason?.wire == "compania" && req.toUid == adultPhone.currentUid)
        // El adulto responde desde su teléfono
        kotlinx.coroutines.runBlocking { adultPhone.respondToRequest(adultFamilyId, req.id, com.example.data.family.RequestStatus.VOY) }
        compose.waitForIdle()
        tag("salvavidas_status_voy").assertExists()
        click("salvavidas_close")
        tag("salvavidas_new_request").assertExists()
    }

    @Test
    fun `mis salvavidas - sin conexion se avisa y no se envia nada`() {
        linkChildThroughUi()
        server.online = false
        click("home_salvavidas")
        click("salvavidas_reason_hablar")
        click("salvavidas_request_button")
        tag("salvavidas_message").assertExists()
        assert(server.requests[adultFamilyId].isNullOrEmpty())
    }

    @Test
    fun `compartir mi semana - apagado por defecto, el adulto solo ve conteos al activarlo`() {
        linkChildThroughUi()
        goToMareas()
        saveTide("alta")
        assert(server.weeks.isEmpty())
        tag("menu_dots_button").performClick()
        click("menu_family_button")
        click("child_share_week_switch")
        compose.waitForIdle()
        val week = server.weeks.getValue(adultFamilyId)
        assert(week.counts == mapOf("ALTA" to 1))
        click("child_share_week_switch")
        compose.waitForIdle()
        assert(server.weeks.isEmpty())
    }

    @Test
    fun `adulto - crea la familia, genera un codigo y responde un pedido desde Resumen`() {
        click("welcome_start_button")
        click("role_adult")
        family.onGoogleIdToken("juan")
        compose.waitForIdle()
        tag("adult_child_alias").performTextInput("Sofi")
        click("adult_create_family")
        tag("adult_tab_familia").performClick()
        click("adult_generate_child_code")
        tag("active_code_text").assertExists()
        val code = family.activeCode.value!!.code
        // El niño se vincula desde su teléfono y pide compañía
        val childPhone = FakeFamilyBackend(server)
        val fid = family.familyId.value!!
        kotlinx.coroutines.runBlocking {
            childPhone.signInAnonymously()
            childPhone.redeemLinkCode(LinkCode.hash(code), LinkRole.CHILD, "Sofi")
            childPhone.sendCompanionRequest(fid, listOf(family.myUid!!), com.example.data.family.CompanionReason.HABLAR)
        }
        compose.waitForIdle()
        tag("adult_child_linked").assertExists()
        tag("adult_tab_resumen").performClick()
        compose.onNodeWithText("Sofi necesita acompañamiento").assertExists()
        click("adult_respond_voy")
        assert(server.requests.getValue(fid).single().status == com.example.data.family.RequestStatus.VOY)
        tag("adult_week_not_shared").assertExists()
        tag("adult_no_push_notice").assertExists()
    }

    // ------------------------------------------------------------------ entrega 5

    private fun adultWithFamily() {
        click("welcome_start_button")
        click("role_adult")
        family.onGoogleIdToken("juan")
        compose.waitForIdle()
        click("adult_create_family")
    }

    @Test
    fun `bitacora - recorre una experiencia completa sin guardar nada`() {
        adultWithFamily()
        tag("adult_tab_bitacora").performClick()
        tag("bitacora_notice").assertExists()
        click("bit_mas_explicacion")
        tag("experience_intro").assertExists()
        click("experience_next")
        click("reflection_edad")
        compose.onNodeWithText("¿Qué otras hay?", substring = true).assertExists()
        click("experience_next")
        click("reflection_amigo"); click("reflection_cansado")
        click("experience_next")
        click("reflection_abierta")
        click("experience_next")
        tag("experience_learning").assertExists()
        click("experience_done")
        tag("bit_antes_reaccion").assertExists()
    }

    @Test
    fun `conexion familiar - el adulto ve las 5 guias y abre una`() {
        adultWithFamily()
        tag("adult_tab_conexion").performClick()
        listOf("con_dos_perspectivas", "con_me_gustaria", "con_acuerdo_semana", "con_sin_preguntas", "con_senal_apoyo").forEach {
            tag(it).performScrollTo().assertExists()
        }
        click("con_senal_apoyo")
        tag("connection_care").assertExists()
        click("connection_done")
        tag("connection_notice").assertExists()
    }

    @Test
    fun `conexion familiar - el nino la ve en Actividades solo si esta vinculado`() {
        startApp()
        tag("tab_activities").performClick()
        tag("activities_family_connection").assertDoesNotExist()
    }

    @Test
    fun `conexion familiar - el nino vinculado puede abrir las ideas`() {
        linkChildThroughUi()
        tag("tab_activities").performClick()
        click("activities_family_connection")
        click("con_sin_preguntas")
        tag("connection_guide").assertExists()
    }
}
