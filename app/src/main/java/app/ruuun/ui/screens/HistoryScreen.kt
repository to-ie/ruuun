package app.ruuun.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Route
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ruuun.data.RunLog
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.ui.AppState
import app.ruuun.ui.Mascot
import app.ruuun.ui.components.BlueHeader
import app.ruuun.ui.components.Gutter
import app.ruuun.ui.components.HeaderTitleRow
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.RuuImage
import app.ruuun.ui.components.RunRow
import app.ruuun.ui.components.SpeechBubble
import app.ruuun.ui.components.StatTile
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.OrangeShade
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HistoryScreen(
    state: AppState,
    today: LocalDate,
    onOpenRun: (String) -> Unit,
    onLogRun: () -> Unit,
) {
    val stats = JourneyView.historyStats(state.logs, today)
    // Newest month first, newest run first inside it. Rows are keyed so a 150-run
    // history scrolls without rebuilding anything.
    val grouped = remember(state.logs) {
        state.logs
            .sortedWith(compareByDescending<RunLog> { it.date }.thenByDescending { it.createdAt })
            .groupBy { YearMonth.from(it.date) }
            .toList()
            .sortedByDescending { (month, _) -> month }
    }

    LazyColumn(Modifier.fillMaxSize().background(Ground)) {
        item {
            BlueHeader {
                HeaderTitleRow("History")
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap),
                ) {
                    StatTile(
                        value = "${Formatters.km(stats.totalDistanceM)} km",
                        label = "All-time distance",
                        color = Orange,
                        shade = OrangeShade,
                        icon = Icons.Rounded.Route,
                        iconTint = Navy,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatTile(
                        "${stats.runDays}",
                        "Run days",
                        icon = Icons.Rounded.CalendarMonth,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                Spacer(Modifier.height(Dimens.ItemGap))
                Row(
                    Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap),
                ) {
                    StatTile(
                        Formatters.distance(stats.longestRunM),
                        "Longest run",
                        icon = Icons.Rounded.EmojiEvents,
                        iconTint = OrangeShade,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatTile(
                        "${stats.daysOutThisMonth}",
                        "Days out in ${Formatters.monthName(today)}",
                        icon = Icons.Rounded.Park,
                        iconTint = app.ruuun.ui.theme.LimeText,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }

        if (state.logs.isEmpty()) {
            item {
                Column(Modifier.padding(horizontal = Gutter)) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RuuImage(pose = Mascot.NOTEBOOK, height = 120.dp)
                        SpeechBubble(
                            "No runs yet. Your first one will show up here.",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    PrimaryCta("Log a run", onLogRun, icon = Icons.Rounded.Add)
                }
            }
        }

        grouped.forEach { (month, logs) ->
            item(key = "month-$month") {
                Column(Modifier.padding(horizontal = Gutter)) {
                    Spacer(Modifier.height(6.dp))
                    Overline(Formatters.monthYear(month.atDay(1)))
                    Spacer(Modifier.height(10.dp))
                }
            }
            items(items = logs, key = { it.id }) { log ->
                Column(Modifier.padding(horizontal = Gutter)) {
                    RunRow(log = log, onClick = { onOpenRun(log.id) })
                    Spacer(Modifier.height(Dimens.ItemGap))
                }
            }
        }

        item { Spacer(Modifier.height(Dimens.ScreenPadding)) }
    }
}
