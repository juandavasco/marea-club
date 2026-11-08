package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Sistema visual de Marea Club (Prompt Maestro §3.4).
 *
 * Un solo lugar para espaciados, tarjetas, botones, encabezados, opciones seleccionables,
 * avisos y estados vacíos, para que todas las pantallas se sientan como un mismo producto.
 *
 * Reglas: una acción principal por pantalla; los botones primarios usan el azul oscuro de marca
 * (#0E4273, contraste 10:1 con blanco); el azul #1D95D3 y el turquesa quedan para acentos;
 * áreas táctiles de 48 dp o más.
 */
object MareaSpace {
    val xs: Dp = 4.dp
    val s: Dp = 8.dp
    val m: Dp = 12.dp
    val l: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    /** Margen lateral de todas las pantallas. */
    val screen: Dp = 20.dp
}

val MareaCardShape = RoundedCornerShape(20.dp)
val MareaButtonShape = RoundedCornerShape(16.dp)
private val CardBorder = Color(0xFFD6E5EB)

/** Encabezado de pantalla: título, explicación breve y regreso opcional. */
@Composable
fun MareaHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = MareaSpace.s, bottom = MareaSpace.l),
        verticalAlignment = Alignment.Top
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(end = MareaSpace.xs)
                    .size(48.dp)
                    .testTag("header_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MareaDeepBlue)
            }
        }
        Column(modifier = Modifier.weight(1f).padding(top = if (onBack != null) 6.dp else 0.dp)) {
            Text(
                text = title,
                fontFamily = SnigletFontFamily,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                color = MareaDeepBlue
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = NunitoFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(top = MareaSpace.xs)
                )
            }
        }
    }
}

/** Título de sección dentro de una pantalla. */
@Composable
fun MareaSectionTitle(text: String, modifier: Modifier = Modifier, supporting: String? = null) {
    Column(modifier = modifier.padding(top = MareaSpace.xl, bottom = MareaSpace.m)) {
        Text(
            text = text,
            fontFamily = QuicksandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MareaTextDark
        )
        if (supporting != null) {
            Text(
                text = supporting,
                fontFamily = NunitoFontFamily,
                fontSize = 13.sp,
                color = MareaTextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Tarjeta base: superficie blanca, borde suave, sin sombras. Solo agrupa contenido relacionado. */
@Composable
fun MareaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(MareaSpace.l),
    background: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .clip(MareaCardShape)
        .background(background)
        .border(1.dp, CardBorder, MareaCardShape)
    Column(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base).padding(contentPadding),
        content = content
    )
}

@Composable
fun MareaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MareaButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MareaDeepBlue,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFC9D6DF),
            disabledContentColor = Color(0xFF4A5A66)
        ),
        contentPadding = PaddingValues(horizontal = MareaSpace.xl, vertical = 14.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
    ) {
        Text(text, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun MareaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tag: String? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MareaButtonShape,
        border = BorderStroke(1.5.dp, if (enabled) MareaDeepBlue else Color(0xFFC9D6DF)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MareaDeepBlue),
        contentPadding = PaddingValues(horizontal = MareaSpace.xl, vertical = 12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
    ) {
        Text(text, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}

/** Botón de texto para acciones menores (omitir, ahora no). */
@Composable
fun MareaTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, tag: String? = null) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
    ) {
        Text(text, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTealText)
    }
}

/**
 * Opción seleccionable con estado visible (borde, fondo y ✓): no depende solo del color.
 */
@Composable
fun MareaOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    tag: String? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(if (selected) Color(0xFFE3F2F8) else Color.White)
            .border(if (selected) 2.dp else 1.dp, if (selected) MareaDeepBlue else CardBorder, shape)
            .semantics { this.selected = selected }
            .clickable(role = Role.Checkbox, onClick = onClick)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .padding(horizontal = MareaSpace.l, vertical = MareaSpace.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                fontFamily = QuicksandFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MareaTextDark
            )
            if (!supporting.isNullOrBlank()) {
                Text(supporting, fontFamily = NunitoFontFamily, fontSize = 13.sp, color = MareaTextMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Box(
            modifier = Modifier
                .padding(start = MareaSpace.s)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) MareaDeepBlue else Color.Transparent)
                .border(1.5.dp, if (selected) MareaDeepBlue else Color(0xFFB7C9D3), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

enum class NoticeTone { INFO, SUPPORT, CONFIRM }

/** Aviso breve dentro de una pantalla: información, apoyo o confirmación. */
@Composable
fun MareaNotice(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.INFO,
    title: String? = null,
    tag: String? = null
) {
    val (bg, border) = when (tone) {
        NoticeTone.INFO -> Color(0xFFEAF4F8) to Color(0xFFCFE3EC)
        NoticeTone.SUPPORT -> Color(0xFFFFF6E5) to Color(0xFFF0D9A8)
        NoticeTone.CONFIRM -> Color(0xFFE6F4EC) to Color(0xFFB9DCC7)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .padding(MareaSpace.l)
    ) {
        if (title != null) {
            Text(title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTextDark)
            Spacer(Modifier.height(MareaSpace.xs))
        }
        Text(text, fontFamily = NunitoFontFamily, fontSize = 14.sp, lineHeight = 20.sp, color = MareaTextDark)
    }
}

/** Estado vacío: qué pasa aquí y qué se puede hacer. */
@Composable
fun MareaEmptyState(title: String, message: String, modifier: Modifier = Modifier, emoji: String = "🌊") {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MareaSpace.xl, horizontal = MareaSpace.l),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 34.sp)
        Spacer(Modifier.height(MareaSpace.s))
        Text(title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark, textAlign = TextAlign.Center)
        Text(message, fontFamily = NunitoFontFamily, fontSize = 14.sp, color = MareaTextMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = MareaSpace.xs))
    }
}

/** Fila de lista para elementos navegables (herramientas, ajustes). */
@Composable
fun MareaListRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String? = null,
    trailing: String = "›"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .padding(horizontal = MareaSpace.l, vertical = MareaSpace.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF4F8)),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 20.sp) }
        Column(modifier = Modifier.weight(1f).padding(horizontal = MareaSpace.m)) {
            Text(title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTextDark)
            Text(subtitle, fontFamily = NunitoFontFamily, fontSize = 13.sp, color = MareaTextMuted)
        }
        Text(trailing, fontSize = 22.sp, color = MareaTextMuted)
    }
}

/** Selector de dos o tres vistas dentro de una misma pantalla. */
@Composable
fun MareaSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tags: List<String> = emptyList()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFDDEBF1))
            .padding(MareaSpace.xs)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .semantics { this.selected = selected }
                    .clickable(role = Role.Tab) { onSelect(i) }
                    .then(if (tags.size > i) Modifier.testTag(tags[i]) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (selected) MareaDeepBlue else MareaTextMuted
                )
            }
        }
    }
}
