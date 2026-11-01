package app.ruuun.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ruuun.data.RunLog
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.ui.AppState
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.RunRow
import app.ruuun.ui.components.StatTile
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.TextMuted
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

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("History")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.ScreenPadding),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                    StatTile(
                        value = "${Formatters.km(stats.totalDistanceM)} km",
                        label = "All-time distance",
                        color = Navy,
                        contentColor = Ground,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile("${stats.outingDays}", "Outing days", Modifier.weight(1f))
                }
                Spacer(Modifier.height(Dimens.ItemGap))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                    StatTile(
                        Formatters.distance(stats.longestOutingM),
                        "Longest outing",
                        Modifier.weight(1f),
                    )
                    StatTile(
                        "${stats.daysOutThisMonth}",
                        "Days out in ${Formatters.monthName(today)}",
                        Modifier.weight(1f),
                    )
                }
            }

            if (state.logs.isEmpty()) {
                item {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        "No runs yet. Your first one will show up here.",
                        style = RuuunText.BodyLarge,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryCta("Log a run", onLogRun, icon = Icons.Outlined.Add)
                }
            }

            grouped.forEach { (month, logs) ->
                item(key = "month-$month") {
                    Spacer(Modifier.height(Dimens.SectionGap))
                    Overline(Formatters.monthYear(month.atDay(1)))
                    Spacer(Modifier.height(10.dp))
                }
                items(items = logs, key = { it.id }) { log ->
                    RunRow(log = log, onClick = { onOpenRun(log.id) })
                    Spacer(Modifier.height(Dimens.ItemGap))
                }
            }

            item { Spacer(Modifier.height(Dimens.ScreenPadding)) }
        }
    }
}
