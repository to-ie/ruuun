package app.ruuun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import app.ruuun.data.RunLog
import app.ruuun.logic.StopGoal
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.StopBlockIdle

/** Most blocks drawn in a row; more show as "+N". */
private const val MAX_BLOCKS = 12

/**
 * Stops as blocks, one per stop: the first counted run against the latest. Fewer blocks
 * is the story. Nothing shows until stops have been counted.
 */
@Composable
fun StopsTrendCard(logs: List<RunLog>, stopGoal: Int?) {
    val counted = StopGoal.countedDays(logs)
    if (counted.isEmpty()) return
    val first = counted.first().stops ?: 0
    val latest = counted.last().stops ?: 0
    val single = counted.size == 1
    val goalText = stopGoal?.let { " Stop goal: ${StopGoal.goalLabel(it).lowercase()}." } ?: ""
    val description = if (single) {
        "Stops: ${StopGoal.stopsLabel(first)} on your first count.$goalText"
    } else {
        "Stops: $first on your first counted run, $latest on the latest.$goalText"
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Navy)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (single) "Stops: $first" else "Stops: $first → $latest",
                    style = RuuunText.CardTitle,
                    color = Ground,
                    modifier = Modifier.weight(1f),
                )
                val change = when {
                    single -> null
                    latest < first -> "${first - latest} fewer!"
                    latest == first -> "Holding steady"
                    else -> null
                }
                if (change != null) Text(change, style = RuuunText.BodyStrong, color = Lime)
            }
            Spacer(Modifier.height(12.dp))
            BlockRow(if (single) "First count" else "First run", first, StopBlockIdle)
            if (!single) {
                Spacer(Modifier.height(8.dp))
                BlockRow("Latest run", latest, Orange)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                (if (single) "Count them again next run to compare." else "Fewer blocks mean fewer stops.") + goalText,
                style = RuuunText.Label,
                color = Ground,
            )
        }
    }
}

@Composable
private fun BlockRow(label: String, stops: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = RuuunText.Tiny, color = Ground, modifier = Modifier.width(76.dp))
        if (stops == 0) {
            Text("Non-stop!", style = RuuunText.BodyStrong, color = Lime)
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                repeat(minOf(stops, MAX_BLOCKS)) {
                    Box(
                        Modifier
                            .weight(1f, fill = false)
                            .size(width = 18.dp, height = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color),
                    )
                }
                if (stops > MAX_BLOCKS) {
                    Text("+${stops - MAX_BLOCKS}", style = RuuunText.Tiny, color = Ground)
                }
            }
        }
    }
}
