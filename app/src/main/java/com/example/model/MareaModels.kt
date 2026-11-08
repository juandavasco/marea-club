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
        description = "Me siento en calma o con poco movimiento.",
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
        description = "Siento movimiento o varias cosas pasando.",
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
        description = "La emoción se siente intensa u ocupa mucho espacio.",
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
        description = "Siento mucha intensidad o me cuesta encontrar calma.",
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
        description = "No encuentro palabras o no estoy seguro.",
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
    val note: String = "",
    /** Solo para NO_SE: "NO_SE" (no sé qué siento) o "SIN_PALABRA" (no encuentro una emoción que me describa). */
    val uncertainty: String = ""
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
    /** Ícono decorativo de la herramienta. */
    val icon: String,
    val recommendedFor: List<MareaType>
)

/** Objeto que el niño eligió para su isla. La reflexión es privada y opcional. */
data class PlacedIslandObject(
    val objectId: String,
    val sourceActivityId: String,
    val sourceTitle: String,
    val timestamp: Long,
    val reflection: String = ""
)

data class IslandState(
    val placed: List<PlacedIslandObject> = emptyList(),
    val skyMode: SkyMode = SkyMode.DIA
) {
    val placedIds: Set<String> get() = placed.map { it.objectId }.toSet()
}

/** Elección de objeto que se ofrece al terminar una actividad significativa. */
data class IslandOffer(
    val activityId: String,
    val activityTitle: String,
    val objectIds: List<String>
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

/** Navegación infantil del Prompt Maestro §5: Inicio, Mareas, Actividades y Mi isla. */
enum class BottomNavTab(val label: String) {
    HOME("Inicio"),
    MAREA("Mareas"),
    ACTIVIDADES("Actividades"),
    ISLAND("Mi isla")
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

/** Cierre de una herramienta de "Para calmarme": solo la pregunta opcional "¿te ayudó?". */
data class ClosingInfo(
    val completionId: String,
    val title: String
)
