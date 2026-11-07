package com.example.screenshots

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.MareaClubApp
import com.example.data.MareaRepository
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
    val compose = createComposeRule()

    private lateinit var viewModel: MareaViewModel
    private val outDir = File("build/screenshots").apply { mkdirs() }

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences(MareaRepository.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = MareaViewModel(app)
        compose.setContent { MareaClubTheme { MareaClubApp(viewModel = viewModel) } }
    }

    private fun shot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        FileOutputStream(File(outDir, "$name.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun tag(t: String) = compose.onNodeWithTag(t, useUnmergedTree = true)

    private fun enter() {
        tag("welcome_start_button").performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun capturar_pantallas_principales() {
        shot("01-bienvenida")
        enter()
        shot("02-inicio")
        ScreenshotRoutes.capture(this::tag, this::shot, viewModel)
    }
}
