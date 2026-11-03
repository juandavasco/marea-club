package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Gentle floating effect for Muni logo
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcome_offset"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MareaBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Official Logo with Muni & Wave
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .offset(y = offsetY.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.marea_full_logo),
                    contentDescription = "Marea Club Logo Oficial",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tu espacio de bienestar emocional",
                fontFamily = SnigletFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                color = MareaDeepBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Las emociones cambian igual que las mareas del mar. Aquí puedes reconocer lo que sientes, cuidar tu isla personal y encontrar calma a tu propio ritmo.",
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MareaTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp, start = 8.dp, end = 8.dp)
            )

            // 3 Pillar Highlights
            PillarCard(
                icon = "🌊",
                title = "Las Cuatro Mareas",
                description = "Tranquila, Movida, Alta, Tormenta o No sé qué siento. Cada emoción tiene un lugar respetado sin juicios."
            )

            Spacer(modifier = Modifier.height(10.dp))

            PillarCard(
                icon = "🏝️",
                title = "Tu Isla Evolutiva",
                description = "Un refugio vivo que crece y florece cada vez que practicas una pausa, respiras o escribes."
            )

            Spacer(modifier = Modifier.height(10.dp))

            PillarCard(
                icon = "🐙",
                title = "Acompañado por Muni",
                description = "Un compañero cercano que te orienta con herramientas de calma y te recuerda que ninguna tormenta es para siempre."
            )

            Spacer(modifier = Modifier.height(10.dp))

            PillarCard(
                icon = "🔒",
                title = com.example.ui.dialogs.PrivacyText.TITLE,
                description = "Todo se guarda cifrado solo en este teléfono: sin internet, sin cuentas y sin copia en la nube. Puedes borrarlo cuando quieras desde el menú. Marea Club acompaña, pero no diagnostica ni reemplaza a un profesional."
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Start Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaTealText),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(52.dp)
                    .testTag("welcome_start_button")
            ) {
                Text(
                    text = "Comenzar mi recorrido ➔",
                    fontFamily = SnigletFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Para preadolescentes de ${com.example.data.MareaContent.AGE_RANGE_LABEL} años · Si eres menor de edad, cuéntale a tu acudiente que usas la app",
                fontFamily = NunitoFontFamily,
                fontSize = 11.sp,
                color = MareaTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PillarCard(
    icon: String,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = icon,
                fontSize = 26.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SnigletFontFamily,
                    fontSize = 16.sp,
                    color = MareaTextDark
                )
                Text(
                    text = description,
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
