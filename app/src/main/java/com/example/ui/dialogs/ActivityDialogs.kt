package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.IslandObjects
import com.example.model.ActivityFeedback
import com.example.model.ClosingInfo
import com.example.model.IslandOffer
import com.example.ui.components.*
import com.example.ui.screens.islandObjectIcon
import com.example.ui.theme.*

/**
 * Después de una actividad significativa: elegir UN objeto entre 2 o 3, o ninguno (Prompt Maestro §6).
 * No es un premio: es una forma de recordar lo explorado.
 */
@Composable
fun IslandOfferDialog(offer: IslandOffer, onChoose: (String?) -> Unit) {
    var chosen by remember(offer) { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = { onChoose(null) }) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.testTag("island_offer_dialog")) {
            Column(Modifier.padding(MareaSpace.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "¿Quieres llevar algo a tu isla?",
                    fontFamily = SnigletFontFamily,
                    fontSize = 21.sp,
                    color = MareaDeepBlue,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Elige un objeto para recordar lo que exploraste en «${offer.activityTitle}». Es opcional.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    color = MareaTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = MareaSpace.xs, bottom = MareaSpace.l)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MareaSpace.s), modifier = Modifier.fillMaxWidth()) {
                    offer.objectIds.mapNotNull(IslandObjects::byId).forEach { obj ->
                        val isSel = chosen == obj.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) Color(0xFFE3F2F8) else Color.White)
                                .border(if (isSel) 2.dp else 1.dp, if (isSel) MareaDeepBlue else Color(0xFFD6E5EB), RoundedCornerShape(16.dp))
                                .semantics { selected = isSel }
                                .clickable(role = Role.RadioButton) { chosen = obj.id }
                                .testTag("offer_${obj.id}")
                                .padding(vertical = MareaSpace.m, horizontal = MareaSpace.xs)
                        ) {
                            Box(
                                Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEAF4F8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(islandObjectIcon(obj.id), contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(28.dp))
                            }
                            Text(obj.name, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MareaTextDark, modifier = Modifier.padding(top = MareaSpace.xs))
                            Text(obj.meaning, fontFamily = NunitoFontFamily, fontSize = 12.sp, color = MareaTextMuted, textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(Modifier.height(MareaSpace.l))
                MareaPrimaryButton("Llevar a mi isla", onClick = { onChoose(chosen) }, enabled = chosen != null, tag = "offer_confirm")
                MareaTextButton("Ahora no", onClick = { onChoose(null) }, tag = "offer_skip")
            }
        }
    }
}

/** Cierre de una herramienta de "Para calmarme": pregunta opcional "¿te ayudó?". */
@Composable
fun ClosingDialog(info: ClosingInfo, onDone: (ActivityFeedback?) -> Unit) {
    Dialog(onDismissRequest = { onDone(null) }) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.testTag("closing_dialog")) {
            Column(Modifier.padding(MareaSpace.xl)) {
                Text("Terminaste «${info.title}»", fontFamily = SnigletFontFamily, fontSize = 20.sp, color = MareaDeepBlue)
                Text(
                    "¿Te ayudó? Responder es opcional.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(top = MareaSpace.xs, bottom = MareaSpace.l)
                )
                Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
                    ActivityFeedback.entries.forEach { option ->
                        MareaOption(
                            text = option.label,
                            selected = false,
                            onClick = { onDone(option) },
                            tag = "feedback_${option.name.lowercase()}"
                        )
                    }
                }
                MareaTextButton("Prefiero no responder", onClick = { onDone(null) }, tag = "feedback_skip", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
