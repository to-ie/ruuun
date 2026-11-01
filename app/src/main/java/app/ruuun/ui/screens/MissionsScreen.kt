package app.ruuun.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.WeekCalculator
import app.ruuun.ui.AppState
import app.ruuun.ui.bannerColor
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.QuestRow
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.WorldCard
import app.ruuun.ui.components.decorative
import app.ruuun.ui.icon
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.InfoBg
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.SegmentBg
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor
import java.time.LocalDate

private const val WEEKS_AHEAD = 2

@Composable
fun MissionsScreen(
    state: AppState,
    today: LocalDate,
    onMakeEasier: () -> Unit,
    onMakeHarder: () -> Unit,
) {
    val settings = state.settings ?: return
    val stats = state.stats
    val thisWeek = JourneyView.thisWeek(state.logs, settings, today)
    val chapter = JourneyView.chapterProgress(stats, settings)
    val reachable = JourneyView.badgesWithinReach(stats)
    val currentJourneyWeek = WeekCalculator.journeyWeek(settings.onboardedDate, today)
        .coerceAtLeast(1)

    var showAllWeeks by remember { mutableStateOf(false) }

    val weeksToShow = if (showAllWeeks) {
        maxOf(52, currentJourneyWeek + WEEKS_AHEAD)
    } else {
        currentJourneyWeek + WEEKS_AHEAD
    }
    val weeks = JourneyView.journeyWeeks(state.logs, settings, weeksToShow)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Missions")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.ScreenPadding),
        ) {
            item {
            Surface(shape = Shapes.Hero, color = Navy, modifier = Modifier.fillMaxWidth()) {
                Box {
                    Box(
                        Modifier
                            .size(110.dp)
                            .offset(x = 260.dp, y = (-30).dp)
                            .clip(Shapes.Pill)
                            .background(Sky.copy(alpha = 0.16f))
                            .decorative(),
                    )
                    Row(
                        Modifier.padding(Dimens.HeroPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Overline("Next target", color = Sky)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                Formatters.distance(settings.currentTargetM),
                                style = RuuunText.BigNumber,
                                color = Ground,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "The distance to aim for on your next run.",
                                style = RuuunText.Body,
                                color = Ground,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SecondaryButton("Easier", onMakeEasier, onNavy = true)
                            SecondaryButton("Harder", onMakeHarder, onNavy = true)
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            RuuunCard {
                Overline("Week ${thisWeek.journeyWeek} · ${thisWeek.theme.flavour}")
                Spacer(Modifier.height(4.dp))
                Text(thisWeek.theme.title, style = RuuunText.CardTitle, color = Navy)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Two things to tick off. They reset every Monday.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
                Spacer(Modifier.height(12.dp))
                QuestRow(
                    label = "Main quest",
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
                progressLabel = if (chapter.isFinale) {
                    "Every chapter complete"
                } else {
                    "${chapter.runDays} / ${chapter.runDaysNeeded} days"
                },
            )

            if (reachable.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.SectionGap))
                Text("Closest badges", style = RuuunText.SectionTitle, color = Navy)
                Spacer(Modifier.height(2.dp))
                Text(
                    "The ones you are nearest to earning.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
                Spacer(Modifier.height(10.dp))
                reachable.forEach { badge ->
                    RuuunCard(modifier = Modifier.padding(bottom = Dimens.ItemGap)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                badge.title,
                                style = RuuunText.BodyStrong,
                                color = Navy,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${BadgeEvaluator.progress(badge, stats)}/${badge.threshold} " +
                                    badge.unit,
                                style = RuuunText.Label,
                                color = TextMuted,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        RuuunProgressBar(
                            fraction = BadgeEvaluator.progressFraction(badge, stats),
                            color = Orange,
                            description = badge.hint,
                        )
                    }
                }
            }

                Spacer(Modifier.height(Dimens.SectionGap))
                Text("Your 52 weeks", style = RuuunText.SectionTitle, color = Navy)
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
