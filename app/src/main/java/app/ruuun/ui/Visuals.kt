package app.ruuun.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.DoubleArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
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

/** Banner colour and icon per world (UI spec, section 2). */
fun World.bannerColor(): Color = when (this) {
    World.ROOKIE_WOODS -> Lime
    World.DESERT_DASH -> SunTint
    World.SKY_KINGDOM -> SkyTint
    World.PLANET_ZOOM -> OrangeTint
    World.CORAL_COAST -> app.ruuun.ui.theme.CoralTint
    World.JUNGLE_RUN -> app.ruuun.ui.theme.JungleTint
    World.FROSTY_PEAKS -> app.ruuun.ui.theme.FrostTint
    World.COMET_RUN -> app.ruuun.ui.theme.CometTint
}

fun World.icon(): ImageVector = when (this) {
    World.ROOKIE_WOODS -> Icons.Outlined.Park
    World.DESERT_DASH -> Icons.Outlined.WbSunny
    World.SKY_KINGDOM -> Icons.Outlined.Cloud
    World.PLANET_ZOOM -> Icons.Outlined.Public
    World.CORAL_COAST -> Icons.Outlined.BeachAccess
    World.JUNGLE_RUN -> Icons.Outlined.Forest
    World.FROSTY_PEAKS -> Icons.Outlined.AcUnit
    World.COMET_RUN -> Icons.Outlined.AutoAwesome
}

/**
 * One colour and one fallback icon per badge category. Each earned badge also has its
 * own icon, in BadgeIcons.kt.
 */
fun BadgeCategory.earnedColor(): Color = when (this) {
    BadgeCategory.FIRST_STEPS, BadgeCategory.RUN_DAYS -> Sky
    BadgeCategory.BEST_OUTING, BadgeCategory.TOTAL_DISTANCE -> Orange
    BadgeCategory.WEEKLY, BadgeCategory.ADVENTURE -> Lime
    BadgeCategory.REFLECTION -> SkyTint
    BadgeCategory.STORY -> Lime
    BadgeCategory.QUESTS, BadgeCategory.SEASONS -> Orange
    BadgeCategory.NON_STOP -> Sky
    BadgeCategory.SECRET -> Lime
}

fun BadgeCategory.icon(): ImageVector = when (this) {
    BadgeCategory.FIRST_STEPS -> Icons.AutoMirrored.Outlined.DirectionsWalk
    BadgeCategory.RUN_DAYS -> Icons.Outlined.CalendarMonth
    BadgeCategory.BEST_OUTING -> Icons.Outlined.Bolt
    BadgeCategory.TOTAL_DISTANCE -> Icons.Outlined.Route
    BadgeCategory.WEEKLY -> Icons.Outlined.EventRepeat
    BadgeCategory.ADVENTURE -> Icons.Outlined.Explore
    BadgeCategory.REFLECTION -> Icons.Outlined.EditNote
    BadgeCategory.STORY -> Icons.AutoMirrored.Outlined.MenuBook
    BadgeCategory.QUESTS -> Icons.Outlined.Flag
    BadgeCategory.SEASONS -> Icons.Outlined.Eco
    BadgeCategory.NON_STOP -> Icons.Outlined.DoubleArrow
    BadgeCategory.SECRET -> Icons.Outlined.Star
}

fun Effort.label(): String = when (this) {
    Effort.EASY -> "Easy"
    Effort.OKAY -> "Okay"
    Effort.HARD -> "Hard"
}

fun Effort.icon(): ImageVector = when (this) {
    Effort.EASY -> Icons.Outlined.SentimentSatisfied
    Effort.OKAY -> Icons.Outlined.SentimentNeutral
    Effort.HARD -> Icons.Outlined.SentimentDissatisfied
}

/** Tint used behind effort pills and selected effort buttons. Never red. */
fun Effort.tint(): Color = when (this) {
    Effort.EASY -> LimeTint
    Effort.OKAY -> SkyTint
    Effort.HARD -> OrangeTint
}
