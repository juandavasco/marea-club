package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.GuidedActivities
import com.example.domain.GuidedActivities.Answers
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/**
 * Motor de las 6 actividades significativas.
 * Introducción → pasos → conclusión. Se puede salir en cualquier momento sin perder nada
 * ni guardar nada; las respuestas solo se guardan si el niño marca "Guardar en mi diario".
 */
@Composable
fun GuidedActivityScreen(
    activity: GuidedActivities.Activity,
    onClose: () -> Unit,
    onFinish: (diaryText: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // -1 = introducción; steps.size = conclusión
    var index by remember(activity.id) { mutableIntStateOf(-1) }
    var answers by remember(activity.id) { mutableStateOf(Answers()) }
    val scroll = rememberScrollState()
    LaunchedEffect(index) { scroll.scrollTo(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MareaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Barra superior: salir siempre visible
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MareaSpace.s, vertical = MareaSpace.xs)
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("guided_close")) {
                Icon(Icons.Default.Close, contentDescription = "Pausar y salir", tint = MareaDeepBlue)
            }
            Text(
                activity.title,
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MareaTextDark,
                modifier = Modifier.weight(1f)
            )
            if (index in activity.steps.indices) {
                Text(
                    "${index + 1} de ${activity.steps.size}",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = MareaTextMuted,
                    modifier = Modifier.padding(end = MareaSpace.m)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = MareaSpace.screen)
                .padding(bottom = MareaSpace.xl)
        ) {
            when {
                index < 0 -> Intro(activity, onStart = { index = 0 }, onLater = onClose)
                index >= activity.steps.size -> Conclusion(activity, answers, onFinish)
                else -> StepView(
                    activity = activity,
                    step = activity.steps[index],
                    answers = answers,
                    onAnswers = { answers = it },
                    onNext = { index++ },
                    onBack = { index-- }
                )
            }
        }
    }
}

@Composable
private fun Intro(activity: GuidedActivities.Activity, onStart: () -> Unit, onLater: () -> Unit) {
    MareaHeader(title = activity.title, subtitle = activity.intro)
    MareaCard {
        Label("Para qué sirve")
        Body(activity.objective)
        Spacer(Modifier.height(MareaSpace.m))
        Label("Qué vas a practicar")
        Body(activity.skill)
        Spacer(Modifier.height(MareaSpace.m))
        Label("Tiempo aproximado")
        Body("${activity.minutes}. Puedes pausar o salir cuando quieras.")
    }
    Spacer(Modifier.height(MareaSpace.m))
    MareaNotice(
        text = "Tus respuestas son privadas. No se guardan, a menos que al final elijas guardarlas en tu diario.",
        tone = NoticeTone.INFO,
        tag = "guided_privacy_notice"
    )
    Text(
        GuidedActivities.CONTENT_STATUS,
        fontFamily = NunitoFontFamily,
        fontSize = 12.sp,
        color = MareaTextMuted,
        modifier = Modifier.padding(top = MareaSpace.s)
    )
    Spacer(Modifier.height(MareaSpace.xl))
    MareaPrimaryButton("Empezar", onClick = onStart, tag = "guided_start")
    MareaTextButton("Ahora no", onClick = onLater, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun StepView(
    activity: GuidedActivities.Activity,
    step: GuidedActivities.Step,
    answers: Answers,
    onAnswers: (Answers) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Text(
        step.prompt,
        fontFamily = SnigletFontFamily,
        fontSize = 22.sp,
        lineHeight = 27.sp,
        color = MareaDeepBlue,
        modifier = Modifier.padding(top = MareaSpace.m)
    )
    if (step.help.isNotBlank()) {
        Text(step.help, fontFamily = NunitoFontFamily, fontSize = 14.sp, color = MareaTextMuted, modifier = Modifier.padding(top = MareaSpace.xs))
    }
    Spacer(Modifier.height(MareaSpace.l))

    val canContinue = when (step) {
        is GuidedActivities.Choice -> step.optional || answers.all(step.id).isNotEmpty()
        is GuidedActivities.TwoTides -> step.optional || answers.all(step.id).size == 2
        else -> true
    }
    var pauseDone by remember(step.id) { mutableStateOf(step !is GuidedActivities.Pause) }

    when (step) {
        is GuidedActivities.Choice -> ChoiceStep(step, answers, onAnswers)
        is GuidedActivities.TwoTides -> TwoTidesStep(step, answers, onAnswers)
        is GuidedActivities.Pause -> PauseStep(step) { pauseDone = true }
        is GuidedActivities.Consequences -> ConsequencesStep(step, activity, answers)
        is GuidedActivities.Phrase -> PhraseStep(step, answers, onAnswers)
        is GuidedActivities.Summary -> SummaryStep(step, answers)
    }

    Spacer(Modifier.height(MareaSpace.xl))
    MareaPrimaryButton(
        text = if (step is GuidedActivities.Pause && !pauseDone) "Respira con la ola…" else "Continuar",
        onClick = onNext,
        enabled = canContinue && pauseDone,
        tag = "guided_next"
    )
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        MareaTextButton("Atrás", onClick = onBack, tag = "guided_back")
        if (step.optional && step !is GuidedActivities.Summary && step !is GuidedActivities.Consequences) {
            MareaTextButton("Omitir", onClick = onNext, tag = "guided_skip")
        }
    }
}

@Composable
private fun ChoiceStep(step: GuidedActivities.Choice, answers: Answers, onAnswers: (Answers) -> Unit) {
    val selected = answers.all(step.id)
    Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
        step.optionsFor(answers).forEach { option ->
            val isSel = option.id in selected
            MareaOption(
                text = option.label,
                supporting = if (step.id == "escena") option.detail else null,
                selected = isSel,
                onClick = {
                    val next = when {
                        isSel -> selected - option.id
                        step.maxSelected == 1 -> listOf(option.id)
                        selected.size < step.maxSelected -> selected + option.id
                        else -> selected
                    }
                    onAnswers(answers.copy(choices = answers.choices + (step.id to next)))
                },
                tag = "option_${option.id}"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TwoTidesStep(step: GuidedActivities.TwoTides, answers: Answers, onAnswers: (Answers) -> Unit) {
    val selected = answers.all(step.id)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(MareaSpace.s),
        verticalArrangement = Arrangement.spacedBy(MareaSpace.s)
    ) {
        step.options.forEach { option ->
            val isSel = option.id in selected
            val shape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(if (isSel) MareaDeepBlue else Color.White)
                    .border(1.5.dp, if (isSel) MareaDeepBlue else Color(0xFFB7C9D3), shape)
                    .semantics { this.selected = isSel }
                    .clickable(role = Role.Checkbox) {
                        val next = when {
                            isSel -> selected - option.id
                            selected.size < 2 -> selected + option.id
                            else -> listOf(selected.last(), option.id)
                        }
                        onAnswers(answers.copy(choices = answers.choices + (step.id to next)))
                    }
                    .testTag("emotion_${option.id}")
                    .padding(horizontal = MareaSpace.l, vertical = MareaSpace.m),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option.label,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSel) Color.White else MareaTextDark
                )
            }
        }
    }
    val labels = selected.mapNotNull { id -> step.options.firstOrNull { it.id == id }?.label }
    if (labels.size == 2) {
        Spacer(Modifier.height(MareaSpace.l))
        TwoWaves(labels[0], labels[1])
    }
}

/** Dos olas que se cruzan: las dos emociones existen a la vez. */
@Composable
private fun TwoWaves(first: String, second: String) {
    val transition = rememberInfiniteTransition(label = "two_waves")
    val phase by transition.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "phase")
    MareaCard(modifier = Modifier.testTag("two_waves_card")) {
        Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            fun wave(color: Color, offset: Float, amp: Float) {
                val path = Path()
                val steps = 60
                for (i in 0..steps) {
                    val x = size.width * i / steps
                    val y = size.height / 2 + amp * sin((i.toFloat() / steps) * 4 * PI.toFloat() + phase + offset)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color, style = Stroke(width = 6.dp.toPx()))
            }
            wave(TideMovidaPrimary, 0f, size.height * 0.3f)
            wave(TideTranquilaPrimary, PI.toFloat() / 2, size.height * 0.22f)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(first, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TideMovidaPrimary)
            Text("y también", fontFamily = NunitoFontFamily, fontSize = 14.sp, color = MareaTextMuted)
            Text(second, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaDeepBlue)
        }
    }
}

@Composable
private fun PauseStep(step: GuidedActivities.Pause, onDone: () -> Unit) {
    var remaining by remember(step.id) { mutableIntStateOf(step.seconds) }
    LaunchedEffect(step.id) {
        while (remaining > 0) {
            delay(1000)
            remaining--
        }
        onDone()
    }
    val transition = rememberInfiniteTransition(label = "pause")
    val scale by transition.animateFloat(0.8f, 1.15f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "breath")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().testTag("pause_step")) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(TideTranquilaSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(if (remaining > 0) "$remaining" else "✓", fontFamily = SnigletFontFamily, fontSize = 36.sp, color = MareaDeepBlue)
        }
        Text(
            "Inhala cuando el círculo crece, exhala cuando se encoge.",
            fontFamily = NunitoFontFamily,
            fontSize = 14.sp,
            color = MareaTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MareaSpace.l)
        )
    }
}

@Composable
private fun ConsequencesStep(step: GuidedActivities.Consequences, activity: GuidedActivities.Activity, answers: Answers) {
    val activitySource = activity.stepById(step.sourceStepId) as? GuidedActivities.Choice
    val options = activitySource?.optionsFor(answers).orEmpty()
    var shown by remember(step.id) { mutableStateOf(answers.first(step.sourceStepId)) }
    Column(verticalArrangement = Arrangement.spacedBy(MareaSpace.s)) {
        options.forEach { option ->
            val isShown = option.id == shown
            MareaOption(
                text = option.label,
                supporting = if (isShown) option.detail else null,
                selected = isShown,
                onClick = { shown = option.id },
                tag = "consequence_${option.id}"
            )
        }
    }
    Spacer(Modifier.height(MareaSpace.m))
    MareaNotice("No hay una respuesta correcta para todos. Lo que te sirve puede cambiar según la persona y el momento.")
}

@Composable
private fun PhraseStep(step: GuidedActivities.Phrase, answers: Answers, onAnswers: (Answers) -> Unit) {
    val text = answers.texts[step.id] ?: step.build(answers)
    OutlinedTextField(
        value = text,
        onValueChange = { onAnswers(answers.copy(texts = answers.texts + (step.id to it))) },
        textStyle = LocalTextStyle.current.copy(fontFamily = NunitoFontFamily, fontSize = 17.sp, color = MareaTextDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .testTag("phrase_input")
    )
}

@Composable
private fun SummaryStep(step: GuidedActivities.Summary, answers: Answers) {
    MareaCard(modifier = Modifier.testTag("summary_card")) {
        step.build(answers).forEachIndexed { i, (label, value) ->
            if (i > 0) HorizontalDivider(color = Color(0xFFE3EDF1), modifier = Modifier.padding(vertical = MareaSpace.s))
            Label(label)
            Body(value)
        }
    }
}

@Composable
private fun Conclusion(activity: GuidedActivities.Activity, answers: Answers, onFinish: (String?) -> Unit) {
    var save by remember { mutableStateOf(false) }
    MareaHeader(title = "Lo que exploraste", subtitle = activity.conclusion)
    MareaCard {
        Label("Para tu día a día")
        Body(activity.dailyTip)
    }
    Spacer(Modifier.height(MareaSpace.l))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(value = save, onValueChange = { save = it }, role = Role.Checkbox)
            .testTag("guided_save_diary")
    ) {
        Checkbox(checked = save, onCheckedChange = null)
        Column(modifier = Modifier.padding(start = MareaSpace.s)) {
            Text("Guardar en mi diario", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MareaTextDark)
            Text("Solo tú lo ves, en este teléfono.", fontFamily = NunitoFontFamily, fontSize = 13.sp, color = MareaTextMuted)
        }
    }
    Spacer(Modifier.height(MareaSpace.l))
    MareaPrimaryButton(
        text = "Terminar",
        onClick = { onFinish(if (save) GuidedActivities.diaryText(activity, answers) else null) },
        tag = "guided_finish"
    )
}

@Composable
private fun Label(text: String) {
    Text(text, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MareaTealText)
}

@Composable
private fun Body(text: String) {
    Text(text, fontFamily = NunitoFontFamily, fontSize = 15.sp, lineHeight = 21.sp, color = MareaTextDark, modifier = Modifier.padding(top = 2.dp))
}
