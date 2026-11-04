package app.ruuun.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import app.ruuun.ui.theme.IconMuted
import app.ruuun.ui.theme.SkyDeep
import app.ruuun.ui.theme.SkyTint
import app.ruuun.ui.theme.SkyShade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ruuun.ui.screens.BadgesScreen
import app.ruuun.ui.screens.HistoryScreen
import app.ruuun.ui.screens.HomeScreen
import app.ruuun.ui.screens.LogRunScreen
import app.ruuun.ui.screens.JourneyScreen
import app.ruuun.ui.components.UpdateBanner
import app.ruuun.ui.screens.RunDetailScreen
import app.ruuun.ui.screens.RunSavedScreen
import app.ruuun.ui.screens.SettingsScreen
import app.ruuun.ui.screens.SetupScreen
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor

object Routes {
    const val SETUP = "setup"
    const val HOME = "home"
    const val JOURNEY = "journey"
    const val BADGES = "badges"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val LOG_RUN = "log"
    const val RUN_SAVED = "saved"
    const val EDIT_RUN = "edit/{runId}"
    const val RUN_DETAIL = "run/{runId}"

    fun editRun(id: String) = "edit/$id"
    fun runDetail(id: String) = "run/$id"
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val NAV_ITEMS = listOf(
    NavItem(Routes.HOME, "Home", Icons.Rounded.Home),
    NavItem(Routes.JOURNEY, "Journey", Icons.Rounded.Map),
    NavItem(Routes.BADGES, "Badges", Icons.Rounded.MilitaryTech),
    NavItem(Routes.HISTORY, "History", Icons.Rounded.History),
)

@Composable
fun RuuunApp(viewModel: RuuunViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val update by viewModel.update.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val toast = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(toast.text)
        viewModel.clearMessage()
    }

    // The splash covers the screen while the data loads (see MainActivity).
    if (state.loading) return

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Deleting or importing can remove the settings row; setup is the only sensible place then.
    LaunchedEffect(state.needsSetup, currentRoute) {
        if (state.needsSetup && currentRoute != null && currentRoute != Routes.SETUP) {
            navController.navigate(Routes.SETUP) { popUpTo(0) { inclusive = true } }
        }
    }
    val showNav = currentRoute in NAV_ITEMS.map { it.route }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Ground,
        // Screens pad for the status bar themselves, inside their own header colour.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Settings too: that is where "Check for updates" lives, so its answer shows there.
            if (showNav || currentRoute == Routes.SETTINGS) {
                UpdateBanner(
                    state = update,
                    onUpdate = viewModel::downloadUpdate,
                    onInstall = { viewModel.installIntent()?.let(context::startActivity) },
                    onLater = viewModel::dismissUpdate,
                )
            }
        },
        bottomBar = { if (showNav) BottomNav(navController, currentRoute) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (state.needsSetup) Routes.SETUP else Routes.HOME,
            // The update banner already covers the status bar when it shows.
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            composable(Routes.SETUP) {
                SetupScreen(
                    onFinish = { nickname, startingTargetM, weeklyGoal ->
                        viewModel.completeSetup(nickname, startingTargetM, weeklyGoal)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    state = state,
                    today = viewModel.today(),
                    onLogRun = { navController.navigate(Routes.LOG_RUN) },
                    onMakeEasier = viewModel::makeEasier,
                    onMakeHarder = viewModel::makeHarder,
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    newlyFilledDots = viewModel::newlyFilledDots,
                )
            }
            composable(Routes.JOURNEY) {
                JourneyScreen(
                    state = state,
                    today = viewModel.today(),
                )
            }
            composable(Routes.BADGES) {
                BadgesScreen(state = state)
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    state = state,
                    today = viewModel.today(),
                    onOpenRun = { navController.navigate(Routes.runDetail(it)) },
                    onLogRun = { navController.navigate(Routes.LOG_RUN) },
                )
            }
            composable(Routes.LOG_RUN) {
                LogRunScreen(
                    state = state,
                    viewModel = viewModel,
                    editingId = null,
                    onClose = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(Routes.RUN_SAVED) {
                            popUpTo(Routes.HOME)
                        }
                    },
                )
            }
            composable(Routes.EDIT_RUN) { entry ->
                LogRunScreen(
                    state = state,
                    viewModel = viewModel,
                    editingId = entry.arguments?.getString("runId"),
                    onClose = { navController.popBackStack() },
                    onSaved = { navController.popBackStack(Routes.HISTORY, inclusive = false) },
                )
            }
            composable(Routes.RUN_SAVED) {
                RunSavedScreen(
                    state = state,
                    viewModel = viewModel,
                    onDone = {
                        viewModel.clearLastSave()
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    },
                    onViewBadges = {
                        viewModel.clearLastSave()
                        navController.navigate(Routes.BADGES) { popUpTo(Routes.HOME) }
                    },
                )
            }
            composable(Routes.RUN_DETAIL) { entry ->
                RunDetailScreen(
                    state = state,
                    viewModel = viewModel,
                    runId = entry.arguments?.getString("runId"),
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editRun(it)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    state = state,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
    // Every screen starts in header blue, so the status bar stays blue while content
    // scrolls underneath it.
    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(Sky),
    )
    }
}

@Composable
private fun BottomNav(navController: NavHostController, currentRoute: String?) {
    Column(Modifier.fillMaxWidth().background(SurfaceColor)) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(Border))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NAV_ITEMS.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) SkyTint else Color.Transparent)
                            .then(
                                if (selected) Modifier.border(2.dp, Sky, RoundedCornerShape(14.dp)) else Modifier,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Icons only on screen; TalkBack still hears the tab's name.
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selected) SkyShade else IconMuted,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
        }
    }
}
