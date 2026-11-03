package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class QuestionnaireItem(
    val question: String,
    val options: List<String>
)

@Composable
fun SelfEvaluationDialog(
    onDismiss: () -> Unit,
    onOpenSupportRoutes: () -> Unit
) {
    val questions = listOf(
        QuestionnaireItem(
            question = "¿Con qué frecuencia has sentido tu marea en Tormenta o Alta en las últimas semanas?",
            options = listOf("Casi nunca o pocas veces", "Algunos días a la semana", "La mayor parte del tiempo")
        ),
        QuestionnaireItem(
            question = "¿Sientes que tus preocupaciones te impiden dormir o concentrarte en el colegio?",
            options = listOf("No, puedo descansar bien", "A veces me cuesta", "Sí, me ocurre con frecuencia")
        ),
        QuestionnaireItem(
            question = "¿Tienes a alguien de confianza (familia, amigos, profesores) con quien hablar de lo que sientes?",
            options = listOf("Sí, cuento con apoyo", "Me cuesta abrirme pero lo intento", "Siento que nadie me escucha")
        )
    )

    val answers = remember { mutableStateMapOf<Int, Int>() }
    var evaluationDone by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
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
                    Text(
                        text = "Autoevaluación Preliminar",
                        fontFamily = SnigletFontFamily,
                        fontSize = 19.sp,
                        color = MareaTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = "Cuestionario breve y orientativo (no clínico). Te ayuda a reflexionar y saber si una ruta de apoyo puede acompañarte.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (!evaluationDone) {
                    questions.forEachIndexed { qIdx, item ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MareaBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${qIdx + 1}. ${item.question}",
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MareaTextDark
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                item.options.forEachIndexed { optIdx, optText ->
                                    val isSelected = answers[qIdx] == optIdx
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) MareaTealBar else Color.White)
                                            .clickable { answers[qIdx] = optIdx }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = optText,
                                            fontFamily = NunitoFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else MareaTextDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { evaluationDone = true },
                        enabled = answers.size == questions.size,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("submit_self_eval_button")
                    ) {
                        Text("Ver orientación personalizada ➔", fontFamily = SnigletFontFamily, color = Color.White)
                    }
                } else {
                    // Result & Guidance
                    val score = answers.values.sum()
                    val needsProfessionalRoute = score >= 4

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (needsProfessionalRoute) MareaCardMedium else MareaCardLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (needsProfessionalRoute) "🌊 Tu marea ha estado alta o tempestuosa" else "🌱 Tu marea se mantiene en movimiento manejable",
                                fontFamily = SnigletFontFamily,
                                fontSize = 16.sp,
                                color = MareaTextDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (needsProfessionalRoute) {
                                    "Has estado cargando con olas pesadas. Aunque las herramientas de respiración e isla ayudan, te sugerimos acercarte a un adulto de confianza o contactar las rutas gratuitas de atención psicológica en Medellín como la Línea Amiga (106) o los Escuchaderos del Metro."
                                } else {
                                    "Tus emociones fluyen con naturalidad. Continúa utilizando los ejercicios de calma, el diario de reflexiones y visitando tu isla personal para nutrir tu bienestar."
                                },
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MareaTextDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (needsProfessionalRoute) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenSupportRoutes()
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text("Ver Rutas de Atención en Medellín ➔", fontFamily = SnigletFontFamily, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", fontFamily = SnigletFontFamily, color = MareaTextDark)
                    }
                }
            }
        }
    }
}
