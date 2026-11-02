package app.ruuun.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ruuun.content.BadgeDef
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.StatsSnapshot
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor

/**
 * What a badge is and how close you are. Shared by the badge cabinet and the
 * celebration after a save, so a badge explains itself wherever it is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeDetailSheet(
    badge: BadgeDef,
    stats: StatsSnapshot,
    onDismiss: () -> Unit,
) {
    val earned = badge.id in stats.earnedBadgeIds
    val hidden = badge.secret && !earned
    val current = BadgeEvaluator.progress(badge, stats)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = SurfaceColor,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BadgeCell(
                badge = badge,
                earned = earned,
                progressText = null,
                modifier = Modifier.width(150.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (hidden) "Secret badge" else badge.title,
                style = RuuunText.SubTitle,
                color = Navy,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Overline(badge.category.title)

            Spacer(Modifier.height(14.dp))
            Text(
                text = when {
                    hidden -> "This one stays hidden until you find it. Keep running."
                    earned -> "Earned for this: ${badge.hint.replaceFirstChar { it.lowercase() }}"
                    else -> "How to earn it: ${badge.hint.replaceFirstChar { it.lowercase() }}"
                },
                style = RuuunText.BodyLarge,
                color = Navy,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(18.dp))
            Pill(
                text = if (earned) "Earned" else "Not yet",
                background = if (earned) Lime else Track,
            )

            if (!hidden && !earned && badge.threshold > 1) {
                Spacer(Modifier.height(18.dp))
                Text(
                    "$current of ${badge.threshold} ${badge.unit}",
                    style = RuuunText.BodyStrong,
                    color = TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                RuuunProgressBar(
                    fraction = BadgeEvaluator.progressFraction(badge, stats),
                    color = Orange,
                    description = "$current of ${badge.threshold} ${badge.unit}",
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "${badge.threshold - current} ${badge.unit} to go.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
            }

            if (earned) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Badges are worked out from your runs, so if a run is corrected " +
                        "or deleted this can change.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
