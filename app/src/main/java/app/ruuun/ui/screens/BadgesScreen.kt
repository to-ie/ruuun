package app.ruuun.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ruuun.content.Achievements
import app.ruuun.content.BadgeCategory
import app.ruuun.content.BadgeDef
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.ui.AppState
import app.ruuun.ui.components.BadgeCell
import app.ruuun.ui.components.BadgeDetailSheet
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.ToggleChip
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.TextMuted

private const val COLUMNS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(state: AppState) {
    val stats = state.stats
    var filter by remember { mutableStateOf<BadgeCategory?>(null) }
    var selected by remember { mutableStateOf<BadgeDef?>(null) }

    val categories = BadgeCategory.entries.filter { category ->
        Achievements.BY_CATEGORY[category]?.isNotEmpty() == true
    }
    val visible = categories.filter { filter == null || it == filter }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Badges") {
            Text(
                "${stats.earnedBadgeIds.size} of ${Achievements.ALL.size}",
                style = RuuunText.BodyStrong,
                color = TextMuted,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Dimens.ScreenPadding,
            ),
        ) {
            item {
                Spacer(Modifier.height(14.dp))
                RuuunProgressBar(
                    fraction = stats.earnedBadgeIds.size.toFloat() / Achievements.ALL.size,
                    color = Orange,
                    description = "${stats.earnedBadgeIds.size} of ${Achievements.ALL.size} badges earned",
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ToggleChip(
                        text = "All",
                        selected = filter == null,
                        onToggle = { filter = null },
                    )
                    categories.forEach { category ->
                        ToggleChip(
                            text = category.title,
                            selected = filter == category,
                            onToggle = { filter = if (filter == category) null else category },
                        )
                    }
                }
            }

            visible.forEach { category ->
                val badges = Achievements.BY_CATEGORY[category].orEmpty()
                val earnedCount = badges.count { it.id in stats.earnedBadgeIds }
                item(key = "header-$category") {
                    Spacer(Modifier.height(Dimens.SectionGap))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            category.title,
                            style = RuuunText.SectionTitle,
                            color = Navy,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "$earnedCount of ${badges.size}",
                            style = RuuunText.Label,
                            color = TextMuted,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                // Earned first, then locked, each by threshold. Rows of three keep the
                // grid lazy, so only the visible cells are ever composed.
                val rows = badges
                    .sortedWith(
                        compareByDescending<BadgeDef> { it.id in stats.earnedBadgeIds }
                            .thenBy { it.threshold },
                    )
                    .chunked(COLUMNS)
                items(items = rows, key = { row -> "row-" + row.first().id }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                        row.forEach { badge ->
                            val earned = badge.id in stats.earnedBadgeIds
                            BadgeCell(
                                badge = badge,
                                earned = earned,
                                progressText = progressLabel(badge, earned, stats),
                                onClick = { selected = badge },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(Dimens.ItemGap))
                }
            }

            item { Spacer(Modifier.height(Dimens.ScreenPadding)) }
        }
    }

    val sheetBadge = selected
    if (sheetBadge != null) {
        BadgeDetailSheet(badge = sheetBadge, stats = stats, onDismiss = { selected = null })
    }
}

private fun progressLabel(
    badge: BadgeDef,
    earned: Boolean,
    stats: app.ruuun.logic.StatsSnapshot,
): String = when {
    earned -> "Earned"
    badge.secret -> "Locked"
    else -> "${BadgeEvaluator.progress(badge, stats)}/${badge.threshold}"
}
