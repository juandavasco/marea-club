package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActivityItem
import com.example.model.MareaType
import com.example.domain.SafetyRules
import com.example.data.MareaContent
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MareaSelectionScreen(
    currentMarea: MareaType?,
    availableActivities: List<ActivityItem>,
    onSaveMarea: (MareaType, String, String, String, String) -> Unit,
    onLaunchActivity: (ActivityItem) -> Unit,
    onBack: () -> Unit,
    onOpenSupportDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Nada viene preseleccionado: el usuario elige su marea y, si quiere, el contexto (§4–§5).
    var selectedMarea by remember { mutableStateOf<MareaType?>(null) }
    var selectedSituation by remember(selectedMarea) { mutableStateOf("") }
    var selectedTimeOfDay by remember(selectedMarea) { mutableStateOf("") }
    var selectedLocation by remember(selectedMarea) { mutableStateOf("") }
    var customNote by remember { mutableStateOf("") }
    var hasSaved by remember(selectedMarea) { mutableStateOf(false) }

    val standardSituations = listOf("Amigos", "Escuela / Tareas", "Familia", "Cansancio", "Tiempo para mí", "Cambio inesperado")
    val noSeBodyStates = listOf("Cuerpo tenso", "Con mucha energía", "Cansado / pesado", "Relajado y sereno")
    val timeOptions = listOf("Mañana", "Tarde", "Noche", "Todo el día")
    val locationOptions = listOf("En casa", "En el cole", "Con amigos", "En camino")

    val screenBg by animateColorAsState(
        targetValue = selectedMarea?.backgroundColor ?: MareaBackground,
        animationSpec = tween(500),
        label = "tide_screen_bg"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "¿Cómo viene mi marea?",
                            fontFamily = SnigletFontFamily,
                            fontSize = 20.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = "Reconoce lo que sientes, a tu propio ritmo",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_from_marea_selection")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MareaDeepBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        },
        containerColor = screenBg,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animated subtle wave ripples in background
            SubtleOceanCanvas(
                tide = selectedMarea ?: MareaType.TRANQUILA,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Friendly Muni Welcome Note
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background((selectedMarea?.primaryColor ?: MareaTurquoise).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.muni_isotipo),
                                contentDescription = "Muni",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "El océano interior cambia constantemente",
                                fontFamily = SnigletFontFamily,
                                fontSize = 15.sp,
                                color = MareaTextDark
                            )
                            Text(
                                text = "Ninguna marea es buena o mala; cada una te da una pista de lo que necesitas hoy.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = MareaTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecciona la corriente que sientes ahora",
                    fontFamily = SnigletFontFamily,
                    fontSize = 17.sp,
                    color = MareaTextDark,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // The 5 Oceanic Tide Cards
                val allTides = listOf(
                    MareaType.TRANQUILA,
                    MareaType.MOVIDA,
                    MareaType.ALTA,
                    MareaType.TORMENTA,
                    MareaType.NO_SE
                )

                allTides.forEach { tide ->
                    OceanicTideCard(
                        tide = tide,
                        isSelected = selectedMarea == tide,
                        onClick = { selectedMarea = tide },
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                val tide = selectedMarea
                if (tide == null) {
                    Text(
                        text = "Elige una marea para continuar. Si no sabes qué sientes, «No sé qué siento» también es válido.",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = MareaTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("marea_choose_hint")
                    )
                } else {
                if (SafetyRules.showsSupportFor(tide.name)) {
                    TideSupportCard(tide = tide, onOpenSupportDirectory = onOpenSupportDirectory)
                    Spacer(modifier = Modifier.height(14.dp))
                }
                // Empathy Reflection Workspace for the Selected Tide
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with tide colors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(tide.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Waves,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Marea ${tide.title}",
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 18.sp,
                                    color = MareaTextDark
                                )
                                Text(
                                    text = tide.subtitle,
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = tide.textColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Muni compassionate message container
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = tide.backgroundColor.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tide.muniMessage,
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MareaTextDark,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Context reflection chips
                        if (tide == MareaType.NO_SE) {
                            Text(
                                text = "¿Cómo se siente tu cuerpo? (opcional)",
                                fontFamily = SnigletFontFamily,
                                fontSize = 14.sp,
                                color = MareaTextDark,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            noSeBodyStates.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { opt ->
                                        val isSel = selectedSituation == opt
                                        OutlinedButton(
                                            onClick = { selectedSituation = if (isSel) "" else opt },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSel) tide.primaryColor else Color(0xFFF1F6F9),
                                                contentColor = if (isSel) Color.White else MareaTextDark
                                            ),
                                            border = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text(text = opt, fontFamily = QuicksandFontFamily, fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        } else {
                            Text(
                                text = "¿Qué crees que influyó? (opcional)",
                                fontFamily = SnigletFontFamily,
                                fontSize = 14.sp,
                                color = MareaTextDark,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            standardSituations.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { sit ->
                                        val isSel = selectedSituation == sit
                                        OutlinedButton(
                                            onClick = { selectedSituation = if (isSel) "" else sit },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSel) tide.primaryColor else Color(0xFFF1F6F9),
                                                contentColor = if (isSel) Color.White else MareaTextDark
                                            ),
                                            border = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text(text = sit, fontFamily = QuicksandFontFamily, fontSize = 11.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "¿Cuándo y dónde? (opcional)",
                                fontFamily = SnigletFontFamily,
                                fontSize = 13.sp,
                                color = MareaTextDark,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Time row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                timeOptions.forEach { t ->
                                    val isSel = selectedTimeOfDay == t
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) tide.secondaryColor else Color(0xFFF1F6F9))
                                            .clickable { selectedTimeOfDay = if (isSel) "" else t }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = t,
                                            fontFamily = QuicksandFontFamily,
                                            fontSize = 11.sp,
                                            color = if (isSel) Color.White else MareaTextDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Location row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                locationOptions.forEach { loc ->
                                    val isSel = selectedLocation == loc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) tide.secondaryColor else Color(0xFFF1F6F9))
                                            .clickable { selectedLocation = if (isSel) "" else loc }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = loc,
                                            fontFamily = QuicksandFontFamily,
                                            fontSize = 11.sp,
                                            color = if (isSel) Color.White else MareaTextDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Personal note
                        OutlinedTextField(
                            value = customNote,
                            onValueChange = { customNote = it },
                            placeholder = {
                                Text(
                                    "Escribe tus pensamientos o palabras si quieres...",
                                    fontSize = 12.sp,
                                    color = MareaTextMuted
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(86.dp)
                                .testTag("marea_selection_note_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tide.primaryColor,
                                unfocusedBorderColor = Color(0xFFD4E6EE),
                                focusedContainerColor = Color(0xFFF9FBFC),
                                unfocusedContainerColor = Color(0xFFF9FBFC)
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action buttons
                        Button(
                            onClick = {
                                onSaveMarea(tide, selectedSituation, selectedTimeOfDay, selectedLocation, customNote)
                                hasSaved = true
                            },
                            enabled = !hasSaved,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tide.primaryColor,
                                disabledContainerColor = tide.primaryColor.copy(alpha = 0.85f),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_marea_screen_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hasSaved) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Text(
                                        text = "Marea guardada en tu registro",
                                        fontFamily = SnigletFontFamily,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Guardar en mi registro ➔",
                                        fontFamily = SnigletFontFamily,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Direct recommended activity
                        val topRecommendation = availableActivities.firstOrNull { it.recommendedFor.contains(tide) }
                        if (topRecommendation != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    if (!hasSaved) {
                                        onSaveMarea(tide, selectedSituation, selectedTimeOfDay, selectedLocation, customNote)
                                        hasSaved = true
                                    }
                                    onLaunchActivity(topRecommendation)
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, tide.primaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("tide_activity_shortcut_btn")
                            ) {
                                Text(
                                    text = "Ir a ${topRecommendation.title} →",
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MareaDeepBlue
                                )
                            }
                        }
                    }
                }

                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun OceanicTideCard(
    tide: MareaType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "tide_card_scale"
    )

    val cardBorder = if (isSelected) {
        androidx.compose.foundation.BorderStroke(2.5.dp, tide.primaryColor)
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0E3EA))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick)
            .testTag("tide_card_${tide.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Jellyfish in glowing circular portal
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(tide.backgroundColor)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) tide.primaryColor else tide.secondaryColor.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = tide.drawableRes),
                    contentDescription = "Marea ${tide.title}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Marea ${tide.title}",
                        fontFamily = SnigletFontFamily,
                        fontSize = 17.sp,
                        color = if (isSelected) tide.textColor else MareaTextDark
                    )

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tide.textColor,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "Activa ✓",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = tide.subtitle,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = tide.textColor
                )

                Text(
                    text = tide.description,
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SubtleOceanCanvas(
    tide: MareaType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean_ambient")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val waveColor = tide.primaryColor.copy(alpha = 0.05f)

        val path = Path()
        path.moveTo(0f, height * 0.75f)
        for (x in 0..width.toInt() step 16) {
            val y = (height * 0.75f) + sin((x * 0.008f) + phase) * 20f
            path.lineTo(x.toFloat(), y)
        }
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(path, waveColor)
    }
}


/**
 * Rutas de apoyo visibles al elegir marea Alta o Tormenta (Documento Técnico §5).
 */
@Composable
private fun TideSupportCard(tide: MareaType, onOpenSupportDirectory: () -> Unit) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, MareaDeepBlue),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tide_support_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "No tienes que pasar esto en soledad",
                fontFamily = SnigletFontFamily,
                fontSize = 16.sp,
                color = MareaTextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Busca a un adulto de confianza (alguien de tu familia, un docente o el orientador/a) y cuéntale cómo te sientes. También puedes llamar gratis a cualquier hora.",
                fontFamily = NunitoFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MareaTextDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${MareaContent.URGENT_PHONE}"))
                    runCatching { context.startActivity(intent) }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tide_support_call_106")
            ) {
                Text("Llamar a la Línea 106", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onOpenSupportDirectory,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tide_support_open_directory")
            ) {
                Text("Ver todas las rutas de apoyo", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = MareaDeepBlue)
            }
            Text(
                text = "Si estás en peligro ahora mismo, llama al 123.",
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                color = MareaTextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
