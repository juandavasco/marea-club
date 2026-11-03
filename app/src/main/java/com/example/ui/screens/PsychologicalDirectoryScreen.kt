package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PsychologicalRoute
import com.example.model.RouteCategory
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsychologicalDirectoryScreen(
    routes: List<PsychologicalRoute>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit,
    onOpenSelfEvaluation: () -> Unit,
    onOpenAnonymousConsultation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RouteCategory.TODOS) }
    var selectedComuna by remember { mutableStateOf("Todas") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // List of distinct comunas
    val comunas = remember(routes) {
        listOf("Todas") + routes.map { it.comuna }.distinct().sorted()
    }

    // Filtered list
    val filteredRoutes = remember(routes, favoriteIds, searchQuery, selectedCategory, selectedComuna, showOnlyFavorites) {
        routes.filter { route ->
            val matchesCategory = selectedCategory == RouteCategory.TODOS || route.category == selectedCategory
            val matchesComuna = selectedComuna == "Todas" || route.comuna == selectedComuna
            val matchesFav = !showOnlyFavorites || favoriteIds.contains(route.id)
            val matchesQuery = searchQuery.isBlank() ||
                    route.name.contains(searchQuery, ignoreCase = true) ||
                    route.description.contains(searchQuery, ignoreCase = true) ||
                    route.address.contains(searchQuery, ignoreCase = true) ||
                    route.comuna.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesComuna && matchesFav && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Atención Psicológica Medellín",
                        fontFamily = SnigletFontFamily,
                        fontSize = 20.sp,
                        color = MareaTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MareaDeepBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        modifier = Modifier.testTag("filter_favs_button")
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favoritos",
                            tint = if (showOnlyFavorites) Color(0xFFE89806) else MareaDeepBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = MareaBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header info & emergency reminder
            item {
                Spacer(modifier = Modifier.height(4.dp))
                LineaAmigaHeroCard(context = context)
            }

            // Quick secondary tools (Self-evaluation & anonymous consultation)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenSelfEvaluation,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MareaTealBar)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("dir_to_self_eval")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📋", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Autoevaluación", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = MareaDeepBlue)
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenAnonymousConsultation,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MareaTealBar)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("dir_to_anonymous")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💬", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consulta Anónima", fontFamily = SnigletFontFamily, fontSize = 12.sp, color = MareaDeepBlue)
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Buscar por estación, barrio o universidad...",
                            fontFamily = NunitoFontFamily,
                            fontSize = 13.sp,
                            color = MareaTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MareaTealBar)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar", tint = MareaTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MareaTealBar,
                        unfocusedBorderColor = MareaCardMedium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("directory_search_input")
                )
            }

            // Category filter chips
            item {
                Column {
                    Text(
                        text = "Tipo de atención:",
                        fontFamily = SnigletFontFamily,
                        fontSize = 13.sp,
                        color = MareaTextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(RouteCategory.values()) { cat ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat.displayName,
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MareaTealBar,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = MareaTextDark
                                )
                            )
                        }
                    }
                }
            }

            // Comuna selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Puntos verificados (${filteredRoutes.size})",
                        fontFamily = SnigletFontFamily,
                        fontSize = 15.sp,
                        color = MareaTextDark
                    )

                    if (showOnlyFavorites) {
                        Text(
                            text = "⭐ Mostrando favoritos",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFC07700)
                        )
                    }
                }
            }

            // Routes list
            if (filteredRoutes.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 34.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron rutas con los filtros seleccionados",
                                fontFamily = SnigletFontFamily,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                color = MareaTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Intenta buscar con otras palabras o reiniciar los filtros.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = MareaTextMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredRoutes, key = { it.id }) { route ->
                    PsychologicalRouteCard(
                        route = route,
                        isFavorite = favoriteIds.contains(route.id),
                        onToggleFavorite = { onToggleFavorite(route.id) },
                        onCall = { dialNumber(context, route.phone) },
                        onWhatsApp = {
                            if (route.whatsapp != null) {
                                openWhatsApp(context, route.whatsapp)
                            }
                        },
                        onOpenMap = { openMapLocation(context, route.mapQuery, route.name) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LineaAmigaHeroCard(context: Context) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MareaTealBar.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💙", fontSize = 22.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Línea Amiga de Medellín",
                        fontFamily = SnigletFontFamily,
                        fontSize = 17.sp,
                        color = MareaTextDark
                    )
                    Text(
                        text = "Atención psicológica telefónica gratuita 24/7",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = MareaTealBar
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Servicio de escucha, orientación y acompañamiento inicial de la Secretaría de Salud de Medellín. Atiende a niños, jóvenes y familias sin costo.",
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MareaTextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { dialNumber(context, "6044444448") },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Llamar (444 44 48)", fontFamily = SnigletFontFamily, fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = { openWhatsApp(context, "+573016044444") },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Text("WhatsApp Salud", fontFamily = SnigletFontFamily, fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PsychologicalRouteCard(
    route: PsychologicalRoute,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onOpenMap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Category tag & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = route.name,
                            fontFamily = SnigletFontFamily,
                            fontSize = 16.sp,
                            color = MareaTextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = route.comuna,
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MareaTealBar
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFE89806) else MareaTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = route.description,
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MareaTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badges: Cost & Appointment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(route.costDescription, fontSize = 11.sp, fontFamily = QuicksandFontFamily) },
                    leadingIcon = { Text("🏷️", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (route.requiresAppointment) "Requiere cita previa" else "Sin cita previa",
                            fontSize = 11.sp,
                            fontFamily = QuicksandFontFamily
                        )
                    },
                    leadingIcon = { Text(if (route.requiresAppointment) "📅" else "🚪", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address & Hours
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(15.dp))
                Text(
                    text = route.address,
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = MareaDeepBlue, modifier = Modifier.size(15.dp))
                Text(
                    text = "Horario: ${route.schedule}",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    color = MareaTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MareaTealBar),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (route.phone == "106" || route.phone == "123") "Llamar ${route.phone}" else "Llamar",
                        fontFamily = SnigletFontFamily,
                        fontSize = 11.sp
                    )
                }

                if (route.whatsapp != null) {
                    Button(
                        onClick = onWhatsApp,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text("WhatsApp", fontFamily = SnigletFontFamily, fontSize = 11.sp, color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onOpenMap,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp), tint = MareaDeepBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ubicación", fontFamily = SnigletFontFamily, fontSize = 11.sp, color = MareaDeepBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verification footer
            Text(
                text = "✓ Verificado: ${route.verificationDate} • Fuente: ${route.verificationSource}",
                fontFamily = NunitoFontFamily,
                fontSize = 10.sp,
                color = MareaTextMuted.copy(alpha = 0.8f)
            )
        }
    }
}

private fun dialNumber(context: Context, rawNumber: String) {
    try {
        val cleanNumber = rawNumber.replace(" ", "").replace("(", "").replace(")", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir el marcador telefónico: $rawNumber", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context, rawPhone: String) {
    try {
        val clean = rawPhone.replace("+", "").replace(" ", "")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean?text=Hola,%20deseo%20orientacion%20psicologica"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp para el contacto", Toast.LENGTH_SHORT).show()
    }
}

private fun openMapLocation(context: Context, query: String, placeName: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode("$placeName, Medellin, Colombia")}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to web search maps
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("$placeName Medellin")}")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
    }
}
