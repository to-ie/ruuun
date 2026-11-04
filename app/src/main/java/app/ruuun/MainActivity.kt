package app.ruuun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ruuun.ui.components.RuuunSplash
import kotlinx.coroutines.delay
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.ruuun.ui.RuuunApp
import app.ruuun.ui.RuuunViewModel
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.RuuunTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.motionAllowed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        // Light bars on every screen: the app has light backgrounds only, so the status
        // icons stay dark even when the phone itself is in dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // The system splash (Ruu's head on navy) hands straight over to the app's own
        // animated splash, which stays until the settings and logs have been read.
        val viewModel: RuuunViewModel by viewModels()

        setContent { RuuunRoot(viewModel) }
    }
}

/** The app's own splash stays at least this long, so it reads as a moment, not a flash. */
private const val SPLASH_MIN_MS = 900L

@Composable
private fun RuuunRoot(viewModel: RuuunViewModel) {
    RuuunTheme {
        // Read once per launch: "Remove animations" turns all movement off.
        val context = LocalContext.current
        val motion = remember { context.motionAllowed() }
        val state by viewModel.state.collectAsStateWithLifecycle()
        // Saved, so turning the phone does not bring the splash back.
        var minTimeDone by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (!minTimeDone) delay(SPLASH_MIN_MS)
            minTimeDone = true
        }
        CompositionLocalProvider(LocalMotion provides motion) {
            // Painted edge to edge first, so no window colour shows behind the system bars.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Ground),
            ) {
                Surface(
                    color = Ground,
                    // The top is left to each screen, so its header colour reaches behind the
                    // status bar. Sides and bottom (navigation bar, keyboard) are handled here.
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ),
                ) {
                    RuuunApp(viewModel)
                }
                AnimatedVisibility(
                    visible = state.loading || !minTimeDone,
                    enter = EnterTransition.None,
                    exit = if (motion) fadeOut(tween(200)) else ExitTransition.None,
                ) {
                    RuuunSplash()
                }
            }
        }
    }
}
