package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.GuidedActivities
import com.example.model.MareaLogEntry
import com.example.model.MareaType
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Calendar

/**
 * Inicio del niño (Prompt Maestro §5.1): despejado, con tres caminos claros y sin repetir
 * los accesos que ya están en la barra inferior.
 */
@Composable
fun HomeScreen(
    userName: String,
    logs: List<MareaLogEntry>,
    suggested: GuidedActivities.Activity,
    onRegisterMarea: () -> Unit,
    onOpenSuggested: () -> Unit,
    onOpenSalvavidas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = logs.firstOrNull { isToday(it.timestamp) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MareaSpace.screen)
            .padding(bottom = MareaSpace.xl)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = MareaSpace.s, bottom = MareaSpace.l)
        ) {
            Image(
                painter = painterResource(R.drawable.muni_waving),
                contentDescription = "Muni saluda",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.padding(start = MareaSpace.m)) {
                Text("¡Hola, $userName!", fontFamily = SnigletFontFamily, fontSize = 26.sp, color = MareaDeepBlue)
                Text(
                    "Este es tu espacio. Ve a tu ritmo.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 15.sp,
                    color = MareaTextMuted
                )
            }
        }

        // 1. Acción principal: registrar la marea
        MareaCard(contentPadding = PaddingValues(MareaSpace.l)) {
            Text("¿Cómo viene tu marea?", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = MareaTextDark)
            Text(
                text = if (today != null) "Hoy registraste: ${today.type.title}. Puedes registrar otra si cambió." else "Elige lo que más se acerque a cómo te sientes. También vale no saber.",
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MareaTextMuted,
                modifier = Modifier
                    .padding(top = MareaSpace.xs, bottom = MareaSpace.m)
                    .testTag("home_today_text")
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MareaSpace.l)
            ) {
                MareaType.entries.forEach { tide ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(tide.drawableRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            if (tide == MareaType.NO_SE) "No sé" else tide.title,
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = MareaTextDark,
                            modifier = Modifier.padding(top = MareaSpace.xs)
                        )
                    }
                }
            }
            MareaPrimaryButton(text = "Registrar mi marea", onClick = onRegisterMarea, tag = "home_register_marea")
        }

        // 2. Una actividad sugerida (no todo el catálogo: para eso está la pestaña)
        MareaSectionTitle("Para explorar hoy")
        MareaCard(onClick = onOpenSuggested, modifier = Modifier.testTag("home_suggested_activity")) {
            Text(suggested.title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MareaTextDark)
            Text(
                suggested.objective,
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MareaTextMuted,
                modifier = Modifier.padding(top = MareaSpace.xs)
            )
            Text(
                "${suggested.minutes} · Puedes salir cuando quieras  ›",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MareaTealText,
                modifier = Modifier.padding(top = MareaSpace.m)
            )
        }

        // 3. Mis salvavidas, siempre a la mano
        MareaSectionTitle("Si necesitas a alguien")
        MareaListRow(
            icon = "🛟",
            title = "Mis salvavidas",
            subtitle = "Pide compañía a un adulto de confianza o encuentra ayuda",
            onClick = onOpenSalvavidas,
            tag = "home_salvavidas"
        )
    }
}

private fun isToday(timestamp: Long): Boolean {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    return now.get(Calendar.YEAR) == then.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
}
