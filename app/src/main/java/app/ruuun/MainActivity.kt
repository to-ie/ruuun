package app.ruuun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Hold the logo on screen until the settings and logs have been read, so the
        // app never flashes an empty frame on the way in.
        val viewModel: RuuunViewModel by viewModels()
        splash.setKeepOnScreenCondition { viewModel.state.value.loading }

        setContent { RuuunRoot(viewModel) }
    }
}

@Composable
private fun RuuunRoot(viewModel: RuuunViewModel) {
    RuuunTheme {
        Surface(
            color = Ground,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            RuuunApp(viewModel)
        }
    }
}
