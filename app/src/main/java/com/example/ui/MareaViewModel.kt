package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MareaRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MareaViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MareaRepository(application.applicationContext)

    val logs = repository.logs
    val activeMarea = repository.activeMarea
    val islandState = repository.islandState
    val calmMomentsCount = repository.calmMomentsCount
    val activitiesExploredCount = repository.activitiesExploredCount
    val reflections = repository.reflections
    val weeklyOverview = repository.weeklyOverview
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

    private val _selectedMareaForDetail = MutableStateFlow<MareaType?>(null)
    val selectedMareaForDetail: StateFlow<MareaType?> = _selectedMareaForDetail.asStateFlow()

    // Activity runner state: when non-null, an interactive activity is active
    private val _activeActivity = MutableStateFlow<ActivityItem?>(null)
    val activeActivity: StateFlow<ActivityItem?> = _activeActivity.asStateFlow()

    // Dialogs
    private val _showReflectionDialog = MutableStateFlow(false)
    val showReflectionDialog: StateFlow<Boolean> = _showReflectionDialog.asStateFlow()

    private val _showCustomizationDialog = MutableStateFlow(false)
    val showCustomizationDialog: StateFlow<Boolean> = _showCustomizationDialog.asStateFlow()

    private val _showMenuDialog = MutableStateFlow(false)
    val showMenuDialog: StateFlow<Boolean> = _showMenuDialog.asStateFlow()

    private val _showArchivoCorrientes = MutableStateFlow(false)
    val showArchivoCorrientes: StateFlow<Boolean> = _showArchivoCorrientes.asStateFlow()

    private val _showSupportDialog = MutableStateFlow(false)
    val showSupportDialog: StateFlow<Boolean> = _showSupportDialog.asStateFlow()

    private val _showSelfEvaluation = MutableStateFlow(false)
    val showSelfEvaluation: StateFlow<Boolean> = _showSelfEvaluation.asStateFlow()

    private val _showAnonymousConsultation = MutableStateFlow(false)
    val showAnonymousConsultation: StateFlow<Boolean> = _showAnonymousConsultation.asStateFlow()

    private val _earnedReward = MutableStateFlow<Pair<String, String>?>(null)
    val earnedReward: StateFlow<Pair<String, String>?> = _earnedReward.asStateFlow()

    fun completeOnboarding() {
        repository.completeOnboarding()
    }

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun openMareaExploration(marea: MareaType) {
        _selectedMareaForDetail.value = marea
    }

    fun closeMareaExploration() {
        _selectedMareaForDetail.value = null
    }

    fun logMarea(type: MareaType, situation: String, timeOfDay: String, location: String, note: String) {
        viewModelScope.launch {
            repository.recordMarea(type, situation, timeOfDay, location, note)
            closeMareaExploration()
        }
    }

    fun openActivity(activity: ActivityItem) {
        _activeActivity.value = activity
    }

    fun closeActiveActivity() {
        _activeActivity.value = null
    }

    fun completeCurrentActivity(activity: ActivityItem) {
        viewModelScope.launch {
            repository.completeActivity(activity.id)
            _earnedReward.value = Pair(activity.rewardTitle, activity.rewardIcon)
            _activeActivity.value = null
        }
    }

    fun dismissRewardDialog() {
        _earnedReward.value = null
    }

    fun saveReflection(text: String) {
        viewModelScope.launch {
            repository.addReflection(text)
            _showReflectionDialog.value = false
        }
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

    fun setSupportDialogVisible(visible: Boolean) {
        _showSupportDialog.value = visible
    }

    fun setSelfEvaluationVisible(visible: Boolean) {
        _showSelfEvaluation.value = visible
    }

    fun setAnonymousConsultationVisible(visible: Boolean) {
        _showAnonymousConsultation.value = visible
    }

    fun changeSkyMode(skyMode: SkyMode) {
        repository.setIslandSkyMode(skyMode)
    }

    fun sendAnonymousConsultation(message: String): AnonymousConsultation {
        return repository.submitAnonymousConsultation(message)
    }

    fun toggleFavoriteRoute(routeId: String) {
        repository.toggleFavoriteRoute(routeId)
    }

    fun setUserName(name: String) {
        repository.setUserName(name)
    }

    fun evaluateQuestionnaire(questionnaireId: String, selectedOptions: Map<Int, Int>): EvaluationResult {
        return repository.evaluateQuestionnaire(questionnaireId, selectedOptions)
    }
}
