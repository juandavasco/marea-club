package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SelfCheckResult
import com.example.model.QuestionnaireDefinition
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfEvaluationScreen(
    questionnaires: List<QuestionnaireDefinition>,
    onEvaluate: (String, Map<Int, Int>) -> SelfCheckResult,
    onBack: () -> Unit,
    onOpenSupportDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeQuestionnaire by remember { mutableStateOf<QuestionnaireDefinition?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val selectedOptions = remember { mutableStateMapOf<Int, Int>() }
    var currentResult by remember { mutableStateOf<SelfCheckResult?>(null) }

    fun resetState() {
        activeQuestionnaire = null
        currentQuestionIndex = 0
        selectedOptions.clear()
        currentResult = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (activeQuestionnaire == null) "Autoevaluación Emocional" else activeQuestionnaire!!.title,
                        fontFamily = SnigletFontFamily,
                        fontSize = 18.sp,
                        color = MareaTextDark
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeQuestionnaire != null) {
                                resetState()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MareaDeepBlue)
                    }
                },
                actions = {
                    if (activeQuestionnaire != null && currentResult == null) {
                        TextButton(onClick = { resetState() }) {
                            Text("Salir", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = MareaTealText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MareaBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Category selection
                activeQuestionnaire == null -> {
                    QuestionnaireCatalog(
                        questionnaires = questionnaires,
                        onSelect = { q ->
                            activeQuestionnaire = q
                            currentQuestionIndex = 0
                            selectedOptions.clear()
                            currentResult = null
                        }
                    )
                }

                // Result display
                currentResult != null -> {
                    SelfCheckResultView(
                        result = currentResult!!,
                        onRestart = { resetState() },
                        onOpenSupportDirectory = onOpenSupportDirectory
                    )
                }

                // Active question flow
                else -> {
                    QuestionFlowView(
                        questionnaire = activeQuestionnaire!!,
                        currentIndex = currentQuestionIndex,
                        selectedOptionIndex = selectedOptions[currentQuestionIndex],
                        onOptionSelected = { optIdx ->
                            selectedOptions[currentQuestionIndex] = optIdx
                        },
                        onNext = {
                            if (currentQuestionIndex < activeQuestionnaire!!.questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                // Calculate result
                                currentResult = onEvaluate(activeQuestionnaire!!.id, selectedOptions.toMap())
                            }
                        },
                        onPrev = {
                            if (currentQuestionIndex > 0) {
                                currentQuestionIndex--
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionnaireCatalog(
    questionnaires: List<QuestionnaireDefinition>,
    onSelect: (QuestionnaireDefinition) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Non-clinical disclaimer banner
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MareaCardLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🛡️", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Herramienta orientativa y de reflexión",
                            fontFamily = SnigletFontFamily,
                            fontSize = 14.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = "Estos cuestionarios te ayudan a explorar cómo te sientes. No son diagnósticos clínicos ni pruebas médicas. Su objetivo es acompañar tu autoconocimiento.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MareaTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Elige un área para explorar:",
                fontFamily = SnigletFontFamily,
                fontSize = 16.sp,
                color = MareaTextDark
            )
        }

        items(questionnaires, key = { it.id }) { q ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(q) }
                    .testTag("questionnaire_card_${q.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MareaBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = q.iconEmoji, fontSize = 24.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = q.title,
                            fontFamily = SnigletFontFamily,
                            fontSize = 15.sp,
                            color = MareaTextDark
                        )
                        Text(
                            text = q.subtitle,
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = MareaTealText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${q.questions.size} preguntas sencillas • ~2 min",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 11.sp,
                            color = MareaTextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Comenzar",
                        tint = MareaDeepBlue
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuestionFlowView(
    questionnaire: QuestionnaireDefinition,
    currentIndex: Int,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val currentQuestion = questionnaire.questions[currentIndex]
    val total = questionnaire.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MareaTealText,
            trackColor = MareaCardMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pregunta ${currentIndex + 1} de $total",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MareaTealText
            )
            Text(
                text = questionnaire.title,
                fontFamily = NunitoFontFamily,
                fontSize = 11.sp,
                color = MareaTextMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = currentQuestion.prompt,
                    fontFamily = SnigletFontFamily,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    color = MareaTextDark
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Options
                currentQuestion.options.forEachIndexed { idx, option ->
                    val isSelected = selectedOptionIndex == idx
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) MareaTealBar else MareaBackground)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MareaDeepBlue else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onOptionSelected(idx) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else MareaCardMedium),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(MareaDeepBlue)
                                    )
                                }
                            }

                            Text(
                                text = option.label,
                                fontFamily = NunitoFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MareaTextDark
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(20.dp))

        // Navigation actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = onPrev,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Anterior", fontFamily = SnigletFontFamily, color = MareaDeepBlue)
                }
            }

            Button(
                onClick = onNext,
                enabled = selectedOptionIndex != null,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
                modifier = Modifier
                    .weight(if (currentIndex > 0) 1.5f else 1f)
                    .height(48.dp)
                    .testTag("next_question_button")
            ) {
                Text(
                    text = if (currentIndex < total - 1) "Siguiente ➔" else "Ver Resultado ➔",
                    fontFamily = SnigletFontFamily,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Resultado de la autoevaluación NO clínica (decisión 4):
 * sin puntaje, sin nivel y sin etiquetas; solo orientación y rutas de apoyo siempre visibles.
 */
@Composable
private fun SelfCheckResultView(
    result: SelfCheckResult,
    onRestart: () -> Unit,
    onOpenSupportDirectory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
            .testTag("self_check_result"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🌊", fontSize = 44.sp)

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = result.title,
            fontFamily = SnigletFontFamily,
            fontSize = 20.sp,
            color = MareaTextDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Orientación preliminar · no es un diagnóstico",
            fontFamily = QuicksandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MareaTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = result.message,
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MareaTextDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Lo que podría ayudarte ahora:",
                    fontFamily = SnigletFontFamily,
                    fontSize = 14.sp,
                    color = MareaTealText
                )

                Spacer(modifier = Modifier.height(8.dp))

                result.suggestions.forEach { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("•", fontSize = 16.sp, color = MareaTealText, fontWeight = FontWeight.Bold)
                        Text(
                            text = step,
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MareaTextDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rutas de apoyo SIEMPRE visibles, sin importar las respuestas
        Button(
            onClick = onOpenSupportDirectory,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MareaDeepBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("result_to_directory_button")
        ) {
            Text(
                text = "Ver rutas de apoyo en Medellín ➔",
                fontFamily = SnigletFontFamily,
                fontSize = 13.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRestart,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text("Explorar otro cuestionario", fontFamily = SnigletFontFamily, color = MareaDeepBlue)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
