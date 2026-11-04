package app.ruuun.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.ruuun.content.Chapter
import app.ruuun.content.Chapters
import app.ruuun.logic.ChapterStatus
import app.ruuun.logic.WorldStatus
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.Mascot
import app.ruuun.ui.bannerColor
import app.ruuun.ui.icon
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.IconMuted
import app.ruuun.ui.theme.InputBorder
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.LimeShade
import app.ruuun.ui.theme.LimeText
import app.ruuun.ui.theme.LockedCard
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.OrangeShade
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor

/** How far in from the left each of a world's three chapters sits, for the zig-zag. */
private val PATH_OFFSETS = listOf(24.dp, 120.dp, 52.dp)

/**
 * One world on the Journey map. The current world shows its chapters as a zig-zag path
 * with "You are here"; finished worlds fold to a card that opens to the same path, so
 * their chapter pictures stay one tap away; worlds ahead show what is in them and how
 * many runs they need. A whole season that has not opened yet is drawn dashed.
 */
@Composable
fun WorldJourneyCard(status: WorldStatus, seasonOpen: Boolean, modifier: Modifier = Modifier) {
    // The open picture is kept by chapter number, so it survives rotation.
    var shownNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    shownNumber?.let { number ->
        Chapters.ALL.getOrNull(number - 1)?.let { ChapterPictureDialog(it) { shownNumber = null } }
    }
    val open: (Chapter) -> Unit = { shownNumber = it.number }

    if (status.isCurrent) {
        Column(modifier.fillMaxWidth()) {
            Chunky(
                color = status.world.bannerColor(),
                shade = shadeOf(status.world.bannerColor()),
                border = null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(status.world.icon(), contentDescription = null, tint = Navy, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Overline(status.world.title, color = Navy, modifier = Modifier.weight(1f))
                    Text("${status.completed} of ${status.total}", style = RuuunText.BodyStrong, color = Navy)
                }
            }
            Spacer(Modifier.height(10.dp))
            ChapterPath(status.chapters, open)
        }
        return
    }

    var expanded by rememberSaveable(status.world.name) { mutableStateOf(false) }
    val foldable = status.isComplete
    val motion = LocalMotion.current
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = if (motion) tween(300) else snap(),
        label = "chevron",
    )
    val dashed = !seasonOpen
    val first = status.chapters.first().runDaysNeeded
    val last = status.chapters.last().runDaysNeeded
    val description = if (status.isComplete) {
        "${status.world.title}, all ${status.total} chapters complete"
    } else {
        "${status.world.title}, locked, $first to $last runs"
    }

    Column(modifier.fillMaxWidth()) {
        Chunky(
            color = if (dashed) LockedCard else SurfaceColor,
            shade = if (dashed) LockedCard else Border,
            border = if (dashed) null else Border,
            shape = RoundedCornerShape(18.dp),
            depth = if (dashed) 0.dp else 5.dp,
            onClick = if (foldable) ({ expanded = !expanded }) else null,
            onClickLabel = if (expanded) "Hide chapters" else "Show chapters",
            modifier = Modifier
                .fillMaxWidth()
                .then(if (dashed) Modifier.dashedOutline() else Modifier)
                // Set, not cleared, on the card itself, so the tap to open stays for TalkBack.
                .semantics {
                    contentDescription = description
                    if (foldable) {
                        stateDescription = if (expanded) "Open" else "Folded"
                        role = Role.Button
                        onClick(label = if (expanded) "Hide chapters" else "Show chapters") {
                            expanded = !expanded
                            true
                        }
                    }
                },
        ) {
            Row(
                Modifier.padding(12.dp).clearAndSetSemantics { },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .alpha(if (dashed) 0.7f else 1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(status.world.bannerColor()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(status.world.icon(), contentDescription = null, tint = Navy, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Overline(status.world.title, color = Navy)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        status.chapters.joinToString(" · ") { it.chapter.title },
                        style = RuuunText.Label,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.width(8.dp))
                if (foldable) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Lime),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
                        }
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer { rotationZ = chevron },
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Text("$first to $last runs", style = RuuunText.Tiny, color = TextMuted)
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = foldable && expanded,
            enter = if (motion) expandVertically(expandFrom = Alignment.Top) + fadeIn() else EnterTransition.None,
            exit = if (motion) shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut() else ExitTransition.None,
        ) {
            Column {
                Spacer(Modifier.height(10.dp))
                ChapterPath(status.chapters, open)
            }
        }
    }
}

/** A world's three chapters as stepping stones, zig-zagging down the screen. */
@Composable
private fun ChapterPath(chapters: List<ChapterStatus>, onOpen: (Chapter) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        chapters.forEachIndexed { index, chapter ->
            Row(
                Modifier.padding(start = PATH_OFFSETS[index % PATH_OFFSETS.size], top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChapterNode(chapter, onOpen)
            }
        }
    }
}

@Composable
private fun ChapterNode(status: ChapterStatus, onOpen: (Chapter) -> Unit) {
    val chapter = status.chapter
    when {
        status.isComplete -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Chunky(
                    color = SurfaceColor,
                    shade = LimeShade,
                    border = null,
                    shape = CircleShape,
                    depth = 5.dp,
                    onClick = { onOpen(chapter) },
                    onClickLabel = "See the picture",
                    modifier = Modifier
                        .size(width = 64.dp, height = 69.dp)
                        .semantics { contentDescription = "Chapter ${chapter.number}, ${chapter.title}, complete" },
                ) {
                    Box(Modifier.fillMaxSize().padding(4.dp)) {
                        Image(
                            painter = painterResource(Mascot.chapterThumb(chapter.number)),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Lime),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Navy, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.clearAndSetSemantics { }) {
                    Text("${chapter.number}. ${chapter.title}", style = RuuunText.BodyStrong, color = Navy)
                    Text("Done · tap to see", style = RuuunText.Label, color = LimeText)
                }
            }
        }
        status.isCurrent -> {
            val motion = LocalMotion.current
            val pulse = if (motion) {
                val transition = rememberInfiniteTransition(label = "pulse")
                val t by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1_600, easing = LinearOutSlowInEasing), RepeatMode.Restart),
                    label = "pulse",
                )
                t
            } else {
                0f
            }
            Row(
                Modifier.clearAndSetSemantics {
                    contentDescription = "You are here: chapter ${chapter.number}, ${chapter.title}, " +
                        "${status.runDays} of ${status.runDaysNeeded} runs"
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(width = 76.dp, height = 82.dp)
                        .drawBehind {
                            if (pulse > 0f) {
                                drawCircle(
                                    color = Orange.copy(alpha = 0.45f * (1f - pulse)),
                                    radius = size.minDimension / 2f + 16.dp.toPx() * pulse,
                                    center = center.copy(y = center.y - 3.dp.toPx()),
                                )
                            }
                        },
                ) {
                    Chunky(
                        color = Orange,
                        shade = OrangeShade,
                        border = null,
                        shape = CircleShape,
                        depth = 6.dp,
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${status.runDays.coerceAtMost(status.runDaysNeeded)}/${status.runDaysNeeded}",
                            style = RuuunText.SectionTitle,
                            color = Navy,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Overline("You are here", color = app.ruuun.ui.theme.OrangeText)
                    Text("${chapter.number}. ${chapter.title}", style = RuuunText.BodyStrong, color = Navy)
                }
            }
        }
        else -> {
            Row(
                Modifier.clearAndSetSemantics {
                    contentDescription = "Chapter ${chapter.number}, ${chapter.title}, needs ${status.runDaysNeeded} runs"
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Chunky(
                    color = Track,
                    shade = InputBorder,
                    border = null,
                    shape = CircleShape,
                    depth = 5.dp,
                    modifier = Modifier.size(width = 58.dp, height = 63.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = IconMuted, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${chapter.number}. ${chapter.title}", style = RuuunText.BodyStrong, color = Navy)
                    Text("${status.runDaysNeeded} runs", style = RuuunText.Label, color = TextMuted)
                }
            }
        }
    }
}

/** A grey dashed outline, for worlds in a season that has not opened yet. */
private fun Modifier.dashedOutline(): Modifier = drawWithContent {
    drawContent()
    val stroke = 2.dp.toPx()
    drawRoundRect(
        color = InputBorder,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(18.dp.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
    )
}

/** A finished chapter's picture, full width, with what happened in it. */
@Composable
fun ChapterPictureDialog(chapter: Chapter, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = Shapes.Card, color = SurfaceColor, modifier = Modifier.fillMaxWidth()) {
            // Scrolls, so the text and Close stay reachable in landscape.
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Image(
                    painter = painterResource(Mascot.chapter(chapter.number)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(Mascot.CHAPTER_ASPECT),
                )
                Column(Modifier.padding(16.dp)) {
                    Overline("Chapter ${chapter.number} · ${chapter.world.title}")
                    Spacer(Modifier.height(4.dp))
                    Text(chapter.title, style = RuuunText.CardTitle, color = Navy)
                    Spacer(Modifier.height(4.dp))
                    Text(chapter.revealText, style = RuuunText.Body, color = TextMuted)
                    Spacer(Modifier.height(16.dp))
                    SecondaryButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
