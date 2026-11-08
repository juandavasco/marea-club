package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.domain.GuidedActivities
import com.example.model.ActivityCompletion
import com.example.model.ActivityItem
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Pestaña Actividades (Prompt Maestro §5.3):
 * - "Para explorar": las 6 experiencias con propósito.
 * - "Para calmarme": las herramientas que ya existían (decisión 3), sin relación con la isla.
 * No se muestran puntajes ni "completadas": solo una marca discreta de "ya la exploraste".
 */
@Composable
fun ActivitiesScreen(
    guided: List<GuidedActivities.Activity>,
    calmTools: List<ActivityItem>,
    completions: List<ActivityCompletion>,
    onOpenGuided: (GuidedActivities.Activity) -> Unit,
    onOpenTool: (ActivityItem) -> Unit,
    /** Solo si el niño está vinculado con su familia. */
    onOpenFamilyConnection: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val explored = completions.map { it.sourceId }.toSet()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MareaSpace.screen)
            .padding(bottom = MareaSpace.xl)
    ) {
        MareaHeader(
            title = "Actividades",
            subtitle = "Experiencias cortas para conocerte mejor. Puedes pausar o salir cuando quieras."
        )

        MareaSectionTitle("Para explorar", supporting = "Cada una trabaja algo distinto. No hay respuestas correctas.")
        Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.m)) {
            guided.forEach { activity ->
                MareaCard(
                    onClick = { onOpenGuided(activity) },
                    modifier = Modifier.testTag("guided_${activity.id}")
                ) {
                    Text(activity.title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
                    Text(
                        activity.objective,
                        fontFamily = NunitoFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MareaTextMuted,
                        modifier = Modifier.padding(top = MareaSpace.xs)
                    )
                    Text(
                        if (activity.id in explored) "${activity.minutes} · Ya la exploraste: puedes volver cuando quieras" else activity.minutes,
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MareaTealText,
                        modifier = Modifier.padding(top = MareaSpace.s)
                    )
                }
            }
        }

        if (onOpenFamilyConnection != null) {
            MareaSectionTitle("Con mi familia", supporting = "Ideas para compartir un rato con tu adulto. Solo si tú quieres.")
            MareaListRow(
                icon = "💙",
                title = "Ideas para hacer juntos",
                subtitle = "5 actividades cortas, en persona. Nada se guarda.",
                onClick = onOpenFamilyConnection,
                tag = "activities_family_connection"
            )
        }

        MareaSectionTitle("Para calmarme", supporting = "Herramientas rápidas para cuando la marea está movida.")
        Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
            calmTools.forEach { tool ->
                MareaListRow(
                    icon = tool.icon,
                    title = tool.title,
                    subtitle = "${tool.durationMin} · ${tool.description}",
                    onClick = { onOpenTool(tool) },
                    tag = "tool_${tool.id}"
                )
            }
        }
    }
}
