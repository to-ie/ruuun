package app.ruuun.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.North
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.South
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ruuun.ui.components.ThickCheck
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StopGoal
import app.ruuun.logic.TargetRecommender
import app.ruuun.ui.AppState
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.Mascot
import app.ruuun.ui.PopSpring
import app.ruuun.ui.buzz
import app.ruuun.ui.components.Chunky
import app.ruuun.ui.components.FoxCircle
import app.ruuun.ui.components.Gutter
import app.ruuun.ui.components.HeaderChip
import app.ruuun.ui.components.HeaderIconChip
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.RuuunProgressBar
import app.ruuun.ui.components.SheetTop
import app.ruuun.ui.components.SpeechCard
import app.ruuun.ui.icon
import app.ruuun.ui.ruu
import app.ruuun.ui.scene
import app.ruuun.ui.theme.Divider
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.LimeShade
import app.ruuun.ui.theme.LimeText
import app.ruuun.ui.theme.LimeTint
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.OrangeShade
import app.ruuun.ui.theme.OrangeTint
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.SkyDeep
import app.ruuun.ui.theme.SkyShade
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Ruu's round frame on Home. */
private val FOX_SIZE = 180.dp

/**
 * Home: Ruu and the next mission on a big blue header that fills most of the screen,
 * then the week, the buttons and the two closest goals on the sheet below.
 */
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
    val stopGoal = remember(state.logs, settings) { StopGoal.next(state.logs, settings) }
    val lastStops = remember(state.logs) { StopGoal.goalSource(state.logs)?.stops }
    val world = chapter.chapter?.world

    BoxWithConstraints(Modifier.fillMaxSize().background(Ground)) {
        val viewport = maxHeight
        Column(Modifier.verticalScroll(rememberScrollState())) {
            // The header takes whatever height the sheet leaves, so on a tall phone the
            // blue fills the screen; on a short one everything simply scrolls.
            FillViewport(viewport) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Sky)
                        .statusBarsPadding(),
                ) {
                    Column(Modifier.padding(start = Gutter, end = Gutter, top = 12.dp)) {
                        TopChips(
                            level = stats.level,
                            levelName = StatsCalculator.levelName(stats.level),
                            xp = stats.xp,
                            onSettings = onSettings,
                        )
                        Spacer(Modifier.height(12.dp))
                        RuuunProgressBar(
                            fraction = stats.levelProgress,
                            color = SurfaceColor,
                            trackColor = SkyShade,
                            height = 12.dp,
                            description = "${stats.runsToNextLevel} more runs to level ${stats.level + 1}",
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "50 XP for each day you run. " + (
                                if (stats.runsToNextLevel == 1) "1 more to Level ${stats.level + 1}"
                                else "${stats.runsToNextLevel} more to Level ${stats.level + 1}"
                                ),
                            style = RuuunText.Tiny,
                            color = SkyDeep,
                        )
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = Gutter, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        MissionSpeech(targetM = settings.currentTargetM, welcomeBack = welcomeBack)
                        Spacer(Modifier.height(16.dp))
                        FoxCircle(
                            pose = when {
                                welcomeBack -> Mascot.WELCOME_BACK
                                world != null -> world.ruu()
                                else -> Mascot.CHEER
                            },
                            background = world?.scene() ?: Mascot.FINALE_SCENE,
                            size = FOX_SIZE,
                            zoom = 1.55f,
                            breathe = true,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            JourneyView.homePill(
                                stopGoal = stopGoal,
                                lastStops = lastStops,
                                lowStopsLimit = settings.lowStopsLimit,
                                atCap = settings.currentTargetM >= settings.maxTargetM,
                                welcomeBack = welcomeBack,
                            ),
                            style = RuuunText.Label.copy(fontWeight = FontWeight.ExtraBold),
                            color = Lime,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(Navy)
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                    SheetTop()
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Ground)
                        .padding(start = Gutter, end = Gutter, bottom = 18.dp),
                ) {
                    val fresh = remember(week.weekStart, week.runDays) {
                        newlyFilledDots(week.weekStart, week.runDays)
                    }
                    WeekRow(
                        days = JourneyView.weekDays(state.logs, week.weekStart, today),
                        runDays = week.runDays,
                        weeklyGoal = week.weeklyGoal,
                        fresh = fresh,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepButton("Easier", Icons.Rounded.South, SkyShade, onMakeEasier)
                        Spacer(Modifier.width(8.dp))
                        PrimaryCta("Log a run", onLogRun, Modifier.weight(1f), icon = Icons.Rounded.Add, compact = true)
                        Spacer(Modifier.width(8.dp))
                        StepButton("Harder", Icons.Rounded.North, OrangeShade, onMakeHarder)
                    }
                    Spacer(Modifier.height(14.dp))
                    RuuunCard(padding = 0.dp, shape = RoundedCornerShape(18.dp), depth = 4.dp) {
                        if (upcoming != null) {
                            QuestLine(
                                icon = upcoming.category.icon(),
                                iconBackground = OrangeTint,
                                iconTint = OrangeShade,
                                title = upcoming.title,
                                value = "${BadgeEvaluator.progress(upcoming, stats)}/${upcoming.threshold}",
                                fraction = BadgeEvaluator.progressFraction(upcoming, stats),
                                barColor = Orange,
                                description = "Closest badge, ${upcoming.title}: " +
                                    "${BadgeEvaluator.progress(upcoming, stats)} of ${upcoming.threshold} ${upcoming.unit}",
                            )
                            Box(Modifier.fillMaxWidth().height(2.dp).background(Divider))
                        }
                        QuestLine(
                            icon = world?.icon() ?: Icons.Rounded.Check,
                            iconBackground = LimeTint,
                            iconTint = LimeText,
                            title = chapter.title,
                            value = if (chapter.isFinale) "Done" else "${chapter.runDays} of ${chapter.runDaysNeeded} runs",
                            fraction = chapter.fraction,
                            barColor = LimeShade,
                            description = (chapter.chapter?.let { "Chapter ${it.number}, " } ?: "") +
                                "${chapter.title}: ${chapter.progressLabel}",
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lays out the header and the sheet so the pair fills at least [viewport]: the sheet
 * keeps its own height and the header gets the rest. Taller content just scrolls.
 */
@Composable
private fun FillViewport(viewport: Dp, content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        val width = constraints.maxWidth
        val sheet = measurables[1].measure(Constraints(minWidth = width, maxWidth = width))
        // Never shorter than its own content: on a small phone the page scrolls instead.
        val natural = measurables[0].minIntrinsicHeight(width)
        val headerMin = maxOf(viewport.roundToPx() - sheet.height, natural)
        val header = measurables[0].measure(
            Constraints(minWidth = width, maxWidth = width, minHeight = headerMin),
        )
        layout(width, header.height + sheet.height) {
            header.place(0, 0)
            sheet.place(0, header.height)
        }
    }
}

/** Level, XP and the settings button across the top of the header. */
@Composable
private fun TopChips(level: Int, levelName: String, xp: Int, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // The level chip gives way (with an ellipsis) so Settings always has room.
        Box(Modifier.weight(1f)) {
            HeaderChip(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                Text(
                    "Lv $level",
                    style = RuuunText.SpeechWords.copy(fontSize = 18.sp),
                    color = SkyShade,
                    modifier = Modifier.cropFontSpace(),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    levelName,
                    style = RuuunText.Medal.copy(fontSize = 14.sp),
                    color = Navy,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        HeaderChip(modifier = Modifier.clearAndSetSemantics { contentDescription = "$xp XP" }) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = SkyShade, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(2.dp))
            Text("$xp", style = RuuunText.Medal.copy(fontSize = 14.sp), color = Navy)
        }
        Spacer(Modifier.width(8.dp))
        HeaderIconChip(Icons.Rounded.Settings, "Settings", onSettings, size = 42.dp)
    }
}

/** Ruu's speech card: "Let's do 1.3 km". Easier rolls the number down, Harder rolls it up. */
@Composable
private fun MissionSpeech(targetM: Int, welcomeBack: Boolean) {
    val motion = LocalMotion.current
    // One stop for TalkBack: "Let's do 1.3 km".
    SpeechCard(
        Modifier.semantics(mergeDescendants = true) {},
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
    ) {
        if (welcomeBack) {
            Text("Welcome back!", style = RuuunText.SpeechWords, color = Navy)
            Spacer(Modifier.height(4.dp))
        }
        // Bottom-aligned with a fixed nudge, not by baseline: while the number rolls there
        // are two of them, and a baseline would make the card jump.
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "Let's do ",
                style = RuuunText.SpeechWords,
                color = Navy,
                maxLines = 1,
                // The distance keeps its width; the words give way first.
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(bottom = 3.dp)
                    .cropFontSpace(),
            )
            AnimatedContent(
                targetState = targetM,
                transitionSpec = {
                    when {
                        !motion -> EnterTransition.None togetherWith ExitTransition.None
                        targetState > initialState ->
                            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                        else ->
                            (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                    }.using(SizeTransform(clip = true) { _, _ -> tween(220) })
                },
                label = "mission",
            ) { metres ->
                Text(
                    Formatters.mission(metres),
                    style = RuuunText.SpeechNumber,
                    color = OrangeShade,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.cropFontSpace(),
                )
            }
        }
    }
}

/**
 * Baloo 2 keeps a lot of empty space above and below its letters. This trims it from the
 * layout (the glyphs still draw in full), so the speech card hugs its words.
 */
private fun Modifier.cropFontSpace(top: Float = 0.2f, bottom: Float = 0.16f): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val cutTop = (placeable.height * top).toInt()
        val cutBottom = (placeable.height * bottom).toInt()
        layout(placeable.width, placeable.height - cutTop - cutBottom) {
            placeable.place(0, -cutTop)
        }
    }

/** Monday to Sunday: lime with a check on days run, a dashed orange ring on today. */
@Composable
private fun WeekRow(days: List<JourneyView.WeekDay>, runDays: Int, weeklyGoal: Int, fresh: IntRange) {
    val runOrder = days.filter { it.ran }.map { it.date }
    val description = JourneyView.weekDaysDescription(days) + ". Weekly goal: $runDays of $weeklyGoal."
    Row(
        Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        days.forEach { day ->
            val index = runOrder.indexOf(day.date)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                DayCircle(
                    ran = day.ran,
                    isToday = day.isToday,
                    animateIn = day.ran && index in fresh,
                    delayMillis = 350 + (index - fresh.first).coerceAtLeast(0) * 250,
                    completesGoal = index == weeklyGoal - 1,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.UK),
                    style = RuuunText.Tiny,
                    color = when {
                        day.isToday -> app.ruuun.ui.theme.OrangeText
                        day.ran -> Navy
                        else -> TextMuted
                    },
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 4.dp)) {
            Text("Goal", style = RuuunText.Tiny, color = LimeText)
            Text("$runDays of $weeklyGoal", style = RuuunText.Tiny, color = LimeText)
        }
    }
}

/** One day. A newly run day fills with a bounce; filling the goal's last day buzzes. */
@Composable
private fun DayCircle(ran: Boolean, isToday: Boolean, animateIn: Boolean, delayMillis: Int, completesGoal: Boolean) {
    val motion = LocalMotion.current
    val view = LocalView.current
    var filled by remember { mutableStateOf(ran && !(animateIn && motion)) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(ran, animateIn) {
        if (!ran) {
            filled = false
            return@LaunchedEffect
        }
        if (!animateIn || !motion) {
            filled = true
            return@LaunchedEffect
        }
        delay(delayMillis.toLong())
        filled = true
        if (completesGoal) view.buzz()
        scale.snapTo(0.5f)
        scale.animateTo(1f, PopSpring)
    }
    // Today always has the dashed orange ring; a day run is lime with a tick, inside the
    // ring when that day is today.
    Box(
        Modifier
            .size(34.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .then(
                if (isToday) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = Orange,
                            radius = size.minDimension / 2f - 1.5.dp.toPx(),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.5.dp.toPx())),
                            ),
                        )
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(if (isToday) 25.dp else 32.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(
                    when {
                        filled -> Lime
                        isToday -> SurfaceColor
                        else -> Track
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (filled) ThickCheck(Modifier.size(if (isToday) 17.dp else 20.dp))
        }
    }
}

/** Easier or Harder: a small square chunky button with an arrow and its name. */
@Composable
private fun StepButton(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Chunky(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        depth = 5.dp,
        // At least 56 dp square, wider when large text needs the room for the word.
        modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 57.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(label, style = RuuunText.Tiny.copy(fontSize = 10.sp), color = Navy, maxLines = 1, softWrap = false)
        }
    }
}

/** A goal on the Home quest card: icon tile, name, progress number and bar. */
@Composable
private fun QuestLine(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    value: String,
    fraction: Float,
    barColor: Color,
    description: String,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = RuuunText.Medal.copy(fontSize = 14.sp),
                    color = Navy,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(6.dp))
                Text(value, style = RuuunText.Tiny.copy(fontSize = 12.sp), color = TextMuted, maxLines = 1)
            }
            Spacer(Modifier.height(5.dp))
            RuuunProgressBar(fraction = fraction, color = barColor, height = 8.dp)
        }
    }
}
