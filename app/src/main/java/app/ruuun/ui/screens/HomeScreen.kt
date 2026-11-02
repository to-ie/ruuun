package app.ruuun.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StopGoal
import app.ruuun.logic.TargetRecommender
import app.ruuun.ui.AppState
import app.ruuun.ui.bannerColor
import app.ruuun.ui.components.CheckDot
import app.ruuun.ui.components.RuuImage
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.StatTile
import app.ruuun.ui.components.WorldCard
import app.ruuun.ui.icon
import app.ruuun.ui.Mascot
import app.ruuun.ui.ruu
import app.ruuun.ui.scene
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.IconButtonBg
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.OrangeTint
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor
import java.time.LocalDate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.PopSpring
import app.ruuun.ui.buzz
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    state: AppState,
    today: LocalDate,
    onLogRun: () -> Unit,
    onMakeEasier: () -> Unit,
    onMakeHarder: () -> Unit,
    onSettings: () -> Unit,
    newlyFilledDots: (weekStart: LocalDate, runDays: Int) -> IntRange = { _, _ -> IntRange.EMPTY },
) {
    val settings = state.settings ?: return
    val stats = state.stats
    val chapter = JourneyView.chapterProgress(stats, settings)
    val week = JourneyView.thisWeek(state.logs, settings, today)
    val upcoming = JourneyView.upcomingBadge(stats)
    val welcomeBack = TargetRecommender.needsWelcomeBack(state.logs, today)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Hello, ${settings.nickname}") {
            Surface(
                onClick = onSettings,
                shape = Shapes.Pill,
                color = IconButtonBg,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Navy,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Level ${stats.level} · ${StatsCalculator.levelName(stats.level)}",
                    style = RuuunText.BodyStrong,
                    color = Navy,
                    modifier = Modifier.weight(1f),
                )
                Text("${stats.xp} XP", style = RuuunText.Body, color = TextMuted)
            }
            Spacer(Modifier.height(8.dp))
            RuuunProgressBar(
                fraction = stats.levelProgress,
                color = Sky,
                height = Dimens.XpBarHeight,
                description = "${stats.runsToNextLevel} more runs to level ${stats.level + 1}",
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "50 XP for each day you run. " + (
                    if (stats.runsToNextLevel == 1) "1 more to Level ${stats.level + 1}"
                    else "${stats.runsToNextLevel} more to Level ${stats.level + 1}"
                    ),
                style = RuuunText.Secondary,
                color = TextMuted,
            )

            Spacer(Modifier.height(Dimens.SectionGap))
            NextMissionHero(
                targetM = settings.currentTargetM,
                stopGoal = remember(state.logs, settings) {
                    StopGoal.next(state.logs, settings)
                },
                lastStops = remember(state.logs) { StopGoal.goalSource(state.logs)?.stops },
                lowStopsLimit = settings.lowStopsLimit,
                atCap = settings.currentTargetM >= settings.maxTargetM,
                welcomeBack = welcomeBack,
                onLogRun = onLogRun,
                onMakeEasier = onMakeEasier,
                onMakeHarder = onMakeHarder,
            )

            Spacer(Modifier.height(Dimens.SectionGap))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                StatTile("${stats.runDays}", "Runs", Modifier.weight(1f))
                StatTile(Formatters.km(stats.totalDistanceM), "Distance (km)", Modifier.weight(1f))
                StatTile("${stats.earnedBadgeIds.size}", "Badges", Modifier.weight(1f))
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            RuuunCard {
                Overline("This week · ${week.theme.title}")
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Weekly goal: ${week.runDays} of ${week.weeklyGoal}",
                        style = RuuunText.CardTitle,
                        color = Navy,
                        modifier = Modifier.weight(1f),
                    )
                    // Dots filled since Home last showed them bounce in, one after another.
                    Spacer(Modifier.width(10.dp))
                    val fresh = remember(week.weekStart, week.runDays) {
                        newlyFilledDots(week.weekStart, week.runDays)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(week.weeklyGoal) { index ->
                            if (index in fresh) {
                                FillingCheckDot(
                                    delayMillis = 350 + (index - fresh.first) * 250,
                                    completesGoal = index == week.weeklyGoal - 1,
                                )
                            } else {
                                CheckDot(done = index < week.runDays)
                            }
                        }
                    }
                }
            }

            if (upcoming != null) {
                Spacer(Modifier.height(Dimens.SectionGap))
                RuuunCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(52.dp)
                                .clip(Shapes.Tile)
                                .background(OrangeTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                upcoming.category.icon(),
                                contentDescription = null,
                                tint = Navy,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Overline("Closest badge")
                            Text(upcoming.title, style = RuuunText.BodyStrong, color = Navy)
                            Text(upcoming.hint, style = RuuunText.Secondary, color = TextMuted)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${BadgeEvaluator.progress(upcoming, stats)}/${upcoming.threshold}",
                            style = RuuunText.BodyStrong,
                            color = Navy,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    RuuunProgressBar(
                        fraction = BadgeEvaluator.progressFraction(upcoming, stats),
                        color = Orange,
                        description = "${BadgeEvaluator.progress(upcoming, stats)} of " +
                            "${upcoming.threshold} ${upcoming.unit}",
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            WorldCard(
                bannerColor = chapter.chapter?.world?.bannerColor() ?: Sky,
                bannerIcon = chapter.chapter?.world?.icon() ?: Icons.Outlined.Add,
                worldName = chapter.worldTitle,
                title = if (chapter.isFinale) {
                    chapter.title
                } else {
                    "Chapter ${chapter.chapter?.number} · ${chapter.title}"
                },
                teaser = chapter.teaser,
                progressFraction = chapter.fraction,
                progressLabel = chapter.progressLabel,
                picture = chapter.chapter?.world?.scene() ?: Mascot.FINALE_SCENE,
                ruu = chapter.chapter?.world?.ruu() ?: Mascot.CHEER,
            )
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }
}

/** Ruu's height in the hero when there is room, and how far Ruu stands above the card. */
private val RUU_HERO_HEIGHT = 176.dp
private val RUU_HERO_MIN_HEIGHT = 96.dp
private val RUU_OVERHANG = 44.dp
private val RUU_END_INSET = 4.dp
private val RUU_GAP = 8.dp

@Composable
private fun NextMissionHero(
    targetM: Int,
    stopGoal: Int?,
    lastStops: Int?,
    lowStopsLimit: Int,
    atCap: Boolean,
    welcomeBack: Boolean,
    onLogRun: () -> Unit,
    onMakeEasier: () -> Unit,
    onMakeHarder: () -> Unit,
) {
    val pose = if (welcomeBack) Mascot.WELCOME_BACK else Mascot.READY
    // How far Ruu's body reaches in from the right edge of the picture: arms wide
    // takes more room than standing with folded paws.
    val bodyReach = if (welcomeBack) 0.91f else 0.81f
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Ruu stands on the right of the card, big, with the head above its top edge. The
    // distance keeps the room it needs: on a narrow phone or with large text Ruu gets
    // smaller, and steps out altogether rather than wrap the number.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val numberWidth = with(density) {
            measurer.measure(Formatters.distance(targetM), RuuunText.HeroNumber).size.width.toDp()
        }
        val roomForRuu = maxWidth - Dimens.HeroPadding - RUU_END_INSET - RUU_GAP - numberWidth
        val ruuHeight = (roomForRuu / (bodyReach * Mascot.RUU_ASPECT)).coerceAtMost(RUU_HERO_HEIGHT)
        val showRuu = ruuHeight >= RUU_HERO_MIN_HEIGHT
        val overhang = if (showRuu) RUU_OVERHANG else 0.dp
        val ruuReach = ruuHeight * Mascot.RUU_ASPECT * bodyReach

        Surface(
            shape = Shapes.Hero,
            color = Navy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = overhang),
        ) {
            Column(Modifier.padding(Dimens.HeroPadding)) {
                Row(
                    Modifier.heightIn(
                        min = if (showRuu) ruuHeight - overhang - Dimens.HeroPadding else 0.dp,
                    ),
                ) {
                    Column(Modifier.weight(1f)) {
                        Overline("Next mission", color = Sky)
                        Spacer(Modifier.height(6.dp))
                        // Easier rolls the number down, Harder rolls it up.
                        val motion = LocalMotion.current
                        AnimatedContent(
                            targetState = targetM,
                            transitionSpec = {
                                when {
                                    !motion -> EnterTransition.None togetherWith ExitTransition.None
                                    targetState > initialState ->
                                        (slideInVertically { it } + fadeIn()) togetherWith
                                            (slideOutVertically { -it } + fadeOut())
                                    else ->
                                        (slideInVertically { -it } + fadeIn()) togetherWith
                                            (slideOutVertically { it } + fadeOut())
                                }
                            },
                            label = "mission",
                        ) { metres ->
                            Text(
                                Formatters.distance(metres),
                                style = RuuunText.HeroNumber,
                                color = Ground,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                    if (showRuu) {
                        Spacer(Modifier.width(RUU_END_INSET + ruuReach + RUU_GAP - Dimens.HeroPadding))
                    }
                }
                if (stopGoal != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Stop goal: ${StopGoal.goalLabel(stopGoal).lowercase()}" +
                            (lastStops?.takeIf { it > stopGoal }?.let { " · from $it" } ?: ""),
                        style = RuuunText.BodyStrong,
                        color = Lime,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        welcomeBack -> "Welcome back. Repeat this one, or make it easier."
                        atCap -> "Breaks are fine. This is your longest mission setting."
                        else -> "Breaks are fine. It gets longer after a run with " +
                            "${JourneyView.stopsOrFewer(lowStopsLimit)}."
                    },
                    style = RuuunText.BodyLarge,
                    color = Ground,
                )
                Spacer(Modifier.height(18.dp))
                PrimaryCta("Log a run", onLogRun, icon = Icons.Outlined.Add)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(
                        text = "Easier",
                        onClick = onMakeEasier,
                        onNavy = true,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        text = "Harder",
                        onClick = onMakeHarder,
                        onNavy = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (showRuu) {
            RuuImage(
                pose = pose,
                height = ruuHeight,
                breathe = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = RUU_END_INSET),
            )
        }
    }
}

/**
 * A weekly goal dot that was empty last time Home was shown: it starts empty, then
 * fills with a bounce. Filling the last one also gives a small buzz.
 */
@Composable
private fun FillingCheckDot(delayMillis: Int, completesGoal: Boolean) {
    val motion = LocalMotion.current
    val view = LocalView.current
    var filled by remember { mutableStateOf(!motion) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (!motion) return@LaunchedEffect
        delay(delayMillis.toLong())
        filled = true
        if (completesGoal) view.buzz()
        scale.snapTo(0.5f)
        scale.animateTo(1f, PopSpring)
    }
    CheckDot(
        done = filled,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}
