package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.domain.IslandObjects
import com.example.model.IslandState
import com.example.model.PlacedIslandObject
import com.example.model.SkyMode
import com.example.ui.components.*
import com.example.ui.theme.*

/** Ícono provisional de cada objeto hasta que diseño entregue sus ilustraciones. */
fun islandObjectIcon(id: String): ImageVector = when (id) {
    "hamaca" -> Icons.Default.Bedtime
    "jardin" -> Icons.Default.LocalFlorist
    "puente" -> Icons.Default.People
    else -> Icons.Default.Lightbulb
}

/** Posición de cada objeto en la escena base (fracción del ancho y alto). */
private val objectPositions = mapOf(
    "hamaca" to (0.20f to 0.48f),
    "jardin" to (0.58f to 0.26f),
    "puente" to (0.74f to 0.58f),
    "faro" to (0.36f to 0.16f)
)

private fun skyBrush(mode: SkyMode): Brush = when (mode) {
    SkyMode.DIA -> Brush.verticalGradient(listOf(Color(0xFFD7F0F8), Color(0xFFF3FAFC)))
    SkyMode.ATARDECER -> Brush.verticalGradient(listOf(Color(0xFFF9D9C4), Color(0xFFFCEFE4)))
    SkyMode.NOCHE -> Brush.verticalGradient(listOf(Color(0xFF173A63), Color(0xFF2C5A86)))
}

/**
 * Mi isla (Prompt Maestro §6): una sola escena base y los objetos que el niño eligió.
 * Sin niveles, porcentajes, candados ni contadores de progreso.
 */
@Composable
fun IslandScreen(
    islandState: IslandState,
    onSaveReflection: (objectId: String, text: String) -> Unit,
    onSkyMode: (SkyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var inspected by remember { mutableStateOf<PlacedIslandObject?>(null) }
    var showSky by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MareaSpace.screen)
            .padding(bottom = MareaSpace.xl)
    ) {
        MareaHeader(
            title = "Mi isla",
            subtitle = "Guarda objetos de lo que has explorado. No hay que completarla ni compararla."
        )

        // Escena base única
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clip(RoundedCornerShape(24.dp))
                .background(skyBrush(islandState.skyMode))
                .testTag("island_scene")
        ) {
            Image(
                painter = painterResource(R.drawable.island_illustration),
                contentDescription = "Isla de Muni",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MareaSpace.l)
            )
            BoxWithConstraints(Modifier.fillMaxSize()) {
                islandState.placed.forEach { placed ->
                    val obj = IslandObjects.byId(placed.objectId) ?: return@forEach
                    val (fx, fy) = objectPositions[placed.objectId] ?: (0.5f to 0.5f)
                    Box(
                        modifier = Modifier
                            .offset(x = maxWidth * fx - 26.dp, y = maxHeight * fy - 26.dp)
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.5.dp, MareaDeepBlue, CircleShape)
                            .semantics { contentDescription = "${obj.name}: ${obj.meaning}" }
                            .clickable { inspected = placed }
                            .testTag("island_object_${obj.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(islandObjectIcon(obj.id), contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        if (islandState.placed.isEmpty()) {
            MareaEmptyState(
                title = "Tu isla está tranquila",
                message = "Cuando termines una actividad de «Para explorar», podrás elegir un objeto para ella si quieres.",
                emoji = "🏝️",
                modifier = Modifier.testTag("island_empty")
            )
        } else {
            MareaSectionTitle("En tu isla", supporting = "Toca un objeto para ver qué significa.")
            Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
                islandState.placed.forEach { placed ->
                    val obj = IslandObjects.byId(placed.objectId) ?: return@forEach
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .clickable { inspected = placed }
                            .testTag("island_row_${obj.id}")
                            .padding(horizontal = MareaSpace.l, vertical = MareaSpace.m)
                    ) {
                        Icon(islandObjectIcon(obj.id), contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f).padding(start = MareaSpace.m)) {
                            Text(obj.name, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTextDark)
                            Text("${obj.meaning} · después de «${placed.sourceTitle}»", fontFamily = NunitoFontFamily, fontSize = 13.sp, color = MareaTextMuted)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(MareaSpace.l))
        MareaSecondaryButton(text = "Cambiar el cielo", onClick = { showSky = true }, tag = "island_sky_button")
    }

    inspected?.let { placed ->
        IslandObjectDialog(
            placed = placed,
            onSave = { text ->
                onSaveReflection(placed.objectId, text)
                inspected = null
            },
            onDismiss = { inspected = null }
        )
    }

    if (showSky) {
        Dialog(onDismissRequest = { showSky = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(Modifier.padding(MareaSpace.xl), verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
                    Text("El cielo de tu isla", fontFamily = SnigletFontFamily, fontSize = 20.sp, color = MareaDeepBlue)
                    SkyMode.entries.forEach { mode ->
                        MareaOption(
                            text = mode.title,
                            supporting = mode.description,
                            selected = islandState.skyMode == mode,
                            onClick = {
                                onSkyMode(mode)
                                showSky = false
                            },
                            tag = "sky_${mode.name.lowercase()}"
                        )
                    }
                }
            }
        }
    }
}

/** Detalle de un objeto: significado, de dónde vino y una reflexión privada opcional. */
@Composable
private fun IslandObjectDialog(placed: PlacedIslandObject, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val obj = IslandObjects.byId(placed.objectId) ?: return
    var text by remember(placed.objectId) { mutableStateOf(placed.reflection) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.testTag("island_object_dialog")) {
            Column(Modifier.padding(MareaSpace.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(islandObjectIcon(obj.id), contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(48.dp))
                }
                Text(obj.name, fontFamily = SnigletFontFamily, fontSize = 24.sp, color = MareaDeepBlue, modifier = Modifier.padding(top = MareaSpace.m))
                Text(obj.meaning, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTextDark)
                Text(
                    "Llegó a tu isla después de «${placed.sourceTitle}».",
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    color = MareaTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = MareaSpace.s)
                        .testTag("island_object_origin")
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("¿Qué significa para ti? (opcional)") },
                    supportingText = { Text("Privado: solo tú lo ves.") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MareaSpace.l)
                        .testTag("island_reflection_input")
                )
                Spacer(Modifier.height(MareaSpace.m))
                MareaPrimaryButton("Guardar", onClick = { onSave(text) }, tag = "island_reflection_save")
                MareaTextButton("Cerrar", onClick = onDismiss, tag = "island_object_close")
            }
        }
    }
}
