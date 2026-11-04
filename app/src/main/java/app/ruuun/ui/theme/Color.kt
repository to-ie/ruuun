package app.ruuun.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette (1.5 restyle: the "chunky" kids design system)
val Navy = Color(0xFF111A2E)        // ink
val Sky = Color(0xFF3DBDF5)         // header zone, first-steps medals
val SkyShade = Color(0xFF1597D6)    // the solid "depth" edge under things on blue
val SkyDeep = Color(0xFF0B3A55)     // text on Sky (5.6:1)
val Orange = Color(0xFFFF8A1F)
val OrangeShade = Color(0xFFD66A00) // depth edge, and orange text on white (3.55:1, large text only)
val OrangeText = Color(0xFFA84F00)  // small orange text (5.6:1 on white)
val Lime = Color(0xFFC6F03C)
val LimeShade = Color(0xFF9CC417)
val LimeText = Color(0xFF5C7A00)    // lime-family text on light grounds (4.7:1)
val Ground = Color(0xFFF6F8FB)      // the content sheet
val Surface = Color(0xFFFFFFFF)     // cards, top bars, bottom nav

// Neutrals
val Border = Color(0xFFE3E8EF)       // card borders and their depth edge
val Divider = Color(0xFFEEF1F5)
val InputBorder = Color(0xFFC9D1DC)  // idle inputs, dashed outlines
val TextMuted = Color(0xFF4A5468)    // secondary text, 7.6:1 on white
val IconMuted = Color(0xFF6B7588)    // inactive and locked icons (4.6:1 on white, 3.7:1 on Track)
val Track = Color(0xFFE3E8EF)        // progress track, future days
val SegmentBg = Color(0xFFEEF1F5)    // segmented control background
val QuestRowBg = Color(0xFFF2F6FB)
val LockedCard = Color(0xFFEEF1F5)
val Link = Color(0xFF0B5E9C)

// Tints (backgrounds only, always with Navy text)
val SkyTint = Color(0xFFE4F6FF)      // "Okay" effort, date blocks, current week
val InfoBg = Color(0xFFE4F6FF)       // info and next-mission panels
val InfoText = Color(0xFF1F2E45)
val InfoLabel = Color(0xFF0B4A7A)
val LimeTint = Color(0xFFF1FBD4)     // "Easy" effort
val OrangeTint = Color(0xFFFFE2C7)   // "Hard" effort, progress pill
val OrangeTintLight = Color(0xFFFFF1E3) // selected setup plan card
val SunTint = Color(0xFFFFD98A)      // Desert Dash world colour
val SkyWorld = Color(0xFFA8E2FF)     // Sky Kingdom world colour
val PlanetTint = Color(0xFFD6C8FF)   // Planet Zoom world colour
val CoralTint = Color(0xFFFFC2B5)    // Coral Coast world colour (season 2)
val JungleTint = Color(0xFF9FE6B6)   // Jungle Run world colour
val FrostTint = Color(0xFFCDEFFA)    // Frosty Peaks world colour
val CometTint = Color(0xFFFFE98A)    // Comet Run world colour

// On-navy (stops card, pills)
val HeroTrack = Color(0xFF2A3950)
val StopBlockIdle = Color(0xFF7A869E) // first-run blocks on Navy (4.7:1)

// Destructive text (Delete all progress only)
val Destructive = Color(0xFFA12A12)
val DangerFrame = Color(0xFFE5533D)  // the frame of a dialog that deletes or replaces data

/*
 Contrast rules (checked against WCAG AA):
 - Text on Orange, Lime, Sky or any tint is Navy (or SkyDeep on Sky). Never white:
   white on Orange is 2.4:1 and white on Sky 2.1:1, both too faint for young readers.
 - Orange text on white uses OrangeShade, and only at large sizes.
 - Medal icons are Navy ink, except Secret (lime on black).
 - No red overdue or failure states anywhere.
*/
