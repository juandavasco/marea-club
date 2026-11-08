package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.GuidedActivities
import com.example.domain.IslandObjects
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
 * - Todo queda en el teléfono, sin copia de seguridad en la nube. Internet solo se usa para el vínculo
 *   familiar y Mis salvavidas (ver data/family), nunca para estos datos.
 * - Los datos emocionales (registros, reflexiones, diario, objetos de la isla, actividades) se guardan cifrados.
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

    private val _reflections = MutableStateFlow<List<ReflectionEntry>>(emptyList())
    val reflections: StateFlow<List<ReflectionEntry>> = _reflections.asStateFlow()

    private val _weeklyOverview = MutableStateFlow<List<WeeklyDayData>>(emptyList())
    val weeklyOverview: StateFlow<List<WeeklyDayData>> = _weeklyOverview.asStateFlow()

    private val _weekHasEnoughData = MutableStateFlow(false)
    val weekHasEnoughData: StateFlow<Boolean> = _weekHasEnoughData.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _favoriteRouteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteRouteIds: StateFlow<Set<String>> = _favoriteRouteIds.asStateFlow()

    private val _userName = MutableStateFlow(DEFAULT_NAME)
    val userName: StateFlow<String> = _userName.asStateFlow()

    /** Herramientas de "Para calmarme". */
    val calmTools: List<ActivityItem> = MareaContent.calmTools

    /** Las 6 actividades significativas del Documento Técnico §6. */
    val guidedActivities: List<GuidedActivities.Activity> = GuidedActivities.all

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
            "family_linked_time", "family_share_tides", "family_share_island", "family_share_activities",
            // v1.1: isla por niveles y consulta anónima (retiradas el 7 nov 2026)
            "unlocked_elements", "seen_elements", "enc_consultations"
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
                note = o.optString("note", ""),
                uncertainty = o.optString("uncertainty", "")
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
        _islandState.value = IslandState(
            placed = readEncryptedArray(KEY_ISLAND) { o ->
                PlacedIslandObject(
                    objectId = o.getString("objectId"),
                    sourceActivityId = o.getString("sourceActivityId"),
                    sourceTitle = o.getString("sourceTitle"),
                    timestamp = o.getLong("timestamp"),
                    reflection = o.optString("reflection", "")
                )
            }.filter { IslandObjects.byId(it.objectId) != null },
            skyMode = runCatching { SkyMode.valueOf(sky) }.getOrDefault(SkyMode.DIA)
        )
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
            .put("uncertainty", log.uncertainty)
    }

    private fun saveReflections() = writeEncryptedArray(KEY_REFLECTIONS, _reflections.value) { r ->
        JSONObject().put("id", r.id).put("dateText", r.dateText).put("prompt", r.prompt).put("text", r.text)
    }

    private fun saveIsland() = writeEncryptedArray(KEY_ISLAND, _islandState.value.placed) { p ->
        JSONObject()
            .put("objectId", p.objectId)
            .put("sourceActivityId", p.sourceActivityId)
            .put("sourceTitle", p.sourceTitle)
            .put("timestamp", p.timestamp)
            .put("reflection", p.reflection)
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
    fun recordMarea(
        type: MareaType,
        situation: String,
        timeOfDay: String,
        location: String,
        note: String,
        uncertainty: String = ""
    ): MareaLogEntry {
        val entry = MareaLogEntry(
            id = newId(),
            type = type,
            timestamp = clock(),
            situation = situation.trim(),
            timeOfDay = timeOfDay.trim(),
            location = location.trim(),
            note = note.trim(),
            uncertainty = if (type == MareaType.NO_SE) uncertainty else ""
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

    /** Registra que se terminó una actividad o herramienta (solo participación, sin respuestas). */
    fun recordCompletion(sourceId: String): ActivityCompletion {
        val completion = ActivityCompletion(id = newId(), sourceId = sourceId, timestamp = clock())
        _completions.value = listOf(completion) + _completions.value
        saveCompletions()
        return completion
    }

    /** Cierre de una herramienta de "Para calmarme". No cambia la isla. */
    fun completeCalmTool(toolId: String): ClosingInfo {
        val completion = recordCompletion(toolId)
        val title = calmTools.firstOrNull { it.id == toolId }?.title ?: "Actividad"
        return ClosingInfo(completion.id, title)
    }

    /**
     * Cierre de una actividad significativa. Devuelve los objetos que el niño puede elegir
     * para su isla, o null si ya los tiene todos. Elegir es opcional.
     */
    fun completeGuidedActivity(activityId: String): IslandOffer? {
        recordCompletion(activityId)
        val activity = GuidedActivities.byId(activityId) ?: return null
        val offers = IslandObjects.offersFor(activity.islandOffers, _islandState.value.placedIds)
        if (offers.isEmpty()) return null
        return IslandOffer(activity.id, activity.title, offers.map { it.id })
    }

    /** Respuesta opcional "¿te ayudó?" al cerrar una actividad (Documento Técnico §4). */
    fun recordActivityFeedback(completionId: String, feedback: ActivityFeedback?) {
        _completions.value = _completions.value.map { if (it.id == completionId) it.copy(feedback = feedback) else it }
        saveCompletions()
    }

    /** El niño elige un objeto de los ofrecidos. Un objeto no ofrecido se rechaza. */
    fun placeIslandObject(offer: IslandOffer, objectId: String): Boolean {
        if (objectId !in offer.objectIds || objectId in _islandState.value.placedIds) return false
        IslandObjects.place(_islandState.value.placedIds, objectId)
        val placed = PlacedIslandObject(
            objectId = objectId,
            sourceActivityId = offer.activityId,
            sourceTitle = offer.activityTitle,
            timestamp = clock()
        )
        _islandState.value = _islandState.value.copy(placed = _islandState.value.placed + placed)
        saveIsland()
        return true
    }

    /** Reflexión privada y opcional sobre un objeto de la isla. */
    fun setIslandReflection(objectId: String, text: String) {
        _islandState.value = _islandState.value.copy(
            placed = _islandState.value.placed.map { if (it.objectId == objectId) it.copy(reflection = text.trim()) else it }
        )
        saveIsland()
    }

    fun setIslandSkyMode(skyMode: SkyMode) {
        prefs.edit().putString(KEY_SKY, skyMode.name).apply()
        _islandState.value = _islandState.value.copy(skyMode = skyMode)
    }

    // ---------------------------------------------------------------------------------
    // Reflexiones y diario
    // ---------------------------------------------------------------------------------

    /** Reflexión semanal ("Un momento para ti"). Privada; no cambia la isla. */
    fun addReflection(text: String) {
        if (text.isBlank()) return
        addEntry(WEEKLY_PROMPT, text)
    }

    /** Entrada del Diario del Mar. Se guarda cifrada y se puede borrar. */
    fun addDiaryEntry(prompt: String, text: String) {
        if (text.isBlank()) return
        addEntry("Diario del Mar · $prompt", text)
    }

    /** Guarda en el diario lo de una actividad significativa, SOLO si el niño lo elige. */
    fun saveActivityToDiary(activityTitle: String, text: String) {
        if (text.isBlank()) return
        addEntry(activityTitle, text)
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
    // Rutas y nombre
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

    companion object {
        const val PREFS_NAME = "marea_club_prefs"
        const val DEFAULT_NAME = "Navegante"
        const val WEEKLY_PROMPT = "¿Qué descubriste sobre tus emociones esta semana?"
        private const val MAX_LOGS = 200
        private const val MAX_COMPLETIONS = 300

        private const val KEY_ONBOARDED = "is_onboarded"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_FAVORITES = "favorite_routes"
        private const val KEY_ISLAND = "enc_island_objects"
        private const val KEY_SKY = "island_sky_mode"
        private const val KEY_LOGS = "enc_logs"
        private const val KEY_REFLECTIONS = "enc_reflections"
        private const val KEY_COMPLETIONS = "enc_completions"
        private const val KEY_ACTIVE_MAREA = "enc_active_marea"
    }
}
