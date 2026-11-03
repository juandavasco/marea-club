package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MareaLogEntry
import com.example.ui.theme.*

@Composable
fun ArchivoCorrientesDialog(
    logs: List<MareaLogEntry>,
    onDismiss: () -> Unit,
    onDeleteLog: (String) -> Unit = {}
) {
    var pendingDelete by remember { mutableStateOf<MareaLogEntry?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Archivo de Corrientes",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = "Tus registros. Solo están en este teléfono y puedes borrarlos.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = MareaTextMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_archivo_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no has registrado mareas.\n¡Selecciona cómo te sientes hoy para comenzar!",
                            fontFamily = NunitoFontFamily,
                            fontSize = 14.sp,
                            color = MareaTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logs) { log ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MareaBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(log.type.primaryColor)
                                    ) {
                                        Image(
                                            painter = painterResource(id = log.type.drawableRes),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Marea ${log.type.title}",
                                                fontFamily = QuicksandFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MareaTextDark
                                            )
                                            Text(
                                                text = log.dayOfWeek,
                                                fontFamily = QuicksandFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = MareaDeepBlue
                                            )
                                        }

                                        Text(
                                            text = if (log.situation.isNotBlank()) "Motivo: ${log.situation}" else "Sin contexto (opcional)",
                                            fontFamily = NunitoFontFamily,
                                            fontSize = 12.sp,
                                            color = MareaTextDark,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )

                                        if (log.note.isNotBlank()) {
                                            Text(
                                                text = "\"${log.note}\"",
                                                fontFamily = NunitoFontFamily,
                                                fontSize = 12.sp,
                                                color = MareaTextMuted
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { pendingDelete = log },
                                        modifier = Modifier.testTag("delete_log_${log.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Borrar este registro",
                                            tint = MareaTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("¿Borrar este registro?", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Marea ${log.type.title} del ${log.dayOfWeek}. Se quitará también del resumen semanal. No se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLog(log.id)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_log")
                ) { Text("Borrar", color = Color(0xFF9A2A10)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar", color = MareaTextDark) }
            }
        )
    }
}
