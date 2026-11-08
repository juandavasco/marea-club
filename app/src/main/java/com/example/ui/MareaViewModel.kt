package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.MareaRepository
import com.example.domain.GuidedActivities
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MareaViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MareaRepository(application.applicationContext)

    val logs = repository.logs
    val activeMarea = repository.activeMarea
    val islandState = repository.islandState
    val reflections = repository.reflections
    val weeklyOverview = repository.weeklyOverview
    val weekHasEnoughData = repository.weekHasEnoughData
    val isOnboarded = repository.isOnboarded
    val userName = repository.userName
    val favoriteRouteIds = repository.favoriteRouteIds
    val completions = repository.completions

    val calmTools: List<ActivityItem> = repository.calmTools
    val guidedActivities: List<GuidedActivities.Activity> = repository.guidedActivities

    val psychologicalRoutes: List<PsychologicalRoute>
        get() = repository.psychologicalRoutesMedellin

    private val _currentTab = MutableStateFlow(BottomNavTab.HOME)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    /** Herramienta de "Para calmarme" abierta; null = ninguna. */
    private val _activeTool = MutableStateFlow<ActivityItem?>(null)
    val activeTool: StateFlow<ActivityItem?> = _activeTool.asStateFlow()

    /** Actividad significativa abierta; null = ninguna. */
    private val _activeGuided = MutableStateFlow<GuidedActivities.Activity?>(null)
    val activeGuided: StateFlow<GuidedActivities.Activity?> = _activeGuided.asStateFlow()

    private val _closing = MutableStateFlow<ClosingInfo?>(null)
    val closing: StateFlow<ClosingInfo?> = _closing.asStateFlow()

    private val _islandOffer = MutableStateFlow<IslandOffer?>(null)
    val islandOffer: StateFlow<IslandOffer?> = _islandOffer.asStateFlow()

    private val _showReflectionDialog = MutableStateFlow(false)
    val showReflectionDialog: StateFlow<Boolean> = _showReflectionDialog.asStateFlow()

    private val _showCustomizationDialog = MutableStateFlow(false)
    val showCustomizationDialog: StateFlow<Boolean> = _showCustomizationDialog.asStateFlow()

    private val _showMenuDialog = MutableStateFlow(false)
    val showMenuDialog: StateFlow<Boolean> = _showMenuDialog.asStateFlow()

    private val _showArchivoCorrientes = MutableStateFlow(false)
    val showArchivoCorrientes: StateFlow<Boolean> = _showArchivoCorrientes.asStateFlow()

    fun completeOnboarding() = repository.completeOnboarding()

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun logMarea(type: MareaType, situation: String, timeOfDay: String, location: String, note: String, uncertainty: String = "") {
        repository.recordMarea(type, situation, timeOfDay, location, note, uncertainty)
    }

    fun deleteLog(id: String) = repository.deleteLog(id)

    // --- Para calmarme ---------------------------------------------------------------

    fun openTool(tool: ActivityItem) {
        _activeTool.value = tool
    }

    /** Salir sin terminar: no cambia nada (sin penalización). */
    fun closeTool() {
        _activeTool.value = null
    }

    fun completeTool(tool: ActivityItem) {
        _closing.value = repository.completeCalmTool(tool.id)
        _activeTool.value = null
    }

    fun saveDiaryEntry(prompt: String, text: String) = repository.addDiaryEntry(prompt, text)

    fun dismissClosing(feedback: ActivityFeedback?) {
        val info = _closing.value
        if (info != null && feedback != null) repository.recordActivityFeedback(info.completionId, feedback)
        _closing.value = null
    }

    // --- Actividades significativas ----------------------------------------------------

    fun openGuided(activity: GuidedActivities.Activity) {
        _activeGuided.value = activity
    }

    /** Pausar o salir: no se guarda nada. */
    fun closeGuided() {
        _activeGuided.value = null
    }

    /** Termina la actividad. [diaryText] solo llega si el niño eligió guardarlo. */
    fun finishGuided(activity: GuidedActivities.Activity, diaryText: String?) {
        if (diaryText != null) repository.saveActivityToDiary(activity.title, diaryText)
        _islandOffer.value = repository.completeGuidedActivity(activity.id)
        _activeGuided.value = null
    }

    /** null = "Ahora no". */
    fun chooseIslandObject(objectId: String?) {
        val offer = _islandOffer.value
        if (offer != null && objectId != null) repository.placeIslandObject(offer, objectId)
        _islandOffer.value = null
    }

    fun setIslandReflection(objectId: String, text: String) = repository.setIslandReflection(objectId, text)

    // --- Otros -------------------------------------------------------------------------

    fun saveReflection(text: String) {
        repository.addReflection(text)
        _showReflectionDialog.value = false
    }

    fun deleteReflection(id: String) = repository.deleteReflection(id)

    fun resetAllData() {
        repository.resetAllData()
        _activeTool.value = null
        _activeGuided.value = null
        _closing.value = null
        _islandOffer.value = null
        _showMenuDialog.value = false
        _showArchivoCorrientes.value = false
        _currentTab.value = BottomNavTab.HOME
    }

    fun setReflectionDialogVisible(visible: Boolean) {
        _showReflectionDialog.value = visible
    }

    fun setCustomizationDialogVisible(visible: Boolean) {
        _showCustomizationDialog.value = visible
    }

    fun setMenuDialogVisible(visible: Boolean) {
        _showMenuDialog.value = visible
    }

    fun setArchivoCorrientesVisible(visible: Boolean) {
        _showArchivoCorrientes.value = visible
    }

    fun changeSkyMode(skyMode: SkyMode) = repository.setIslandSkyMode(skyMode)

    fun toggleFavoriteRoute(routeId: String) = repository.toggleFavoriteRoute(routeId)

    fun setUserName(name: String) = repository.setUserName(name)
}
