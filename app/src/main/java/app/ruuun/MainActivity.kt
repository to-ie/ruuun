package app.ruuun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.ruuun.ui.RuuunApp
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.RuuunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { RuuunRoot() }
    }
}

@Composable
private fun RuuunRoot() {
    RuuunTheme {
        Surface(
            color = Ground,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            RuuunApp()
        }
    }
}
