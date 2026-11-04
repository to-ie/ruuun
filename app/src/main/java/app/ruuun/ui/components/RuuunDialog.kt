package app.ruuun.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.ruuun.ui.theme.SkyShade
import app.ruuun.ui.theme.Surface as SurfaceColor

/**
 * The app's dialog: a white chunky card with a solid blue edge and outline, so it stands
 * out from the dimmed screen behind it (red when the choice removes something). Takes the same slots as Material's AlertDialog,
 * so swapping one for the other is a rename. [containerColor] is accepted and ignored.
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") containerColor: Color = SurfaceColor,
    paneTitle: String = "Dialog",
    /** For a choice that removes something: the frame turns red. */
    danger: Boolean = false,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Chunky(
            color = SurfaceColor,
            shade = if (danger) app.ruuun.ui.theme.Destructive else SkyShade,
            border = if (danger) app.ruuun.ui.theme.DangerFrame else app.ruuun.ui.theme.Sky,
            borderWidth = 3.dp,
            shape = RoundedCornerShape(26.dp),
            depth = 7.dp,
            modifier = modifier
                .fillMaxWidth()
                .semantics { this.paneTitle = paneTitle },
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
            ) {
                if (title != null) {
                    Box(Modifier.padding(end = 8.dp)) { title() }
                    Spacer(Modifier.height(12.dp))
                }
                if (text != null) {
                    Box(Modifier.padding(end = 8.dp)) { text() }
                    Spacer(Modifier.height(16.dp))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(Modifier.width(4.dp))
                    }
                    confirmButton()
                }
            }
        }
    }
}
