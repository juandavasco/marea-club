package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AnonymousConsultation
import com.example.ui.theme.*

@Composable
fun AnonymousConsultationDialog(
    onDismiss: () -> Unit,
    onSubmitConsultation: (String) -> AnonymousConsultation,
    onOpenSupportRoutes: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var sentConsultation by remember { mutableStateOf<AnonymousConsultation?>(null) }

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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Canal de Consulta Anónima",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "No solicitamos tu nombre ni datos personales. Puedes consultar dudas o desahogar una situación que te inquiete.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (sentConsultation == null) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(
                                "Escribe aquí lo que te gustaría consultar u orientar...",
                                fontSize = 13.sp,
                                color = MareaTextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("anonymous_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MareaBackground,
                            unfocusedContainerColor = MareaBackground,
                            focusedBorderColor = MareaTealBar
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                sentConsultation = onSubmitConsultation(messageText)
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_anonymous_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar consulta confidencial", fontFamily = SnigletFontFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                } else {
                    // Muni Response Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.muni_waving),
                                    contentDescription = "Muni",
                                    modifier = Modifier.size(46.dp)
                                )
                                Text(
                                    text = "Respuesta de Acompañamiento",
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 15.sp,
                                    color = MareaTextDark
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = sentConsultation!!.muniResponse,
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MareaTextDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Ruta recomendada: ${sentConsultation!!.recommendedRoute}",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MareaDeepBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenSupportRoutes()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Ver Directorio de Rutas en Medellín ➔", fontFamily = SnigletFontFamily, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", fontFamily = SnigletFontFamily, color = MareaTextDark)
                    }
                }
            }
        }
    }
}
