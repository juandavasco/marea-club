package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.PsychologicalRoute
import com.example.ui.theme.*

@Composable
fun SupportDialog(
    routes: List<PsychologicalRoute>,
    onDismiss: () -> Unit,
    onOpenSelfEvaluation: () -> Unit,
    onOpenAnonymousConsultation: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
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
                        text = "Rutas de Atención y Ayuda",
                        fontFamily = SnigletFontFamily,
                        fontSize = 19.sp,
                        color = MareaTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Muni guidance card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MareaCardLight),
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
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "Pedir ayuda cuando el mar se pone tempestuoso es un acto de valentía. En Medellín tienes rutas gratuitas y profesionales dispuestos a escucharte.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MareaTextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Buttons (Self-Evaluation & Anonymous Form)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenSelfEvaluation()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTurquoise),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Autoevaluación", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = MareaDeepBlue)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenAnonymousConsultation()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Consulta Anónima", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Directorio activo en Medellín:",
                    fontFamily = SnigletFontFamily,
                    fontSize = 15.sp,
                    color = MareaTextDark,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                routes.forEach { route ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MareaBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = route.name,
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 13.sp,
                                    color = MareaTextDark
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MareaDeepBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = route.phoneOrContact,
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MareaDeepBlue
                                    )
                                }
                            }

                            Text(
                                text = "Horario: ${route.schedule}",
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = MareaTealBar,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Text(
                                text = route.description,
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = MareaTextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MareaCardLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Marea Club es una herramienta de autorregulación. No sustituye la consulta médica ni la atención psicológica profesional.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 10.sp,
                            color = MareaTextMuted,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Volver", fontFamily = SnigletFontFamily, color = Color.White)
                }
            }
        }
    }
}
