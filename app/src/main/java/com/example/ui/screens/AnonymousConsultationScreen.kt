package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AnonymousConsultation
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonymousConsultationScreen(
    consultations: List<AnonymousConsultation>,
    onSubmitConsultation: (String) -> AnonymousConsultation,
    onBack: () -> Unit,
    onOpenSupportDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("d MMM, hh:mm a", Locale("es", "CO")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Consulta y Orientación",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MareaDeepBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { showPrivacyDialog = true }) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Privacidad", tint = MareaTealBar)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MareaBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Emergency Quick Access Ribbon
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MareaCardLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🚨", fontSize = 14.sp)
                        Text(
                            text = "¿Necesitas apoyo humano urgente en Medellín?",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextDark
                        )
                    }

                    TextButton(
                        onClick = onOpenSupportDirectory,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Línea 106 ➔",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MareaDeepBlue
                        )
                    }
                }
            }

            // Consultations conversation / message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Welcome card from Muni explaining limits
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.muni_waving),
                                contentDescription = "Muni",
                                modifier = Modifier.size(52.dp)
                            )
                            Column {
                                Text(
                                    text = "Rincón de desahogo confidencial",
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 14.sp,
                                    color = MareaTextDark
                                )
                                Text(
                                    text = "No te pedimos nombre ni colegio. Puedes escribir aquí lo que estás viviendo. Te daremos pautas de orientación y las rutas que te pueden acompañar.",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MareaTextMuted
                                )
                            }
                        }
                    }
                }

                if (consultations.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("✍️", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Aún no has enviado consultas",
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 14.sp,
                                    color = MareaTextDark
                                )
                                Text(
                                    text = "Escribe en el campo inferior lo que te inquiete o desees orientar.",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 11.sp,
                                    color = MareaTextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(consultations, key = { it.id }) { item ->
                        ConsultationMessageBubble(
                            item = item,
                            dateFormat = dateFormat,
                            onOpenDirectory = onOpenSupportDirectory,
                            onDialDirect = { dialQuick(context, "6044444448") }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Input field & Send button
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Escribe tu inquietud o consulta anónima...",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = MareaTextMuted
                            )
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MareaBackground,
                            unfocusedContainerColor = MareaBackground,
                            focusedBorderColor = MareaTealBar
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("anonymous_chat_input")
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSubmitConsultation(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) MareaDeepBlue else MareaCardMedium)
                            .testTag("send_anonymous_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacidad y Alcance",
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = MareaTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Anonimato: No guardamos tu nombre ni información de identidad personal.",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTextDark
                    )
                    Text(
                        text = "• Almacenamiento local: Tus consultas se guardan únicamente en la memoria de este teléfono.",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTextDark
                    )
                    Text(
                        text = "• Pautas de orientación: Las respuestas proporcionan orientación psicoeducativa de autorregulación y no constituyen una consulta clínica formal.",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTextDark
                    )
                    Text(
                        text = "• Emergencias: Si estás en riesgo inminente, comunícate de inmediato con la Línea 106 o 123 Social.",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MareaDeepBlue
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar)
                ) {
                    Text("Entendido", fontFamily = SnigletFontFamily, color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun ConsultationMessageBubble(
    item: AnonymousConsultation,
    dateFormat: SimpleDateFormat,
    onOpenDirectory: () -> Unit,
    onDialDirect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // User query (right aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(MareaDeepBlue)
                    .padding(14.dp)
            ) {
                Text(
                    text = item.message,
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    fontFamily = QuicksandFontFamily,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // Response bubble (left aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isEmergencyFlagged) Color(0xFFFFF3CD) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.muni_waving),
                            contentDescription = "Muni",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (item.isEmergencyFlagged) "Orientación Prioritaria de Muni" else "Orientación de Marea Club",
                            fontFamily = SnigletFontFamily,
                            fontSize = 13.sp,
                            color = if (item.isEmergencyFlagged) Color(0xFF856404) else MareaTextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.muniResponse,
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MareaTextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ruta: ${item.recommendedRoute}",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MareaDeepBlue
                        )

                        if (item.isEmergencyFlagged) {
                            Button(
                                onClick = onDialDirect,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Llamar 106", fontFamily = SnigletFontFamily, fontSize = 10.sp, color = Color.White)
                            }
                        } else {
                            TextButton(
                                onClick = onOpenDirectory,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Ver rutas ➔", fontFamily = QuicksandFontFamily, fontSize = 11.sp, color = MareaTealBar)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun dialQuick(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {}
}
