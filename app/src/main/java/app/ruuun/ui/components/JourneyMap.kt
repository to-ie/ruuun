package app.ruuun.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.ruuun.logic.WorldStatus
import app.ruuun.ui.bannerColor
import app.ruuun.ui.icon
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.InfoBg
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Track
import app.ruuun.ui.theme.Surface as SurfaceColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import app.ruuun.ui.LocalMotion

/**
 * One world and its three chapters, so the whole story is visible at a glance:
 * what is finished, what is in progress and how far off the rest is.
 *
 * A finished world folds down to its banner so the current one stays near the top;
 * tapping the banner opens it again. Open or closed survives rotation, not restarts.
 */
@Composable
fun WorldJourneyCard(status: WorldStatus, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(status.world.name) { mutableStateOf(false) }
    val foldable = status.isComplete
    val folded = foldable && !expanded
    val motion = LocalMotion.current
    val chevron by animateFloatAsState(
        targetValue = if (folded) 0f else 180f,
        animationSpec = if (motion) tween(300) else snap(),
        label = "chevron",
    )

    Surface(
        shape = Shapes.Card,
        color = SurfaceColor,
        border = BorderStroke(if (status.isCurrent) 2.dp else 1.dp, if (status.isCurrent) Navy else Border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            val bannerModifier = if (foldable) {
                Modifier
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (folded) "Show chapters" else "Hide chapters",
                    ) { expanded = !expanded }
                    .semantics {
                        contentDescription = "${status.world.title}, all ${status.total} chapters complete"
                        stateDescription = if (folded) "Folded" else "Open"
                    }
            } else {
                Modifier
            }
            Row(
                bannerModifier
                    .fillMaxWidth()
                    .background(if (status.isLocked) Track else status.world.bannerColor())
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    status.world.icon(),
                    contentDescription = null,
                    tint = if (status.isLocked) TextMuted else Navy,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(10.dp))
                Overline(
                    status.world.title,
                    color = if (status.isLocked) TextMuted else Navy,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (foldable) "Done · ${status.completed} of ${status.total}"
                    else "${status.completed} of ${status.total}",
                    style = RuuunText.Label,
                    color = if (status.isLocked) TextMuted else Navy,
                )
                if (foldable) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = chevron },
                    )
                }
            }

            AnimatedVisibility(
                visible = !folded,
                enter = if (motion) expandVertically(expandFrom = Alignment.Top) + fadeIn() else EnterTransition.None,
                exit = if (motion) shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut() else ExitTransition.None,
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    status.chapters.forEach { chapter ->
                        val label = "Chapter ${chapter.chapter.number}, ${chapter.chapter.title}, " +
                            when {
                                chapter.isComplete -> "complete"
                                chapter.isCurrent ->
                                    "in progress, ${chapter.runDays} of ${chapter.runDaysNeeded} runs"
                                else -> "needs ${chapter.runDaysNeeded} runs"
                            }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Input)
                                .background(if (chapter.isCurrent) InfoBg else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .semantics { contentDescription = label },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusDot(
                                complete = chapter.isComplete,
                                current = chapter.isCurrent,
                                modifier = Modifier.clearAndSetSemantics { },
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${chapter.chapter.number}. ${chapter.chapter.title}",
                                    style = RuuunText.Body,
                                    color = if (chapter.isComplete || chapter.isCurrent) Navy else TextMuted,
                                )
                                if (chapter.isCurrent) {
                                    Spacer(Modifier.height(6.dp))
                                    RuuunProgressBar(
                                        fraction = chapter.fraction,
                                        color = Navy,
                                        height = 6.dp,
                                        description = null,
                                        animate = true,
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (chapter.isCurrent) {
                                    "${chapter.runDays}/${chapter.runDaysNeeded}"
                                } else {
                                    "${chapter.runDaysNeeded} runs"
                                },
                                style = RuuunText.Label,
                                color = TextMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(complete: Boolean, current: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(Shapes.Pill)
            .background(
                when {
                    complete -> Lime
                    current -> Navy
                    else -> Track
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            complete -> Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = Navy,
                modifier = Modifier.size(16.dp),
            )
            current -> Box(Modifier.size(9.dp).clip(Shapes.Pill).background(Ground))
            else -> Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}
