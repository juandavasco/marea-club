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
    )
}

data class MareaLogEntry(
    val id: String,
    val type: MareaType,
    val dayOfWeek: String,
    val timestamp: Long,
    val situation: String,
    val timeOfDay: String = "Hoy",
    val location: String = "Cotidiano",
    val note: String = ""
)

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
    val stage: Int,
    val category: ActivityCategory,
    val isUnlocked: Boolean,
    val meaning: String
)

data class IslandState(
    val level: Int = 1,
    val levelName: String = "Orilla Inicial",
    val stage: Int = 1, // 1: Descubrimiento, 2: Crecimiento, 3: Desarrollo, 4: Consolidación
    val progressPercent: Int = 0,
    val skyMode: SkyMode = SkyMode.DIA,
    val unlockedElementIds: Set<String> = emptySet()
)

enum class SkyMode(val title: String, val description: String) {
    DIA("Día sereno", "Luz suave y agua cristalina"),
    ATARDECER("Atardecer dorado", "Tonos cálidos y brisa tibia"),
    NOCHE("Noche estrellada", "Cielo profundo y reflejos en el agua")
}

data class WeeklyDayData(
    val dayName: String,
    val shortName: String,
    val mareaType: MareaType,
    val heightFraction: Float
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
    val verificationSource: String = "Secretaría de Salud de Medellín / Fuente Oficial",
    val verificationDate: String = "Octubre 2026",
    val isFavorite: Boolean = false,
    val isLocalMedellin: Boolean = true
) {
    val phoneOrContact: String get() = phone
}

data class QuestionOption(
    val label: String,
    val score: Int // 0: baja/leve, 1: moderada, 2: notable
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

enum class EvaluationLevel(val title: String, val badgeColor: Long) {
    ESTABLE("Aguas Tranquilas y Estables", 0xFF0EA9CE),
    OBSERVAR("Marea en Movimiento (Observar)", 0xFF1D95D3),
    APOYO_RECOMENDADO("Marea Alta (Buscar Acompañamiento)", 0xFF0E4273)
}

data class EvaluationResult(
    val questionnaireId: String,
    val title: String,
    val score: Int,
    val level: EvaluationLevel,
    val description: String,
    val recommendedSteps: List<String>,
    val suggestedRouteId: String? = null
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

enum class UserProfileType {
    CHILD,
    PARENT
}

enum class AccompanimentStatus {
    PENDING,
    ACKNOWLEDGED
}

data class AccompanimentRequest(
    val id: String,
    val timestamp: Long,
    val note: String,
    val status: AccompanimentStatus = AccompanimentStatus.PENDING
)

data class FamilyLinkState(
    val isLinked: Boolean = false,
    val linkCode: String = "MAR-482",
    val childName: String = "Navegante",
    val parentName: String = "Acompañante Familiar",
    val linkedSinceTimestamp: Long = 0L,
    val shareTidesSummary: Boolean = true,
    val shareIslandProgress: Boolean = true,
    val shareActivitiesSummary: Boolean = true
)

data class ParentGuideArticle(
    val id: String,
    val title: String,
    val category: String,
    val readTime: String,
    val summary: String,
    val fullContent: String,
    val practicalTips: List<String>
)

