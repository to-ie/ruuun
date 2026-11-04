package app.ruuun.ui.screens

import androidx.compose.foundation.layout.statusBarsPadding
import app.ruuun.ui.components.SubScreenHeader
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import app.ruuun.ui.components.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ruuun.logic.Formatters
import app.ruuun.ui.AppState
import app.ruuun.ui.RuuunViewModel
import app.ruuun.ui.components.EffortPill
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Destructive
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor

/** Copy shown whenever data that feeds the badges is about to change. */
const val RECOMPUTE_WARNING =
    "Totals, level and badges are worked out from your runs, so they may change."

@Composable
fun RunDetailScreen(
    state: AppState,
    viewModel: RuuunViewModel,
    runId: String?,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val log = remember(runId, state.logs) { state.logs.firstOrNull { it.id == runId } }
    var confirmDelete by remember { mutableStateOf(false) }

    if (log == null) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(Dimens.ScreenPadding)) {
            Text("That run is no longer here.", style = RuuunText.BodyLarge, color = Navy)
            Spacer(Modifier.height(16.dp))
            PrimaryCta("Back", onBack)
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        SubScreenHeader(title = "Your run", onBack = onBack)

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
        ) {
            RuuunCard {
                Overline(Formatters.dayMonth(log.date))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Formatters.distance(log.distanceM),
                        style = RuuunText.CardTitle,
                        color = Navy,
                        modifier = Modifier.weight(1f),
                    )
                    EffortPill(log.effort)
                }
                Spacer(Modifier.height(12.dp))
                DetailLine("Mission at the time", Formatters.mission(log.targetMAtLog))
                log.stops?.let {
                    DetailLine("Stops", if (it == 0) "Non-stop" else "$it")
                }
                log.stopsGoalAtLog?.let {
                    DetailLine("Stop goal at the time", app.ruuun.logic.StopGoal.goalLabel(it))
                }
                log.durationMinutes?.let { DetailLine("Minutes", "$it") }
                log.mode?.let { DetailLine("Walk or jog", modeLabel(it)) }
                if (log.tags.isNotEmpty()) {
                    DetailLine("Tags", log.tags.joinToString(", ") { tagLabel(it) })
                }
                if (!log.note.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Overline("A tiny memory")
                    Spacer(Modifier.height(4.dp))
                    Text(log.note, style = RuuunText.Body, color = Navy)
                }
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            Text(RECOMPUTE_WARNING, style = RuuunText.Secondary, color = TextMuted)

            Spacer(Modifier.height(Dimens.SectionGap))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                SecondaryButton(
                    text = "Edit run",
                    onClick = { onEdit(log.id) },
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    text = "Delete run",
                    onClick = { confirmDelete = true },
                    lowEmphasis = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = SurfaceColor,
            title = { Text("Delete this run?", style = RuuunText.SubTitle, color = Navy) },
            text = { Text(RECOMPUTE_WARNING, style = RuuunText.Body, color = Navy) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteRun(log, onBack)
                }) {
                    Text("Delete run", color = Destructive, style = RuuunText.BodyStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Keep it", color = Navy, style = RuuunText.BodyStrong)
                }
            },
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = RuuunText.Body, color = TextMuted, modifier = Modifier.weight(1f))
        Text(value, style = RuuunText.BodyStrong, color = Navy)
    }
}

private fun modeLabel(mode: app.ruuun.data.RunMode): String = when (mode) {
    app.ruuun.data.RunMode.WALK_ONLY -> "Walk"
    app.ruuun.data.RunMode.WALK_JOG -> "Walk + jog"
    app.ruuun.data.RunMode.MOSTLY_JOG -> "Mostly jog"
}

private fun tagLabel(tag: app.ruuun.data.RunTag): String = when (tag) {
    app.ruuun.data.RunTag.PARK -> "Park"
    app.ruuun.data.RunTag.FAMILY -> "Family"
    app.ruuun.data.RunTag.NEW_ROUTE -> "New route"
    app.ruuun.data.RunTag.NATURE -> "Nature"
    app.ruuun.data.RunTag.FRIEND -> "With a friend"
    app.ruuun.data.RunTag.TRAIL -> "Trail"
    app.ruuun.data.RunTag.HILLS -> "Hills"
    app.ruuun.data.RunTag.BEACH -> "Beach"
}
