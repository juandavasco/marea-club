package com.example.screenshots

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.model.MareaType
import com.example.ui.MareaViewModel

/** Recorrido de pantallas para las capturas. Se actualiza cuando cambia la navegación. */
object ScreenshotRoutes {
    fun capture(tag: (String) -> SemanticsNodeInteraction, shot: (String) -> Unit, vm: MareaViewModel) {
        fun click(t: String) {
            val node = tag(t)
            runCatching { node.performScrollTo() }
            node.performClick()
        }

        // Mareas
        tag("tab_marea").performClick(); shot("03-mareas")
        click("tide_card_tormenta"); shot("04-mareas-tormenta")
        click("tide_card_no_se"); shot("05-mareas-no-se")
        vm.logMarea(MareaType.MOVIDA, "", "", "", "")
        vm.logMarea(MareaType.TRANQUILA, "", "", "", "")
        click("marea_segment_week"); shot("06-mi-semana")

        // Actividades
        tag("tab_activities").performClick(); shot("07-actividades")
        click("guided_act_dos_mareas"); shot("08-actividad-intro")
        click("guided_start"); shot("09-actividad-paso")
        click("guided_close")

        // Isla: vacía y con un objeto elegido
        tag("tab_island").performClick(); shot("10-isla-vacia")
        tag("tab_activities").performClick()
        click("guided_act_mapa_situaciones")
        click("guided_start")
        click("option_examen"); click("guided_next")
        click("guided_skip")
        click("option_ALTA"); click("guided_next")
        click("option_hablo"); click("guided_next")
        shot("11-actividad-resumen")
        click("guided_next"); shot("12-actividad-cierre")
        click("guided_finish"); shot("13-elegir-objeto")
        click("offer_faro"); click("offer_confirm")
        tag("tab_island").performClick(); shot("14-isla-con-objeto")
        click("island_row_faro"); shot("15-isla-detalle-objeto")
        click("island_object_close")

        // Mis salvavidas
        tag("tab_home").performClick()
        click("home_salvavidas"); shot("16-salvavidas")
    }
}
