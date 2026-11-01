package app.ruuun.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import app.ruuun.ui.tint
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

/** Card with a coloured world banner across the top. */
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
    progressColor: Color = Navy,
) {
    Surface(
        shape = Shapes.Card,
        color = SurfaceColor,
        border = BorderStroke(1.dp, Border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
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
            Column(Modifier.padding(16.dp)) {
                Text(title, style = RuuunText.CardTitle, color = Navy)
                Spacer(Modifier.height(4.dp))
                Text(teaser, style = RuuunText.Body, color = TextMuted)
                if (progressFraction != null) {
                    Spacer(Modifier.height(12.dp))
                    RuuunProgressBar(
                        fraction = progressFraction,
                        color = progressColor,
                        description = progressLabel,
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

/** Light blue information panel. */
@Composable
fun InfoPanel(
    text: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    content: @Composable (() -> Unit)? = null,
) {
    Surface(shape = Shapes.Card, color = InfoBg, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            if (showIcon) {
                Icon(
                    Icons.Outlined.Info,
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

/** One badge in the three-column grid. */
@Composable
fun BadgeCell(
    badge: BadgeDef,
    earned: Boolean,
    progressText: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val hidden = badge.secret && !earned
    val title = if (hidden) "???" else badge.title
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = if (earned) SurfaceColor else LockedCard,
        border = BorderStroke(if (earned) 2.dp else 1.dp, if (earned) Navy else Border),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.BadgeIcon)
                    .clip(Shapes.Pill)
                    .background(if (earned) badge.category.earnedColor() else Border),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (earned) badge.category.icon() else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (earned) Navy else TextMuted,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = RuuunText.Label,
                color = Navy,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (progressText != null) {
                Spacer(Modifier.height(8.dp))
                Pill(
                    text = progressText,
                    background = when {
                        earned -> Lime
                        hidden -> Track
                        else -> OrangeTint
                    },
                )
            }
        }
    }
}

/** A row in History. */
@Composable
fun RunRow(log: RunLog, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, Border),
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.width(46.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(Formatters.weekday(log.date), style = RuuunText.Pill, color = TextMuted)
                Text("${log.date.dayOfMonth}", style = RuuunText.CardTitle, color = Navy)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Formatters.distance(log.distanceM),
                        style = RuuunText.BodyStrong,
                        color = Navy,
                    )
                    Spacer(Modifier.width(8.dp))
                    Pill(log.effort.label(), log.effort.tint())
                }
                if (!log.note.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    OneLine(log.note, RuuunText.Secondary, TextMuted)
                }
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Quest row with its check dot, used on Missions and Run saved. */
@Composable
fun QuestRow(
    label: String,
    description: String,
    progressText: String,
    done: Boolean,
    modifier: Modifier = Modifier,
    background: Color = app.ruuun.ui.theme.QuestRowBg,
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = background,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(14.dp),
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

/** Effort tint helper used by the pill above. */
@Composable
fun EffortPill(effort: Effort, modifier: Modifier = Modifier) {
    Pill(effort.label(), effort.tint(), modifier)
}

/** Large three-up effort selector. */
@Composable
fun EffortButton(
    effort: Effort,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSelect,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = if (selected) effort.tint() else SurfaceColor,
        border = BorderStroke(if (selected) 3.dp else 2.dp, if (selected) Navy else Border),
        modifier = modifier
            .heightIn(min = 92.dp)
            .semanticsSelected(selected),
    ) {
        Column(
            Modifier.padding(12.dp),
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

/** Empty-ish helper used by Home and the celebration: a label above a big value. */
@Composable
fun LabelledValue(
    overline: String,
    value: String,
    modifier: Modifier = Modifier,
    overlineColor: Color = TextMuted,
    valueColor: Color = Navy,
) {
    Column(modifier) {
        Overline(overline, color = overlineColor)
        Spacer(Modifier.height(2.dp))
        Text(value, style = RuuunText.CardTitle, color = valueColor)
    }
}

/** Ground-coloured spacer used between stacked cards. */
@Composable
fun SectionSpacer() {
    Spacer(Modifier.height(Dimens.SectionGap))
}
