package app.ruuun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Light theme only for v1. The mockup has no dark mode.
private val RuuunColors = lightColorScheme(
    primary = Orange,            // main CTA fill
    onPrimary = Navy,
    secondary = Sky,             // XP bar, active nav indicator
    onSecondary = Navy,
    tertiary = Lime,             // completed checks, earned pills
    onTertiary = Navy,
    background = Ground,
    onBackground = Navy,
    surface = Surface,
    onSurface = Navy,
    surfaceVariant = SegmentBg,
    onSurfaceVariant = TextMuted,
    outline = InputBorder,
    outlineVariant = Border,
    inverseSurface = Navy,       // hero and celebration panels
    inverseOnSurface = Ground,
    error = Destructive,
    onError = Surface,
)

@Composable
fun RuuunTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RuuunColors, typography = RuuunTypography, content = content)
}
