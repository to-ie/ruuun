package app.ruuun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ruuun.content.Chapters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StopGoal
import app.ruuun.logic.WeekCalculator
import app.ruuun.ui.AppState
import app.ruuun.ui.Mascot
import app.ruuun.ui.bannerColor
import app.ruuun.ui.components.BlueHeader
import app.ruuun.ui.components.CheckDot
import app.ruuun.ui.components.Chunky
import app.ruuun.ui.components.Gutter
import app.ruuun.ui.components.HeaderChip
import app.ruuun.ui.components.HeaderTitleRow
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.Pill
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.StopsTrendCard
import app.ruuun.ui.components.WorldCard
import app.ruuun.ui.components.WorldJourneyCard
import app.ruuun.ui.icon
import app.ruuun.ui.ruu
import app.ruuun.ui.scene
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.OrangeShade
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.SkyTint
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import java.time.LocalDate

private const val WEEKS_AHEAD = 2

/** Twenty years of weeks. Far past any real journey, and a hard stop on bad data. */
private const val MAX_JOURNEY_WEEKS = 1_040

/**
 * The Journey tab: the current chapter on the blue header, then this week, stops, the
 * whole story as a path of worlds, and every week. The next mission lives on Home only.
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
    val chapterNumber = chapter.chapter?.number ?: Chapters.ALL.size

    LazyColumn(Modifier.fillMaxSize().background(Ground)) {
        item {
            BlueHeader {
                HeaderTitleRow("Journey") {
                    HeaderChip(
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Chapter $chapterNumber of ${Chapters.ALL.size}"
                        },
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = null, tint = OrangeShade, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$chapterNumber of ${Chapters.ALL.size}", style = RuuunText.BodyStrong, color = Navy)
                    }
                }
                Spacer(Modifier.height(14.dp))
                WorldCard(
                    bannerColor = chapter.chapter?.world?.bannerColor() ?: Lime,
                    bannerIcon = chapter.chapter?.world?.icon() ?: Icons.Rounded.Flag,
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
                    picture = chapter.chapter?.world?.scene() ?: Mascot.FINALE_SCENE,
                    ruu = chapter.chapter?.world?.ruu() ?: Mascot.CHEER,
                    onBlue = true,
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = Gutter)) {
                Overline("Week ${thisWeek.journeyWeek} · ${thisWeek.theme.flavour}")
                Spacer(Modifier.height(2.dp))
                Text(
                    thisWeek.theme.title,
                    style = RuuunText.CardTitle,
                    color = Navy,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap),
                ) {
                    QuestTile(
                        label = "Weekly goal",
                        description = "Run on ${thisWeek.weeklyGoal} days this week",
                        progress = "${thisWeek.runDays.coerceAtMost(thisWeek.weeklyGoal)}/${thisWeek.weeklyGoal}",
                        done = thisWeek.mainQuestMet,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    QuestTile(
                        label = "Side quest",
                        description = thisWeek.theme.sideQuest.description,
                        progress = if (thisWeek.sideQuestMet) "1/1" else "0/1",
                        done = thisWeek.sideQuestMet,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Goals reset every Monday, and missing a week costs you nothing.",
                    style = RuuunText.Label,
                    color = TextMuted,
                )
            }
        }

        if (state.logs.any { it.stops != null }) {
            item {
                Column(Modifier.padding(start = Gutter, end = Gutter, top = Dimens.SectionGap + 4.dp)) {
                    StopsTrendCard(
                        logs = state.logs,
                        stopGoal = remember(state.logs, settings) { StopGoal.next(state.logs, settings) },
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(start = Gutter, end = Gutter, top = 24.dp)) {
                Text(
                    "The whole journey",
                    style = RuuunText.SectionTitle,
                    color = Navy,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Two seasons, eight worlds, 24 chapters. Every day you run moves the story " +
                        "on, whatever the distance. Finished worlds fold up: tap one to look back, " +
                        "and tap a finished chapter to see its picture.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
            }
        }

        journey.groupBy { it.world.season }.forEach { (season, worlds) ->
            val seasonOpen = season == 1 || stats.completedChapters >= Chapters.SEASON_ONE_CHAPTERS
            item(key = "season-$season") {
                Overline(
                    when {
                        season == 1 -> "Season 1"
                        seasonOpen -> "Season $season"
                        else -> "Season $season · opens after the festival"
                    },
                    modifier = Modifier.padding(start = Gutter, end = Gutter, top = 18.dp, bottom = 10.dp),
                )
            }
            items(items = worlds, key = { "world-" + it.world.name }) { world ->
                Column(Modifier.padding(horizontal = Gutter)) {
                    WorldJourneyCard(world, seasonOpen = seasonOpen)
                    Spacer(Modifier.height(Dimens.ItemGap))
                }
            }
        }

        item {
            Column(Modifier.padding(start = Gutter, end = Gutter, top = 22.dp)) {
                Text(
                    "Week by week",
                    style = RuuunText.SectionTitle,
                    color = Navy,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(2.dp))
                Text("A new theme every week. Nothing expires.", style = RuuunText.Secondary, color = TextMuted)
                Spacer(Modifier.height(10.dp))
            }
        }

        items(items = weeks, key = { it.journeyWeek }) { week ->
            Column(Modifier.padding(horizontal = Gutter)) {
                WeekRow(
                    week = week,
                    isCurrent = week.journeyWeek == currentJourneyWeek,
                    isPast = week.journeyWeek < currentJourneyWeek,
                )
                Spacer(Modifier.height(Dimens.ItemGap))
            }
        }

        item {
            Column(Modifier.padding(horizontal = Gutter)) {
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

/** This week's goal or side quest, as a chunky tile with its check. */
@Composable
private fun QuestTile(label: String, description: String, progress: String, done: Boolean, modifier: Modifier) {
    Chunky(modifier = modifier.semantics(mergeDescendants = true) {}, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckDot(done = done, size = 30.dp)
                Spacer(Modifier.weight(1f))
                Text(progress, style = RuuunText.CardTitle, color = Navy)
            }
            Spacer(Modifier.height(8.dp))
            Overline(label, color = app.ruuun.ui.theme.LimeText)
            Spacer(Modifier.height(2.dp))
            Text(description, style = RuuunText.BodyStrong, color = Navy)
        }
    }
}

@Composable
private fun WeekRow(
    week: WeekCalculator.WeekProgress,
    isCurrent: Boolean,
    isPast: Boolean,
) {
    Chunky(
        color = if (isCurrent) SkyTint else app.ruuun.ui.theme.Surface,
        shade = if (isCurrent) Sky else app.ruuun.ui.theme.Border,
        border = if (isCurrent) Sky else app.ruuun.ui.theme.Border,
        shape = RoundedCornerShape(18.dp),
        depth = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isCurrent -> Sky
                            isPast -> Lime
                            else -> Track
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${week.journeyWeek}",
                    style = RuuunText.BodyStrong,
                    color = Navy,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(week.theme.title, style = RuuunText.BodyStrong, color = Navy)
                Text(
                    "Side quest: ${week.theme.sideQuest.description}",
                    style = RuuunText.Label,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.width(10.dp))
            when {
                isCurrent -> Pill("Now · ${week.tasksDone}/2", Lime)
                isPast -> Text("${week.tasksDone}/2 done", style = RuuunText.Label, color = TextMuted)
                else -> Text("Coming up", style = RuuunText.Label, color = TextMuted)
            }
        }
    }
}
