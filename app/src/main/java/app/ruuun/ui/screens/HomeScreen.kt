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
import androidx.compose.ui.unit.dp
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.TargetRecommender
import app.ruuun.ui.AppState
import app.ruuun.ui.bannerColor
import app.ruuun.ui.components.CheckDot
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.StatTile
import app.ruuun.ui.components.WorldCard
import app.ruuun.ui.components.decorative
import app.ruuun.ui.icon
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.IconButtonBg
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
                        "Weekly goal: ${week.runDays} of ${week.weeklyGoal} runs",
                        style = RuuunText.CardTitle,
                        color = Navy,
                        modifier = Modifier.weight(1f),
                    )
                    // Dots filled since Home last showed them bounce in, one after another.
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
            )
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }
}

@Composable
private fun NextMissionHero(
    targetM: Int,
    welcomeBack: Boolean,
    onLogRun: () -> Unit,
    onMakeEasier: () -> Unit,
    onMakeHarder: () -> Unit,
) {
    Surface(shape = Shapes.Hero, color = Navy, modifier = Modifier.fillMaxWidth()) {
        Box {
            // Decorative only.
            Box(
                Modifier
                    .size(120.dp)
                    .offset(x = 250.dp, y = (-40).dp)
                    .clip(Shapes.Pill)
                    .background(Orange.copy(alpha = 0.18f))
                    .decorative(),
            )
            Box(
                Modifier
                    .size(80.dp)
                    .offset(x = 300.dp, y = 60.dp)
                    .clip(Shapes.Pill)
                    .background(Sky.copy(alpha = 0.16f))
                    .decorative(),
            )
            Column(Modifier.padding(Dimens.HeroPadding)) {
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
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (welcomeBack) {
                        "Welcome back. Repeat this one, or make it easier."
                    } else {
                        "Walk it or run it. Breaks are fine."
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
