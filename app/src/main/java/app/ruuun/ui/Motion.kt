package app.ruuun.ui

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Whether movement should play. False when the phone's "Remove animations" setting is
 * on, and on screens that have already played their entrance (after a rotation, say).
 * Every animation in the app checks this, and every screen reads the same without it.
 */
val LocalMotion = compositionLocalOf { true }

/** Android's "Remove animations" sets the animator duration scale to zero. */
fun Context.motionAllowed(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

/** A short buzz. Needs no permission and follows the phone's own touch-feedback setting. */
fun View.buzz(strong: Boolean = false) {
    val type = when {
        strong -> HapticFeedbackConstants.LONG_PRESS
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
        else -> HapticFeedbackConstants.VIRTUAL_KEY
    }
    performHapticFeedback(type)
}

/** The bouncy spring shared by everything that pops. */
val PopSpring = spring<Float>(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)

/**
 * Pops [content] in from nothing with a little overshoot after [delayMillis]. Drawing
 * only: the layout never moves, so nothing jumps around it.
 */
@Composable
fun PopIn(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val animate = LocalMotion.current
    val scale = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!animate) return@LaunchedEffect
        delay(delayMillis.toLong())
        scale.animateTo(1f, PopSpring)
    }
    Box(
        modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            alpha = scale.value.coerceIn(0f, 1f)
        },
        propagateMinConstraints = true,
    ) { content() }
}

/** Unfolds [content] downwards from its top edge, like a banner rolling open. */
@Composable
fun Unfold(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val animate = LocalMotion.current
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!animate) return@LaunchedEffect
        delay(delayMillis.toLong())
        progress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    Box(
        modifier.graphicsLayer {
            transformOrigin = TransformOrigin(0.5f, 0f)
            scaleY = progress.value
            alpha = progress.value
        },
        propagateMinConstraints = true,
    ) { content() }
}
