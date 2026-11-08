package com.example.screenshots

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.MareaClubApp
import com.example.data.MareaRepository
import com.example.data.family.FakeFamilyBackend
import com.example.data.family.FakeServer
import com.example.ui.FamilyViewModel
import com.example.ui.MareaViewModel
import com.example.ui.theme.MareaClubTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Capturas de pantalla para revisión visual (no comparan nada: solo generan imágenes).
 * GitHub Actions las publica en la rama `screenshots` del repositorio.
 * Ventana alta para ver cada pantalla completa sin desplazarse.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h1700dp-xhdpi")
class ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: MareaViewModel
    private lateinit var family: FamilyViewModel
    private val server = FakeServer()
    private val outDir = File("build/screenshots").apply { mkdirs() }

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = MareaViewModel(app)
        family = FamilyViewModel(app, backendFactory = { FakeFamilyBackend(server) })
        compose.setContent { MareaClubTheme { MareaClubApp(viewModel = viewModel, family = family) } }
    }

    private fun shot(name: String) {
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(1500)
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        FileOutputStream(File(outDir, "$name.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun tag(t: String) = compose.onNodeWithTag(t, useUnmergedTree = true)

    private fun enter() {
        tag("welcome_start_button").performScrollTo().performClick()
        compose.waitForIdle()
        shot("01b-quien-usa")
        tag("role_child").performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun capturar_pantallas_principales() {
        shot("01-bienvenida")
        enter()
        shot("02-inicio")
        ScreenshotRoutes.capture(this::tag, this::shot, viewModel)
    }

    @Test
    fun capturar_perfil_adulto() {
        tag("welcome_start_button").performScrollTo().performClick()
        compose.waitForIdle()
        tag("role_adult").performClick()
        shot("20-adulto-inicio-sesion")
        family.onGoogleIdToken("juan")
        shot("21-adulto-crear-familia")
        tag("adult_create_family").performScrollTo().performClick()
        shot("22-adulto-resumen")
        tag("adult_tab_familia").performClick()
        tag("adult_generate_child_code").performScrollTo().performClick()
        shot("23-adulto-codigo")
        tag("adult_tab_bitacora").performClick()
        shot("24-adulto-bitacora")
        tag("bit_rabia").performScrollTo().performClick()
        tag("experience_next").performScrollTo().performClick()
        tag("experience_next").performScrollTo().performClick()
        tag("reflection_valida").performScrollTo().performClick()
        shot("25-adulto-experiencia")
        tag("adult_tab_conexion").performClick()
        shot("26-adulto-conexion")
        tag("adult_tab_config").performClick()
        shot("27-adulto-configuracion")
    }
}
