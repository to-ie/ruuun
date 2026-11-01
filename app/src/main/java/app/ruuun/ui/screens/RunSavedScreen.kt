package app.ruuun.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ruuun.content.Chapters
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
import app.ruuun.ui.AppState
import app.ruuun.ui.RuuunViewModel
import app.ruuun.ui.bannerColor
import app.ruuun.ui.components.BadgeCell
import app.ruuun.ui.components.BadgeDetailSheet
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.Pill
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.QuestRow
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.WorldCard
import app.ruuun.ui.components.decorative
import app.ruuun.ui.icon
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.HeroTrack
import app.ruuun.ui.theme.InfoBg
import app.ruuun.ui.theme.InfoLabel
import app.ruuun.ui.theme.InfoText
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.SkyTint
import app.ruuun.ui.theme.SunTint
import app.ruuun.ui.theme.TextMuted

/** The celebration. Everything shown is a diff between two computed snapshots. */
@Composable
fun RunSavedScreen(
    state: AppState,
    viewModel: RuuunViewModel,
    onDone: () -> Unit,
    onViewBadges: () -> Unit,
) {
    val settings = state.settings ?: return
    val outcome by viewModel.lastSave.collectAsStateWithLifecycle()
    val saved = outcome ?: run {
        // Nothing to celebrate (e.g. after a process restart): go straight back.
        PrimaryCta("Back to Home", onDone, Modifier.padding(Dimens.ScreenPadding))
        return
    }
    val after = saved.after
    val week = JourneyView.thisWeek(state.logs, settings, saved.log.date)
    var showAllBadges by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<app.ruuun.content.BadgeDef?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CelebrationPanel(saved = saved, nickname = settings.nickname)

        Column(Modifier.padding(Dimens.ScreenPadding)) {
            if (saved.newBadges.isNotEmpty()) {
                val all = saved.newBadges
                val shown = if (showAllBadges) all else all.take(SaveOutcome.MAX_BADGES_SHOWN)
                Text(
                    if (all.size == 1) "1 new badge" else "${all.size} new badges",
                    style = RuuunText.SectionTitle,
                    color = Navy,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Tap one to see what it is for.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
                Spacer(Modifier.height(10.dp))
                shown.chunked(SaveOutcome.MAX_BADGES_SHOWN).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                        row.forEach { badge ->
                            BadgeCell(
                                badge = badge,
                                earned = true,
                                progressText = null,
                                onClick = { selectedBadge = badge },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(SaveOutcome.MAX_BADGES_SHOWN - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(Dimens.ItemGap))
                }
                if (all.size > SaveOutcome.MAX_BADGES_SHOWN) {
                    SecondaryButton(
                        text = if (showAllBadges) {
                            "Show fewer"
                        } else {
                            "Show all ${all.size} badges"
                        },
                        onClick = { showAllBadges = !showAllBadges },
                        lowEmphasis = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(Dimens.SectionGap))
            }

            val completed = saved.completedChapter
            if (completed != null) {
                WorldCard(
                    bannerColor = SunTint,
                    bannerIcon = completed.world.icon(),
                    worldName = "Chapter complete",
                    title = completed.title,
                    teaser = completed.revealText,
                )
                Spacer(Modifier.height(10.dp))
                val next = Chapters.ALL.getOrNull(completed.number)
                val progress = JourneyView.chapterProgress(after, settings)
                if (next != null) {
                    WorldCard(
                        bannerColor = next.world.bannerColor(),
                        bannerIcon = next.world.icon(),
                        worldName = "Next: ${next.world.title}",
                        title = "Chapter ${next.number} · ${next.title}",
                        teaser = next.teaser,
                        progressFraction = progress.fraction,
                        progressLabel = "${progress.runDays} of ${progress.runDaysNeeded} runs",
                    )
                }
            } else {
                val progress = JourneyView.chapterProgress(after, settings)
                WorldCard(
                    bannerColor = progress.chapter?.world?.bannerColor() ?: Lime,
                    bannerIcon = progress.chapter?.world?.icon() ?: Icons.Outlined.Check,
                    worldName = progress.worldTitle,
                    title = if (progress.isFinale) {
                        progress.title
                    } else {
                        "Chapter ${progress.chapter?.number} · ${progress.title}"
                    },
                    teaser = progress.teaser,
                    progressFraction = progress.fraction,
                    progressLabel = if (progress.isFinale) {
                        "Every chapter complete"
                    } else {
                        "${progress.runDays} of ${progress.runDaysNeeded} runs"
                    },
                )
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            QuestRow(
                label = "Main quest",
                description = "Weekly goal: ${week.runDays} / ${week.weeklyGoal} runs",
                progressText = "${week.runDays}/${week.weeklyGoal}",
                done = week.mainQuestMet,
            )
            Spacer(Modifier.height(Dimens.ItemGap))
            QuestRow(
                label = "Side quest (optional)",
                description = week.theme.sideQuest.description,
                progressText = if (week.sideQuestMet) "1/1" else "0/1",
                done = week.sideQuestMet,
            )

            Spacer(Modifier.height(Dimens.SectionGap))
            Surface(shape = Shapes.Card, color = InfoBg, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Overline("Next mission", color = InfoLabel)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            Formatters.distance(saved.nextTargetM),
                            style = RuuunText.CardTitle,
                            color = Navy,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        JourneyView.nextMissionNote(saved, settings),
                        style = RuuunText.Secondary,
                        color = InfoText,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                SecondaryButton(
                    text = "View badges",
                    onClick = onViewBadges,
                    modifier = Modifier.weight(1f),
                )
                PrimaryCta(
                    text = "Done",
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }

    selectedBadge?.let { badge ->
        BadgeDetailSheet(badge = badge, stats = after, onDismiss = { selectedBadge = null })
    }
}

@Composable
private fun CelebrationPanel(saved: SaveOutcome, nickname: String) {
    val after = saved.after
    Surface(color = Navy, modifier = Modifier.fillMaxWidth()) {
        Box {
            Confetti()
            Column(
                Modifier.padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(Shapes.Pill)
                        .background(Lime),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(46.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    JourneyView.savedHeadline(nickname),
                    style = RuuunText.Greeting,
                    color = Ground,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${Formatters.distance(saved.log.distanceM)} saved for " +
                        Formatters.dayMonth(saved.log.date),
                    style = RuuunText.BodyLarge,
                    color = Ground,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                if (saved.isNewRunDay) {
                    Pill("+50 XP", Orange)
                } else {
                    Pill("Distance added", SkyTint)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "Level ${after.level} · ${StatsCalculator.levelName(after.level)}",
                    style = RuuunText.BodyStrong,
                    color = Ground,
                )
                Spacer(Modifier.height(8.dp))
                RuuunProgressBar(
                    fraction = after.levelProgress,
                    color = Sky,
                    height = Dimens.XpBarHeight,
                    trackColor = HeroTrack,
                    description = "${after.runsToNextLevel} more runs to level ${after.level + 1}",
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (after.runsToNextLevel == 1) "1 more run to Level ${after.level + 1}"
                    else "${after.runsToNextLevel} more runs to Level ${after.level + 1}",
                    style = RuuunText.Secondary,
                    color = Ground,
                )
            }
        }
    }
}

/** Static decorative shapes: no animation required, nothing announced. */
@Composable
private fun Confetti() {
    val pieces = listOf(
        Triple(30.dp, 24.dp, Sky),
        Triple(90.dp, 54.dp, Orange),
        Triple(250.dp, 30.dp, Lime),
        Triple(310.dp, 70.dp, Sky),
        Triple(170.dp, 18.dp, Orange),
    )
    pieces.forEach { (x, y, color) ->
        Box(
            Modifier
                .offset(x = x, y = y)
                .size(10.dp)
                .clip(Shapes.Pill)
                .background(color.copy(alpha = 0.7f))
                .decorative(),
        )
    }
}
