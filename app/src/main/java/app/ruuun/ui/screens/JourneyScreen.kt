package app.ruuun.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ruuun.content.Chapters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StopGoal
import app.ruuun.ui.components.StopsTrendCard
import app.ruuun.logic.WeekCalculator
import app.ruuun.ui.AppState
import app.ruuun.ui.bannerColor
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.QuestRow
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.WorldCard
import app.ruuun.ui.components.WorldJourneyCard
import app.ruuun.ui.icon
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.InfoBg
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.SegmentBg
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor
import java.time.LocalDate

private const val WEEKS_AHEAD = 2

/** Twenty years of weeks. Far past any real journey, and a hard stop on bad data. */
private const val MAX_JOURNEY_WEEKS = 1_040

/**
 * The Journey tab, ordered by time span: this week, then the story, then every week.
 * The next mission lives on Home only, and badges on the Badges tab.
 */
@Composable
fun JourneyScreen(
    state: AppState,
    today: LocalDate,
) {
    val settings = state.settings ?: return
    val stats = state.stats
    val thisWeek = JourneyView.thisWeek(state.logs, settings, today)
    val chapter = JourneyView.chapterProgress(stats, settings)
    val journey = remember(stats, settings) { JourneyView.journeyMap(stats, settings) }
    // Clamped: a corrupted start date must not ask for thousands of rows.
    val currentJourneyWeek = WeekCalculator.journeyWeek(settings.onboardedDate, today)
        .coerceIn(1, MAX_JOURNEY_WEEKS)

    var showAllWeeks by remember { mutableStateOf(false) }

    val weeksToShow = if (showAllWeeks) {
        maxOf(52, currentJourneyWeek + WEEKS_AHEAD)
    } else {
        currentJourneyWeek + WEEKS_AHEAD
    }.coerceAtMost(MAX_JOURNEY_WEEKS)
    val weeks = remember(state.logs, settings, weeksToShow) {
        JourneyView.journeyWeeks(state.logs, settings, weeksToShow)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Journey")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.ScreenPadding),
        ) {
            item {
                RuuunCard {
                    Overline("Week ${thisWeek.journeyWeek} · ${thisWeek.theme.flavour}")
                    Spacer(Modifier.height(4.dp))
                    Text(thisWeek.theme.title, style = RuuunText.CardTitle, color = Navy)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your weekly goal, plus an optional side quest. They reset every " +
                            "Monday, and missing a week costs you nothing.",
                        style = RuuunText.Secondary,
                        color = TextMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                    QuestRow(
                        label = "Weekly goal",
                        description = "Run on ${thisWeek.weeklyGoal} days this week",
                        progressText = "${thisWeek.runDays}/${thisWeek.weeklyGoal}",
                        done = thisWeek.mainQuestMet,
                    )
                    Spacer(Modifier.height(Dimens.ItemGap))
                    QuestRow(
                        label = "Side quest (optional)",
                        description = thisWeek.theme.sideQuest.description,
                        progressText = if (thisWeek.sideQuestMet) "1/1" else "0/1",
                        done = thisWeek.sideQuestMet,
                    )
                }

                if (state.logs.any { it.stops != null }) {
                    Spacer(Modifier.height(Dimens.SectionGap))
                    StopsTrendCard(
                        logs = state.logs,
                        stopGoal = remember(state.logs, settings) {
                            StopGoal.next(state.logs, settings)
                        },
                    )
                }

                Spacer(Modifier.height(Dimens.SectionGap))
                WorldCard(
                    bannerColor = chapter.chapter?.world?.bannerColor() ?: Lime,
                    bannerIcon = chapter.chapter?.world?.icon() ?: Icons.Outlined.Flag,
                    worldName = chapter.worldTitle,
                    title = if (chapter.isFinale) {
                        chapter.title
                    } else {
                        "Chapter ${chapter.chapter?.number} · ${chapter.title}"
                    },
                    teaser = chapter.teaser,
                    progressFraction = chapter.fraction,
                    progressLabel = chapter.progressLabel,
                    animateProgress = true,
                )

                Spacer(Modifier.height(Dimens.SectionGap))
                Text("The whole journey", style = RuuunText.SectionTitle, color = Navy)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Two seasons, eight worlds, 24 chapters. Every day you run moves the story " +
                        "on, whatever the distance. Finished worlds fold up: tap one to look back.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
                Spacer(Modifier.height(10.dp))
                journey.groupBy { it.world.season }.forEach { (season, worlds) ->
                    Overline(
                        when {
                            season == 1 -> "Season 1"
                            stats.completedChapters >= Chapters.SEASON_ONE_CHAPTERS -> "Season $season"
                            else -> "Season $season · opens after the festival"
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                    worlds.forEach { world ->
                        WorldJourneyCard(world)
                        Spacer(Modifier.height(Dimens.ItemGap))
                    }
                    Spacer(Modifier.height(Dimens.ItemGap))
                }

                Spacer(Modifier.height(Dimens.SectionGap))
                Text("Week by week", style = RuuunText.SectionTitle, color = Navy)
                Spacer(Modifier.height(2.dp))
                Text(
                    "A new theme every week. Nothing expires.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
                Spacer(Modifier.height(10.dp))
            }

            items(items = weeks, key = { it.journeyWeek }) { week ->
                WeekRow(
                    week = week,
                    isCurrent = week.journeyWeek == currentJourneyWeek,
                    isPast = week.journeyWeek < currentJourneyWeek,
                )
                Spacer(Modifier.height(Dimens.ItemGap))
            }

            item {
                if (!showAllWeeks) {
                    SecondaryButton(
                        text = "See all 52 weeks",
                        onClick = { showAllWeeks = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(Dimens.ScreenPadding))
            }
        }
    }
}

@Composable
private fun WeekRow(
    week: WeekCalculator.WeekProgress,
    isCurrent: Boolean,
    isPast: Boolean,
) {
    val status = when {
        isCurrent -> "Now · ${week.tasksDone}/2"
        isPast -> "${week.tasksDone}/2 done"
        else -> "Coming up"
    }
    Surface(
        shape = Shapes.Card,
        color = if (isCurrent) InfoBg else SurfaceColor,
        border = BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) Navy else Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(Shapes.Segment)
                    .background(
                        when {
                            isCurrent -> Navy
                            isPast -> Lime
                            else -> SegmentBg
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${week.journeyWeek}",
                    style = RuuunText.Label,
                    color = if (isCurrent) Ground else Navy,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(week.theme.title, style = RuuunText.BodyStrong, color = Navy)
                Text(
                    "Side quest: ${week.theme.sideQuest.description}",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(status, style = RuuunText.Label, color = TextMuted)
        }
    }
}

/** Plain white screen header with a bottom hairline. */
@Composable
fun ScreenHeader(title: String, trailing: @Composable (() -> Unit)? = null) {
    Surface(color = SurfaceColor, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = RuuunText.ScreenTitle,
                    color = Navy,
                    modifier = Modifier.weight(1f),
                )
                if (trailing != null) trailing()
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
        }
    }
}
