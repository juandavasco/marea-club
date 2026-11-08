package com.example.ui.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.domain.AdultGuide
import com.example.domain.FamilyConnection
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
private fun ContentColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MareaBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MareaSpace.screen)
            .padding(bottom = MareaSpace.xl),
        content = content
    )
}

@Composable
private fun Label(text: String) {
    Text(text, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MareaTealText, modifier = Modifier.padding(top = MareaSpace.m))
}

@Composable
private fun Body(text: String, modifier: Modifier = Modifier, color: Color = MareaTextDark) {
    Text(text, fontFamily = NunitoFontFamily, fontSize = 15.sp, lineHeight = 21.sp, color = color, modifier = modifier)
}

@Composable
private fun Status(text: String) {
    Text(text, fontFamily = NunitoFontFamily, fontSize = 12.sp, color = MareaTextMuted, modifier = Modifier.padding(top = MareaSpace.s))
}

// =====================================================================================
// Bitácora del acompañamiento
// =====================================================================================

/** Pestaña Bitácora: los 4 recorridos con sus experiencias. Nada de lo que se elige se guarda. */
@Composable
fun BitacoraScreen(modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf<AdultGuide.Experience?>(null) }
    open?.let { exp ->
        ExperienceScreen(exp, onClose = { open = null }, modifier = modifier)
        return
    }
    ContentColumn(modifier) {
        MareaHeader(title = "Bitácora del acompañamiento", subtitle = AdultGuide.SUBTITLE)
        MareaNotice(
            text = "No es terapia ni una evaluación: no hay puntajes ni respuestas correctas. Lo que elija aquí no se guarda.",
            tag = "bitacora_notice"
        )
        AdultGuide.routes.forEach { route ->
            MareaSectionTitle("${route.number}. ${route.title}", supporting = route.description)
            Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
                AdultGuide.experiencesFor(route.id).forEach { exp ->
                    MareaCard(onClick = { open = exp }, modifier = Modifier.testTag(exp.id)) {
                        Text(exp.title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark)
                        Body(exp.objective, Modifier.padding(top = MareaSpace.xs), color = MareaTextMuted)
                        Text(exp.minutes, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MareaTealText, modifier = Modifier.padding(top = MareaSpace.s))
                    }
                }
            }
        }
        Status(AdultGuide.CONTENT_STATUS)
    }
}

/** Objetivo y situación → reflexiones (cada opción muestra un comentario) → aprendizaje y aplicación. */
@Composable
private fun ExperienceScreen(exp: AdultGuide.Experience, onClose: () -> Unit, modifier: Modifier = Modifier) {
    // 0 = situación; 1..n = reflexiones; n+1 = aprendizaje
    var step by remember(exp.id) { mutableIntStateOf(0) }
    val chosen = remember(exp.id) { mutableStateMapOf<String, Set<String>>() }
    val last = exp.reflections.size + 1

    ContentColumn(modifier) {
        MareaHeader(title = exp.title, subtitle = if (step in 1 until last) "Reflexión $step de ${exp.reflections.size}" else null, onBack = onClose)
        when (step) {
            0 -> {
                MareaCard(modifier = Modifier.testTag("experience_intro")) {
                    Label("Objetivo")
                    Body(exp.objective)
                    Label("Situación")
                    Body(exp.situation)
                }
            }
            last -> {
                MareaCard(modifier = Modifier.testTag("experience_learning")) {
                    Label("Lo que podemos aprender")
                    Body(exp.learning)
                    Label("Para el día a día")
                    Body(exp.dailyApplication)
                }
                Status(AdultGuide.CONTENT_STATUS)
            }
            else -> {
                val r = exp.reflections[step - 1]
                Body(r.prompt, Modifier.padding(bottom = MareaSpace.m))
                val selected = chosen[r.id].orEmpty()
                Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
                    r.options.forEach { o ->
                        val isSel = o.id in selected
                        MareaOption(
                            text = o.label,
                            supporting = if (isSel) o.note else null,
                            selected = isSel,
                            onClick = {
                                chosen[r.id] = when {
                                    isSel -> selected - o.id
                                    r.multiple -> selected + o.id
                                    else -> setOf(o.id)
                                }
                            },
                            tag = "reflection_${o.id}"
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(MareaSpace.l))
        if (step < last) {
            MareaPrimaryButton(if (step == 0) "Empezar" else "Continuar", onClick = { step++ }, tag = "experience_next")
        } else {
            MareaPrimaryButton("Volver a la bitácora", onClick = onClose, tag = "experience_done")
        }
        if (step > 0) MareaTextButton("Atrás", onClick = { step-- }, tag = "experience_back")
    }
}

// =====================================================================================
// Conexión familiar
// =====================================================================================

/**
 * Lista de guías de Conexión familiar. La usan el adulto (pestaña Conexión) y el niño
 * (Actividades › Con mi familia). Son para hacer en persona y no se registran.
 */
@Composable
fun ConnectionListScreen(forChild: Boolean, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    var open by remember { mutableStateOf<FamilyConnection.Guide?>(null) }
    open?.let { g ->
        ConnectionGuideScreen(g, forChild, onClose = { open = null }, modifier = modifier)
        return
    }
    ContentColumn(modifier) {
        MareaHeader(
            title = if (forChild) "Con mi familia" else "Conexión familiar",
            subtitle = if (forChild) "Ideas para hacer con un adulto, con los dos o con ninguno: tú decides."
            else "Actividades cortas para hacer juntos, en persona. Son voluntarias: el niño o niña decide si participa.",
            onBack = onBack
        )
        MareaNotice(text = "La app no guarda quién participó ni lo que se dijo.", tag = "connection_notice")
        Spacer(Modifier.height(MareaSpace.m))
        Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
            FamilyConnection.guides.forEach { g ->
                MareaCard(onClick = { open = g }, modifier = Modifier.testTag(g.id)) {
                    Text(g.title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MareaTextDark)
                    Body(g.objective, Modifier.padding(top = MareaSpace.xs), color = MareaTextMuted)
                    Text(g.minutes, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MareaTealText, modifier = Modifier.padding(top = MareaSpace.s))
                }
            }
        }
        Status(FamilyConnection.CONTENT_STATUS)
    }
}

@Composable
private fun ConnectionGuideScreen(g: FamilyConnection.Guide, forChild: Boolean, onClose: () -> Unit, modifier: Modifier = Modifier) {
    ContentColumn(modifier) {
        MareaHeader(title = g.title, subtitle = "${g.minutes} · en persona", onBack = onClose)
        MareaCard(modifier = Modifier.testTag("connection_guide")) {
            Label("Para qué sirve")
            Body(g.objective)
            Label("Cómo hacerlo")
            g.steps.forEachIndexed { i, s -> Body("${i + 1}. $s", Modifier.padding(top = MareaSpace.xs)) }
            Label(if (g.id == "con_me_gustaria") "Frases guía" else "Ideas")
            g.examples.forEach { Body("• $it", Modifier.padding(top = MareaSpace.xs)) }
        }
        Spacer(Modifier.height(MareaSpace.m))
        MareaNotice(text = g.careNote, tone = NoticeTone.SUPPORT, tag = "connection_care")
        if (forChild) {
            Body("Si no quieres hacerla ahora, está bien. Puedes proponerla otro día.", Modifier.padding(top = MareaSpace.m), color = MareaTextMuted)
        }
        Spacer(Modifier.height(MareaSpace.l))
        MareaPrimaryButton("Volver", onClick = onClose, tag = "connection_done")
    }
}
