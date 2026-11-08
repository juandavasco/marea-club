package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Mis salvavidas (Prompt Maestro §10, Documento Técnico §11).
 *
 * ESTADO: la solicitud a un adulto vinculado necesita el backend de la entrega 3 y 4.
 * Mientras no exista un vínculo real, el botón se muestra deshabilitado y lo explica:
 * no se simula un envío. Las líneas de ayuda funcionan desde ya como alternativa segura.
 */
@Composable
fun SalvavidasScreen(
    /** null mientras no exista un adulto vinculado de verdad (no se simula el envío). */
    onRequestCompanion: (() -> Unit)?,
    /** Por qué no se puede enviar todavía (se muestra si [onRequestCompanion] es null). */
    unavailableText: String,
    /** Abre "Mi familia" para vincularse; null si ya está vinculado. */
    onOpenFamily: (() -> Unit)?,
    onOpenAllLines: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasLinkedAdult = onRequestCompanion != null
    fun dial(number: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MareaSpace.screen)
            .padding(bottom = MareaSpace.xl)
    ) {
        MareaHeader(
            title = "Mis salvavidas",
            subtitle = "Pide compañía a un adulto de confianza sin tener que explicar todo lo que pasa.",
            onBack = onBack
        )

        MareaCard {
            Text("Necesito acompañamiento", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MareaTextDark)
            Text(
                "Tu adulto recibirá: «Necesito que estés conmigo. Cuando puedas, ven a acompañarme». No tienes que escribir qué pasó.",
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MareaTextMuted,
                modifier = Modifier.padding(top = MareaSpace.xs, bottom = MareaSpace.l)
            )
            MareaPrimaryButton(
                text = "Necesito acompañamiento",
                onClick = { onRequestCompanion?.invoke() },
                enabled = hasLinkedAdult,
                tag = "salvavidas_request_button"
            )
            if (!hasLinkedAdult) {
                Text(
                    unavailableText,
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MareaTextDark,
                    modifier = Modifier
                        .padding(top = MareaSpace.s)
                        .testTag("salvavidas_not_linked")
                )
                if (onOpenFamily != null) {
                    MareaSecondaryButton(
                        text = "Vincular con mi familia",
                        onClick = onOpenFamily,
                        tag = "salvavidas_open_family",
                        modifier = Modifier.padding(top = MareaSpace.s)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = MareaSpace.m))
        MareaNotice(
            text = "Mis salvavidas no es una línea de emergencia y no garantiza una respuesta inmediata.",
            tone = NoticeTone.SUPPORT
        )

        MareaSectionTitle("Si es urgente o nadie responde", supporting = "Líneas gratuitas de Medellín, verificadas por el equipo.")
        Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
            MareaListRow(icon = "📞", title = "Línea 106", subtitle = "Escucha y orientación · 24 horas · gratis", onClick = { dial("106") }, tag = "salvavidas_call_106", trailing = "Llamar")
            MareaListRow(icon = "💬", title = "Línea Amiga Saludable", subtitle = "(604) 444 44 48 · 24 horas · gratis", onClick = { dial("6044444448") }, tag = "salvavidas_call_amiga", trailing = "Llamar")
            MareaListRow(icon = "🚨", title = "Línea 123", subtitle = "Si tú u otra persona está en peligro ahora", onClick = { dial("123") }, tag = "salvavidas_call_123", trailing = "Llamar")
        }
        Spacer(modifier = Modifier.padding(top = MareaSpace.s))
        MareaSecondaryButton(text = "Ver todas las líneas y escuchaderos", onClick = onOpenAllLines, tag = "salvavidas_all_lines")
    }
}
