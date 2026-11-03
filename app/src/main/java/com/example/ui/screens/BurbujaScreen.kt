package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActivityCategory
import com.example.model.ActivityItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class BurbujaMode {
    BIBLIOTECA,
    BURBUJAS,
    SONIDOS
}

@Composable
fun BurbujaScreen(
    availableActivities: List<ActivityItem>,
    onOpenActivity: (ActivityItem) -> Unit,
    onActivityCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMode by remember { mutableStateOf(BurbujaMode.BIBLIOTECA) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Burbuja de Calma",
            fontFamily = SnigletFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            color = MareaTextDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Herramientas interactivas para autorregularte y hacer crecer tu isla",
            fontFamily = NunitoFontFamily,
            fontSize = 12.sp,
            color = MareaTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Mode switch tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MareaCardLight)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModeTabButton(
                title = "Biblioteca de Actividades",
                icon = Icons.Default.GridView,
                isSelected = currentMode == BurbujaMode.BIBLIOTECA,
                onClick = { currentMode = BurbujaMode.BIBLIOTECA },
                modifier = Modifier.weight(1.3f)
            )

            ModeTabButton(
                title = "Soltar Cargas",
                icon = Icons.Default.BubbleChart,
                isSelected = currentMode == BurbujaMode.BURBUJAS,
                onClick = { currentMode = BurbujaMode.BURBUJAS },
                modifier = Modifier.weight(1f)
            )

            ModeTabButton(
                title = "Sonidos",
                icon = Icons.Default.MusicNote,
                isSelected = currentMode == BurbujaMode.SONIDOS,
                onClick = { currentMode = BurbujaMode.SONIDOS },
                modifier = Modifier.weight(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (currentMode) {
                BurbujaMode.BIBLIOTECA -> ActivityLibraryCatalog(
                    activities = availableActivities,
                    onSelectActivity = onOpenActivity
                )
                BurbujaMode.BURBUJAS -> BubblePopTool(
                    onCompleted = { onActivityCompleted("act_burbujas") }
                )
                BurbujaMode.SONIDOS -> OceanSoundPlayer(
                    onCompleted = { onActivityCompleted("act_sonidos") }
                )
            }
        }
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MareaTealBar else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MareaTextDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontFamily = SnigletFontFamily,
                fontSize = 11.sp,
                color = if (isSelected) Color.White else MareaTextDark
            )
        }
    }
}

@Composable
private fun ActivityLibraryCatalog(
    activities: List<ActivityItem>,
    onSelectActivity: (ActivityItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        activities.forEach { act ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MareaCardLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectActivity(act) }
                    .testTag("activity_card_${act.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Reward / Category Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MareaBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = act.rewardIcon, fontSize = 24.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = act.title,
                                fontFamily = SnigletFontFamily,
                                fontSize = 15.sp,
                                color = MareaTextDark
                            )
                            Text(
                                text = act.durationMin,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MareaDeepBlue
                            )
                        }

                        Text(
                            text = act.description,
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MareaTextDark,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Recompensa para tu isla:",
                                fontFamily = NunitoFontFamily,
                                fontSize = 10.sp,
                                color = MareaTextMuted
                            )
                            Text(
                                text = act.rewardTitle,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MareaDeepBlue
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Iniciar",
                        tint = MareaDeepBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

data class BubbleItem(
    val id: Int,
    val xOffset: Float,
    val yOffset: Float,
    val size: Float,
    val color: Color
)

@Composable
fun BubblePopTool(
    onCompleted: () -> Unit
) {
    var worryText by remember { mutableStateOf("") }
    var poppedCount by remember { mutableIntStateOf(0) }
    var floatingWorry by remember { mutableStateOf<String?>(null) }

    val bubbles = remember {
        mutableStateListOf(
            BubbleItem(1, 40f, 60f, 64f, Color(0xFF70CFE1)),
            BubbleItem(2, 160f, 120f, 78f, Color(0xFF4FAEC2)),
            BubbleItem(3, 230f, 50f, 58f, Color(0xFF88DDEB)),
            BubbleItem(4, 90f, 210f, 85f, Color(0xFF5AB6CB)),
            BubbleItem(5, 200f, 240f, 68f, Color(0xFF9AE6F2)),
            BubbleItem(6, 50f, 320f, 60f, Color(0xFF70CFE1))
        )
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Burbujas de Descarga",
                fontFamily = SnigletFontFamily,
                fontSize = 18.sp,
                color = MareaTextDark
            )
            Text(
                text = "Toca las burbujas para soltar tensión o escribe una carga para verla flotar mar adentro",
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                color = MareaTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MareaBackground)
            ) {
                bubbles.forEach { b ->
                    Box(
                        modifier = Modifier
                            .offset(x = b.xOffset.dp, y = b.yOffset.dp)
                            .size(b.size.dp)
                            .clip(CircleShape)
                            .background(b.color.copy(alpha = 0.65f))
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable {
                                bubbles.remove(b)
                                poppedCount++
                                // Se completa una vez al reventar la primera ronda de 6 burbujas
                                if (poppedCount == 6) {
                                    onCompleted()
                                }
                                if (bubbles.isEmpty()) {
                                    bubbles.addAll(
                                        listOf(
                                            BubbleItem(7, 60f, 80f, 70f, Color(0xFF70CFE1)),
                                            BubbleItem(8, 180f, 150f, 80f, Color(0xFF4FAEC2)),
                                            BubbleItem(9, 110f, 250f, 65f, Color(0xFF88DDEB))
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 14.sp)
                    }
                }

                if (floatingWorry != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MareaDeepBlue.copy(alpha = 0.88f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Soltando al océano:\n\"$floatingWorry\"",
                            color = Color.White,
                            fontFamily = SnigletFontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = worryText,
                    onValueChange = { worryText = it },
                    placeholder = { Text("Escribe algo que quieras soltar...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MareaTealBar
                    )
                )

                Button(
                    onClick = {
                        if (worryText.isNotBlank()) {
                            floatingWorry = worryText
                            worryText = ""
                            onCompleted()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Soltar", fontFamily = SnigletFontFamily, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun OceanSoundPlayer(
    onCompleted: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var selectedTrack by remember { mutableStateOf("Olas suaves de la costa") }
    var completedThisSession by remember { mutableStateOf(false) }
    var secondsListened by remember { mutableIntStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Sonidos reales (res/raw), generados con tools/generar_sonidos.py: originales, sin licencias de terceros
    val tracks = listOf(
        "Olas suaves de la costa" to "Sonido rítmico para tranquilizar la mente",
        "Brisa en la playa" to "Susurro de viento costero sereno",
        "Campanas submarinas" to "Tonos suaves y envolventes",
        "Lluvia tibia en el muelle" to "Gotas suaves sobre la madera"
    )
    val trackRes = mapOf(
        "Olas suaves de la costa" to R.raw.sonido_olas,
        "Brisa en la playa" to R.raw.sonido_brisa,
        "Campanas submarinas" to R.raw.sonido_campanas,
        "Lluvia tibia en el muelle" to R.raw.sonido_lluvia
    )

    // Reproductor: se crea al cambiar de pista y se libera al salir de la pantalla
    val player = remember(selectedTrack) {
        android.media.MediaPlayer.create(context, trackRes.getValue(selectedTrack))?.apply { isLooping = true }
    }
    DisposableEffect(player) {
        onDispose { runCatching { player?.release() } }
    }
    LaunchedEffect(player, isPlaying) {
        if (isPlaying) runCatching { player?.start() } else runCatching { if (player?.isPlaying == true) player.pause() }
    }
    // Escuchar 1 minuto completa la herramienta (una vez por visita)
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            kotlinx.coroutines.delay(1000)
            secondsListened++
            if (!completedThisSession && secondsListened >= LISTEN_SECONDS_TO_COMPLETE) {
                completedThisSession = true
                onCompleted()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sonidos del Océano",
                fontFamily = SnigletFontFamily,
                fontSize = 18.sp,
                color = MareaTextDark
            )
            Text(
                text = "Cierra los ojos y escucha la serenidad del agua",
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                color = MareaTextMuted
            )

            Text(
                text = if (completedThisSession) "¡Completaste un minuto de calma!" else "Escucha 1 minuto para que aparezca algo nuevo en tu isla (${secondsListened.coerceAtMost(LISTEN_SECONDS_TO_COMPLETE)}/$LISTEN_SECONDS_TO_COMPLETE s)",
                fontFamily = NunitoFontFamily,
                fontSize = 11.sp,
                color = MareaTextDark,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Visual Wave Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MareaBackground)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(12) { index ->
                    val infinite = rememberInfiniteTransition(label = "wave_$index")
                    val heightFactor by infinite.animateFloat(
                        initialValue = 0.2f,
                        targetValue = if (isPlaying) 0.9f else 0.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400 + (index * 80), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wave_height_$index"
                    )

                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight(heightFactor)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isPlaying) MareaTealBar else MareaTextMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            tracks.forEach { (name, desc) ->
                val isSelected = selectedTrack == name
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MareaCardMedium else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("sound_track_${tracks.indexOfFirst { it.first == name }}")
                        .clickable {
                            if (selectedTrack == name) {
                                isPlaying = !isPlaying
                            } else {
                                selectedTrack = name
                                isPlaying = true
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = name,
                                fontFamily = SnigletFontFamily,
                                fontSize = 14.sp,
                                color = MareaTextDark
                            )
                            Text(
                                text = desc,
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = MareaTextMuted
                            )
                        }

                        Icon(
                            imageVector = if (isSelected && isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isSelected && isPlaying) "Pausar" else "Reproducir",
                            tint = MareaDeepBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isPlaying = !isPlaying },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaTealText),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(44.dp)
                    .testTag("sound_play_pause_button")
            ) {
                Text(
                    text = if (isPlaying) "Pausar ambiente" else "Reproducir ambiente",
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}


private const val LISTEN_SECONDS_TO_COMPLETE = 60
