package app.ruuun.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette (from handoff v2, section 2)
val Navy = Color(0xFF101C2E)
val Sky = Color(0xFF49C6FF)
val Orange = Color(0xFFFF8C24)
val Lime = Color(0xFFC9F54A)
val Ground = Color(0xFFF7F9FC)      // app background ("white" in the brief)
val Surface = Color(0xFFFFFFFF)     // cards, header, bottom nav

// Neutrals
val Border = Color(0xFFDDE4EE)       // card and divider borders
val InputBorder = Color(0xFFC5CFDD)  // idle inputs and secondary buttons
val TextMuted = Color(0xFF4B5872)    // secondary text, 7:1 on white
val Track = Color(0xFFE3E9F2)        // progress track
val SegmentBg = Color(0xFFE9EEF5)    // segmented control background
val IconButtonBg = Color(0xFFEEF3F9) // header gear button
val QuestRowBg = Color(0xFFF2F6FB)
val LockedCard = Color(0xFFF0F3F8)
val PipDashed = Color(0xFF8A97AD)    // empty weekly pip outline
val Link = Color(0xFF0B5E9C)

// Tints (backgrounds only, always with Navy text)
val SkyTint = Color(0xFFE1F5FF)      // "Okay" effort
val InfoBg = Color(0xFFE6F7FF)       // info and next-mission panels
val InfoText = Color(0xFF1F2E45)
val InfoLabel = Color(0xFF0B4A7A)
val LimeTint = Color(0xFFE8FBC2)     // "Easy" effort
val OrangeTint = Color(0xFFFFE4CC)   // "Hard" effort, badge icon bg, progress pill
val OrangeTintLight = Color(0xFFFFF1E3) // selected setup plan card
val SunTint = Color(0xFFFFE9A8)      // Desert Dash world banner
val CoralTint = Color(0xFFFFD9D1)    // Coral Coast world banner (season 2)
val JungleTint = Color(0xFFCFEFD6)   // Jungle Run world banner
val FrostTint = Color(0xFFE2EAFF)    // Frosty Peaks world banner
val CometTint = Color(0xFFEADFFF)    // Comet Run world banner

// On-navy (hero, celebration)
val HeroOutline = Color(0xFF3A4A63)
val HeroTrack = Color(0xFF2A3950)

// Destructive text (Delete all progress only)
val Destructive = Color(0xFFA12A12)

/*
 Contrast rules:
 - Text on Orange, Lime, Sky or any tint is always Navy. Never white.
 - Sky and Orange are fills, never text colours on white.
 - No red overdue or failure states anywhere.
*/
