package app.ruuun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.ruuun.content.Achievements
import app.ruuun.content.BadgeCategory
import app.ruuun.content.BadgeDef
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsSnapshot
import app.ruuun.ui.AppState
import app.ruuun.ui.Mascot
import app.ruuun.ui.components.BadgeCell
import app.ruuun.ui.components.BadgeDetailSheet
import app.ruuun.ui.components.BlueHeader
import app.ruuun.ui.components.Chunky
import app.ruuun.ui.components.FoxCircle
import app.ruuun.ui.components.Gutter
import app.ruuun.ui.components.HeaderChip
import app.ruuun.ui.components.HeaderTitleRow
import app.ruuun.ui.components.Medal
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.SpeechCard
import app.ruuun.ui.components.ToggleChip
import app.ruuun.ui.earnedColor
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Link
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.SkyDeep
import app.ruuun.ui.theme.SkyShade
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor

private const val COLUMNS = 3

/** Badges shown per category before "See all". */
private const val PREVIEW = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(state: AppState) {
    val stats = state.stats
    var filter by remember { mutableStateOf<BadgeCategory?>(null) }
    var selected by remember { mutableStateOf<BadgeDef?>(null) }
    // Categories opened with "See all", by name so the choice survives rotation.
    var opened by rememberSaveable { mutableStateOf(setOf<String>()) }

    val categories = BadgeCategory.entries.filter { category ->
        Achievements.BY_CATEGORY[category]?.isNotEmpty() == true
    }
    val visible = categories.filter { filter == null || it == filter }
    val earned = stats.earnedBadgeIds.size
    val total = Achievements.ALL.size
    val next = JourneyView.upcomingBadge(stats)

    LazyColumn(Modifier.fillMaxSize().background(Ground)) {
        item {
            BlueHeader {
                HeaderTitleRow("Badges") {
                    HeaderChip(
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "$earned of $total badges earned"
                        },
                    ) {
                        Icon(
                            Icons.Rounded.MilitaryTech,
                            contentDescription = null,
                            tint = app.ruuun.ui.theme.OrangeShade,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("$earned of $total", style = RuuunText.BodyStrong, color = Navy)
                    }
                }
                Spacer(Modifier.height(12.dp))
                RuuunProgressBar(
                    fraction = earned.toFloat() / total,
                    color = SurfaceColor,
                    trackColor = SkyShade,
                    height = 12.dp,
                    description = null,
                )
                Spacer(Modifier.height(14.dp))
                // Ruu shows off a medal and names the closest badge. Decorative: the same
                // facts are in the card and grid below.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FoxCircle(pose = Mascot.BADGE_PROUD, size = 104.dp, zoom = 1.35f)
                    Spacer(Modifier.width(12.dp))
                    SpeechCard(Modifier.weight(1f)) {
                        Text(
                            when {
                                earned == 0 -> "Your first badge is one run away."
                                next != null -> "$earned so far! Closest next: ${next.title}."
                                else -> "$earned so far. What a collection!"
                            },
                            style = RuuunText.BodyStrong,
                            color = Navy,
                        )
                    }
                }
                if (next != null) {
                    Spacer(Modifier.height(14.dp))
                    ClosestBadgeCard(next, stats) { selected = next }
                }
            }
        }

        item {
            val chipScroll = rememberScrollState()
            Row(
                Modifier
                    .fadedEdges(
                        start = chipScroll.value > 0,
                        end = chipScroll.value < chipScroll.maxValue,
                    )
                    .horizontalScroll(chipScroll)
                    .padding(horizontal = Gutter),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ToggleChip(text = "All", selected = filter == null, onToggle = { filter = null })
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
            val showAll = filter == category || category.name in opened
            item(key = "header-$category") {
                Row(
                    Modifier.padding(start = Gutter, end = Gutter, top = 22.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(category.earnedColor()),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        category.title,
                        style = RuuunText.CardTitle,
                        color = Navy,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                    )
                    Text("$earnedCount of ${badges.size}", style = RuuunText.Label, color = TextMuted)
                }
            }
            // Earned first, then locked, each by threshold. Rows of three keep the grid
            // lazy, so only the visible cells are ever composed.
            val sorted = badges.sortedWith(
                compareByDescending<BadgeDef> { it.id in stats.earnedBadgeIds }.thenBy { it.threshold },
            )
            val rows = (if (showAll) sorted else sorted.take(PREVIEW)).chunked(COLUMNS)
            items(items = rows, key = { row -> "row-" + row.first().id }) { row ->
                Row(
                    Modifier
                        .padding(horizontal = Gutter)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { badge ->
                        val isEarned = badge.id in stats.earnedBadgeIds
                        BadgeCell(
                            badge = badge,
                            earned = isEarned,
                            progressText = progressLabel(badge, isEarned, stats),
                            progressFraction = BadgeEvaluator.progressFraction(badge, stats),
                            onClick = { selected = badge },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (badges.size > PREVIEW && filter != category) {
                item(key = "more-$category") {
                    SeeAllLink(
                        text = if (showAll) "Show fewer" else "Show ${badges.size - PREVIEW} more",
                        onClick = {
                            opened = if (showAll) opened - category.name else opened + category.name
                        },
                    )
                }
            }
        }

        item { Spacer(Modifier.height(Dimens.ScreenPadding)) }
    }

    val sheetBadge = selected
    if (sheetBadge != null) {
        BadgeDetailSheet(badge = sheetBadge, stats = stats, onDismiss = { selected = null })
    }
}

/**
 * Fades a sideways-scrolling row out at whichever edge has more to see, so a chip cut by
 * the screen edge reads as "scroll for more" rather than a broken chip.
 */
private fun Modifier.fadedEdges(start: Boolean, end: Boolean): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fade = 36.dp.toPx()
            if (start) {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = 0f, endX = fade),
                    size = Size(fade, size.height),
                    blendMode = BlendMode.DstIn,
                )
            }
            if (end) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Black, Color.Transparent),
                        startX = size.width - fade,
                        endX = size.width,
                    ),
                    topLeft = Offset(size.width - fade, 0f),
                    size = Size(fade, size.height),
                    blendMode = BlendMode.DstIn,
                )
            }
        }

/** The badge closest to being earned, on the header, with its progress. */
@Composable
private fun ClosestBadgeCard(badge: BadgeDef, stats: StatsSnapshot, onClick: () -> Unit) {
    Chunky(
        color = SurfaceColor,
        shade = SkyShade,
        border = null,
        shape = RoundedCornerShape(20.dp),
        depth = 5.dp,
        onClick = onClick,
        onClickLabel = "Show the badge",
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Medal(badge, earned = false, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(badge.title, style = RuuunText.BodyStrong, color = Navy, modifier = Modifier.weight(1f))
                    Text(
                        "${BadgeEvaluator.progress(badge, stats)}/${badge.threshold}",
                        style = RuuunText.Label,
                        color = SkyDeep,
                    )
                }
                Spacer(Modifier.height(6.dp))
                RuuunProgressBar(
                    fraction = BadgeEvaluator.progressFraction(badge, stats),
                    color = Orange,
                    description = "${BadgeEvaluator.progress(badge, stats)} of ${badge.threshold} ${badge.unit}",
                )
                Spacer(Modifier.height(4.dp))
                Text(badge.hint, style = RuuunText.Label, color = TextMuted)
            }
        }
    }
}

/** "See all 20 >" under a category. */
@Composable
private fun SeeAllLink(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.Center,
    ) {
        Chunky(
            onClick = onClick,
            color = Ground,
            shade = Ground,
            border = null,
            depth = 0.dp,
            role = Role.Button,
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                Modifier
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text, style = RuuunText.BodyStrong, color = Link)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Link, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun progressLabel(
    badge: BadgeDef,
    earned: Boolean,
    stats: StatsSnapshot,
): String = when {
    earned -> "Earned"
    badge.secret -> "Locked"
    else -> "${BadgeEvaluator.progress(badge, stats)}/${badge.threshold}"
}
