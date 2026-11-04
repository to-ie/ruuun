package app.ruuun.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.ruuun.content.BadgeCategory
import app.ruuun.content.World
import app.ruuun.data.Effort
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.LimeTint
import app.ruuun.ui.theme.Orange
import app.ruuun.ui.theme.OrangeTint
import app.ruuun.ui.theme.Sky
import app.ruuun.ui.theme.SkyTint
import app.ruuun.ui.theme.SunTint

/** Colour and icon per world (1.5 design system). Text on these is always Navy. */
fun World.bannerColor(): Color = when (this) {
    World.ROOKIE_WOODS -> Lime
    World.DESERT_DASH -> SunTint
    World.SKY_KINGDOM -> app.ruuun.ui.theme.SkyWorld
    World.PLANET_ZOOM -> app.ruuun.ui.theme.PlanetTint
    World.CORAL_COAST -> app.ruuun.ui.theme.CoralTint
    World.JUNGLE_RUN -> app.ruuun.ui.theme.JungleTint
    World.FROSTY_PEAKS -> app.ruuun.ui.theme.FrostTint
    World.COMET_RUN -> app.ruuun.ui.theme.CometTint
}

fun World.icon(): ImageVector = when (this) {
    World.ROOKIE_WOODS -> Icons.Rounded.Forest
    World.DESERT_DASH -> Icons.Rounded.WbSunny
    World.SKY_KINGDOM -> Icons.Rounded.Cloud
    World.PLANET_ZOOM -> Icons.Rounded.Public
    World.CORAL_COAST -> Icons.Rounded.BeachAccess
    World.JUNGLE_RUN -> Icons.Rounded.Park
    World.FROSTY_PEAKS -> Icons.Rounded.AcUnit
    World.COMET_RUN -> Icons.Rounded.AutoAwesome
}

/**
 * One medal colour per badge category, its darker edge, and the ink for the icon on it.
 * Each earned badge also has its own icon, in BadgeIcons.kt.
 */
fun BadgeCategory.earnedColor(): Color = when (this) {
    BadgeCategory.FIRST_STEPS -> Sky
    BadgeCategory.RUN_DAYS -> Color(0xFFB9A4FF)
    BadgeCategory.BEST_OUTING -> Orange
    BadgeCategory.TOTAL_DISTANCE -> Color(0xFFFFC83D)
    BadgeCategory.WEEKLY -> Lime
    BadgeCategory.ADVENTURE -> Color(0xFF4FD08A)
    BadgeCategory.REFLECTION -> Color(0xFFFFA8CF)
    BadgeCategory.STORY -> Color(0xFFF2B880)
    BadgeCategory.QUESTS -> Color(0xFFFF9C8A)
    BadgeCategory.SEASONS -> Color(0xFF7FE0D3)
    BadgeCategory.NON_STOP -> Color(0xFF7FB2FF)
    BadgeCategory.SECRET -> app.ruuun.ui.theme.Navy
}

fun BadgeCategory.earnedShade(): Color = when (this) {
    BadgeCategory.FIRST_STEPS -> app.ruuun.ui.theme.SkyShade
    BadgeCategory.RUN_DAYS -> Color(0xFF8A6FE8)
    BadgeCategory.BEST_OUTING -> app.ruuun.ui.theme.OrangeShade
    BadgeCategory.TOTAL_DISTANCE -> Color(0xFFD9A200)
    BadgeCategory.WEEKLY -> app.ruuun.ui.theme.LimeShade
    BadgeCategory.ADVENTURE -> Color(0xFF2AA566)
    BadgeCategory.REFLECTION -> Color(0xFFE0709F)
    BadgeCategory.STORY -> Color(0xFFC98A4B)
    BadgeCategory.QUESTS -> Color(0xFFE0604F)
    BadgeCategory.SEASONS -> Color(0xFF3BB5A5)
    BadgeCategory.NON_STOP -> Color(0xFF3D7BE0)
    BadgeCategory.SECRET -> Color.Black
}

/** Navy on every medal (white would be too faint), lime on the black Secret one. */
fun BadgeCategory.medalInk(): Color =
    if (this == BadgeCategory.SECRET) Lime else app.ruuun.ui.theme.Navy

fun BadgeCategory.icon(): ImageVector = when (this) {
    BadgeCategory.FIRST_STEPS -> Icons.AutoMirrored.Rounded.DirectionsWalk
    BadgeCategory.RUN_DAYS -> Icons.Rounded.CalendarMonth
    BadgeCategory.BEST_OUTING -> Icons.Rounded.Bolt
    BadgeCategory.TOTAL_DISTANCE -> Icons.Rounded.Route
    BadgeCategory.WEEKLY -> Icons.Rounded.EventRepeat
    BadgeCategory.ADVENTURE -> Icons.Rounded.Explore
    BadgeCategory.REFLECTION -> Icons.Rounded.EditNote
    BadgeCategory.STORY -> Icons.AutoMirrored.Rounded.MenuBook
    BadgeCategory.QUESTS -> Icons.Rounded.Flag
    BadgeCategory.SEASONS -> Icons.Rounded.Eco
    BadgeCategory.NON_STOP -> Icons.Rounded.DoubleArrow
    BadgeCategory.SECRET -> Icons.Rounded.Star
}

fun Effort.label(): String = when (this) {
    Effort.EASY -> "Easy"
    Effort.OKAY -> "Okay"
    Effort.HARD -> "Hard"
}

fun Effort.icon(): ImageVector = when (this) {
    Effort.EASY -> Icons.Rounded.SentimentSatisfied
    Effort.OKAY -> Icons.Rounded.SentimentNeutral
    Effort.HARD -> Icons.Rounded.SentimentDissatisfied
}

/** The brighter pill colour for how a run felt, as on History. Never red. */
fun Effort.pillColor(): Color = when (this) {
    Effort.EASY -> Lime
    Effort.OKAY -> app.ruuun.ui.theme.SkyWorld
    Effort.HARD -> OrangeTint
}

/** Tint used behind selected effort buttons. Never red. */
fun Effort.tint(): Color = when (this) {
    Effort.EASY -> LimeTint
    Effort.OKAY -> SkyTint
    Effort.HARD -> OrangeTint
}
