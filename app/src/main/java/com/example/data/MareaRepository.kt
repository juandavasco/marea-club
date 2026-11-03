package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.IslandRules
import com.example.domain.SafetyRules
import com.example.domain.SelfCheckGuidance
import com.example.domain.WeeklySummary
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Almacenamiento local de Marea Club.
 *
 * - Todo queda en el teléfono: la app no usa internet ni copia de seguridad en la nube.
 * - Los datos emocionales (registros, reflexiones, diario, consultas, actividades) se guardan cifrados.
 * - Las reglas (isla, resumen semanal, orientación) viven en el paquete `domain` y tienen pruebas propias.
 */
class MareaRepository(
    context: Context,
    private val cipher: TextCipher = TinkTextCipher(context),
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _logs = MutableStateFlow<List<MareaLogEntry>>(emptyList())
    val logs: StateFlow<List<MareaLogEntry>> = _logs.asStateFlow()

    private val _activeMarea = MutableStateFlow<MareaType?>(null)
    val activeMarea: StateFlow<MareaType?> = _activeMarea.asStateFlow()

    private val _islandState = MutableStateFlow(IslandState())
    val islandState: StateFlow<IslandState> = _islandState.asStateFlow()

    private val _completions = MutableStateFlow<List<ActivityCompletion>>(emptyList())
    val completions: StateFlow<List<ActivityCompletion>> = _completions.asStateFlow()

    /** "Momentos de calma": actividades terminadas (cuenta real). */
    private val _calmMomentsCount = MutableStateFlow(0)
    val calmMomentsCount: StateFlow<Int> = _calmMomentsCount.asStateFlow()

    /** "Actividades exploradas": actividades distintas que se han terminado al menos una vez. */
    private val _activitiesExploredCount = MutableStateFlow(0)
    val activitiesExploredCount: StateFlow<Int> = _activitiesExploredCount.asStateFlow()

    private val _reflections = MutableStateFlow<List<ReflectionEntry>>(emptyList())
    val reflections: StateFlow<List<ReflectionEntry>> = _reflections.asStateFlow()

    private val _weeklyOverview = MutableStateFlow<List<WeeklyDayData>>(emptyList())
    val weeklyOverview: StateFlow<List<WeeklyDayData>> = _weeklyOverview.asStateFlow()

    private val _weekHasEnoughData = MutableStateFlow(false)
    val weekHasEnoughData: StateFlow<Boolean> = _weekHasEnoughData.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _consultations = MutableStateFlow<List<AnonymousConsultation>>(emptyList())
    val consultations: StateFlow<List<AnonymousConsultation>> = _consultations.asStateFlow()

    private val _favoriteRouteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteRouteIds: StateFlow<Set<String>> = _favoriteRouteIds.asStateFlow()

    private val _userName = MutableStateFlow(DEFAULT_NAME)
    val userName: StateFlow<String> = _userName.asStateFlow()

    val availableActivities: List<ActivityItem> = MareaContent.activities
    val allIslandElements: List<IslandElement> = MareaContent.islandElements
    val questionnaires: List<QuestionnaireDefinition> = MareaContent.questionnaires

    /** Solo rutas verificadas, con su marca de favorita. */
    val psychologicalRoutesMedellin: List<PsychologicalRoute>
        get() {
            val favs = _favoriteRouteIds.value
            return MareaContent.visibleRoutes.map { it.copy(isFavorite = favs.contains(it.id)) }
        }

    init {
        removeLegacyPlaintextData()
        loadData()
    }

    // ---------------------------------------------------------------------------------
    // Carga y guardado
    // ---------------------------------------------------------------------------------

    /** La versión de AI Studio guardaba datos emocionales sin cifrar. Se eliminan. */
    private fun removeLegacyPlaintextData() {
        val legacy = listOf(
            "saved_logs", "saved_reflections", "saved_consultations", "saved_accompaniment_requests",
            "active_marea", "calm_count", "activities_count", "island_level", "island_stage",
            "island_level_name", "island_progress", "clean_fresh_start_v5", "current_profile_type",
            "family_is_linked", "family_link_code", "family_child_name", "family_parent_name",
            "family_linked_time", "family_share_tides", "family_share_island", "family_share_activities"
        )
        if (legacy.any { prefs.contains(it) }) {
            prefs.edit().apply { legacy.forEach { remove(it) } }.apply()
        }
    }

    private fun loadData() {
        _isOnboarded.value = prefs.getBoolean(KEY_ONBOARDED, false)
        _userName.value = prefs.getString(KEY_USER_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
        _favoriteRouteIds.value = readSet(KEY_FAVORITES)

        _logs.value = readEncryptedArray(KEY_LOGS) { o ->
            MareaLogEntry(
                id = o.getString("id"),
                type = MareaType.valueOf(o.getString("type")),
                timestamp = o.getLong("timestamp"),
                situation = o.optString("situation", ""),
                timeOfDay = o.optString("timeOfDay", ""),
                location = o.optString("location", ""),
                note = o.optString("note", "")
            )
        }
        _reflections.value = readEncryptedArray(KEY_REFLECTIONS) { o ->
            ReflectionEntry(
                id = o.getString("id"),
                dateText = o.getString("dateText"),
                prompt = o.getString("prompt"),
                text = o.getString("text")
            )
        }
        _consultations.value = readEncryptedArray(KEY_CONSULTATIONS) { o ->
            AnonymousConsultation(
                id = o.getString("id"),
                timestamp = o.getLong("timestamp"),
                message = o.getString("message"),
                muniResponse = o.getString("muniResponse"),
                recommendedRoute = o.getString("recommendedRoute"),
                isEmergencyFlagged = o.optBoolean("isEmergencyFlagged", false)
            )
        }
        _completions.value = readEncryptedArray(KEY_COMPLETIONS) { o ->
            ActivityCompletion(
                id = o.getString("id"),
                sourceId = o.getString("sourceId"),
                timestamp = o.getLong("timestamp"),
                feedback = o.optString("feedback", "").takeIf { it.isNotBlank() }
                    ?.let { runCatching { ActivityFeedback.valueOf(it) }.getOrNull() }
            )
        }
        _activeMarea.value = readEncrypted(KEY_ACTIVE_MAREA)?.let { runCatching { MareaType.valueOf(it) }.getOrNull() }

        val sky = prefs.getString(KEY_SKY, SkyMode.DIA.name) ?: SkyMode.DIA.name
        updateIsland(
            unlocked = readSet(KEY_UNLOCKED),
            seen = readSet(KEY_SEEN),
            skyMode = runCatching { SkyMode.valueOf(sky) }.getOrDefault(SkyMode.DIA)
        )
        updateCounters()
        updateWeeklyData()
    }

    private fun readSet(key: String): Set<String> =
        (prefs.getString(key, "") ?: "").split(",").filter { it.isNotBlank() }.toSet()

    private fun writeSet(key: String, value: Set<String>) {
        prefs.edit().putString(key, value.joinToString(",")).apply()
    }

    private fun readEncrypted(key: String): String? {
        val stored = prefs.getString(key, null) ?: return null
        return runCatching { cipher.decrypt(stored) }.getOrNull()
    }

    private fun writeEncrypted(key: String, plain: String) {
        prefs.edit().putString(key, cipher.encrypt(plain)).apply()
    }

    private fun <T> readEncryptedArray(key: String, parse: (JSONObject) -> T): List<T> {
        val json = readEncrypted(key) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i -> runCatching { parse(arr.getJSONObject(i)) }.getOrNull() }
        }.getOrDefault(emptyList())
    }

    private fun <T> writeEncryptedArray(key: String, items: List<T>, toJson: (T) -> JSONObject) {
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        writeEncrypted(key, arr.toString())
    }

    private fun saveLogs() = writeEncryptedArray(KEY_LOGS, _logs.value.take(MAX_LOGS)) { log ->
        JSONObject()
            .put("id", log.id)
            .put("type", log.type.name)
            .put("timestamp", log.timestamp)
            .put("situation", log.situation)
            .put("timeOfDay", log.timeOfDay)
            .put("location", log.location)
            .put("note", log.note)
    }

    private fun saveReflections() = writeEncryptedArray(KEY_REFLECTIONS, _reflections.value) { r ->
        JSONObject().put("id", r.id).put("dateText", r.dateText).put("prompt", r.prompt).put("text", r.text)
    }

    private fun saveConsultations() = writeEncryptedArray(KEY_CONSULTATIONS, _consultations.value.take(30)) { c ->
        JSONObject()
            .put("id", c.id)
            .put("timestamp", c.timestamp)
            .put("message", c.message)
            .put("muniResponse", c.muniResponse)
            .put("recommendedRoute", c.recommendedRoute)
            .put("isEmergencyFlagged", c.isEmergencyFlagged)
    }

    private fun saveCompletions() = writeEncryptedArray(KEY_COMPLETIONS, _completions.value.take(MAX_COMPLETIONS)) { c ->
        JSONObject()
            .put("id", c.id)
            .put("sourceId", c.sourceId)
            .put("timestamp", c.timestamp)
            .put("feedback", c.feedback?.name ?: "")
    }

    private fun newId(): String = "${clock()}-${(1000..9999).random()}"

    // ---------------------------------------------------------------------------------
    // Bienvenida y reinicio
    // ---------------------------------------------------------------------------------

    fun completeOnboarding() {
        _isOnboarded.value = true
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    /**
     * Borra todos los datos y deja la app como recién instalada (Documento Técnico §18).
     * La interfaz pide confirmación explícita antes de llamar a esta función.
     */
    fun resetAllData() {
        prefs.edit().clear().apply()
        loadData()
    }

    // ---------------------------------------------------------------------------------
    // Mareas
    // ---------------------------------------------------------------------------------

    /** Guarda un registro. El contexto es opcional: lo que el usuario no eligió queda vacío. */
    fun recordMarea(type: MareaType, situation: String, timeOfDay: String, location: String, note: String): MareaLogEntry {
        val entry = MareaLogEntry(
            id = newId(),
            type = type,
            timestamp = clock(),
            situation = situation.trim(),
            timeOfDay = timeOfDay.trim(),
            location = location.trim(),
            note = note.trim()
        )
        _logs.value = listOf(entry) + _logs.value
        _activeMarea.value = type
        writeEncrypted(KEY_ACTIVE_MAREA, type.name)
        saveLogs()
        updateWeeklyData()
        return entry
    }

    fun deleteLog(id: String) {
        _logs.value = _logs.value.filterNot { it.id == id }
        val latest = _logs.value.maxByOrNull { it.timestamp }
        _activeMarea.value = latest?.type
        if (latest == null) prefs.edit().remove(KEY_ACTIVE_MAREA).apply() else writeEncrypted(KEY_ACTIVE_MAREA, latest.type.name)
        saveLogs()
        updateWeeklyData()
    }

    private fun updateWeeklyData() {
        val week = WeeklySummary.build(_logs.value, clock()) { it.timestamp }
        _weeklyOverview.value = week.days.map { day ->
            val dominant = WeeklySummary.dominant(day.entries, { it.type }, { it.timestamp })
            WeeklyDayData(
                dayName = day.name,
                shortName = day.name.take(3),
                mareaType = dominant,
                heightFraction = heightFor(dominant),
                entries = day.entries
            )
        }
        _weekHasEnoughData.value = week.hasEnoughData
    }

    private fun heightFor(type: MareaType?): Float = when (type) {
        null -> 0f
        MareaType.TRANQUILA -> 0.45f
        MareaType.NO_SE -> 0.55f
        MareaType.MOVIDA -> 0.7f
        MareaType.ALTA -> 0.85f
        MareaType.TORMENTA -> 1f
    }

    // ---------------------------------------------------------------------------------
    // Actividades e isla
    // ---------------------------------------------------------------------------------

    /**
     * Registra una actividad terminada y aplica la regla de la isla.
     * Devuelve qué acción desbloqueó qué elemento, para explicarlo al usuario.
     */
    fun completeActivity(sourceId: String): RewardInfo? {
        val completion = ActivityCompletion(id = newId(), sourceId = sourceId, timestamp = clock())
        _completions.value = listOf(completion) + _completions.value
        saveCompletions()
        updateCounters()

        val outcome = IslandRules.apply(_islandState.value.unlockedElementIds, sourceId)
        if (outcome.newlyUnlocked) {
            writeSet(KEY_UNLOCKED, outcome.unlockedAfter)
            updateIsland(unlocked = outcome.unlockedAfter)
        }
        val unlock = outcome.unlock ?: return null
        return RewardInfo(
            completionId = completion.id,
            sourceTitle = unlock.sourceTitle,
            elementName = unlock.elementName,
            icon = unlock.icon,
            newlyUnlocked = outcome.newlyUnlocked
        )
    }

    /** Respuesta opcional "¿te ayudó?" al cerrar una actividad (Documento Técnico §4). */
    fun recordActivityFeedback(completionId: String, feedback: ActivityFeedback?) {
        _completions.value = _completions.value.map { if (it.id == completionId) it.copy(feedback = feedback) else it }
        saveCompletions()
    }

    fun markElementSeen(elementId: String) {
        val seen = _islandState.value.seenElementIds + elementId
        writeSet(KEY_SEEN, seen)
        updateIsland(seen = seen)
    }

    fun setIslandSkyMode(skyMode: SkyMode) {
        prefs.edit().putString(KEY_SKY, skyMode.name).apply()
        updateIsland(skyMode = skyMode)
    }

    private fun updateIsland(
        unlocked: Set<String> = _islandState.value.unlockedElementIds,
        seen: Set<String> = _islandState.value.seenElementIds,
        skyMode: SkyMode = _islandState.value.skyMode
    ) {
        val progress = IslandRules.progress(unlocked)
        _islandState.value = IslandState(
            level = progress.level,
            levelName = progress.levelName,
            stage = progress.level,
            progressPercent = progress.percentToNext,
            skyMode = skyMode,
            unlockedElementIds = unlocked,
            seenElementIds = seen
        )
    }

    private fun updateCounters() {
        _calmMomentsCount.value = _completions.value.size
        _activitiesExploredCount.value = _completions.value.map { it.sourceId }.distinct().size
    }

    // ---------------------------------------------------------------------------------
    // Reflexiones y diario
    // ---------------------------------------------------------------------------------

    /** Reflexión semanal ("Un momento para ti"): desbloquea el Muelle del Horizonte. */
    fun addReflection(text: String): RewardInfo? {
        if (text.isBlank()) return null
        addEntry(WEEKLY_PROMPT, text)
        return completeActivity(SOURCE_WEEKLY_REFLECTION)
    }

    /** Entrada del Diario del Mar. Se guarda cifrada y se puede borrar. */
    fun addDiaryEntry(prompt: String, text: String) {
        if (text.isBlank()) return
        addEntry("Diario del Mar · $prompt", text)
    }

    private fun addEntry(prompt: String, text: String) {
        val now = clock()
        val entry = ReflectionEntry(id = newId(), dateText = DateLabels.dayLabel(now), prompt = prompt, text = text.trim())
        _reflections.value = listOf(entry) + _reflections.value
        saveReflections()
    }

    fun deleteReflection(id: String) {
        _reflections.value = _reflections.value.filterNot { it.id == id }
        saveReflections()
    }

    // ---------------------------------------------------------------------------------
    // Rutas, autoevaluación y consulta anónima
    // ---------------------------------------------------------------------------------

    fun toggleFavoriteRoute(routeId: String) {
        val current = _favoriteRouteIds.value
        val updated = if (routeId in current) current - routeId else current + routeId
        _favoriteRouteIds.value = updated
        writeSet(KEY_FAVORITES, updated)
    }

    fun setUserName(name: String) {
        val clean = name.trim().ifBlank { DEFAULT_NAME }
        _userName.value = clean
        prefs.edit().putString(KEY_USER_NAME, clean).apply()
    }

    /**
     * Autoevaluación preliminar NO clínica (decisión 4): sin puntaje ni nivel.
     * Las respuestas solo ordenan las sugerencias; las rutas de apoyo siempre se incluyen.
     */
    fun evaluateQuestionnaire(questionnaireId: String, selectedOptions: Map<Int, Int>): SelfCheckResult {
        val definition = questionnaires.find { it.id == questionnaireId } ?: questionnaires.first()
        val weights = selectedOptions.mapNotNull { (qIdx, optIdx) ->
            definition.questions.getOrNull(qIdx)?.options?.getOrNull(optIdx)?.score
        }
        val focus = SelfCheckGuidance.focus(weights)
        val alwaysRoutes = listOf("route_linea_amiga", "route_linea_106")
        return when (focus) {
            SelfCheckGuidance.Focus.BUSCAR_APOYO -> SelfCheckResult(
                questionnaireId = definition.id,
                title = definition.title,
                focus = focus,
                message = "Gracias por responder con sinceridad. Parece que estás cargando bastante en ${definition.title.lowercase()}. No tienes que hacerlo en soledad: hablar pronto con alguien puede ayudarte.",
                suggestions = listOf(
                    "Cuéntale a un adulto de confianza cómo te has sentido (familia, docente u orientador/a).",
                    "Llama gratis a la Línea 106 o a la Línea Amiga (604 444 44 48), a cualquier hora.",
                    "Si estás en peligro ahora mismo, llama al 123.",
                    "Mientras tanto, la Respiración del Océano puede ayudarte a bajar el ritmo."
                ),
                suggestedRouteIds = alwaysRoutes + "route_linea_123"
            )
            SelfCheckGuidance.Focus.CONVERSAR -> SelfCheckResult(
                questionnaireId = definition.id,
                title = definition.title,
                focus = focus,
                message = "Gracias por responder. Hay cosas en ${definition.title.lowercase()} que vale la pena mirar con calma y, si quieres, conversar con alguien.",
                suggestions = listOf(
                    "Conversa con un amigo/a o un adulto de confianza sobre cómo te has sentido.",
                    "Escribe en el Diario del Mar para ordenar lo que piensas.",
                    "Si el oleaje aumenta, la Línea 106 y los Escuchaderos están disponibles."
                ),
                suggestedRouteIds = alwaysRoutes
            )
            SelfCheckGuidance.Focus.CUIDADO_COTIDIANO -> SelfCheckResult(
                questionnaireId = definition.id,
                title = definition.title,
                focus = focus,
                message = "Gracias por responder. Sigue cuidando los momentos que te hacen bien en ${definition.title.lowercase()}.",
                suggestions = listOf(
                    "Elige una actividad de autocuidado cuando la necesites.",
                    "Escribe tu reflexión semanal para notar qué te ayuda.",
                    "Si algo cambia, las rutas de apoyo siguen aquí."
                ),
                suggestedRouteIds = alwaysRoutes
            )
        }
    }

    /**
     * Consulta anónima: es una DEMOSTRACIÓN (decisión 5). Nadie lee estos mensajes;
     * se guardan cifrados solo en el teléfono y Muni responde con un texto fijo que
     * siempre incluye las líneas de apoyo.
     */
    fun submitAnonymousConsultation(message: String): AnonymousConsultation {
        val isRisk = SafetyRules.mentionsRisk(message)
        val response = if (isRisk) {
            "Gracias por contarlo. Lo que describes es importante y mereces ayuda ahora: llama gratis a la Línea 106 o a la Línea Amiga (604 444 44 48), a cualquier hora, o busca a un adulto de confianza. Si estás en peligro en este momento, llama al 123. Recuerda: este espacio es una demostración y ninguna persona lee estos mensajes."
        } else {
            "Gracias por expresar lo que sientes. Poner en palabras tus emociones ayuda a ordenarlas. Recuerda que este espacio es una demostración: ninguna persona lee estos mensajes. Si quieres hablar con alguien, la Línea 106 y la Línea Amiga (604 444 44 48) atienden gratis las 24 horas."
        }
        val item = AnonymousConsultation(
            id = newId(),
            timestamp = clock(),
            message = message,
            muniResponse = response,
            recommendedRoute = "Línea 106 · Línea Amiga (604) 444 44 48 · Emergencias 123",
            isEmergencyFlagged = isRisk
        )
        _consultations.value = listOf(item) + _consultations.value
        saveConsultations()
        return item
    }

    companion object {
        const val PREFS_NAME = "marea_club_prefs"
        const val DEFAULT_NAME = "Navegante"
        const val SOURCE_WEEKLY_REFLECTION = "reflexion_semanal"
        const val WEEKLY_PROMPT = "¿Qué descubriste sobre tus emociones esta semana?"
        private const val MAX_LOGS = 200
        private const val MAX_COMPLETIONS = 300

        private const val KEY_ONBOARDED = "is_onboarded"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_FAVORITES = "favorite_routes"
        private const val KEY_UNLOCKED = "unlocked_elements"
        private const val KEY_SEEN = "seen_elements"
        private const val KEY_SKY = "island_sky_mode"
        private const val KEY_LOGS = "enc_logs"
        private const val KEY_REFLECTIONS = "enc_reflections"
        private const val KEY_CONSULTATIONS = "enc_consultations"
        private const val KEY_COMPLETIONS = "enc_completions"
        private const val KEY_ACTIVE_MAREA = "enc_active_marea"
    }
}
