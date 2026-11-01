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
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.HeroOutline
import app.ruuun.ui.theme.InputBorder
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.PipDashed
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.SegmentBg
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor

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

/** Full-width orange call to action. Taps are debounced by the caller's enabled flag. */
@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = Shapes.Button,
        color = if (enabled) Orange else Track,
        contentColor = if (enabled) Navy else TextMuted,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = Dimens.CtaHeight)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = RuuunText.Cta, textAlign = TextAlign.Center)
        }
    }
}

/** Outlined button. [onNavy] switches to the hero/celebration treatment. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onNavy: Boolean = false,
    lowEmphasis: Boolean = false,
    enabled: Boolean = true,
) {
    val borderColor = when {
        onNavy -> HeroOutline
        lowEmphasis -> InputBorder
        else -> Navy
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = Shapes.Button,
        color = if (onNavy) Color.Transparent else SurfaceColor,
        contentColor = if (onNavy) Ground else Navy,
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = Dimens.SecondaryButtonHeight)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = RuuunText.BodyStrong, textAlign = TextAlign.Center)
        }
    }
}

/** The standard white card: 1 dp border, 20 dp corners, no shadow. */
@Composable
fun RuuunCard(
    modifier: Modifier = Modifier,
    shape: Shape = Shapes.Card,
    color: Color = SurfaceColor,
    border: Color? = Border,
    padding: androidx.compose.ui.unit.Dp = Dimens.CardPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val base = modifier.fillMaxWidth()
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    Surface(
        shape = shape,
        color = color,
        border = border?.let { BorderStroke(1.dp, it) },
        modifier = clickable,
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
) {
    val safe = fraction.coerceIn(0f, 1f)
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
                .fillMaxWidth(safe)
                .fillMaxHeight()
                .clip(Shapes.Pill)
                .background(color),
        )
    }
}

/** One of the three Home counters. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = SurfaceColor,
    contentColor: Color = Navy,
) {
    Surface(
        shape = Shapes.Tile,
        color = color,
        contentColor = contentColor,
        border = if (color == SurfaceColor) BorderStroke(1.dp, Border) else null,
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = RuuunText.StatValue, color = contentColor)
            Text(
                label,
                style = RuuunText.Label,
                color = if (contentColor == Navy) TextMuted else Ground,
            )
        }
    }
}

/** Weekly goal pip. Filled lime when done, dashed outline when not. Never red. */
@Composable
fun CheckDot(done: Boolean, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 32.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(Shapes.Pill)
            .background(if (done) Lime else SurfaceColor)
            .border(
                width = 2.dp,
                color = if (done) Navy else PipDashed,
                shape = Shapes.Pill,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = Navy,
                modifier = Modifier.size(size * 0.6f),
            )
        }
    }
}

/** Equal-width radio segments. */
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
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelect(option) },
                shape = Shapes.Segment,
                color = if (isSelected) SurfaceColor else Color.Transparent,
                contentColor = Navy,
                shadowElevation = if (isSelected) 1.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        role = Role.RadioButton
                        this.selected = isSelected
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label(option), style = RuuunText.BodyStrong, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** Pill toggle used for movement mode (single select) and tags (multi select). */
@Composable
fun ToggleChip(
    text: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    selectedFill: Color = Navy,
    selectedContent: Color = Ground,
    icon: ImageVector? = null,
) {
    Surface(
        onClick = onToggle,
        shape = Shapes.Pill,
        color = if (selected) selectedFill else SurfaceColor,
        contentColor = if (selected) selectedContent else Navy,
        border = BorderStroke(2.dp, if (selected) Navy else InputBorder),
        modifier = modifier
            .heightIn(min = 44.dp)
            .semantics { this.selected = selected },
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, style = RuuunText.Body)
        }
    }
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
