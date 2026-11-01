package app.ruuun.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Dimens {
    val ScreenPadding = 20.dp
    val SectionGap = 16.dp      // between cards on a screen
    val CardPadding = 16.dp
    val HeroPadding = 20.dp
    val ItemGap = 10.dp         // grid and list item gap
    val MinTouch = 48.dp        // spec minimum; chips may be 44.dp only inside larger rows
    val CtaHeight = 56.dp
    val SecondaryButtonHeight = 48.dp
    val InputHeight = 52.dp
    val DistanceInputHeight = 64.dp
    val ProgressHeight = 10.dp
    val XpBarHeight = 12.dp
    val NavHeight = 72.dp
    val NavIndicatorWidth = 56.dp
    val NavIndicatorHeight = 32.dp
    val LogoHeaderHeight = 48.dp
    val BadgeIcon = 52.dp
}

object Shapes {
    val Hero = RoundedCornerShape(24.dp)
    val Card = RoundedCornerShape(20.dp)
    val Tile = RoundedCornerShape(18.dp)
    val Button = RoundedCornerShape(16.dp)
    val Input = RoundedCornerShape(14.dp)
    val Segment = RoundedCornerShape(10.dp)
    val Pill = RoundedCornerShape(999.dp)
}
