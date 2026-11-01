package app.ruuun.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ruuun.R
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
import app.ruuun.ui.theme.Border
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

@Composable
fun HomeScreen(
    state: AppState,
    today: LocalDate,
    onLogRun: () -> Unit,
    onMakeEasier: () -> Unit,
    onMakeHarder: () -> Unit,
    onSettings: () -> Unit,
) {
    val settings = state.settings ?: return
    val stats = state.stats
    val chapter = JourneyView.chapterProgress(stats, settings)
    val week = JourneyView.thisWeek(state.logs, settings, today)
    val upcoming = JourneyView.upcomingBadge(stats)
    val welcomeBack = TargetRecommender.needsWelcomeBack(state.logs, today)

    Column(Modifier.fillMaxSize()) {
        HomeHeader(onSettings = onSettings)

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
        ) {
            Text("Hello, ${settings.nickname}", style = RuuunText.Greeting, color = Navy)
            Spacer(Modifier.height(10.dp))
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
                if (stats.runsToNextLevel == 1) "1 more run to Level ${stats.level + 1}"
                else "${stats.runsToNextLevel} more runs to Level ${stats.level + 1}",
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
                        "${week.runDays} / ${week.weeklyGoal} runs this week",
                        style = RuuunText.CardTitle,
                        color = Navy,
                        modifier = Modifier.weight(1f),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(week.weeklyGoal) { index ->
                            CheckDot(done = index < week.runDays)
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
                progressLabel = if (chapter.isFinale) {
                    "Every chapter complete"
                } else {
                    "${chapter.runDays} / ${chapter.runDaysNeeded} days"
                },
            )
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }
}

@Composable
private fun HomeHeader(onSettings: () -> Unit) {
    // Navy bar: the logo sits on the brand colour rather than floating on white,
    // and it matches the launcher icon.
    Surface(color = Navy, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenPadding, end = 12.dp, top = 10.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ruuun_logo_light),
                contentDescription = stringResource(R.string.logo_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(44.dp),
            )
            Spacer(Modifier.weight(1f))
            Surface(
                onClick = onSettings,
                shape = Shapes.Pill,
                color = Color.White.copy(alpha = 0.10f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Ground,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
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
                Text(
                    Formatters.distance(targetM),
                    style = RuuunText.HeroNumber,
                    color = Ground,
                )
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
