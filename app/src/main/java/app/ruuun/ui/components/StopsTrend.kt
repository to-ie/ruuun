package app.ruuun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ruuun.data.RunLog
import app.ruuun.logic.StopGoal
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.TextMuted

/** How many counted days the chart shows. */
private const val RUNS_SHOWN = 10
private val CHART_HEIGHT = 96.dp
private val ZERO_MARK = 4.dp

/**
 * Stops on the last few counted runs, as bars. Shorter is better, and the story is
 * the first count against the latest one. Nothing shows until stops have been counted.
 */
@Composable
fun StopsTrendCard(logs: List<RunLog>, stopGoal: Int?) {
    val counted = StopGoal.countedDays(logs)
    if (counted.isEmpty()) return
    val shown = counted.takeLast(RUNS_SHOWN)
    val first = counted.first().stops ?: 0
    val latest = counted.last().stops ?: 0

    RuuunCard {
        Overline("Stops")
        Spacer(Modifier.height(4.dp))
        Text(
            if (counted.size == 1) {
                "${StopGoal.stopsLabel(first)} on your first count"
            } else {
                "From $first to $latest"
            },
            style = RuuunText.CardTitle,
            color = Navy,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            when {
                counted.size == 1 -> "Count them again next run to start your chart."
                latest < first -> "${first - latest} fewer than your first count. Shorter bars mean fewer stops."
                else -> "Shorter bars mean fewer stops. Breaks are fine; this is just to see the change."
            } + (stopGoal?.let { " Stop goal: ${StopGoal.goalLabel(it).lowercase()}." } ?: ""),
            style = RuuunText.Secondary,
            color = TextMuted,
        )
        Spacer(Modifier.height(14.dp))
        StopsBars(shown)
    }
}

@Composable
private fun StopsBars(runs: List<RunLog>) {
    val values = runs.map { it.stops ?: 0 }
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val description = "Stops on your last ${values.size} counted days, oldest first: " +
        values.joinToString(", ")

    Column(Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = description }) {
        Row(
            Modifier.fillMaxWidth().height(CHART_HEIGHT + 22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, stops ->
                val labelled = index == 0 || index == values.lastIndex
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (labelled) {
                        Text(
                            "$stops",
                            style = RuuunText.Pill,
                            color = Navy,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    val height = if (stops == 0) ZERO_MARK else CHART_HEIGHT * (stops.toFloat() / max)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(height)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (stops == 0) TextMuted else Navy),
                    )
                }
            }
            // Empty slots keep each bar the same width however few runs there are.
            repeat(RUNS_SHOWN - values.size) { Spacer(Modifier.weight(1f)) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
    }
}
