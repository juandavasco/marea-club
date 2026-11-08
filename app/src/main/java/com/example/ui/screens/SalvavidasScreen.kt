package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.family.CompanionReason
import com.example.data.family.CompanionRequest
import com.example.data.family.RequestStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tiempo sin respuesta tras el cual se sugiere otra ayuda (decisión provisional del 7 nov 2026). */
const val NO_RESPONSE_MINUTES = 15

/** Un pedido se considera vigente durante 3 horas; después se puede pedir de nuevo. */
private const val OPEN_WINDOW_MS = 3 * 60 * 60_000L

/**
 * Mis salvavidas (Documento Técnico §11).
 * El niño elige a qué adulto avisar y un motivo general; ve el estado real del pedido.
 * No es una línea de emergencia: las líneas de ayuda siempre están a un toque.
 */
@Composable
fun SalvavidasScreen(
    /** Adultos vinculados (uid → nombre). Vacío si el niño no está vinculado. */
    linkedAdults: List<Pair<String, String>>,
    requests: List<CompanionRequest>,
    busy: Boolean,
    message: String?,
    onSend: (toUids: List<String>, reason: CompanionReason?) -> Unit,
    onSetStatus: (requestIds: List<String>, status: RequestStatus) -> Unit,
    /** Abre "Mi familia" para vincularse. */
    onOpenFamily: () -> Unit,
    onOpenAllLines: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    clock: () -> Long = { System.currentTimeMillis() }
) {
    val context = LocalContext.current
    fun dial(number: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }
    }
    val now = clock()
    // Pedidos abiertos del último envío (uno por adulto elegido).
    val active = requests.filter { it.status.isOpen && now - it.createdAtMillis < OPEN_WINDOW_MS }
    val names = linkedAdults.toMap()

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

        when {
            linkedAdults.isEmpty() -> NotLinkedCard(onOpenFamily)
            active.isNotEmpty() -> ActiveRequestCard(active, names, clock, busy, onSetStatus)
            else -> NewRequestCard(linkedAdults, busy, onSend)
        }
        if (message != null) {
            MareaNotice(text = message, tone = NoticeTone.SUPPORT, tag = "salvavidas_message", modifier = Modifier.padding(top = MareaSpace.m))
        }

        Spacer(modifier = Modifier.padding(top = MareaSpace.m))
        MareaNotice(
            text = "Mis salvavidas no es una línea de emergencia y no garantiza una respuesta inmediata. Tu adulto verá el pedido cuando abra Marea Club.",
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

@Composable
private fun CardTitle(text: String) {
    Text(text, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MareaTextDark)
}

@Composable
private fun CardBody(text: String, modifier: Modifier = Modifier) {
    Text(text, fontFamily = NunitoFontFamily, fontSize = 14.sp, lineHeight = 20.sp, color = MareaTextDark, modifier = modifier)
}

/** Sin vínculo no hay a quién avisar: no se muestra un botón que no haría nada. */
@Composable
private fun NotLinkedCard(onOpenFamily: () -> Unit) {
    MareaCard(modifier = Modifier.testTag("salvavidas_not_linked")) {
        CardTitle("Primero, vincula tu perfil")
        CardBody(
            "Para pedir compañía, un adulto de tu familia debe darte un código. Con él vinculas tu perfil desde «Mi familia». " +
                "Después podrás avisarle con un toque, sin tener que explicar qué pasó.",
            Modifier.padding(top = MareaSpace.xs, bottom = MareaSpace.m)
        )
        MareaPrimaryButton(text = "Vincular con mi familia", onClick = onOpenFamily, tag = "salvavidas_open_family")
    }
}

@Composable
private fun NewRequestCard(adults: List<Pair<String, String>>, busy: Boolean, onSend: (List<String>, CompanionReason?) -> Unit) {
    // Por defecto se avisa a todos los adultos vinculados; el niño puede quitar a alguno.
    var to by remember(adults) { mutableStateOf(adults.map { it.first }.toSet()) }
    var reason by remember { mutableStateOf<CompanionReason?>(null) }
    MareaCard(modifier = Modifier.testTag("salvavidas_new_request")) {
        CardTitle("Necesito acompañamiento")
        CardBody(
            "Tu adulto recibirá: «Necesito que estés conmigo. Cuando puedas, ven a acompañarme». No tienes que escribir qué pasó.",
            Modifier.padding(top = MareaSpace.xs)
        )
        if (adults.size > 1) {
            CardBody("¿A quién quieres avisar?", Modifier.padding(top = MareaSpace.m, bottom = MareaSpace.s))
            Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
                adults.forEachIndexed { i, (uid, name) ->
                    MareaOption(
                        text = name,
                        selected = uid in to,
                        onClick = { to = if (uid in to) to - uid else to + uid },
                        tag = "salvavidas_to_$i"
                    )
                }
            }
        }
        CardBody("¿Qué necesitas? (opcional)", Modifier.padding(top = MareaSpace.m, bottom = MareaSpace.s))
        Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
            CompanionReason.entries.forEach { r ->
                MareaOption(
                    text = r.label,
                    selected = reason == r,
                    onClick = { reason = if (reason == r) null else r },
                    tag = "salvavidas_reason_${r.wire}"
                )
            }
        }
        Spacer(Modifier.height(MareaSpace.l))
        val chosen = adults.filter { it.first in to }
        MareaPrimaryButton(
            text = when {
                busy -> "Enviando…"
                chosen.size == 1 -> "Avisar a ${chosen.single().second}"
                chosen.size > 1 -> "Avisar a ${chosen.joinToString(" y ") { it.second }}"
                else -> "Elige a quién avisar"
            },
            onClick = { if (to.isNotEmpty()) onSend(chosen.map { it.first }, reason) },
            enabled = !busy && to.isNotEmpty(),
            tag = "salvavidas_request_button"
        )
    }
}

@Composable
private fun ActiveRequestCard(
    active: List<CompanionRequest>,
    names: Map<String, String>,
    clock: () -> Long,
    busy: Boolean,
    onSetStatus: (List<String>, RequestStatus) -> Unit
) {
    val first = active.minByOrNull { it.createdAtMillis } ?: return
    val key = active.joinToString { it.id }
    val deadline = first.createdAtMillis + NO_RESPONSE_MINUTES * 60_000L
    var checkedAt by remember(key) { mutableLongStateOf(clock()) }
    // Volver a mirar una sola vez, cuando se cumplan los 15 minutos (sin ciclos).
    LaunchedEffect(key) {
        val wait = deadline - clock()
        if (wait > 0) {
            delay(wait)
            checkedAt = clock()
        }
    }
    val sentAt = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-CO")).format(Date(first.createdAtMillis))
    val anyAnswer = active.any { it.status != RequestStatus.ENVIADA }
    val noResponse = !anyAnswer && checkedAt >= deadline
    val allCannot = active.all { it.status == RequestStatus.NO_PUEDO }
    val ids = active.map { it.id }

    MareaCard(modifier = Modifier.testTag("salvavidas_active_request")) {
        CardTitle(
            when {
                active.any { it.status == RequestStatus.VOY } -> "Alguien va en camino"
                allCannot -> "Ahora no pueden ir"
                noResponse -> "Todavía no lo han visto"
                else -> "Pedido enviado a las $sentAt"
            }
        )
        first.reason?.let { CardBody("Lo que necesitas: ${it.label}", Modifier.padding(top = MareaSpace.xs)) }
        Column(Modifier.padding(vertical = MareaSpace.s)) {
            active.forEach { r ->
                val name = names[r.toUid] ?: "Tu adulto"
                CardBody(
                    "• $name: " + when (r.status) {
                        RequestStatus.ENVIADA -> "aún no lo ha visto"
                        RequestStatus.VISTA -> "lo vio"
                        RequestStatus.VOY -> "va en camino"
                        RequestStatus.NO_PUEDO -> "no puede ahora"
                        else -> ""
                    },
                    Modifier.testTag("salvavidas_status_${r.status.wire}")
                )
            }
        }
        if (noResponse || allCannot) {
            CardBody(
                if (noResponse) "Pasaron más de $NO_RESPONSE_MINUTES minutos. Si necesitas ayuda ya, busca a otro adulto cerca de ti o llama a una de las líneas de abajo."
                else "Busca a otro adulto de confianza cerca de ti o llama a una de las líneas de abajo.",
                Modifier
                    .padding(bottom = MareaSpace.s)
                    .testTag(if (noResponse) "salvavidas_status_sin_respuesta" else "salvavidas_all_cannot")
            )
        }
        if (active.none { it.status == RequestStatus.VOY }) {
            MareaSecondaryButton("Cancelar el pedido", onClick = { onSetStatus(ids, RequestStatus.CANCELADA) }, enabled = !busy, tag = "salvavidas_cancel")
        }
        MareaTextButton("Ya estoy acompañado", onClick = { onSetStatus(ids, RequestStatus.CERRADA) }, tag = "salvavidas_close")
    }
}
