package app.ruuun.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ruuun.content.BadgeDef
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.logic.Formatters
import app.ruuun.ui.icon
import app.ruuun.ui.label
import app.ruuun.ui.earnedColor
import app.ruuun.ui.earnedShade
import app.ruuun.ui.medalInk
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.ui.draw.drawBehind
import app.ruuun.ui.earnedIcon
import app.ruuun.ui.tint
import app.ruuun.ui.pillColor
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.ui.semantics.clearAndSetSemantics
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.InfoBg
import app.ruuun.ui.theme.InfoText
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.LockedCard
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.OrangeTint
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.Mascot
import kotlinx.coroutines.launch

/**
 * Card with a world banner across the top. With a [picture], the banner is that picture
 * (a world scene or a chapter picture) with the world name on a chip, and [ruu], if
 * given, standing on the right. Without one it is a plain coloured band.
 */
@Composable
fun WorldCard(
    bannerColor: Color,
    bannerIcon: ImageVector,
    worldName: String,
    title: String,
    teaser: String,
    modifier: Modifier = Modifier,
    progressFraction: Float? = null,
    progressLabel: String? = null,
    progressColor: Color = app.ruuun.ui.theme.Orange,
    animateProgress: Boolean = false,
    @DrawableRes picture: Int? = null,
    pictureAspect: Float = WIDE_BANNER_ASPECT,
    @DrawableRes ruu: Int? = null,
    onBlue: Boolean = false,
) {
    Chunky(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = SurfaceColor,
        shade = if (onBlue) app.ruuun.ui.theme.SkyShade else Border,
        border = if (onBlue) null else Border,
        depth = if (onBlue) 7.dp else 5.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (picture != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(pictureAspect)
                        .background(bannerColor),
                ) {
                    Image(
                        painter = painterResource(picture),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                    if (ruu != null) {
                        Image(
                            painter = painterResource(ruu),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 10.dp)
                                .fillMaxHeight(0.97f)
                                .aspectRatio(Mascot.RUU_ASPECT),
                        )
                    }
                    // With Ruu on the right, the chip keeps to the left part so the two never
                    // overlap; a long name at a large text size wraps instead.
                    Surface(
                        shape = Shapes.Pill,
                        color = bannerColor,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(if (ruu != null) CHIP_SHARE_WITH_RUU else 1f)
                            .padding(12.dp)
                            .wrapContentWidth(Alignment.Start),
                    ) {
                        Row(
                            Modifier.padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(bannerIcon, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Overline(worldName, color = Navy)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerColor)
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(bannerIcon, contentDescription = null, tint = Navy, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Overline(worldName, color = Navy)
                }
            }
            Column(Modifier.padding(16.dp)) {
                Text(title, style = RuuunText.CardTitle, color = Navy)
                Spacer(Modifier.height(4.dp))
                Text(teaser, style = RuuunText.Body, color = TextMuted)
                if (progressFraction != null) {
                    Spacer(Modifier.height(12.dp))
                    RuuunProgressBar(
                        fraction = progressFraction,
                        color = progressColor,
                        height = 14.dp,
                        description = progressLabel,
                        animate = animateProgress,
                    )
                    if (progressLabel != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(progressLabel, style = RuuunText.Label, color = TextMuted)
                    }
                }
            }
        }
    }
}

/** How much of the banner's width the world chip may use when Ruu stands on the right. */
private const val CHIP_SHARE_WITH_RUU = 0.56f

/** A short wide strip cut from the middle of a world scene. */
const val WIDE_BANNER_ASPECT = 2f

/** Light blue information panel. */
@Composable
fun InfoPanel(
    text: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    content: @Composable (() -> Unit)? = null,
) {
    Chunky(
        shape = Shapes.Card,
        color = InfoBg,
        shade = app.ruuun.ui.theme.Sky,
        border = null,
        depth = 4.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            if (showIcon) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = InfoText,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(text, style = RuuunText.Body, color = InfoText)
                if (content != null) {
                    Spacer(Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

/** Degrees the badge icon swings through when an earned badge is tapped. */
private val WIGGLE = listOf(-14f, 12f, -9f, 6f, -3f, 0f)

/** One badge in the three-column grid: a coloured medal when earned, a dashed one when not. */
@Composable
fun BadgeCell(
    badge: BadgeDef,
    earned: Boolean,
    progressText: String?,
    modifier: Modifier = Modifier,
    progressFraction: Float? = null,
    onClick: (() -> Unit)? = null,
) {
    val hidden = badge.secret && !earned
    val title = if (hidden) "???" else badge.title
    // An earned badge gives a little wiggle, then opens. Locked ones open straight away.
    val motion = LocalMotion.current
    val scope = rememberCoroutineScope()
    val wiggle = remember { Animatable(0f) }
    val open: () -> Unit = {
        if (onClick != null && !wiggle.isRunning) {
            if (earned && motion) {
                scope.launch {
                    for (angle in WIGGLE) wiggle.animateTo(angle, tween(65))
                    onClick()
                }
            } else {
                onClick()
            }
        }
    }
    Chunky(
        onClick = if (onClick != null) open else null,
        color = if (earned) SurfaceColor else LockedCard,
        shade = if (earned) Border else app.ruuun.ui.theme.InputBorder,
        border = if (earned) Border else null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        depth = if (earned) 5.dp else 3.dp,
        modifier = modifier,
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Medal(
                    badge = badge,
                    earned = earned,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = wiggle.value
                        val grow = 1f + kotlin.math.abs(wiggle.value) / 120f
                        scaleX = grow
                        scaleY = grow
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = RuuunText.Medal,
                    color = Navy,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!earned && progressText != null) {
                    Spacer(Modifier.height(6.dp))
                    if (progressFraction != null && !hidden) {
                        RuuunProgressBar(
                            fraction = progressFraction,
                            color = app.ruuun.ui.theme.Orange,
                            height = 7.dp,
                            trackColor = SurfaceColor,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(progressText, style = RuuunText.Tiny, color = TextMuted, maxLines = 1)
                }
            }
            if (earned) {
                // The lime check, top right.
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(Shapes.Pill)
                        .background(app.ruuun.ui.theme.Lime),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

/** A badge's medal: its category colour with a solid edge, or a dashed outline when locked. */
@Composable
fun Medal(badge: BadgeDef, earned: Boolean, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 58.dp) {
    val hidden = badge.secret && !earned
    if (earned) {
        Chunky(
            color = badge.category.earnedColor(),
            shade = badge.category.earnedShade(),
            border = null,
            shape = androidx.compose.foundation.shape.CircleShape,
            depth = 4.dp,
            // The edge adds to the height, so the face itself stays a circle.
            modifier = modifier.size(width = size, height = size + 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                badge.earnedIcon(),
                contentDescription = null,
                tint = badge.category.medalInk(),
                modifier = Modifier.size(size * 0.48f),
            )
        }
    } else {
        Box(
            modifier
                .size(width = size, height = size + 4.dp)
                .padding(bottom = 4.dp)
                .drawBehind {
                    drawCircle(
                        color = app.ruuun.ui.theme.InputBorder,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                            ),
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (hidden) Icons.Rounded.QuestionMark else Icons.Rounded.Lock,
                contentDescription = null,
                tint = app.ruuun.ui.theme.IconMuted,
                modifier = Modifier.size(size * 0.4f),
            )
        }
    }
}

/** Most stop dots drawn on a run card; more show as "+N". */
private const val MAX_STOP_DOTS = 12

/** A run in History: a date block, the distance, how it felt, a dot per stop, the note. */
@Composable
fun RunRow(log: RunLog, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Chunky(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateBlock(log.date)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Formatters.distance(log.distanceM), style = RuuunText.CardTitle, color = Navy)
                    Spacer(Modifier.width(8.dp))
                    EffortPill(log.effort)
                }
                log.stops?.let { stops ->
                    Spacer(Modifier.height(4.dp))
                    StopDots(stops)
                }
                if (!log.note.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        OneLine(log.note, RuuunText.Label, TextMuted)
                    }
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/** The day of a run as a small chunky calendar block. */
@Composable
fun DateBlock(date: java.time.LocalDate, modifier: Modifier = Modifier) {
    Chunky(
        color = app.ruuun.ui.theme.SkyTint,
        shade = app.ruuun.ui.theme.Sky,
        border = null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        depth = 4.dp,
        modifier = modifier.size(width = 54.dp, height = 60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Formatters.weekday(date), style = RuuunText.Tiny, color = app.ruuun.ui.theme.SkyDeep)
            Text("${date.dayOfMonth}", style = RuuunText.CardTitle, color = Navy)
        }
    }
}

/** A dot for each stop (up to twelve, then "+N"), and the count in words. */
@Composable
fun StopDots(stops: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (stops > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.clearAndSetSemantics { },
            ) {
                repeat(minOf(stops, MAX_STOP_DOTS)) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .clip(Shapes.Pill)
                            .background(app.ruuun.ui.theme.Orange),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        }
        val extra = if (stops > MAX_STOP_DOTS) "+${stops - MAX_STOP_DOTS} · " else ""
        Text(
            extra + if (stops == 0) "Non-stop" else app.ruuun.logic.StopGoal.stopsLabel(stops),
            style = RuuunText.Label,
            color = TextMuted,
        )
    }
}

/** Quest row with its check dot, used on Journey and Run saved. */
@Composable
fun QuestRow(
    label: String,
    description: String,
    progressText: String,
    done: Boolean,
    modifier: Modifier = Modifier,
    background: Color = app.ruuun.ui.theme.QuestRowBg,
) {
    Chunky(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = SurfaceColor,
        depth = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CheckDot(done = done, size = 30.dp)
            Column(Modifier.weight(1f)) {
                Overline(label)
                Text(description, style = RuuunText.Body, color = Navy)
            }
            Text(progressText, style = RuuunText.BodyStrong, color = Navy)
        }
    }
}

/** How a run felt, as a coloured pill with its face. */
@Composable
fun EffortPill(effort: Effort, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(Shapes.Pill)
            .background(effort.pillColor())
            .padding(start = 6.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(effort.icon(), contentDescription = null, tint = Navy, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(effort.label(), style = RuuunText.Pill, color = Navy, maxLines = 1)
    }
}

/** Large three-up effort selector. */
@Composable
fun EffortButton(
    effort: Effort,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Chunky(
        onClick = onSelect,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = if (selected) effort.pillColor() else SurfaceColor,
        shade = if (selected) Navy else Border,
        border = if (selected) Navy else Border,
        borderWidth = if (selected) 3.dp else 2.dp,
        depth = 5.dp,
        role = Role.RadioButton,
        modifier = modifier
            .heightIn(min = 96.dp)
            .semanticsSelected(selected),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(effort.icon(), contentDescription = null, tint = Navy, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text(effort.label(), style = RuuunText.BodyStrong, color = Navy)
        }
    }
}

private fun Modifier.semanticsSelected(isSelected: Boolean): Modifier =
    this.semantics {
        selected = isSelected
        role = Role.RadioButton
    }

