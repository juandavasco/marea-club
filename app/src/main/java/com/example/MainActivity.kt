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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.MareaViewModel
import com.example.ui.components.MareaBottomBar
import com.example.ui.components.MareaHeader
import com.example.ui.components.MareaSegmented
import com.example.ui.components.MareaSpace
import com.example.ui.components.MareaTopBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.MareaBackground
import com.example.ui.theme.MareaClubTheme

/** Pantallas que se abren encima de las pestañas. */
enum class SubScreen {
    SALVAVIDAS,
    DIRECTORY
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
    /** 0 = Registrar, 1 = Mi semana (dentro de la pestaña Mareas). */
    var mareaSegment by rememberSaveable { mutableIntStateOf(0) }
    /** "NO_SE" o "SIN_PALABRA" cuando el niño eligió No sé; se guarda junto con el registro. */
    var pendingUncertainty by remember { mutableStateOf("") }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeMarea by viewModel.activeMarea.collectAsStateWithLifecycle()
    val islandState by viewModel.islandState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val weeklyOverview by viewModel.weeklyOverview.collectAsStateWithLifecycle()
    val weekHasEnoughData by viewModel.weekHasEnoughData.collectAsStateWithLifecycle()
    val reflections by viewModel.reflections.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val favoriteRouteIds by viewModel.favoriteRouteIds.collectAsStateWithLifecycle()

    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val activeGuided by viewModel.activeGuided.collectAsStateWithLifecycle()
    val closing by viewModel.closing.collectAsStateWithLifecycle()
    val islandOffer by viewModel.islandOffer.collectAsStateWithLifecycle()

    val showReflectionDialog by viewModel.showReflectionDialog.collectAsStateWithLifecycle()
    val showMenuDialog by viewModel.showMenuDialog.collectAsStateWithLifecycle()
    val showArchivoCorrientes by viewModel.showArchivoCorrientes.collectAsStateWithLifecycle()
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Sugerencia de inicio: la primera actividad aún no explorada (o la primera si ya exploró todas).
    val explored = completions.map { it.sourceId }.toSet()
    val suggested = viewModel.guidedActivities.firstOrNull { it.id !in explored } ?: viewModel.guidedActivities.first()

    val currentBgColor by animateColorAsState(
        targetValue = if (currentTab == BottomNavTab.MAREA) activeMarea?.backgroundColor ?: MareaBackground else MareaBackground,
        animationSpec = tween(600),
        label = "app_bg_anim"
    )

    BackHandler(
        enabled = forceShowWelcome || activeGuided != null || activeTool != null ||
            activeSubScreen != null || currentTab != BottomNavTab.HOME
    ) {
        when {
            forceShowWelcome -> forceShowWelcome = false
            activeGuided != null -> viewModel.closeGuided()
            activeTool != null -> viewModel.closeTool()
            activeSubScreen == SubScreen.DIRECTORY -> activeSubScreen = SubScreen.SALVAVIDAS
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

    // 1. Actividad con propósito en curso
    activeGuided?.let { activity ->
        GuidedActivityScreen(
            activity = activity,
            onClose = { viewModel.closeGuided() },
            onFinish = { diaryText -> viewModel.finishGuided(activity, diaryText) },
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        )
        return
    }

    // 2. Herramienta de "Para calmarme" en curso
    activeTool?.let { tool ->
        ActivityRunnerScreen(
            activity = tool,
            onClose = { viewModel.closeTool() },
            onComplete = { viewModel.completeTool(tool) },
            onSaveDiary = { prompt, text -> viewModel.saveDiaryEntry(prompt, text) },
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        )
        return
    }

    // 3. Pantallas secundarias
    when (activeSubScreen) {
        SubScreen.SALVAVIDAS -> {
            SalvavidasScreen(
                // Entrega 3–4: vínculo real con Firebase. Hasta entonces no hay adulto vinculado.
                onRequestCompanion = null,
                onOpenAllLines = { activeSubScreen = SubScreen.DIRECTORY },
                onBack = { activeSubScreen = null },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MareaBackground)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            )
            return
        }
        SubScreen.DIRECTORY -> {
            PsychologicalDirectoryScreen(
                routes = viewModel.psychologicalRoutes,
                favoriteIds = favoriteRouteIds,
                onToggleFavorite = { viewModel.toggleFavoriteRoute(it) },
                onBack = { activeSubScreen = SubScreen.SALVAVIDAS }
            )
            return
        }
        null -> Unit
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor),
        containerColor = currentBgColor,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { MareaTopBar(onMenuClick = { viewModel.setMenuDialogVisible(true) }) },
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
                BottomNavTab.HOME -> HomeScreen(
                    userName = userName,
                    logs = logs,
                    suggested = suggested,
                    onRegisterMarea = {
                        mareaSegment = 0
                        viewModel.selectTab(BottomNavTab.MAREA)
                    },
                    onOpenSuggested = { viewModel.openGuided(suggested) },
                    onOpenSalvavidas = { activeSubScreen = SubScreen.SALVAVIDAS }
                )

                BottomNavTab.MAREA -> {
                    val header: @Composable () -> Unit = {
                        Column {
                            MareaHeader(
                                title = "Mareas",
                                subtitle = if (mareaSegment == 0) "¿Cómo viene tu marea ahora?" else "Cómo se movieron tus mareas estos días."
                            )
                            MareaSegmented(
                                options = listOf("Registrar", "Mi semana"),
                                selectedIndex = mareaSegment,
                                onSelect = { mareaSegment = it },
                                tags = listOf("marea_segment_register", "marea_segment_week"),
                                modifier = Modifier.padding(bottom = MareaSpace.l)
                            )
                        }
                    }
                    if (mareaSegment == 0) {
                        MareaSelectionScreen(
                            currentMarea = activeMarea,
                            availableActivities = viewModel.calmTools,
                            onSaveMarea = { marea, sit, time, loc, note ->
                                viewModel.logMarea(marea, sit, time, loc, note, pendingUncertainty)
                                pendingUncertainty = ""
                            },
                            onLaunchActivity = { viewModel.openTool(it) },
                            onBack = { viewModel.selectTab(BottomNavTab.HOME) },
                            onOpenSupportDirectory = { activeSubScreen = SubScreen.SALVAVIDAS },
                            headerContent = header,
                            onSaveUncertainty = { pendingUncertainty = it }
                        )
                    } else {
                        MiSemanaScreen(
                            weeklyOverview = weeklyOverview,
                            logs = logs,
                            reflections = reflections,
                            onOpenReflection = { viewModel.setReflectionDialogVisible(true) },
                            weekHasEnoughData = weekHasEnoughData,
                            onDeleteReflection = { viewModel.deleteReflection(it) },
                            headerContent = header
                        )
                    }
                }

                BottomNavTab.ACTIVIDADES -> ActivitiesScreen(
                    guided = viewModel.guidedActivities,
                    calmTools = viewModel.calmTools,
                    completions = completions,
                    onOpenGuided = { viewModel.openGuided(it) },
                    onOpenTool = { viewModel.openTool(it) }
                )

                BottomNavTab.ISLAND -> IslandScreen(
                    islandState = islandState,
                    onSaveReflection = { id, text -> viewModel.setIslandReflection(id, text) },
                    onSkyMode = { viewModel.changeSkyMode(it) }
                )
            }
        }
    }

    // Diálogos posteriores a una actividad
    islandOffer?.let { offer ->
        IslandOfferDialog(offer = offer, onChoose = { viewModel.chooseIslandObject(it) })
    }
    closing?.let { info ->
        ClosingDialog(info = info, onDone = { viewModel.dismissClosing(it) })
    }

    if (showReflectionDialog) {
        ReflectionDialog(
            onDismiss = { viewModel.setReflectionDialogVisible(false) },
            onSaveReflection = { text: String -> viewModel.saveReflection(text) }
        )
    }

    if (showMenuDialog) {
        MenuDotsDialog(
            onDismiss = { viewModel.setMenuDialogVisible(false) },
            onOpenArchivo = { viewModel.setArchivoCorrientesVisible(true) },
            onOpenSupport = { activeSubScreen = SubScreen.SALVAVIDAS },
            onReplayWelcome = { forceShowWelcome = true },
            onOpenPrivacy = { showPrivacyDialog = true },
            onResetData = { showResetDialog = true }
        )
    }

    if (showPrivacyDialog) {
        PrivacyInfoDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showResetDialog) {
        ResetDataDialog(
            onConfirm = {
                showResetDialog = false
                activeSubScreen = null
                mareaSegment = 0
                viewModel.resetAllData()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showArchivoCorrientes) {
        ArchivoCorrientesDialog(
            logs = logs,
            onDismiss = { viewModel.setArchivoCorrientesVisible(false) },
            onDeleteLog = { viewModel.deleteLog(it) }
        )
    }
}
