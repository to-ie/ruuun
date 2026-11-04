package app.ruuun.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.InputBorder
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.OrangeShade
import app.ruuun.ui.theme.LimeShade
import app.ruuun.ui.theme.SkyShade
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.SegmentBg
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import app.ruuun.ui.LocalMotion

/** Uppercase section label. Rendered uppercase, never relying on the source string. */
@Composable
fun Overline(text: String, color: Color = TextMuted, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = RuuunText.Overline,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = RuuunText.SectionTitle, color = Navy, modifier = modifier)
}

/** Full-width orange call to action, chunky, in capitals. Text is Navy: white on orange is too faint. */
@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    Chunky(
        onClick = onClick,
        enabled = enabled,
        color = if (enabled) Orange else Track,
        shade = if (enabled) OrangeShade else InputBorder,
        shape = Shapes.Button,
        depth = 6.dp,
        border = null,
        // Read in normal case: TalkBack can spell out words written in capitals.
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = text
                if (!enabled) disabled()
            },
    ) {
        // On a narrow button (a small phone, large text) the icon gives way to the words.
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val showIcon = icon != null && maxWidth >= 210.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (compact) 52.dp else Dimens.CtaHeight)
                .padding(horizontal = 12.dp, vertical = if (compact) 6.dp else 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val ink = if (enabled) Navy else TextMuted
            if (showIcon && icon != null) {
                Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(if (compact) 26.dp else 24.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text.uppercase(),
                style = if (compact) RuuunText.Cta.copy(fontSize = 19.sp) else RuuunText.Cta,
                color = ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        }
    }
}

/**
 * White chunky button. [onNavy] is for buttons sitting on a coloured header: the depth
 * edge turns blue there, as the design asks for things on Sky.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onNavy: Boolean = false,
    lowEmphasis: Boolean = false,
    enabled: Boolean = true,
    /** Matches [PrimaryCta] in height, depth and lettering, for a pair side by side. */
    large: Boolean = false,
) {
    Chunky(
        onClick = onClick,
        enabled = enabled,
        color = SurfaceColor,
        shade = if (onNavy) SkyShade else if (lowEmphasis) Border else Navy,
        border = if (onNavy) null else if (lowEmphasis) Border else Navy,
        shape = Shapes.Button,
        depth = if (large) 6.dp else 4.dp,
        modifier = modifier.semantics {
            contentDescription = text
            if (!enabled) disabled()
        },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = if (large) Dimens.CtaHeight else Dimens.SecondaryButtonHeight)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val ink = if (enabled) Navy else TextMuted
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                if (large) text.uppercase() else text,
                style = if (large) RuuunText.Cta.copy(fontSize = 19.sp) else RuuunText.BodyStrong,
                color = ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}

/** The standard white card: chunky, with a 2 dp border and a thicker bottom edge. */
@Composable
fun RuuunCard(
    modifier: Modifier = Modifier,
    shape: Shape = Shapes.Card,
    color: Color = SurfaceColor,
    border: Color? = Border,
    padding: androidx.compose.ui.unit.Dp = Dimens.CardPadding,
    onClick: (() -> Unit)? = null,
    shade: Color = border ?: Border,
    depth: androidx.compose.ui.unit.Dp = 5.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Chunky(
        modifier = modifier.fillMaxWidth(),
        color = color,
        shade = shade,
        border = border,
        shape = shape,
        depth = depth,
        onClick = onClick,
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

/** Rounded progress bar with value semantics for TalkBack. */
@Composable
fun RuuunProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = Sky,
    height: androidx.compose.ui.unit.Dp = Dimens.ProgressHeight,
    trackColor: Color = Track,
    description: String? = null,
    animate: Boolean = false,
) {
    val safe = fraction.coerceIn(0f, 1f)
    // When asked, fill from empty on first show and glide to any new value after that.
    val moving = animate && LocalMotion.current
    val shown = remember { Animatable(if (moving) 0f else safe) }
    LaunchedEffect(safe, moving) {
        if (moving) shown.animateTo(safe, tween(800, easing = FastOutSlowInEasing)) else shown.snapTo(safe)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(Shapes.Pill)
            .background(trackColor)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(safe, 0f..1f)
                if (description != null) contentDescription = description
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth(shown.value)
                .fillMaxHeight()
                .clip(Shapes.Pill)
                .background(color),
        )
    }
}

/** A chunky stat tile: an optional icon, a big value and its label. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = SurfaceColor,
    contentColor: Color = Navy,
    shade: Color = if (color == SurfaceColor) SkyShade else shadeOf(color),
    icon: ImageVector? = null,
    iconTint: Color = SkyShade,
) {
    Chunky(
        color = color,
        shade = shade,
        border = null,
        shape = Shapes.Card,
        depth = 6.dp,
        modifier = modifier.semantics(mergeDescendants = true) {},
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
            }
            Text(value, style = RuuunText.StatValue, color = contentColor)
            Text(label, style = RuuunText.Label, color = contentColor)
        }
    }
}

/** Weekly goal pip: lime with a check when done, a plain grey circle when not. Never red. */
@Composable
fun CheckDot(done: Boolean, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 32.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(Shapes.Pill)
            .background(if (done) Lime else Track),
        contentAlignment = Alignment.Center,
    ) {
        if (done) ThickCheck(Modifier.size(size * 0.62f))
    }
}

/** A bold, round-ended tick: heavier than the icon font's, so it reads at a glance. */
@Composable
fun ThickCheck(modifier: Modifier = Modifier, color: Color = Navy) {
    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.2f, h * 0.53f)
            lineTo(w * 0.42f, h * 0.74f)
            lineTo(w * 0.82f, h * 0.3f)
        }
        drawPath(
            path,
            color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = w * 0.17f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

/** Equal-width radio segments; the chosen one is a small chunky tile. */
@Composable
fun <T> SegmentedPicker(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SegmentBg)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Chunky(
                onClick = { onSelect(option) },
                color = if (isSelected) SurfaceColor else Color.Transparent,
                shade = if (isSelected) Border else Color.Transparent,
                border = if (isSelected) Border else null,
                shape = Shapes.Segment,
                depth = 3.dp,
                role = Role.RadioButton,
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics { this.selected = isSelected },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 46.dp)
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label(option), style = RuuunText.BodyStrong, color = Navy, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** Chunky pill toggle used for movement mode (single select), tags and filters. */
@Composable
fun ToggleChip(
    text: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    selectedFill: Color = Navy,
    selectedContent: Color = SurfaceColor,
    icon: ImageVector? = null,
) {
    Chunky(
        onClick = onToggle,
        color = if (selected) selectedFill else SurfaceColor,
        shade = if (selected) shadeOf(selectedFill) else Border,
        border = if (selected) null else Border,
        shape = Shapes.Pill,
        depth = 4.dp,
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val ink = if (selected) selectedContent else Navy
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, style = RuuunText.BodyStrong, color = ink, maxLines = 1)
        }
    }
}

/** The darker edge that goes under a coloured face. */
fun shadeOf(color: Color): Color = when (color) {
    Orange -> OrangeShade
    Lime -> LimeShade
    Sky -> SkyShade
    Navy -> Color.Black
    SurfaceColor -> Border
    else -> Color(
        red = color.red * 0.8f,
        green = color.green * 0.8f,
        blue = color.blue * 0.8f,
        alpha = color.alpha,
    )
}

/** Small status pill, e.g. `Earned`, `12/15`, effort labels. */
@Composable
fun Pill(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Navy,
) {
    Box(
        modifier = modifier
            .clip(Shapes.Pill)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = RuuunText.Pill, color = contentColor, maxLines = 1)
    }
}

/** Key/value row used in Settings. */
@Composable
fun SettingsRow(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    tint: Color = Navy,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val base = modifier.fillMaxWidth().defaultMinSize(minHeight = 64.dp)
    Row(
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Icon(leading, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
            Text(title, style = RuuunText.BodyStrong, color = tint)
            if (subtitle != null) {
                Text(subtitle, style = RuuunText.Secondary, color = TextMuted)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

/** Decorative element that must stay out of the accessibility tree. */
fun Modifier.decorative(): Modifier = this.clearAndSetSemantics { }

/** Truncating one-line body text. */
@Composable
fun OneLine(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    Text(text, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}
