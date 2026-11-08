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
    onOpenFamily: () -> Unit,
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
                    icon = Icons.Default.People,
                    title = "Mi familia",
                    subtitle = "Vincular tu perfil con un adulto de confianza",
                    tag = "menu_family_button",
                    onClick = {
                        onDismiss()
                        onOpenFamily()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuItemRow(
                    icon = Icons.Default.Healing,
                    title = "Mis salvavidas",
                    subtitle = "Pedir compañía y líneas de ayuda de Medellín",
                    tag = "menu_support_button",
                    onClick = {
                        onDismiss()
                        onOpenSupport()
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
                            text = "Un espacio para chicos y chicas de 11 a 14 años: reconocer cómo te sientes, explorar actividades cortas y pedir compañía cuando la necesites, junto a Muni.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Versión ${com.example.BuildConfig.VERSION_NAME}",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextMuted,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .testTag("app_version_text")
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
