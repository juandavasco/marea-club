package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.MareaRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MareaViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MareaRepository(application.applicationContext)

    val logs = repository.logs
    val activeMarea = repository.activeMarea
    val islandState = repository.islandState
    val calmMomentsCount = repository.calmMomentsCount
    val activitiesExploredCount = repository.activitiesExploredCount
    val reflections = repository.reflections
    val weeklyOverview = repository.weeklyOverview
    val weekHasEnoughData = repository.weekHasEnoughData
    val isOnboarded = repository.isOnboarded
    val availableActivities = repository.availableActivities
    val allIslandElements = repository.allIslandElements
    val userName = repository.userName
    val favoriteRouteIds = repository.favoriteRouteIds
    val questionnaires = repository.questionnaires
    val consultations = repository.consultations

    val psychologicalRoutes: List<PsychologicalRoute>
        get() = repository.psychologicalRoutesMedellin

    private val _currentTab = MutableStateFlow(BottomNavTab.HOME)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    /** Actividad interactiva abierta; null = ninguna. */
    private val _activeActivity = MutableStateFlow<ActivityItem?>(null)
    val activeActivity: StateFlow<ActivityItem?> = _activeActivity.asStateFlow()

    private val _showReflectionDialog = MutableStateFlow(false)
    val showReflectionDialog: StateFlow<Boolean> = _showReflectionDialog.asStateFlow()

    private val _showCustomizationDialog = MutableStateFlow(false)
    val showCustomizationDialog: StateFlow<Boolean> = _showCustomizationDialog.asStateFlow()

    private val _showMenuDialog = MutableStateFlow(false)
    val showMenuDialog: StateFlow<Boolean> = _showMenuDialog.asStateFlow()

    private val _showArchivoCorrientes = MutableStateFlow(false)
    val showArchivoCorrientes: StateFlow<Boolean> = _showArchivoCorrientes.asStateFlow()

    /** Qué acción desbloqueó qué elemento; se muestra al terminar una actividad. */
    private val _earnedReward = MutableStateFlow<RewardInfo?>(null)
    val earnedReward: StateFlow<RewardInfo?> = _earnedReward.asStateFlow()

    fun completeOnboarding() = repository.completeOnboarding()

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun logMarea(type: MareaType, situation: String, timeOfDay: String, location: String, note: String) {
        repository.recordMarea(type, situation, timeOfDay, location, note)
    }

    fun deleteLog(id: String) = repository.deleteLog(id)

    fun openActivity(activity: ActivityItem) {
        _activeActivity.value = activity
    }

    /** Salir sin terminar: no cambia nada (sin penalización). */
    fun closeActiveActivity() {
        _activeActivity.value = null
    }

    fun completeCurrentActivity(activity: ActivityItem) {
        _earnedReward.value = repository.completeActivity(activity.id)
        _activeActivity.value = null
    }

    /** Herramientas de la pestaña Actividades (burbujas, sonidos). */
    fun completeTool(sourceId: String) {
        _earnedReward.value = repository.completeActivity(sourceId)
    }

    fun saveDiaryEntry(prompt: String, text: String) = repository.addDiaryEntry(prompt, text)

    /** Cierra el aviso de recompensa, guardando la respuesta opcional "¿te ayudó?". */
    fun dismissRewardDialog(feedback: ActivityFeedback? = null) {
        val reward = _earnedReward.value
        if (reward != null && feedback != null) {
            repository.recordActivityFeedback(reward.completionId, feedback)
        }
        _earnedReward.value = null
    }

    fun saveReflection(text: String) {
        val reward = repository.addReflection(text)
        _showReflectionDialog.value = false
        if (reward != null) _earnedReward.value = reward
    }

    fun deleteReflection(id: String) = repository.deleteReflection(id)

    fun markElementSeen(elementId: String) = repository.markElementSeen(elementId)

    fun resetAllData() {
        repository.resetAllData()
        _activeActivity.value = null
        _earnedReward.value = null
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

    fun sendAnonymousConsultation(message: String): AnonymousConsultation =
        repository.submitAnonymousConsultation(message)

    fun toggleFavoriteRoute(routeId: String) = repository.toggleFavoriteRoute(routeId)

    fun setUserName(name: String) = repository.setUserName(name)

    fun evaluateQuestionnaire(questionnaireId: String, selectedOptions: Map<Int, Int>): SelfCheckResult =
        repository.evaluateQuestionnaire(questionnaireId, selectedOptions)
}
