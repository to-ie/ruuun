package app.ruuun.ui.components

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.ruuun.R
import app.ruuun.ui.LocalMotion
import app.ruuun.ui.Mascot
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.Sky

private val RingOuter = Color(0xFF1A2540)
private val RingInner = Color(0xFF223052)

/**
 * The "night navy" splash: Ruu in two halo rings, the ruuun! name, and three hopping
 * dots, shown while the app reads its data. Android draws its own plain splash first
 * (Ruu's head on navy, in the same inner-ring colour), so this one carries straight on
 * from it. Ruu bobs and the dots hop unless animations are off.
 */
@Composable
fun RuuunSplash() {
    LightStatusIconsWhileShown()
    val motion = LocalMotion.current
    val transition = rememberInfiniteTransition(label = "splash")
    val bob = if (motion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1_200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bob",
        ).value
    } else {
        0.5f
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .semantics { contentDescription = "ruuun! is starting" },
    ) {
        Column(
            Modifier.align(Alignment.Center).padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(300.dp).clip(CircleShape).background(RingOuter))
                Box(Modifier.size(240.dp).clip(CircleShape).background(RingInner))
                Box(
                    Modifier
                        .size(220.dp)
                        .graphicsLayer {
                            // 0 to -8 dp up, tilting from -2 to 2 degrees, over 2.4 s.
                            translationY = -8.dp.toPx() * kotlin.math.sin(bob * Math.PI).toFloat()
                            rotationZ = -2f + 4f * bob
                        }
                        .clip(CircleShape)
                        .background(Navy),
                ) {
                    Image(
                        painter = painterResource(Mascot.FACE_HAPPY),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().padding(top = 14.dp),
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            Image(
                painter = painterResource(R.drawable.ruuun_wordmark),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(250.dp),
            )
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 70.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(Orange, Lime, Sky).forEachIndexed { index, colour ->
                val hop = if (motion) {
                    transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 900
                                0f at 0
                                1f at 360 using FastOutSlowInEasing
                                0f at 900
                            },
                            initialStartOffset = StartOffset(index * 150),
                        ),
                        label = "dot$index",
                    ).value
                } else {
                    0f
                }
                Box(
                    Modifier
                        .size(14.dp)
                        .graphicsLayer { translationY = -10.dp.toPx() * hop }
                        .clip(CircleShape)
                        .background(colour),
                )
            }
        }
    }
}

/** Status bar icons turn light on the navy splash, and dark again when it goes. */
@Composable
private fun LightStatusIconsWhileShown() {
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity) {
        val component = activity as? ComponentActivity
        component?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        onDispose {
            component?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            )
        }
    }
}
