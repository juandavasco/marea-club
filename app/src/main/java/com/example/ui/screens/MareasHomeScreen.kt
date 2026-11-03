package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActivityItem
import com.example.model.IslandState
import com.example.model.MareaType
import com.example.model.WeeklyDayData
import com.example.ui.theme.*

@Composable
fun MareasHomeScreen(
    userName: String,
    activeMarea: MareaType?,
    islandState: IslandState,
    availableActivities: List<ActivityItem>,
    weeklyOverview: List<WeeklyDayData>,
    calmMomentsCount: Int,
    onNavigateToMareaSelection: () -> Unit,
    onNavigateToIsland: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onOpenActivity: (ActivityItem) -> Unit,
    onOpenSupportDirectory: () -> Unit,
    onOpenSelfEvaluation: () -> Unit,
    onOpenAnonymousConsultation: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val animatedBg by animateColorAsState(
        targetValue = activeMarea?.backgroundColor ?: MareaBackground,
        animationSpec = tween(700),
        label = "screen_bg_anim"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ENCABEZADO CON MUNI, SALUDO Y ACCESO A MENÚ Y RUTAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onOpenMenu() }
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.muni_isotipo),
                        contentDescription = "Muni Avatar",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column {
                    Text(
                        text = "¡Hola, $userName! 👋",
                        fontFamily = SnigletFontFamily,
                        fontSize = 20.sp,
                        color = MareaTextDark
                    )
                    Text(
                        text = if (activeMarea != null) "Marea actual: ${activeMarea.title}" else "Tu rincón de calma en el océano",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTealBar
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick support button
                IconButton(
                    onClick = onOpenSupportDirectory,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .testTag("home_support_quick_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Healing,
                        contentDescription = "Rutas de apoyo Medellín",
                        tint = MareaDeepBlue
                    )
                }

                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menú y opciones",
                        tint = MareaDeepBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HUB 1: ¿CÓMO VIENE MI MAREA?
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToMareaSelection() }
                .testTag("marea_hub_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                        Text("🌊", fontSize = 20.sp)
                        Text(
                            text = "¿Cómo viene mi marea?",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }

                    AssistChip(
                        onClick = onNavigateToMareaSelection,
                        label = {
                            Text(
                                text = "Registrar ➔",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MareaDeepBlue
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeMarea != null) {
                    // Current Tide Registered View
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(activeMarea.backgroundColor)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = activeMarea.drawableRes),
                                contentDescription = activeMarea.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Marea ${activeMarea.title}",
                                fontFamily = SnigletFontFamily,
                                fontSize = 17.sp,
                                color = MareaTextDark
                            )
                            Text(
                                text = activeMarea.subtitle,
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = MareaTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeMarea.description,
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = MareaTextDark,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToMareaSelection,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = activeMarea.primaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("explore_marea_btn")
                    ) {
                        Text("Actualizar o explorar mi marea ➔", fontFamily = SnigletFontFamily, fontSize = 13.sp, color = Color.White)
                    }
                } else {
                    // Invitation to discover current tide
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MareaBackground)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(MareaTurquoise.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_muni_feelings),
                                contentDescription = "Muni te acompaña",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¿Cómo te sientes hoy?",
                                fontFamily = SnigletFontFamily,
                                fontSize = 16.sp,
                                color = MareaTextDark
                            )
                            Text(
                                text = "Las emociones cambian igual que las mareas. Toca aquí para reconocer la tuya.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = MareaTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5 Tide Previews (Tranquila, Movida, Alta, Tormenta, No sé)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            MareaType.TRANQUILA,
                            MareaType.MOVIDA,
                            MareaType.ALTA,
                            MareaType.TORMENTA,
                            MareaType.NO_SE
                        ).forEach { tide ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onNavigateToMareaSelection() }
                                    .padding(horizontal = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.5.dp, MareaTurquoise, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = tide.drawableRes),
                                        contentDescription = tide.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                Text(
                                    text = tide.title,
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 9.sp,
                                    color = MareaTextDark,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HUB 2: A TU RITMO (ACTIVIDADES EMOCIONALES CURADAS)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ritmo_hub_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                        Text("🧘", fontSize = 20.sp)
                        Text(
                            text = "A tu ritmo",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }

                    TextButton(onClick = onNavigateToActivities) {
                        Text(
                            text = "Ver todas ➔",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MareaDeepBlue
                        )
                    }
                }

                Text(
                    text = "Herramientas interactivas para autorregularte y hacer crecer tu isla:",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Horizontal list of interactive activities
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableActivities.take(5)) { activity ->
                        ActivityQuickCard(
                            activity = activity,
                            onClick = { onOpenActivity(activity) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HUB 3: MI ISLA (SANCTUARY PREVIEW)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToIsland() }
                .testTag("island_hub_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                        Text("🏝️", fontSize = 20.sp)
                        Text(
                            text = "Mi isla",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }

                    AssistChip(
                        onClick = onNavigateToIsland,
                        label = {
                            Text(
                                text = "Nivel ${islandState.level}",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MareaDeepBlue
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MareaCardLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.island_illustration),
                            contentDescription = "Mi isla",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(72.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tu lugar seguro",
                            fontFamily = SnigletFontFamily,
                            fontSize = 15.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = "${islandState.unlockedElementIds.size} elementos descubiertos",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { islandState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MareaDeepBlue,
                            trackColor = MareaCardLight
                        )

                        Text(
                            text = "${islandState.progressPercent}% para el siguiente nivel",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MareaTealBar,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToIsland,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text("Visitar mi isla ➔", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = MareaDeepBlue)
                    }

                    Button(
                        onClick = onNavigateToActivities,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text("Ganar elementos 🌱", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HUB 4: MI RECORRIDO (SEGUIMIENTO SEMANAL Y CALMA)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToProgress() }
                .testTag("recorrido_hub_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                        Text("📊", fontSize = 20.sp)
                        Text(
                            text = "Mi recorrido",
                            fontFamily = SnigletFontFamily,
                            fontSize = 18.sp,
                            color = MareaTextDark
                        )
                    }

                    TextButton(onClick = onNavigateToProgress) {
                        Text(
                            text = "Ver progreso ➔",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MareaDeepBlue
                        )
                    }
                }

                // Mini bar preview of the week
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MareaCardLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyOverview.forEach { dayData ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight(dayData.heightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(dayData.mareaType.primaryColor)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayData.dayName.take(1),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MareaTextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MareaBackground),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🪷", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "$calmMomentsCount momentos",
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 12.sp,
                                    color = MareaTextDark
                                )
                                Text(
                                    text = "De calma interior",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 10.sp,
                                    color = MareaTextMuted
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MareaBackground),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📝", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Diario personal",
                                    fontFamily = SnigletFontFamily,
                                    fontSize = 12.sp,
                                    color = MareaTextDark
                                )
                                Text(
                                    text = "Tus descubrimientos",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 10.sp,
                                    color = MareaTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECCIÓN DE BIENESTAR Y APOYO (MEDELLÍN + AUTOEVALUACIÓN + CONSULTA ANÓNIMA)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Acompañamiento y Orientación",
                    fontFamily = SnigletFontFamily,
                    fontSize = 15.sp,
                    color = MareaTextDark
                )
                Text(
                    text = "Recursos gratuitos y confidenciales a tu alcance",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SupportQuickAction(
                        icon = Icons.Default.PhoneInTalk,
                        label = "Línea 106",
                        sub = "Medellín 24/7",
                        onClick = onOpenSupportDirectory,
                        modifier = Modifier.weight(1f)
                    )

                    SupportQuickAction(
                        icon = Icons.Default.AssignmentTurnedIn,
                        label = "Autoevaluar",
                        sub = "Guía no clínica",
                        onClick = onOpenSelfEvaluation,
                        modifier = Modifier.weight(1f)
                    )

                    SupportQuickAction(
                        icon = Icons.Default.Lock,
                        label = "Anónimo",
                        sub = "Consulta segura",
                        onClick = onOpenAnonymousConsultation,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActivityQuickCard(
    activity: ActivityItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MareaBackground),
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = activity.rewardIcon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.title,
                fontFamily = SnigletFontFamily,
                fontSize = 13.sp,
                color = MareaTextDark,
                maxLines = 1
            )
            Text(
                text = activity.durationMin,
                fontFamily = NunitoFontFamily,
                fontSize = 10.sp,
                color = MareaTextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Desbloquea: ${activity.rewardTitle}",
                fontFamily = QuicksandFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MareaDeepBlue,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SupportQuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MareaCardLight),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MareaDeepBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = SnigletFontFamily,
                fontSize = 12.sp,
                color = MareaTextDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = sub,
                fontFamily = NunitoFontFamily,
                fontSize = 9.sp,
                color = MareaTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
