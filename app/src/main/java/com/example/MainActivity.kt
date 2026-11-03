package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.MareaViewModel
import com.example.ui.components.MareaBottomBar
import com.example.ui.components.MareaTopBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.MareaBackground
import com.example.ui.theme.MareaClubTheme

enum class SubScreen {
    MAREA_SELECTION,
    DIRECTORY,
    SELF_EVALUATION,
    ANONYMOUS_CONSULTATION
}

class MainActivity : ComponentActivity() {
    private val viewModel: MareaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MareaClubTheme {
                MareaClubApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MareaClubApp(viewModel: MareaViewModel) {
    val isOnboarded by viewModel.isOnboarded.collectAsStateWithLifecycle()
    var forceShowWelcome by remember { mutableStateOf(false) }

    var activeSubScreen by remember { mutableStateOf<SubScreen?>(null) }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeMarea by viewModel.activeMarea.collectAsStateWithLifecycle()
    val islandState by viewModel.islandState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val weeklyOverview by viewModel.weeklyOverview.collectAsStateWithLifecycle()
    val calmMomentsCount by viewModel.calmMomentsCount.collectAsStateWithLifecycle()
    val activitiesExploredCount by viewModel.activitiesExploredCount.collectAsStateWithLifecycle()
    val activeActivity by viewModel.activeActivity.collectAsStateWithLifecycle()
    val earnedReward by viewModel.earnedReward.collectAsStateWithLifecycle()

    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val favoriteRouteIds by viewModel.favoriteRouteIds.collectAsStateWithLifecycle()
    val consultations by viewModel.consultations.collectAsStateWithLifecycle()
    val reflections by viewModel.reflections.collectAsStateWithLifecycle()

    val showReflectionDialog by viewModel.showReflectionDialog.collectAsStateWithLifecycle()
    val showCustomizationDialog by viewModel.showCustomizationDialog.collectAsStateWithLifecycle()
    val showMenuDialog by viewModel.showMenuDialog.collectAsStateWithLifecycle()
    val showArchivoCorrientes by viewModel.showArchivoCorrientes.collectAsStateWithLifecycle()

    // Screen dynamic background color
    val currentBgColor by animateColorAsState(
        targetValue = activeMarea?.backgroundColor ?: MareaBackground,
        animationSpec = tween(600),
        label = "app_bg_anim"
    )

    // Handle back button
    BackHandler(enabled = activeSubScreen != null || activeActivity != null || currentTab != BottomNavTab.HOME || forceShowWelcome) {
        when {
            forceShowWelcome -> forceShowWelcome = false
            activeActivity != null -> viewModel.closeActiveActivity()
            activeSubScreen != null -> activeSubScreen = null
            currentTab != BottomNavTab.HOME -> viewModel.selectTab(BottomNavTab.HOME)
        }
    }

    if (!isOnboarded || forceShowWelcome) {
        WelcomeScreen(
            onStartClick = {
                viewModel.completeOnboarding()
                forceShowWelcome = false
            }
        )
        return
    }

    // Active interactive activity takes priority
    if (activeActivity != null) {
        ActivityRunnerScreen(
            activity = activeActivity!!,
            onClose = { viewModel.closeActiveActivity() },
            onComplete = { viewModel.completeCurrentActivity(activeActivity!!) }
        )
        return
    }

    // Active sub-screen takes full focus
    when (activeSubScreen) {
        SubScreen.MAREA_SELECTION -> {
            MareaSelectionScreen(
                currentMarea = activeMarea,
                availableActivities = viewModel.availableActivities,
                onSaveMarea = { marea, sit, time, loc, note ->
                    viewModel.logMarea(marea, sit, time, loc, note)
                },
                onLaunchActivity = { viewModel.openActivity(it) },
                onBack = { activeSubScreen = null }
            )
            return
        }

        SubScreen.DIRECTORY -> {
            PsychologicalDirectoryScreen(
                routes = viewModel.psychologicalRoutes,
                favoriteIds = favoriteRouteIds,
                onToggleFavorite = { viewModel.toggleFavoriteRoute(it) },
                onBack = { activeSubScreen = null },
                onOpenSelfEvaluation = { activeSubScreen = SubScreen.SELF_EVALUATION },
                onOpenAnonymousConsultation = { activeSubScreen = SubScreen.ANONYMOUS_CONSULTATION }
            )
            return
        }

        SubScreen.SELF_EVALUATION -> {
            SelfEvaluationScreen(
                questionnaires = viewModel.questionnaires,
                onEvaluate = { qId, answers -> viewModel.evaluateQuestionnaire(qId, answers) },
                onBack = { activeSubScreen = null },
                onOpenSupportDirectory = { activeSubScreen = SubScreen.DIRECTORY }
            )
            return
        }

        SubScreen.ANONYMOUS_CONSULTATION -> {
            AnonymousConsultationScreen(
                consultations = consultations,
                onSubmitConsultation = { msg -> viewModel.sendAnonymousConsultation(msg) },
                onBack = { activeSubScreen = null },
                onOpenSupportDirectory = { activeSubScreen = SubScreen.DIRECTORY }
            )
            return
        }

        null -> {
            // Normal dashboard scaffold
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor),
        containerColor = currentBgColor,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            MareaTopBar(
                onMenuClick = { viewModel.setMenuDialogVisible(true) }
            )
        },
        bottomBar = {
            MareaBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(currentBgColor)
        ) {
            when (currentTab) {
                BottomNavTab.HOME -> {
                    MareasHomeScreen(
                        userName = userName,
                        activeMarea = activeMarea,
                        islandState = islandState,
                        availableActivities = viewModel.availableActivities,
                        weeklyOverview = weeklyOverview,
                        calmMomentsCount = calmMomentsCount,
                        onNavigateToMareaSelection = { viewModel.selectTab(BottomNavTab.MAREA) },
                        onNavigateToIsland = { viewModel.selectTab(BottomNavTab.ISLAND) },
                        onNavigateToActivities = { viewModel.selectTab(BottomNavTab.BURBUJA) },
                        onNavigateToProgress = { viewModel.selectTab(BottomNavTab.PROGRESS) },
                        onOpenActivity = { viewModel.openActivity(it) },
                        onOpenSupportDirectory = { activeSubScreen = SubScreen.DIRECTORY },
                        onOpenSelfEvaluation = { activeSubScreen = SubScreen.SELF_EVALUATION },
                        onOpenAnonymousConsultation = { activeSubScreen = SubScreen.ANONYMOUS_CONSULTATION },
                        onOpenMenu = { viewModel.setMenuDialogVisible(true) }
                    )
                }

                BottomNavTab.MAREA -> {
                    MareaSelectionScreen(
                        currentMarea = activeMarea,
                        availableActivities = viewModel.availableActivities,
                        onSaveMarea = { marea, sit, time, loc, note ->
                            viewModel.logMarea(marea, sit, time, loc, note)
                        },
                        onLaunchActivity = { viewModel.openActivity(it) },
                        onBack = { viewModel.selectTab(BottomNavTab.HOME) }
                    )
                }

                BottomNavTab.ISLAND -> {
                    IslandScreen(
                        islandState = islandState,
                        allIslandElements = viewModel.allIslandElements,
                        onNavigateToActivities = { viewModel.selectTab(BottomNavTab.BURBUJA) },
                        onOpenCustomization = { viewModel.setCustomizationDialogVisible(true) }
                    )
                }

                BottomNavTab.BURBUJA -> {
                    BurbujaScreen(
                        availableActivities = viewModel.availableActivities,
                        onOpenActivity = { viewModel.openActivity(it) },
                        onActivityCompleted = { viewModel.repository.completeCalmActivity(it) }
                    )
                }

                BottomNavTab.PROGRESS -> {
                    ProgressScreen(
                        weeklyOverview = weeklyOverview,
                        calmMomentsCount = calmMomentsCount,
                        activitiesExploredCount = activitiesExploredCount,
                        logs = logs,
                        reflections = reflections,
                        availableActivities = viewModel.availableActivities,
                        onOpenReflection = { viewModel.setReflectionDialogVisible(true) },
                        onOpenActivity = { viewModel.openActivity(it) }
                    )
                }
            }
        }
    }

    // Reward Celebration Dialog
    if (earnedReward != null) {
        val (rewardTitle, rewardIcon) = earnedReward!!
        RewardCelebrationDialog(
            rewardTitle = rewardTitle,
            rewardIcon = rewardIcon,
            onDismiss = { viewModel.dismissRewardDialog() },
            onGoToIsland = {
                viewModel.dismissRewardDialog()
                viewModel.selectTab(BottomNavTab.ISLAND)
            }
        )
    }

    // Modal dialogs
    if (showReflectionDialog) {
        ReflectionDialog(
            onDismiss = { viewModel.setReflectionDialogVisible(false) },
            onSaveReflection = { text: String -> viewModel.saveReflection(text) }
        )
    }

    if (showCustomizationDialog) {
        IslandCustomizationDialog(
            islandState = islandState,
            onDismiss = { viewModel.setCustomizationDialogVisible(false) },
            onSkyModeSelected = { viewModel.changeSkyMode(it) }
        )
    }

    if (showMenuDialog) {
        MenuDotsDialog(
            onDismiss = { viewModel.setMenuDialogVisible(false) },
            onOpenArchivo = { viewModel.setArchivoCorrientesVisible(true) },
            onOpenSupport = { activeSubScreen = SubScreen.DIRECTORY },
            onOpenSelfEvaluation = { activeSubScreen = SubScreen.SELF_EVALUATION },
            onOpenAnonymousConsultation = { activeSubScreen = SubScreen.ANONYMOUS_CONSULTATION },
            onReplayWelcome = { forceShowWelcome = true }
        )
    }

    if (showArchivoCorrientes) {
        ArchivoCorrientesDialog(
            logs = logs,
            onDismiss = { viewModel.setArchivoCorrientesVisible(false) }
        )
    }
}
