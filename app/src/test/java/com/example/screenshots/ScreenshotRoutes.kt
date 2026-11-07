package com.example.screenshots

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performClick
import com.example.ui.MareaViewModel

/** Recorrido de pantallas para las capturas. Se actualiza cuando cambia la navegación. */
object ScreenshotRoutes {
    fun capture(tag: (String) -> SemanticsNodeInteraction, shot: (String) -> Unit, vm: MareaViewModel) {
        tag("tab_marea").performClick(); shot("03-mareas")
        tag("tide_card_tormenta").performClick(); shot("04-mareas-tormenta")
        tag("tab_island").performClick(); shot("05-isla")
        tag("tab_progress").performClick(); shot("06-progreso")
    }
}
