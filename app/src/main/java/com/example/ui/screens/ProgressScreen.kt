package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.ActivityItem
import com.example.model.MareaLogEntry
import com.example.model.ReflectionEntry
import com.example.model.WeeklyDayData
import com.example.ui.theme.*

enum class ProgressTabFilter {
    MOMENTOS,
    ACTIVIDADES,
    REFLEXIONES
}

@Composable
fun ProgressScreen(
    weeklyOverview: List<WeeklyDayData>,
    calmMomentsCount: Int,
    activitiesExploredCount: Int,
    logs: List<MareaLogEntry>,
    reflections: List<ReflectionEntry>,
    availableActivities: List<ActivityItem>,
    onOpenReflection: () -> Unit,
    onOpenActivity: (ActivityItem) -> Unit,
    weekHasEnoughData: Boolean = true,
    onDeleteReflection: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedDayName by remember { mutableStateOf<String?>(null) }
    var selectedTabFilter by remember { mutableStateOf(ProgressTabFilter.MOMENTOS) }
    var inspectedLogEntry by remember { mutableStateOf<MareaLogEntry?>(null) }

    // Registros reales del día seleccionado de ESTA semana (por fecha, no por nombre del día)
    val selectedDayData = weeklyOverview.firstOrNull { it.dayName.equals(selectedDayName, ignoreCase = true) }
    val selectedDayLogs = selectedDayData?.entries ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Title and Subtitle matching mockup with Sniglet font
        Text(
            text = "Mi progreso",
            fontFamily = SnigletFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            color = MareaTextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Conoce tus mareas, a tu ritmo",
            fontFamily = SnigletFontFamily,
            fontSize = 16.sp,
            color = MareaTextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 14.dp)
        )

        // Card 1: Interactive Bar Chart matching design
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFC4E4ED)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weekly_chart_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Chart Bars Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyOverview.forEach { dayData ->
                        val isSelected = selectedDayName.equals(dayData.dayName, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    selectedDayName = if (isSelected) null else dayData.dayName
                                }
                        ) {
                            val hasEntries = dayData.entries.isNotEmpty()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .fillMaxHeight(if (hasEntries) dayData.heightFraction else 0.05f)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        dayData.mareaType?.primaryColor ?: Color(0xFF75BBD0).copy(alpha = 0.45f)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MareaDeepBlue, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        else Modifier
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Day Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyOverview.forEach { dayData ->
                        val isSelected = selectedDayName.equals(dayData.dayName, ignoreCase = true)
                        Text(
                            text = dayData.dayName,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = if (isSelected) MareaDeepBlue else MareaTextDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val weekCount = weeklyOverview.sumOf { it.entries.size }
                Text(
                    text = if (weekHasEnoughData) {
                        "$weekCount registros esta semana (lunes a domingo). Toca una barra para ver el día."
                    } else {
                        "Hay pocos datos esta semana ($weekCount de 3 registros). El resumen se completa a medida que registres tus mareas; los días vacíos no cuentan como ninguna marea."
                    },
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag(if (weekHasEnoughData) "week_count_text" else "week_low_data_text")
                )

                // Interactive Day Details when a bar is selected
                if (selectedDayData != null) {
                    val hasEntries = selectedDayLogs.isNotEmpty()
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (hasEntries) (selectedDayData.mareaType?.primaryColor ?: Color.Transparent) else Color(0xFF75BBD0).copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (hasEntries) "🌊" else "🌱", fontSize = 14.sp)
                                    }
                                    Text(
                                        text = if (hasEntries) "${selectedDayData.dayName}: Marea ${selectedDayData.mareaType?.title ?: ""}" else "${selectedDayData.dayName}: Sin registros aún",
                                        fontFamily = SnigletFontFamily,
                                        fontSize = 15.sp,
                                        color = MareaTextDark
                                    )
                                }

                                IconButton(
                                    onClick = { selectedDayName = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MareaTextMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (hasEntries) (selectedDayData.mareaType?.description ?: "") else "Aún no registras emociones este día. ¡Toca la pestaña de Mareas para registrar cómo te sientes hoy!",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = MareaTextMuted
                            )

                            // If there are real logged entries for this day
                            if (selectedDayLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                selectedDayLogs.forEach { log ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = log.type.backgroundColor.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { inspectedLogEntry = log }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Marea ${log.type.title}" + if (log.situation.isNotBlank()) " · ${log.situation}" else "",
                                                fontFamily = QuicksandFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MareaTextDark
                                            )
                                            if (log.note.isNotBlank()) {
                                                Text(
                                                    text = "\"${log.note}\"",
                                                    fontFamily = NunitoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = MareaTextDark,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = listOf(log.timeOfDay, log.location).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Sin contexto (opcional)" },
                                                fontFamily = QuicksandFontFamily,
                                                fontSize = 10.sp,
                                                color = MareaTextMuted,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 2: "Así fue tu semana" with Muni - Functional Exploration Section
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFC4E4ED)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("week_summary_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.muni_waving),
                            contentDescription = "Muni en resumen semanal",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(62.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Así fue tu semana",
                            fontFamily = SnigletFontFamily,
                            fontSize = 19.sp,
                            color = MareaTextDark
                        )

                        Text(
                            text = "Cada emoción cuenta. Explora tus registros, momentos de calma y actividades exploradas.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MareaTextDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Tabs: "Momentos registrados", "Actividades realizadas", "Mis reflexiones"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterTabButton(
                        title = "Momentos",
                        isSelected = selectedTabFilter == ProgressTabFilter.MOMENTOS,
                        onClick = { selectedTabFilter = ProgressTabFilter.MOMENTOS },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        title = "Actividades",
                        isSelected = selectedTabFilter == ProgressTabFilter.ACTIVIDADES,
                        onClick = { selectedTabFilter = ProgressTabFilter.ACTIVIDADES },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        title = "Reflexiones",
                        isSelected = selectedTabFilter == ProgressTabFilter.REFLEXIONES,
                        onClick = { selectedTabFilter = ProgressTabFilter.REFLEXIONES },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content based on selected tab filter
                when (selectedTabFilter) {
                    ProgressTabFilter.MOMENTOS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (logs.isEmpty()) {
                                Text(
                                    text = "Aún no has registrado mareas. ¡Visita la pestaña Mareas para empezar!",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 12.sp,
                                    color = MareaTextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            } else {
                                logs.take(6).forEach { log ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { inspectedLogEntry = log }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(log.type.primaryColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🌊", fontSize = 16.sp)
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Marea ${log.type.title}",
                                                        fontFamily = SnigletFontFamily,
                                                        fontSize = 14.sp,
                                                        color = MareaTextDark
                                                    )
                                                    Text(
                                                        text = log.dayOfWeek,
                                                        fontFamily = QuicksandFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = log.type.textColor
                                                    )
                                                }
                                                Text(
                                                    text = if (log.situation.isNotBlank()) "Influencia: ${log.situation}" else "Sin contexto (opcional)",
                                                    fontFamily = NunitoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = MareaTextMuted
                                                )
                                                if (log.note.isNotBlank()) {
                                                    Text(
                                                        text = "\"${log.note}\"",
                                                        fontFamily = NunitoFontFamily,
                                                        fontSize = 11.sp,
                                                        color = MareaTextDark,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ProgressTabFilter.ACTIVIDADES -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableActivities.forEach { act ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(text = act.rewardIcon, fontSize = 24.sp)

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = act.title,
                                                fontFamily = SnigletFontFamily,
                                                fontSize = 14.sp,
                                                color = MareaTextDark
                                            )
                                            Text(
                                                text = "${act.category.title} · ${act.durationMin}",
                                                fontFamily = NunitoFontFamily,
                                                fontSize = 11.sp,
                                                color = MareaTextMuted
                                            )
                                        }

                                        Button(
                                            onClick = { onOpenActivity(act) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Explorar", fontFamily = SnigletFontFamily, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ProgressTabFilter.REFLEXIONES -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (reflections.isEmpty()) {
                                Text(
                                    text = "Aún no tienes reflexiones guardadas. ¡Toca 'Escribe tu reflexión' para empezar!",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 12.sp,
                                    color = MareaTextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            } else {
                                reflections.forEach { ref ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = ref.prompt,
                                                    fontFamily = SnigletFontFamily,
                                                    fontSize = 13.sp,
                                                    color = MareaTextDark,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = ref.dateText,
                                                    fontFamily = QuicksandFontFamily,
                                                    fontSize = 10.sp,
                                                    color = MareaTextMuted
                                                )
                                                IconButton(
                                                    onClick = { onDeleteReflection(ref.id) },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .testTag("delete_reflection_${ref.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Borrar esta entrada",
                                                        tint = MareaTextMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "\"${ref.text}\"",
                                                fontFamily = NunitoFontFamily,
                                                fontSize = 12.sp,
                                                color = MareaTextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Row of 2 stats cards matching mockup (clean vector, NO white boxes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stat Card 1: "Momentos de calma"
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC4E4ED)),
                modifier = Modifier
                    .weight(1f)
                    .height(124.dp)
                    .testTag("calm_moments_stat_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_lotus),
                        contentDescription = "Flor de loto",
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$calmMomentsCount momentos",
                        fontFamily = SnigletFontFamily,
                        fontSize = 13.sp,
                        color = MareaDeepBlue
                    )

                    Text(
                        text = "Momentos de calma",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MareaTextDark,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Stat Card 2: "Actividades exploradas"
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC4E4ED)),
                modifier = Modifier
                    .weight(1f)
                    .height(124.dp)
                    .testTag("activities_stat_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Actividades exploradas",
                            tint = MareaDeepBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$activitiesExploredCount exploradas",
                        fontFamily = SnigletFontFamily,
                        fontSize = 13.sp,
                        color = MareaDeepBlue
                    )

                    Text(
                        text = "Actividades exploradas",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MareaTextDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 3: "Un momento para ti" with Reflection action
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFC4E4ED)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reflection_prompt_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.algae_starfish),
                        contentDescription = "Estrella de mar y algas",
                        modifier = Modifier.size(54.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Un momento para ti",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )

                        Text(
                            text = "¿Qué descubriste sobre tus emociones esta semana?",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MareaTextDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Escribe tu reflexión ➔" Button
                Button(
                    onClick = onOpenReflection,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF287A9E)),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(42.dp)
                        .testTag("open_reflection_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Escribe tu reflexión",
                            fontFamily = SnigletFontFamily,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "➔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Modal Dialog for Inspected Log Entry
    if (inspectedLogEntry != null) {
        val entry = inspectedLogEntry!!
        Dialog(onDismissRequest = { inspectedLogEntry = null }) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${entry.dayOfWeek} · Marea ${entry.type.title}",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                        IconButton(onClick = { inspectedLogEntry = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = entry.type.backgroundColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = entry.type.description,
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = MareaTextDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Qué influyó: ${entry.situation.ifBlank { "no lo indicaste" }}",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = "Momento y lugar: " + listOf(entry.timeOfDay, entry.location).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "no lo indicaste" },
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = MareaTextMuted
                        )
                        if (entry.note.isNotBlank()) {
                            Text(
                                text = "Notas personales: \"${entry.note}\"",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = MareaTextDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { inspectedLogEntry = null },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = entry.type.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", fontFamily = SnigletFontFamily, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MareaDeepBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = QuicksandFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else MareaTextDark
        )
    }
}
