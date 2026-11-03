package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.ActivityFeedback
import com.example.model.RewardInfo
import com.example.ui.theme.*

/**
 * Cierre de una actividad (Documento Técnico §4 y §7):
 * explica qué acción produjo qué cambio en la isla y pregunta, de forma opcional, si ayudó.
 */
@Composable
fun RewardCelebrationDialog(
    reward: RewardInfo,
    onDismiss: (ActivityFeedback?) -> Unit,
    onGoToIsland: (ActivityFeedback?) -> Unit
) {
    var feedback by remember { mutableStateOf<ActivityFeedback?>(null) }

    Dialog(onDismissRequest = { onDismiss(feedback) }) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("reward_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (reward.newlyUnlocked) "¡Nuevo elemento en tu isla!" else "¡Actividad terminada!",
                    fontFamily = SnigletFontFamily,
                    fontSize = 21.sp,
                    color = MareaDeepBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = reward.icon, fontSize = 44.sp)

                Text(
                    text = reward.elementName,
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = MareaTextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (reward.newlyUnlocked) {
                        "Apareció en tu isla porque completaste «${reward.sourceTitle}»."
                    } else {
                        "Ya tenías este elemento gracias a «${reward.sourceTitle}». Repetir actividades siempre suma a tus momentos de calma."
                    },
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = MareaTextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("reward_explanation")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.muni_waving),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "¿Te ayudó? (opcional)",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MareaTextDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ActivityFeedback.entries.forEach { option ->
                        FilterChip(
                            selected = feedback == option,
                            onClick = { feedback = if (feedback == option) null else option },
                            label = {
                                Text(
                                    option.label,
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("feedback_${option.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onGoToIsland(feedback) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("go_to_island_reward_button")
                ) {
                    Text("Ver mi isla ➔", fontFamily = SnigletFontFamily, fontSize = 15.sp, color = Color.White)
                }

                TextButton(
                    onClick = { onDismiss(feedback) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reward_continue_button")
                ) {
                    Text("Continuar", fontFamily = SnigletFontFamily, fontSize = 13.sp, color = MareaTextMuted)
                }
            }
        }
    }
}
