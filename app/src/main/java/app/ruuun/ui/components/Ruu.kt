package app.ruuun.ui.components

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeJoin
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.Mascot
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.Surface as SurfaceColor

/**
 * Ruu at a given height. Decorative: the words beside Ruu always say the same thing.
 * With [breathe], Ruu rises and settles a little, slowly, unless animations are off.
 */
@Composable
fun RuuImage(
    @DrawableRes pose: Int,
    height: Dp,
    modifier: Modifier = Modifier,
    breathe: Boolean = false,
    zoom: Float = 1f,
) {
    val motion = LocalMotion.current
    val scale = if (breathe && motion) {
        val transition = rememberInfiniteTransition(label = "ruu")
        val value by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "breathe",
        )
        value
    } else {
        1f
    }
    Image(
        painter = painterResource(pose),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(height)
            .aspectRatio(Mascot.RUU_ASPECT)
            .graphicsLayer {
                // Breathing grows from the feet, so Ruu stays standing on the same spot.
                // A zoom (for round frames) grows from the head instead, to show the face.
                transformOrigin = if (zoom > 1f) TransformOrigin(0.5f, 0.12f) else TransformOrigin(0.5f, 0.92f)
                scaleX = scale * zoom
                scaleY = scale * zoom
            },
    )
}

/**
 * A white speech bubble with its tail on the left, pointing at Ruu. The card and the tail
 * are one outline, so there is no seam where they meet, and the whole bubble has the same
 * chunky edge underneath as every other card.
 */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    val tail = 10.dp
    val depth = 4.dp
    Box(
        modifier
            .drawBehind {
                val t = tail.toPx()
                val d = depth.toPx()
                val stroke = 2.dp.toPx()
                val body = Size(size.width - t, size.height - d)
                fun bubble(dy: Float): Path {
                    val radius = 18.dp.toPx()
                    val tailHalf = 9.dp.toPx()
                    val midY = body.height / 2f + dy
                    val shape = Path().apply {
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                left = t, top = dy, right = t + body.width, bottom = dy + body.height,
                                cornerRadius = CornerRadius(radius),
                            ),
                        )
                    }
                    val point = Path().apply {
                        moveTo(t + stroke, midY - tailHalf)
                        lineTo(0f, midY)
                        lineTo(t + stroke, midY + tailHalf)
                        close()
                    }
                    return Path.combine(PathOperation.Union, shape, point)
                }
                drawPath(bubble(d), Border)
                val face = bubble(0f)
                drawPath(face, SurfaceColor)
                drawPath(face, Border, style = Stroke(width = stroke, join = StrokeJoin.Round))
            }
            .padding(start = tail, bottom = depth),
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(text, style = RuuunText.BodyStrong, color = Navy)
        }
    }
}

