package app.ruuun.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Surface as SurfaceColor

/** The depth a pressed surface keeps, so it still reads as a button while held. */
private val PRESSED_DEPTH = 2.dp

/**
 * The chunky surface every card, chip and button is built on: a solid [color] face over
 * a darker [shade] that shows as a thick bottom edge, so it looks like it can be pushed.
 * When [onClick] is set, pressing moves the face down onto the edge. Nothing here uses
 * a blurred shadow.
 *
 * The outer size never changes while pressed, so nothing around it jumps.
 */
@Composable
fun Chunky(
    modifier: Modifier = Modifier,
    color: Color = SurfaceColor,
    shade: Color = Border,
    shape: Shape = Shapes.Card,
    depth: Dp = 5.dp,
    border: Color? = if (color == SurfaceColor) Border else null,
    borderWidth: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    role: Role = Role.Button,
    onClickLabel: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = LocalMotion.current
    val shown by animateDpAsState(
        targetValue = if (pressed && onClick != null && enabled) PRESSED_DEPTH.coerceAtMost(depth) else depth,
        animationSpec = if (motion) tween(80) else snap(),
        label = "press",
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction,
            // The face moving down is the feedback; a flat surface gets the usual ripple.
            indication = if (depth == 0.dp) LocalIndication.current else null,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
    } else {
        Modifier
    }
    // Min constraints pass through, so a fixed or full-width size reaches the face.
    Box(modifier.then(clickModifier), propagateMinConstraints = true) {
        // The edge: the face's own shape in the shade colour, dropped by the full depth, so
        // only a crescent shows below. A circle stays a circle with a circle under it.
        Box(
            Modifier
                .matchParentSize()
                .padding(top = depth)
                .clip(shape)
                .background(shade),
        )
        Box(
            Modifier
                .padding(top = (depth - shown).coerceAtLeast(0.dp), bottom = shown.coerceIn(0.dp, depth))
                .clip(shape)
                .background(color)
                .then(if (border != null) Modifier.border(borderWidth, border, shape) else Modifier),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}
