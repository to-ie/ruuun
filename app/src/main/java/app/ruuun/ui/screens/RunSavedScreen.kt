package app.ruuun.ui.screens

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.statusBarsPadding
import app.ruuun.ui.components.Chunky
import app.ruuun.ui.components.SheetTop
import app.ruuun.ui.theme.SkyShade
import app.ruuun.ui.theme.Surface as SurfaceColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ruuun.content.Chapters
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StopGoal
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
import app.ruuun.ui.Mascot
import app.ruuun.ui.ruu
import app.ruuun.ui.scene
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.PopIn
import app.ruuun.ui.Unfold
import app.ruuun.ui.buzz
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.graphics.graphicsLayer
import app.ruuun.ui.PopSpring
import kotlinx.coroutines.launch

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
        PrimaryCta("Back to Home", onDone, Modifier.statusBarsPadding().padding(Dimens.ScreenPadding))
        return
    }
    val after = saved.after
    val week = JourneyView.thisWeek(state.logs, settings, saved.log.date)
    var showAllBadges by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<app.ruuun.content.BadgeDef?>(null) }

    // The celebration plays once: rotating the phone shows the finished screen instead.
    // Decided once when the screen opens, so marking it played does not cut it short.
    var played by rememberSaveable { mutableStateOf(false) }
    val allowed = LocalMotion.current
    val motion = remember { allowed && !played }
    val view = LocalView.current
    LaunchedEffect(Unit) {
        if (played) return@LaunchedEffect
        played = true
        view.buzz()
        if (motion && saved.newBadges.isNotEmpty()) {
            delay(BADGES_AT_MS.toLong())
            view.buzz()
        }
    }

    CompositionLocalProvider(LocalMotion provides motion) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            CelebrationPanel(saved = saved, nickname = settings.nickname)

            Column(Modifier.padding(Dimens.ScreenPadding)) {
                val records = JourneyView.records(saved)
                if (records.isNotEmpty()) {
                    PopIn(delayMillis = XP_AT_MS) {
                        Chunky(
                            shape = Shapes.Card,
                            color = app.ruuun.ui.theme.LimeTint,
                            shade = app.ruuun.ui.theme.LimeShade,
                            border = null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Overline("Personal best", color = Navy)
                                records.forEach { record ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(record, style = RuuunText.BodyStrong, color = Navy)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(Dimens.SectionGap))
                }
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
                    shown.chunked(SaveOutcome.MAX_BADGES_SHOWN).forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                            row.forEachIndexed { column, badge ->
                                val order = rowIndex * SaveOutcome.MAX_BADGES_SHOWN + column
                                PopIn(
                                    delayMillis = BADGES_AT_MS + order * 160,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    BadgeCell(
                                        badge = badge,
                                        earned = true,
                                        progressText = null,
                                        onClick = { selectedBadge = badge },
                                    )
                                }
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
                    Unfold(delayMillis = CHAPTER_AT_MS) {
                        WorldCard(
                            bannerColor = SunTint,
                            bannerIcon = completed.world.icon(),
                            worldName = "Chapter complete",
                            title = completed.title,
                            teaser = completed.revealText,
                            picture = Mascot.chapter(completed.number),
                            pictureAspect = Mascot.CHAPTER_ASPECT,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    val next = Chapters.ALL.getOrNull(completed.number)
                    val progress = JourneyView.chapterProgress(after, settings)
                    if (next != null) {
                        Unfold(delayMillis = CHAPTER_AT_MS + 300) {
                            WorldCard(
                                bannerColor = next.world.bannerColor(),
                                bannerIcon = next.world.icon(),
                                worldName = "Next: ${next.world.title}",
                                title = "Chapter ${next.number} · ${next.title}",
                                teaser = next.teaser,
                                progressFraction = progress.fraction,
                                progressLabel = progress.progressLabel,
                                picture = next.world.scene(),
                                ruu = next.world.ruu(),
                            )
                        }
                    }
                } else {
                    val progress = JourneyView.chapterProgress(after, settings)
                    WorldCard(
                        bannerColor = progress.chapter?.world?.bannerColor() ?: Lime,
                        bannerIcon = progress.chapter?.world?.icon() ?: Icons.Rounded.Check,
                        worldName = progress.worldTitle,
                        title = if (progress.isFinale) {
                            progress.title
                        } else {
                            "Chapter ${progress.chapter?.number} · ${progress.title}"
                        },
                        teaser = progress.teaser,
                        progressFraction = progress.fraction,
                        progressLabel = progress.progressLabel,
                        picture = progress.chapter?.world?.scene() ?: Mascot.FINALE_SCENE,
                        ruu = progress.chapter?.world?.ruu() ?: Mascot.CHEER,
                    )
                }

                Spacer(Modifier.height(Dimens.SectionGap))
                QuestRow(
                    label = "Weekly goal",
                    description = JourneyView.weeklyGoalText(week.weeklyGoal),
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

                val stopNote = JourneyView.stopNote(saved)
                val stops = saved.log.stops
                if (stopNote != null && stops != null) {
                    Spacer(Modifier.height(Dimens.SectionGap))
                    ResultPanel(
                        label = "Stops",
                        value = "$stops",
                        note = stopNote,
                    )
                }

                Spacer(Modifier.height(Dimens.SectionGap))
                ResultPanel(
                    label = "Next mission",
                    value = Formatters.mission(saved.nextTargetM),
                    detail = saved.nextStopGoal?.let { StopGoal.goalLabel(it) },
                    note = JourneyView.nextMissionNote(saved, settings),
                )

                Spacer(Modifier.height(Dimens.SectionGap))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                    SecondaryButton(
                        text = "View badges",
                        onClick = onViewBadges,
                        large = true,
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
    }

    selectedBadge?.let { badge ->
        BadgeDetailSheet(badge = badge, stats = after, onDismiss = { selectedBadge = null })
    }
}

/**
 * A light blue result card: the label across the top, then the value (with an optional
 * line under it) on the left and what it means beside it, lined up on the value.
 */
@Composable
private fun ResultPanel(label: String, value: String, note: String, detail: String? = null) {
    Chunky(
        shape = Shapes.Card,
        color = InfoBg,
        shade = Sky,
        border = null,
        depth = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Overline(label, color = InfoLabel)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.widthIn(min = 72.dp)) {
                    Text(value, style = RuuunText.CardTitle, color = Navy)
                    if (detail != null) {
                        Text(detail, style = RuuunText.Label, color = Navy)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Text(note, style = RuuunText.Secondary, color = InfoText, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** When each part of the celebration arrives, in milliseconds after the screen opens. */
private const val XP_AT_MS = 450
private const val BAR_AT_MS = 650
private const val BADGES_AT_MS = 1_100
private const val CHAPTER_AT_MS = 1_500

@Composable
private fun CelebrationPanel(saved: SaveOutcome, nickname: String) {
    val after = saved.after
    val before = saved.before
    val motion = LocalMotion.current
    val view = LocalView.current

    // The level bar fills from where it was. A level-up fills it, says so, then starts
    // the new level from empty.
    val bar = remember { Animatable(if (motion) before.levelProgress else after.levelProgress) }
    var shownLevel by remember { mutableIntStateOf(if (motion) before.level else after.level) }
    // "Level up!" has its space from the start, so nothing below jumps when it pops.
    val levelPop = remember { Animatable(if (motion) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!motion) return@LaunchedEffect
        delay(BAR_AT_MS.toLong())
        if (saved.leveledUp) {
            bar.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            shownLevel = after.level
            launch { levelPop.animateTo(1f, PopSpring) }
            view.buzz(strong = true)
            bar.snapTo(0f)
            delay(250)
        }
        bar.animateTo(after.levelProgress, tween(700, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(Sky)
            .statusBarsPadding(),
    ) {
        Box {
            Confetti()
            Column(
                Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PopIn {
                    Image(
                        painter = painterResource(Mascot.afterSave(saved)),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(220.dp)
                            .aspectRatio(Mascot.RUU_ASPECT),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    JourneyView.savedHeadline(nickname),
                    style = RuuunText.Greeting,
                    color = Navy,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${Formatters.distance(saved.log.distanceM)} saved for " +
                        Formatters.dayMonth(saved.log.date),
                    style = RuuunText.BodyLarge,
                    color = Navy,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                PopIn(delayMillis = XP_AT_MS) {
                    if (saved.isNewRunDay) {
                        Pill("+50 XP", Orange)
                    } else {
                        Pill("Distance added", SurfaceColor)
                    }
                }
                Spacer(Modifier.height(20.dp))
                if (saved.leveledUp) {
                    Pill(
                        "Level up!",
                        Lime,
                        Modifier.graphicsLayer {
                            scaleX = levelPop.value
                            scaleY = levelPop.value
                            alpha = levelPop.value.coerceIn(0f, 1f)
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "Level $shownLevel · ${StatsCalculator.levelName(shownLevel)}",
                    style = RuuunText.BodyStrong,
                    color = Navy,
                )
                Spacer(Modifier.height(8.dp))
                RuuunProgressBar(
                    fraction = bar.value,
                    color = SurfaceColor,
                    height = Dimens.XpBarHeight,
                    trackColor = SkyShade,
                    description = "${after.runsToNextLevel} more runs to level ${after.level + 1}",
                )
                Spacer(Modifier.height(6.dp))
                // Follows the level on show, so it never runs ahead of the bar.
                val toNext = if (shownLevel == after.level) after.runsToNextLevel else before.runsToNextLevel
                Text(
                    if (toNext == 1) "1 more run to Level ${shownLevel + 1}"
                    else "$toNext more runs to Level ${shownLevel + 1}",
                    style = RuuunText.Secondary,
                    color = Navy,
                )
            }
            if (motion) FallingConfetti(Modifier.matchParentSize())
        }
        SheetTop()
    }
}

/** Static decorative shapes: they stay put, and are all there is without motion. */
@Composable
private fun Confetti() {
    val pieces = listOf(
        Triple(30.dp, 24.dp, Navy),
        Triple(90.dp, 54.dp, Orange),
        Triple(250.dp, 30.dp, Lime),
        Triple(310.dp, 70.dp, Navy),
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

/** One piece of falling confetti. Positions are fractions of the panel. */
private class ConfettiPiece(
    val x: Float,
    val start: Float,
    val fall: Float,
    val sway: Float,
    val spin: Float,
    val width: Float,
    val height: Float,
    val round: Boolean,
    val color: Color,
)

private val CONFETTI: List<ConfettiPiece> = Random(7).let { r ->
    val colours = listOf(Navy, Orange, Lime, Ground)
    List(36) {
        val round = r.nextFloat() < 0.35f
        val size = 6f + r.nextFloat() * 5f
        ConfettiPiece(
            x = r.nextFloat(),
            start = r.nextFloat() * 0.4f,
            fall = 0.45f + r.nextFloat() * 0.2f,
            sway = 6f + r.nextFloat() * 16f,
            spin = (r.nextFloat() - 0.5f) * 4f,
            width = size,
            height = if (round) size else size * (1.6f + r.nextFloat()),
            round = round,
            color = colours[it % colours.size],
        )
    }
}

/** A single shower of confetti across the panel when the screen opens. Decoration only. */
@Composable
private fun FallingConfetti(modifier: Modifier = Modifier) {
    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) { time.animateTo(1f, tween(3_000, easing = LinearEasing)) }
    Canvas(modifier) {
        val t = time.value
        if (t >= 1f) return@Canvas
        CONFETTI.forEach { p ->
            val local = (t - p.start) / p.fall
            if (local <= 0f || local >= 1f) return@forEach
            val px = p.x * size.width + sin(local * 2f * PI.toFloat() * 1.5f) * p.sway * density
            val py = -20f * density + local * (size.height + 40f * density)
            val alpha = if (local > 0.8f) (1f - local) / 0.2f else 1f
            val w = p.width * density
            val h = p.height * density
            rotate(degrees = local * 360f * p.spin, pivot = Offset(px, py)) {
                if (p.round) {
                    drawCircle(p.color.copy(alpha = 0.9f * alpha), radius = w / 2f, center = Offset(px, py))
                } else {
                    drawRect(
                        p.color.copy(alpha = 0.9f * alpha),
                        topLeft = Offset(px - w / 2f, py - h / 2f),
                        size = Size(w, h),
                    )
                }
            }
        }
    }
}
