package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.random.Random

@Composable
fun ActivityRunnerScreen(
    activity: ActivityItem,
    onClose: () -> Unit,
    onComplete: () -> Unit,
    onSaveDiary: (prompt: String, text: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MareaBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_activity_button")) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar actividad", tint = MareaTextDark)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = activity.title,
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = MareaTextDark
                )
                Text(
                    text = "${activity.category.title} · ${activity.durationMin}",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted
                )
            }

            // Reward badge preview
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MareaCardLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = activity.rewardIcon, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ActivityGuideCard(activityId = activity.id)

        Spacer(modifier = Modifier.height(8.dp))

        // Activity Interaction Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (activity.id) {
                "act_respiracion" -> InteractiveOceanBreathing(onComplete = onComplete)
                "act_diario" -> InteractiveSeaDiary(onSave = { prompt, text ->
                    onSaveDiary(prompt, text)
                    onComplete()
                })
                "act_emociones" -> InteractiveEmotionCompass(onComplete = onComplete)
                "act_autoestima" -> InteractiveStrengthConstellation(onComplete = onComplete)
                "act_conchas" -> InteractiveShellPearlGame(onComplete = onComplete)
                "act_dibujo" -> InteractiveFreeDrawingCanvas(onComplete = onComplete)
                else -> InteractiveOceanBreathing(onComplete = onComplete)
            }
        }
    }
}

// 1. Interactive Ocean Breathing
@Composable
private fun InteractiveOceanBreathing(onComplete: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var phaseText by remember { mutableStateOf("Toca iniciar para sincronizar tu respiración") }
    var cyclesCompleted by remember { mutableIntStateOf(0) }
    var breathScale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                phaseText = "Inhala suavemente como la marea que llega..."
                for (i in 0..40) {
                    breathScale = 1f + (i.toFloat() / 40) * 0.45f
                    delay(100)
                }
                phaseText = "Sostén con calma y serenidad..."
                delay(3000)
                phaseText = "Exhala lento como la ola que vuelve al mar..."
                for (i in 0..40) {
                    breathScale = 1.45f - (i.toFloat() / 40) * 0.45f
                    delay(100)
                }
                cyclesCompleted++
                if (cyclesCompleted >= 3) {
                    delay(500)
                    onComplete()
                    break
                }
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ciclos de respiración: $cyclesCompleted / 3",
                fontFamily = SnigletFontFamily,
                fontSize = 15.sp,
                color = MareaDeepBlue
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(breathScale)
                    .clip(CircleShape)
                    .background(MareaTurquoise.copy(alpha = 0.35f))
                    .border(3.dp, MareaTealBar, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(MareaCardMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.muni_waving),
                        contentDescription = "Muni respirando",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            Text(
                text = phaseText,
                fontFamily = SnigletFontFamily,
                fontSize = 15.sp,
                color = MareaTextDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { isRunning = !isRunning },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFC04444) else MareaTealBar
                ),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(48.dp)
                    .testTag("toggle_breathing_runner")
            ) {
                Text(
                    text = if (isRunning) "Pausar" else "Iniciar respiración",
                    fontFamily = SnigletFontFamily,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// 2. Interactive Sea Diary
@Composable
private fun InteractiveSeaDiary(onSave: (prompt: String, text: String) -> Unit) {
    var diaryText by remember { mutableStateOf("") }
    val prompts = listOf(
        "¿Qué momento de hoy te hizo sentir paz o alegría?",
        "Si pudieras hablar con tu yo de mañana, ¿qué le dirías?",
        "¿Hay algo que quieras soltar en el mar para sentirte más ligero?"
    )
    var currentPromptIndex by remember { mutableIntStateOf(0) }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pregunta de reflexión:",
                    fontFamily = SnigletFontFamily,
                    fontSize = 15.sp,
                    color = MareaDeepBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"${prompts[currentPromptIndex]}\"",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MareaTextDark,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = { currentPromptIndex = (currentPromptIndex + 1) % prompts.size }
                ) {
                    Text(text = "Cambiar pregunta ↻", fontFamily = QuicksandFontFamily, fontSize = 12.sp, color = MareaTealText)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = diaryText,
                    onValueChange = { diaryText = it },
                    placeholder = {
                        Text(
                            "Escribe aquí con tranquilidad. Se guarda cifrado solo en tu teléfono y puedes borrarlo.",
                            fontSize = 13.sp,
                            color = MareaTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("sea_diary_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MareaTealBar
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSave(prompts[currentPromptIndex], diaryText) },
                enabled = diaryText.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .testTag("complete_diary_button")
            ) {
                Text(
                    text = "Guardar en mi diario ➔",
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

// 3. Interactive Emotion Compass
@Composable
private fun InteractiveEmotionCompass(onComplete: () -> Unit) {
    val needs = listOf(
        "Un abrazo o compañía",
        "Silencio y tiempo a solas",
        "Mover mi cuerpo y jugar",
        "Hablar de lo que me preocupa",
        "Dormir y descansar",
        "Escuchar música suave"
    )
    val selectedNeeds = remember { mutableStateListOf<String>() }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Brújula Emocional",
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = MareaTextDark
                )

                Text(
                    text = "¿Qué necesita tu mar interior hoy para sentirse en equilibrio?",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = MareaTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                needs.forEach { need ->
                    val isChecked = selectedNeeds.contains(need)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) MareaTealBar else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isChecked) selectedNeeds.remove(need) else selectedNeeds.add(need)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = need,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isChecked) Color.White else MareaTextDark
                            )
                            if (isChecked) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onComplete,
                enabled = selectedNeeds.isNotEmpty(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .testTag("complete_compass_button")
            ) {
                Text(
                    text = "Listo (${selectedNeeds.size} identificadas) ➔",
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

// 4. Interactive Strength Constellation
@Composable
private fun InteractiveStrengthConstellation(onComplete: () -> Unit) {
    val strengths = listOf(
        "Valentía", "Creatividad", "Empatía",
        "Curiosidad", "Amabilidad", "Paciencia",
        "Sentido del humor", "Persistencia"
    )
    val litStars = remember { mutableStateListOf<String>("Valentía", "Creatividad") }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MareaDeepBlue),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Constelación de Fortalezas",
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Text(
                    text = "Toca las estrellas para encender las cualidades que habitan en ti",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                // Grid of stars
                strengths.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { strength ->
                            val isLit = litStars.contains(strength)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isLit) MareaTurquoise else Color.White.copy(alpha = 0.12f))
                                    .border(
                                        1.5.dp,
                                        if (isLit) Color.White else Color.White.copy(alpha = 0.3f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        if (isLit) litStars.remove(strength) else litStars.add(strength)
                                    }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = if (isLit) "⭐ " else "✦ ", fontSize = 14.sp)
                                    Text(
                                        text = strength,
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isLit) MareaDeepBlue else Color.White
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onComplete,
                enabled = litStars.size >= 3,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaTurquoise),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .testTag("complete_constellation_button")
            ) {
                Text(
                    text = "Iluminar mi isla (${litStars.size} estrellas) ➔",
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = MareaDeepBlue
                )
            }
        }
    }
}

// 5. Interactive Shell & Pearl Game
@Composable
private fun InteractiveShellPearlGame(onComplete: () -> Unit) {
    var pearlIndex by remember { mutableIntStateOf(Random.nextInt(3)) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var roundWon by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Reto de Conchas y Perlas",
                    fontFamily = SnigletFontFamily,
                    fontSize = 18.sp,
                    color = MareaTextDark
                )

                Text(
                    text = "Entrena tu atención y concentración. Encuentra la perla brillante escondida.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = MareaTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Text(
                    text = "Aciertos: $score / 2",
                    fontFamily = SnigletFontFamily,
                    fontSize = 15.sp,
                    color = MareaDeepBlue
                )
            }

            // 3 Shells
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..2) {
                    val isRevealed = selectedIndex != null
                    val hasPearl = i == pearlIndex

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(enabled = selectedIndex == null) {
                                selectedIndex = i
                                if (hasPearl) {
                                    score++
                                    roundWon = true
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isRevealed && hasPearl) MareaTurquoise else Color.White)
                                .border(2.dp, MareaTealBar, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRevealed && hasPearl) "🔮" else "🐚",
                                fontSize = 34.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Concha ${i + 1}",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MareaTextDark
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedIndex != null) {
                    Text(
                        text = if (roundWon) "¡Genial! Has hallado la perla de concentración ✨" else "¡Estaba en otra concha! Sigue atento 🌊",
                        fontFamily = SnigletFontFamily,
                        fontSize = 14.sp,
                        color = if (roundWon) MareaDeepBlue else Color(0xFFC04444),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (score >= 2) {
                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                            modifier = Modifier.fillMaxWidth(0.8f).height(44.dp)
                        ) {
                            Text("Completar reto ➔", fontFamily = SnigletFontFamily, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = {
                                selectedIndex = null
                                roundWon = false
                                pearlIndex = Random.nextInt(3)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                            modifier = Modifier.fillMaxWidth(0.6f).height(40.dp)
                        ) {
                            Text("Siguiente ronda", fontFamily = SnigletFontFamily, color = Color.White)
                        }
                    }
                } else {
                    Text(
                        text = "Elige una concha tocándola",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = MareaTextMuted
                    )
                }
            }
        }
    }
}

data class DrawingLine(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

// 6. Interactive Free Drawing Canvas
@Composable
private fun InteractiveFreeDrawingCanvas(onComplete: () -> Unit) {
    val lines = remember { mutableStateListOf<DrawingLine>() }
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(MareaDeepBlue) }

    val palette = listOf(
        MareaDeepBlue,
        MareaBlue,
        MareaTurquoise,
        Color(0xFF299E7D),
        Color(0xFFE28943),
        Color(0xFFFFFFFF)
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lienzo Libre de Expresión",
                    fontFamily = SnigletFontFamily,
                    fontSize = 17.sp,
                    color = MareaTextDark
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = { lines.clear() }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = MareaTextMuted)
                    }

                    Button(
                        onClick = onComplete,
                        enabled = lines.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Terminar ➔", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Palette
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.forEach { color ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(if (isSelected) 28.dp else 22.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MareaTextDark else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Drawing Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .pointerInput(selectedColor) {
                        detectDragGestures(
                            onDragStart = { offset -> currentLine = listOf(offset) },
                            onDrag = { change, _ ->
                                change.consume()
                                currentLine = currentLine + change.position
                            },
                            onDragEnd = {
                                if (currentLine.isNotEmpty()) {
                                    lines.add(DrawingLine(currentLine, selectedColor, 6f))
                                    currentLine = emptyList()
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    lines.forEach { line ->
                        for (i in 0 until line.points.size - 1) {
                            drawLine(
                                color = line.color,
                                start = line.points[i],
                                end = line.points[i + 1],
                                strokeWidth = line.strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    for (i in 0 until currentLine.size - 1) {
                        drawLine(
                            color = selectedColor,
                            start = currentLine[i],
                            end = currentLine[i + 1],
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}


/** Propósito, pasos y alternativa de la actividad (Documento Técnico §6). */
@Composable
private fun ActivityGuideCard(activityId: String) {
    val guide = com.example.data.MareaContent.activityGuides[activityId] ?: return
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_guide_card")
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = guide.purpose,
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MareaTextDark
            )
            if (expanded) {
                guide.steps.forEachIndexed { i, step ->
                    Text(
                        text = "${i + 1}. $step",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTextDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = "Alternativa: ${guide.alternative}",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "Puedes salir cuando quieras con la X; no pierdes nada.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.testTag("activity_guide_toggle")
            ) {
                Text(
                    text = if (expanded) "Ocultar pasos" else "Ver pasos y alternativas",
                    fontFamily = QuicksandFontFamily,
                    fontSize = 12.sp,
                    color = MareaTealText
                )
            }
        }
    }
}
