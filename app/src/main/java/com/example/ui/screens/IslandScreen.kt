package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.domain.IslandRules
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.model.IslandElement
import com.example.model.IslandState
import com.example.model.SkyMode
import com.example.ui.theme.*
import kotlin.random.Random

data class IslandZone(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String,
    val associatedReward: String
)

data class IslandPin(
    val elementId: String,
    val relativeX: Float,
    val relativeY: Float,
    val iconEmoji: String
)

@Composable
fun IslandScreen(
    islandState: IslandState,
    allIslandElements: List<IslandElement>,
    onNavigateToActivities: () -> Unit,
    onOpenCustomization: () -> Unit,
    onElementOpened: (String) -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var inspectedElement by remember { mutableStateOf<IslandElement?>(null) }
    // Al abrir un elemento desbloqueado deja de mostrarse como "Nuevo".
    LaunchedEffect(inspectedElement) {
        val opened = inspectedElement
        if (opened != null && opened.id in islandState.unlockedElementIds) onElementOpened(opened.id)
    }
    var inspectedZone by remember { mutableStateOf<IslandZone?>(null) }

    // Interactive Zoom and Pan state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Floating bobbing effect for the island
    val infiniteTransition = rememberInfiniteTransition(label = "island_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "island_offset_y"
    )

    val islandZones = listOf(
        IslandZone("muelle", "Muelle de la Calma", "🪵", "Lugar donde atracan tus pensamientos para descansar y soltar cargas.", "Farol de la Calma"),
        IslandZone("bosque", "Bosque de Reflexión", "🌳", "Árboles y senderos que crecen con tus registros y preguntas emocionales.", "Árbol de Sombra Fresca"),
        IslandZone("sofa", "Rincón de Descanso", "🛋️", "Espacio mullido para leer, respirar y dejar pasar cualquier tormenta.", "Sofá Azurita"),
        IslandZone("estanque", "Estanque de Muni", "🐙", "El rincón preferido de Muni para jugar entre aguas tranquilas y cristalinas.", "Piedritas de Mar"),
        IslandZone("jardin", "Jardín de Fortalezas", "🌸", "Donde florecen tus talentos, valentía y momentos de orgullo.", "Flores de Coral")
    )

    // Dynamic background brush according to SkyMode
    val skyBrush = when (islandState.skyMode) {
        SkyMode.DIA -> Brush.verticalGradient(listOf(Color(0xFFE2F4FA), Color(0xFFCBEAF5), Color(0xFFE8F6FA)))
        SkyMode.ATARDECER -> Brush.verticalGradient(listOf(Color(0xFFFFEDD8), Color(0xFFF9D0C4), Color(0xFFE6D6F2)))
        SkyMode.NOCHE -> Brush.verticalGradient(listOf(Color(0xFF13284C), Color(0xFF1E3A63), Color(0xFF0F1E38)))
    }

    val isNight = islandState.skyMode == SkyMode.NOCHE
    val titleColor = if (isNight) Color.White else MareaTextDark
    val subtitleColor = if (isNight) Color(0xFF67D4F8) else MareaDeepBlue

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(skyBrush)
    ) {
        // Night sky fireflies & stars canvas
        if (isNight) {
            NightStarsCanvas(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Title matching mockup: "Tu lugar seguro"
            Text(
                text = "Tu lugar seguro",
                fontFamily = SnigletFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 34.sp,
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Stage indicator subtitle
            Text(
                text = "Nivel ${islandState.level} · ${islandState.unlockedElementIds.size} de ${IslandRules.totalElements} elementos (${islandState.skyMode.title})",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Interactive Island Exploration Viewport with Zoom, Pan, and Element Hotspots
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("island_interactive_viewport")
            ) {
                // Interactive Gesture Container
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val nextScale = (zoomScale * zoom).coerceIn(1.0f, 2.6f)
                                zoomScale = nextScale
                                if (nextScale > 1.0f) {
                                    val maxOffsetX = 220f * (nextScale - 1f)
                                    val maxOffsetY = 150f * (nextScale - 1f)
                                    panOffsetX = (panOffsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    panOffsetY = (panOffsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                } else {
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                }
                            }
                        }
                ) {
                    val boxWidth = maxWidth
                    val boxHeight = maxHeight

                    // Island graphics and pins layer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = if (zoomScale <= 1.1f) offsetY.dp else 0.dp)
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffsetX
                                translationY = panOffsetY
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.island_illustration),
                            contentDescription = "Isla personalizable - Tu lugar seguro",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )

                        // Island Element Pins on the Island
                        val pins = listOf(
                            IslandPin("arbol_verde", 0.35f, 0.28f, "🌳"),
                            IslandPin("farol_calma", 0.53f, 0.29f, "🏮"),
                            IslandPin("sofa_azul", 0.67f, 0.33f, "🛋️"),
                            IslandPin("estanque_muni", 0.55f, 0.50f, "🐙"),
                            IslandPin("muelle_madera", 0.40f, 0.62f, "🪵"),
                            IslandPin("flores_coral", 0.72f, 0.54f, "🌸"),
                            IslandPin("camino_caracoles", 0.48f, 0.43f, "🐚"),
                            IslandPin("estrella_marina", 0.31f, 0.52f, "⭐"),
                            IslandPin("arrecife_colores", 0.22f, 0.44f, "🎨")
                        )

                        pins.forEach { pin ->
                            val elem = allIslandElements.firstOrNull { it.id == pin.elementId }
                            val isUnlocked = elem?.let { islandState.unlockedElementIds.contains(it.id) } == true
                            // Estados (§7): bloqueado, nuevo (desbloqueado sin explorar), desbloqueado, seleccionado
                            val isNew = isUnlocked && pin.elementId !in islandState.seenElementIds
                            val isSelectedPin = inspectedElement?.id == pin.elementId
                            val pinState = when {
                                isSelectedPin -> "seleccionado"
                                !isUnlocked -> "bloqueado"
                                isNew -> "nuevo, por explorar"
                                else -> "desbloqueado"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isUnlocked) Color.White.copy(alpha = 0.95f) else Color(0xFF0E4273).copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelectedPin || isNew) 3.dp else 1.5.dp,
                                        when {
                                            isSelectedPin -> MareaDeepBlue
                                            isNew -> Color(0xFFFFC94D)
                                            isUnlocked -> Color(0xFF1D95D3)
                                            else -> Color.White.copy(alpha = 0.6f)
                                        }
                                    ),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(
                                            x = boxWidth * pin.relativeX - 14.dp,
                                            y = boxHeight * pin.relativeY - 14.dp
                                        )
                                        .size(if (isSelectedPin) 34.dp else 28.dp)
                                        .testTag("island_pin_${pin.elementId}")
                                        .semantics { contentDescription = "${elem?.name ?: pin.elementId}: $pinState" }
                                        .clickable {
                                            elem?.let { inspectedElement = it }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isUnlocked) pin.iconEmoji else "🔒",
                                            fontSize = if (isUnlocked) 13.sp else 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating Controls Overlay (Top Right: Zoom In, Zoom Out, Reset)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (zoomScale > 1.05f || panOffsetX != 0f || panOffsetY != 0f) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.92f),
                                shadowElevation = 2.dp,
                                modifier = Modifier.clickable {
                                    zoomScale = 1.0f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Centrar vista",
                                        tint = MareaDeepBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Centrar",
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MareaDeepBlue
                                    )
                                }
                            }
                        }

                        // Zoom in
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    zoomScale = (zoomScale + 0.35f).coerceAtMost(2.6f)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Acercar",
                                    tint = MareaTextDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Zoom out
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    val next = (zoomScale - 0.35f).coerceAtLeast(1.0f)
                                    zoomScale = next
                                    if (next <= 1.0f) {
                                        panOffsetX = 0f
                                        panOffsetY = 0f
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Alejar",
                                    tint = MareaTextDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Floating Exploratory Helper Pill (Bottom Left)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "${(zoomScale * 100).toInt()}% · Toca los puntos para explorar",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = MareaTextDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Quick zone discovery pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                islandZones.take(3).forEach { zone ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = if (isNight) 0.2f else 0.85f),
                        modifier = Modifier
                            .clickable { inspectedZone = zone }
                            .padding(horizontal = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = zone.iconEmoji, fontSize = 12.sp)
                            Text(
                                text = zone.name.split(" ").first(),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isNight) Color.White else MareaTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 1: Level + Progress Bar ("Nivel 2 · Mi Refugio", "65%")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC7E5EE)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF886BE4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("island_level_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.badge_spiral),
                        contentDescription = "Insignia nivel",
                        modifier = Modifier.size(48.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nivel ${islandState.level} · ${islandState.levelName}",
                            fontFamily = SnigletFontFamily,
                            fontSize = 17.sp,
                            color = MareaTextDark
                        )

                        // Rounded Progress Bar matching mockup
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MareaProgressTrack)
                                .border(1.dp, MareaProgressBar.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (islandState.progressPercent / 100f).coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(MareaProgressBar)
                            )
                        }
                    }

                    Text(
                        text = "${islandState.progressPercent}%",
                        fontFamily = SnigletFontFamily,
                        fontSize = 17.sp,
                        color = MareaTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: "Tu siguiente paso"
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC7E5EE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("island_next_step_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.badge_plant),
                            contentDescription = "Insignia siguiente paso",
                            modifier = Modifier.size(54.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tu siguiente paso",
                                fontFamily = SnigletFontFamily,
                                fontSize = 20.sp,
                                color = MareaTextDark
                            )

                            Text(
                                text = IslandRules.nextSuggestion(islandState.unlockedElementIds)?.let {
                                    "Completa «${it.sourceTitle}» para que aparezca ${it.elementName} en tu isla."
                                } ?: "¡Desbloqueaste todos los elementos! Puedes repetir cualquier actividad cuando la necesites.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MareaTextDark,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Button: "Ver actividades ➔"
                    Button(
                        onClick = onNavigateToActivities,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF287A9E)),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(44.dp)
                            .testTag("see_activities_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ver actividades",
                                fontFamily = SnigletFontFamily,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "➔",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Button: "Personalizar isla"
                    OutlinedButton(
                        onClick = onOpenCustomization,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MareaTextDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MareaTextDark.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(40.dp)
                            .testTag("customize_island_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MareaTextDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Personalizar isla",
                                fontFamily = SnigletFontFamily,
                                fontSize = 13.sp,
                                color = MareaTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Elements in Refuge
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC7E5EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rincones y Elementos (${islandState.unlockedElementIds.size} activos)",
                            fontFamily = SnigletFontFamily,
                            fontSize = 16.sp,
                            color = MareaTextDark
                        )
                    }

                    Text(
                        text = "Toca cada elemento para conocer su significado en tu bienestar",
                        fontFamily = NunitoFontFamily,
                        fontSize = 11.sp,
                        color = MareaTextMuted,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    allIslandElements.forEach { elem ->
                        val isUnlocked = islandState.unlockedElementIds.contains(elem.id)
                        val isNewElem = isUnlocked && elem.id !in islandState.seenElementIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("island_element_${elem.id}")
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isUnlocked) Color.White else Color.White.copy(alpha = 0.45f))
                                .clickable { inspectedElement = elem }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = if (isUnlocked) "✨" else "🔒", fontSize = 14.sp)
                                Text(
                                    text = elem.name,
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isUnlocked) MareaTextDark else MareaTextMuted
                                )
                            }

                            Text(
                                text = when {
                                    isNewElem -> "¡Nuevo! Tócalo"
                                    isUnlocked -> "En tu isla"
                                    else -> "Con «${IslandRules.unlockForElement(elem.id)?.sourceTitle ?: "una actividad"}»"
                                },
                                fontFamily = QuicksandFontFamily,
                                fontSize = 11.sp,
                                color = if (isUnlocked) Color(0xFF1B6E47) else MareaDeepBlue,
                                textAlign = TextAlign.End,
                                modifier = Modifier.widthIn(max = 150.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Element Info Dialog
    if (inspectedElement != null) {
        val elem = inspectedElement!!
        val isUnlocked = islandState.unlockedElementIds.contains(elem.id)
        val rule = IslandRules.unlockForElement(elem.id)
        val associatedActivityName = rule?.sourceTitle ?: "Actividades de autocuidado"
        val isReflection = rule?.sourceId == com.example.data.MareaRepository.SOURCE_WEEKLY_REFLECTION

        Dialog(onDismissRequest = { inspectedElement = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = if (isUnlocked) "✨" else "🔒", fontSize = 18.sp)
                            Text(
                                text = elem.name,
                                fontFamily = SnigletFontFamily,
                                fontSize = 18.sp,
                                color = MareaTextDark
                            )
                        }
                        IconButton(onClick = { inspectedElement = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MareaTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category & Stage Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC7E5EE)
                        ) {
                            Text(
                                text = if (isUnlocked) "Desbloqueado" else "Bloqueado",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MareaDeepBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MareaBackground
                        ) {
                            Text(
                                text = elem.category.title,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = MareaTextDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meaning and pedagogical description
                    Text(
                        text = elem.meaning,
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MareaTextDark,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Associated Activity Information
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F4F8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Se desbloquea con:",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MareaDeepBlue
                            )
                            Text(
                                text = associatedActivityName,
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = MareaTextDark,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isUnlocked) "Apareció en tu isla porque completaste «$associatedActivityName»." else "Aparecerá cuando completes «$associatedActivityName».",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isUnlocked) Color(0xFF1B6E47) else Color(0xFF9A4A10)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Button(
                        onClick = {
                            inspectedElement = null
                            if (isReflection) onNavigateToProgress() else onNavigateToActivities()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealText),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("island_element_go_button")
                    ) {
                        Text(
                            text = when {
                                isReflection -> "Ir a Mi progreso ➔"
                                isUnlocked -> "Practicar de nuevo ➔"
                                else -> "Ir a la actividad ➔"
                            },
                            fontFamily = SnigletFontFamily,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { inspectedElement = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("island_element_back_button")
                    ) {
                        Text(
                            text = "Volver a la isla",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MareaTextMuted
                        )
                    }
                }
            }
        }
    }

    // Zone Info Dialog
    if (inspectedZone != null) {
        val zone = inspectedZone!!
        Dialog(onDismissRequest = { inspectedZone = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${zone.iconEmoji} ${zone.name}",
                        fontFamily = SnigletFontFamily,
                        fontSize = 20.sp,
                        color = MareaTextDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = zone.description,
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MareaTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Elemento asociado: ${zone.associatedReward}",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MareaDeepBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { inspectedZone = null },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a mi isla", fontFamily = SnigletFontFamily, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun NightStarsCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars_twinkle")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    Canvas(modifier = modifier) {
        // Draw static/twinkling stars across upper sky
        val random = Random(42)
        for (i in 0..45) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * (size.height * 0.45f)
            val radius = if (i % 5 == 0) 2.5f else 1.5f
            val alpha = if (i % 2 == 0) alphaAnim else (1f - alphaAnim * 0.5f)
            drawCircle(
                color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 1f)),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun IslandCustomizationDialog(
    islandState: IslandState,
    onDismiss: () -> Unit,
    onSkyModeSelected: (SkyMode) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        text = "Personalizar tu Isla",
                        fontFamily = SnigletFontFamily,
                        fontSize = 20.sp,
                        color = MareaTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ambiente del cielo y agua:",
                    fontFamily = SnigletFontFamily,
                    fontSize = 15.sp,
                    color = MareaTextDark,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                SkyMode.entries.forEach { mode ->
                    val isSelected = islandState.skyMode == mode
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFC7E5EE) else Color(0xFFF1F6F9)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MareaDeepBlue) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSkyModeSelected(mode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = mode.title,
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 15.sp,
                                    color = MareaTextDark
                                )
                                Text(
                                    text = mode.description,
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 12.sp,
                                    color = MareaTextMuted
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "Activo ✨",
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MareaDeepBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Guardar cambios",
                        fontFamily = SnigletFontFamily,
                        color = Color.White
                    )
                }
            }
        }
    }
}
