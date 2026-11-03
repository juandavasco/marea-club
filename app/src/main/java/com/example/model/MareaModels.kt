package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.*

enum class MareaType(
    val title: String,
    val drawableRes: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val subtitle: String,
    val description: String,
    val muniMessage: String,
    val defaultSituation: String
) {
    TRANQUILA(
        title = "Tranquila",
        drawableRes = R.drawable.img_tide_tranquila,
        primaryColor = TideTranquilaPrimary,
        secondaryColor = TideTranquilaSecondary,
        backgroundColor = TideTranquilaBg,
        subtitle = "Aguas calmas y bienestar",
        description = "Momentos de calma, seguridad y recarga interior.",
        muniMessage = "Qué lindo momento para disfrutar de tu calma y nutrir tu isla. Muni te acompaña en este espacio.",
        defaultSituation = "Tiempo para mí"
    ),
    MOVIDA(
        title = "Movida",
        drawableRes = R.drawable.img_tide_movida,
        primaryColor = TideMovidaPrimary,
        secondaryColor = TideMovidaSecondary,
        backgroundColor = TideMovidaBg,
        subtitle = "Inquietud o energía activa",
        description = "Mucha energía, pensamientos rápidos, nerviosismo o dudas.",
        muniMessage = "Tener días de corriente rápida es completamente natural. No tienes que forzar nada, solo observar las olas fluir.",
        defaultSituation = "Escuela y amigos"
    ),
    ALTA(
        title = "Alta",
        drawableRes = R.drawable.img_tide_alta,
        primaryColor = TideAltaPrimary,
        secondaryColor = TideAltaSecondary,
        backgroundColor = TideAltaBg,
        subtitle = "Emociones intensas",
        description = "Sensación de carga, tristeza, frustración o enojo fuerte.",
        muniMessage = "Cuando la marea sube, las cosas pueden sentirse abrumadoras. Recuerda que estás a salvo y que cada emoción merece espacio.",
        defaultSituation = "Exigencias o cambios"
    ),
    TORMENTA(
        title = "Tormenta",
        drawableRes = R.drawable.img_tide_tormenta,
        primaryColor = TideTormentaPrimary,
        secondaryColor = TideTormentaSecondary,
        backgroundColor = TideTormentaBg,
        subtitle = "Momento difícil o desbordado",
        description = "Oleaje pesado, dolor o nudo emocional que necesita cuidado especial.",
        muniMessage = "Ninguna tormenta dura para siempre. No estás fallando por sentirte así; toda marea baja cuando le damos cobijo y amor.",
        defaultSituation = "Momento difícil"
    ),
    NO_SE(
        title = "No sé qué siento",
        drawableRes = R.drawable.img_tide_nose,
        primaryColor = TideNoSePrimary,
        secondaryColor = TideNoSeSecondary,
        backgroundColor = TideNoSeBg,
        subtitle = "Pausa y exploración",
        description = "Confusión, desconexión temporal o simplemente no tener palabras.",
        muniMessage = "Está completamente bien no saber qué sientes. El mar a veces necesita reposar en la orilla antes de mostrar sus secretos.",
        defaultSituation = "Sin motivo claro"
    );

    /** Color para TEXTO con contraste ≥ 4,5:1 sobre blanco (los rellenos claros no lo cumplen). */
    val textColor: Color
        get() = when (this) {
            TRANQUILA -> MareaDeepBlue
            NO_SE -> Color(0xFF555555)
            else -> primaryColor
        }
}

data class MareaLogEntry(
    val id: String,
    val type: MareaType,
    val timestamp: Long,
    /** Contexto opcional: vacío si el usuario no eligió nada. */
    val situation: String = "",
    val timeOfDay: String = "",
    val location: String = "",
    val note: String = ""
) {
    /** Etiqueta de fecha legible, calculada desde la fecha real del registro. */
    val dayOfWeek: String get() = DateLabels.dayLabel(timestamp)
}

enum class ActivityCategory(val title: String, val description: String) {
    RESPIRACION("Respiración y regulación", "Pausas conscientes y ritmo marino"),
    ESCRITURA("Escritura y reflexión", "Diario personal y expresión de pensamientos"),
    AUTOCONOCIMIENTO("Autoconocimiento", "Reconoce lo que sientes y necesitas"),
    AUTOESTIMA("Autoestima y confianza", "Identifica tus fortalezas y cualidades"),
    ATENCION("Atención y concentración", "Juegos breves para calmar la mente"),
    CREATIVIDAD("Creatividad y expresión", "Dibuja y proyecta tus emociones en colores")
}

data class ActivityItem(
    val id: String,
    val title: String,
    val category: ActivityCategory,
    val durationMin: String,
    val description: String,
    val rewardTitle: String,
    val rewardIcon: String,
    val recommendedFor: List<MareaType>
)

data class IslandElement(
    val id: String,
    val name: String,
    val category: ActivityCategory,
    val meaning: String
)

data class IslandState(
    val level: Int = 1,
    val levelName: String = "Orilla Inicial",
    /** Igual al nivel; se conserva para las pantallas existentes. */
    val stage: Int = 1,
    /** Avance hacia el siguiente nivel (0–100), derivado de los elementos desbloqueados. */
    val progressPercent: Int = 0,
    val skyMode: SkyMode = SkyMode.DIA,
    val unlockedElementIds: Set<String> = emptySet(),
    /** Elementos que el usuario ya abrió; los demás desbloqueados se muestran como "Nuevo". */
    val seenElementIds: Set<String> = emptySet()
)

enum class SkyMode(val title: String, val description: String) {
    DIA("Día sereno", "Luz suave y agua cristalina"),
    ATARDECER("Atardecer dorado", "Tonos cálidos y brisa tibia"),
    NOCHE("Noche estrellada", "Cielo profundo y reflejos en el agua")
}

data class WeeklyDayData(
    val dayName: String,
    val shortName: String,
    /** Marea más frecuente del día (en empate, la más reciente). null = sin registros. */
    val mareaType: MareaType?,
    val heightFraction: Float,
    val entries: List<MareaLogEntry> = emptyList()
)

data class ReflectionEntry(
    val id: String,
    val dateText: String,
    val prompt: String,
    val text: String
)

enum class RouteCategory(val displayName: String) {
    TODOS("Todos"),
    TELEFONICA("Líneas 24/7"),
    ESCUCHADERO("Escuchaderos"),
    UNIVERSITARIO("Universitarios")
}

data class PsychologicalRoute(
    val id: String,
    val name: String,
    val category: RouteCategory,
    val phone: String,
    val whatsapp: String? = null,
    val address: String,
    val comuna: String,
    val schedule: String,
    val requiresAppointment: Boolean,
    val costDescription: String,
    val description: String,
    val mapQuery: String,
    /** Fuente pública consultada para verificar el dato. */
    val verificationSource: String,
    val verificationUrl: String,
    /** Fecha en que el equipo verificó el dato (AAAA-MM-DD). */
    val verificationDate: String,
    /** Solo las rutas verificadas se muestran en la app. */
    val verified: Boolean,
    val isFavorite: Boolean = false,
    val isLocalMedellin: Boolean = true
) {
    val phoneOrContact: String get() = phone
}

data class QuestionOption(
    val label: String,
    /** Peso interno para ordenar sugerencias (0, 1 o 2). Nunca se muestra al usuario. */
    val score: Int
)

data class QuestionItem(
    val id: String,
    val prompt: String,
    val options: List<QuestionOption>
)

data class QuestionnaireDefinition(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val subtitle: String,
    val introduction: String,
    val objective: String,
    val questions: List<QuestionItem>
)

/**
 * Resultado de la autoevaluación preliminar NO clínica.
 * No contiene puntaje ni nivel: solo orientación y rutas sugeridas.
 */
data class SelfCheckResult(
    val questionnaireId: String,
    val title: String,
    val focus: com.example.domain.SelfCheckGuidance.Focus,
    val message: String,
    val suggestions: List<String>,
    val suggestedRouteIds: List<String>
)

data class AnonymousConsultation(
    val id: String,
    val timestamp: Long,
    val message: String,
    val muniResponse: String,
    val recommendedRoute: String,
    val isEmergencyFlagged: Boolean = false
)

enum class BottomNavTab(val label: String) {
    HOME("Inicio"),
    MAREA("Marea"),
    ISLAND("Mi isla"),
    PROGRESS("Mi progreso"),
    BURBUJA("Actividades")
}

enum class ActivityFeedback(val label: String) {
    AYUDO("Me ayudó"),
    MAS_O_MENOS("Más o menos"),
    NO_AYUDO("No me ayudó")
}

/** Registro de una actividad terminada, con la respuesta opcional "¿te ayudó?". */
data class ActivityCompletion(
    val id: String,
    val sourceId: String,
    val timestamp: Long,
    val feedback: ActivityFeedback? = null
)

/** Lo que se muestra al terminar una actividad: qué acción desbloqueó qué elemento. */
data class RewardInfo(
    val completionId: String,
    val sourceTitle: String,
    val elementName: String,
    val icon: String,
    val newlyUnlocked: Boolean
)
