package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*

@Composable
fun MenuDotsDialog(
    onDismiss: () -> Unit,
    onOpenArchivo: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenSelfEvaluation: () -> Unit,
    onOpenAnonymousConsultation: () -> Unit,
    onReplayWelcome: () -> Unit,
    onOpenPrivacy: () -> Unit = {},
    onResetData: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.marea_logo),
                        contentDescription = "Marea Club",
                        modifier = Modifier.height(28.dp)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                MenuItemRow(
                    icon = Icons.Default.HistoryEdu,
                    title = "Archivo de Corrientes",
                    subtitle = "Historial de mareas y notas emocionales",
                    tag = "menu_archivo_button",
                    onClick = {
                        onDismiss()
                        onOpenArchivo()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.Default.Healing,
                    title = "Rutas de Atención (Medellín)",
                    subtitle = "Línea 106, Línea Amiga, Escuchaderos y 123",
                    tag = "menu_support_button",
                    onClick = {
                        onDismiss()
                        onOpenSupport()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.Default.AssignmentTurnedIn,
                    title = "Autoevaluación Preliminar",
                    subtitle = "Cuestionario breve no clínico de orientación",
                    tag = "menu_self_eval_button",
                    onClick = {
                        onDismiss()
                        onOpenSelfEvaluation()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.Default.Lock,
                    title = "Consulta Anónima",
                    subtitle = "Demostración: nadie lee estos mensajes",
                    tag = "menu_anonymous_button",
                    onClick = {
                        onDismiss()
                        onOpenAnonymousConsultation()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Introducción a Marea Club",
                    subtitle = "Ver los pilares y la guía de Muni",
                    tag = "menu_welcome_button",
                    onClick = {
                        onDismiss()
                        onReplayWelcome()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.Default.Lock,
                    title = "Tus datos y privacidad",
                    subtitle = "Qué se guarda, dónde y cómo borrarlo",
                    tag = "menu_privacy_button",
                    onClick = {
                        onDismiss()
                        onOpenPrivacy()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.Default.Close,
                    title = "Borrar todos mis datos",
                    subtitle = "Deja la app como recién instalada (pide confirmación)",
                    tag = "menu_reset_button",
                    onClick = {
                        onDismiss()
                        onResetData()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MareaBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "¿Qué es Marea Club?",
                            fontFamily = SnigletFontFamily,
                            fontSize = 14.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = "Un rincón seguro para preadolescentes para reconocer cómo te sientes, respirar y cuidar tu isla personal a tu propio ritmo con la compañía de Muni.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MareaDeepBlue,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = MareaTextDark
                )
                Text(
                    text = subtitle,
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MareaTextDark.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
