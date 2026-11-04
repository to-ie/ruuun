package app.ruuun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.ruuun.update.UpdateState
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.HeroTrack
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText

/**
 * The strip across the top of the tabs when a newer version is on GitHub. Says plainly
 * that runs and badges are kept, because an update replaces the app, not the data.
 */
@Composable
fun UpdateBanner(
    state: UpdateState,
    onUpdate: () -> Unit,
    onInstall: () -> Unit,
    onLater: () -> Unit,
) {
    val (title, body) = when (state) {
        UpdateState.None -> return
        is UpdateState.Available ->
            "ruuun ${state.info.versionName} is ready" to "Your runs and badges stay just as they are."
        is UpdateState.Downloading ->
            "Downloading ruuun ${state.info.versionName}" to "${(state.progress * 100).toInt()}% done"
        is UpdateState.Ready ->
            "ruuun ${state.info.versionName} is downloaded" to "Tap Install, then confirm on the next screen."
        is UpdateState.Failed ->
            "The update did not download" to "Check the internet and try again."
    }

    // A navy card on the header blue, which reaches up behind the status bar.
    Box(
        Modifier
            .fillMaxWidth()
            .background(app.ruuun.ui.theme.Sky)
            .statusBarsPadding()
            .padding(start = Gutter, end = Gutter, top = 10.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(app.ruuun.ui.theme.Shapes.Card)
                .background(Navy)
                .padding(16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Text(title, style = RuuunText.BodyStrong, color = Ground)
            Spacer(Modifier.height(2.dp))
            Text(body, style = RuuunText.Secondary, color = Ground)
            Spacer(Modifier.height(12.dp))
            if (state is UpdateState.Downloading) {
                RuuunProgressBar(
                    fraction = state.progress,
                    color = Lime,
                    trackColor = HeroTrack,
                    description = "Download progress",
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryCta(
                        text = when (state) {
                            is UpdateState.Ready -> "Install"
                            is UpdateState.Failed -> "Try again"
                            else -> "Update"
                        },
                        onClick = if (state is UpdateState.Ready) onInstall else onUpdate,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        text = "Later",
                        onClick = onLater,
                        onNavy = true,
                        large = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
