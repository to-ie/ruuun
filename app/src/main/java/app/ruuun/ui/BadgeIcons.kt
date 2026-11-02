package app.ruuun.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Attractions
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.EscalatorWarning
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Fence
import androidx.compose.material.icons.outlined.Filter9Plus
import androidx.compose.material.icons.outlined.FilterHdr
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Flare
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.FlightLand
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kayaking
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Looks
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MultipleStop
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.NaturePeople
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.NordicWalking
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.OutlinedFlag
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PlusOne
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Rocket
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Sailing
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Signpost
import androidx.compose.material.icons.outlined.Snowshoeing
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Sports
import androidx.compose.material.icons.outlined.SportsMartialArts
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Start
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Surfing
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material.icons.outlined.Tour
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Tram
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.ui.graphics.vector.ImageVector
import app.ruuun.content.BadgeDef

/**
 * Each badge's own icon, revealed once it is earned. Locked badges show a padlock and
 * the Home "closest badge" card keeps the category icon, so earning one is a small
 * surprise. No two badges share an icon; BadgeIconsTest checks that.
 *
 * Each value is a lambda, so only the icons actually on screen are ever built.
 */
fun BadgeDef.earnedIcon(): ImageVector = BADGE_ICONS[id]?.invoke() ?: category.icon()

/** Badge ids that have their own icon. Exposed for the coverage test. */
internal val badgeIconIds: Set<String> get() = BADGE_ICONS.keys

private val BADGE_ICONS: Map<String, () -> ImageVector> = mapOf(
    "FS-001" to { Icons.Outlined.RocketLaunch }, // Liftoff!
    "FS-002" to { Icons.Outlined.Replay }, // Try Again
    "FS-003" to { Icons.Outlined.Start }, // Your First Footprint
    "FS-004" to { Icons.AutoMirrored.Outlined.DirectionsWalk }, // Walk On
    "FS-005" to { Icons.Outlined.Shuffle }, // Mix It Up
    "FS-006" to { Icons.Outlined.SentimentVerySatisfied }, // All Smiles
    "FS-007" to { Icons.Outlined.TaskAlt }, // Checked In
    "FS-008" to { Icons.Outlined.Shield }, // Brave Reporter
    "FS-009" to { Icons.Outlined.HistoryEdu }, // My Story
    "FS-010" to { Icons.Outlined.Flag }, // First Quest
    "RC-001" to { Icons.Outlined.Casino }, // Third Time Lucky
    "RC-002" to { Icons.Outlined.FastForward }, // Fourward!
    "RC-003" to { Icons.Outlined.FrontHand }, // High Five
    "RC-004" to { Icons.Outlined.Stairs }, // Six Steps Ahead
    "RC-005" to { Icons.Outlined.AutoAwesome }, // Lucky Seven
    "RC-006" to { Icons.Outlined.AllInclusive }, // Great Eight
    "RC-007" to { Icons.Outlined.Filter9Plus }, // Double Digits
    "RC-008" to { Icons.Outlined.Egg }, // The Dozen
    "RC-009" to { Icons.Outlined.Attractions }, // Fifteen Adventures
    "RC-010" to { Icons.AutoMirrored.Outlined.DirectionsRun }, // Running Rookie
    "RC-011" to { Icons.Outlined.Toll }, // Quarter Century
    "RC-012" to { Icons.Outlined.LocalFlorist }, // Thirty & Thriving
    "RC-013" to { Icons.Outlined.Snowshoeing }, // Forty Footprints
    "RC-014" to { Icons.Outlined.WorkspacePremium }, // Half-Century Hero
    "RC-015" to { Icons.Outlined.FlashOn }, // Super Sixty
    "RC-016" to { Icons.Outlined.FitnessCenter }, // Seventy-Five Strong
    "RC-017" to { Icons.Outlined.SportsMartialArts }, // Ninety Ninjas
    "RC-018" to { Icons.Outlined.EmojiEvents }, // Century Club
    "RC-019" to { Icons.Outlined.HourglassBottom }, // The Long Game
    "RC-020" to { Icons.Outlined.Diamond }, // Legend of the Trail
    "SD-001" to { Icons.Outlined.Rocket }, // Pocket Rocket
    "SD-002" to { Icons.Outlined.Bolt }, // Tiny but Mighty
    "SD-003" to { Icons.Outlined.Whatshot }, // Three Hundred Hero
    "SD-004" to { Icons.Outlined.Speed }, // Four Hundred Feet-First
    "SD-005" to { Icons.Outlined.Contrast }, // Half-K Hero
    "SD-006" to { Icons.Outlined.Stars }, // Six Hundred Superstar
    "SD-007" to { Icons.Outlined.DonutLarge }, // Three Quarter Quest
    "SD-008" to { Icons.Outlined.TravelExplore }, // Eight Hundred Explorer
    "SD-009" to { Icons.Outlined.Straighten }, // Kilometre Kid
    "SD-010" to { Icons.AutoMirrored.Outlined.TrendingUp }, // A Little Beyond
    "SD-011" to { Icons.Outlined.FlightTakeoff }, // Rocket Runner
    "SD-012" to { Icons.Outlined.Timeline }, // Longer Trail
    "SD-013" to { Icons.Outlined.Grade }, // Two-K Triumph
    "SD-014" to { Icons.Outlined.Sports }, // Two and a Half
    "SD-015" to { Icons.Outlined.Landscape }, // Three-K Trail
    "SD-016" to { Icons.Outlined.Flare }, // Fantastic Four-K
    "SD-017" to { Icons.Outlined.LocalFireDepartment }, // Fantastic Five
    "TD-001" to { Icons.Outlined.Signpost }, // First Half Kilometre
    "TD-002" to { Icons.Outlined.Handshake }, // One Kilometre Together
    "TD-003" to { Icons.Outlined.Map }, // Two Kilometres Travelled
    "TD-004" to { Icons.Outlined.Tram }, // Three Kilometres Travelled
    "TD-005" to { Icons.Outlined.Explore }, // Little Explorer
    "TD-006" to { Icons.Outlined.Place }, // Trail Collector
    "TD-007" to { Icons.Outlined.Language }, // World Wanderer
    "TD-008" to { Icons.Outlined.Groups }, // Fifteen-K Club
    "TD-009" to { Icons.Outlined.Loop }, // Long Way Round
    "TD-010" to { Icons.Outlined.Train }, // Quarter to a Hundred
    "TD-011" to { Icons.Outlined.Kayaking }, // Thirty-K Trek
    "TD-012" to { Icons.Outlined.Sailing }, // Forty-K Trek
    "TD-013" to { Icons.Outlined.Tour }, // Halfway to a Hundred
    "TD-014" to { Icons.Outlined.Flight }, // Seventy-Five K
    "TD-015" to { Icons.Outlined.Public }, // 100-K Adventurer
    "TD-016" to { Icons.Outlined.Luggage }, // Super Long Journey
    "WK-001" to { Icons.Outlined.EventAvailable }, // Weekly Warrior
    "WK-002" to { Icons.Outlined.DateRange }, // Double Weekly
    "WK-003" to { Icons.Outlined.ThumbUp }, // Three Good Weeks
    "WK-004" to { Icons.Outlined.CalendarViewMonth }, // Four Week Wonder
    "WK-005" to { Icons.Outlined.Balance }, // Steady Five
    "WK-006" to { Icons.Outlined.AutoStories }, // Six Weeks of Stories
    "WK-007" to { Icons.Outlined.Navigation }, // Eight Week Explorer
    "WK-008" to { Icons.Outlined.Verified }, // Ten Week Hero
    "WK-009" to { Icons.Outlined.EventRepeat }, // Twelve Week Quest
    "WK-010" to { Icons.AutoMirrored.Outlined.LibraryBooks }, // Sixteen Chapters of Effort
    "WK-011" to { Icons.Outlined.NordicWalking }, // Twenty Week Wanderer
    "WK-012" to { Icons.Outlined.CalendarMonth }, // Half-Year Hero
    "WK-013" to { Icons.AutoMirrored.Outlined.EventNote }, // Three-Quarter Year
    "WK-014" to { Icons.Outlined.Cake }, // Full-Year Footprints
    "CO-001" to { Icons.Outlined.PlusOne }, // One More This Week
    "CO-002" to { Icons.Outlined.SportsSoccer }, // Hat Trick Week
    "CO-003" to { Icons.Outlined.Today }, // New Month, New Start
    "CO-004" to { Icons.Outlined.Air }, // Across the Seasons
    "CO-005" to { Icons.Outlined.Redeem }, // A Whole Year of ruuun!
    "AD-001" to { Icons.Outlined.FamilyRestroom }, // Dynamic Duo
    "AD-002" to { Icons.Outlined.Park }, // Park Ranger
    "AD-003" to { Icons.Outlined.AddRoad }, // The Explorer
    "AD-004" to { Icons.Outlined.Grass }, // Nature Ninja
    "AD-005" to { Icons.Outlined.People }, // Running Buddy
    "AD-006" to { Icons.Outlined.Hiking }, // Off Road
    "AD-007" to { Icons.Outlined.Terrain }, // Hill Starter
    "AD-008" to { Icons.Outlined.BeachAccess }, // Sand Between Your Toes
    "AT-004" to { Icons.Outlined.FilterHdr }, // Hill Habit
    "AT-005" to { Icons.Outlined.Nature }, // Trail Regular
    "AT-001" to { Icons.Outlined.Directions }, // Route Discoverer
    "AT-002" to { Icons.Outlined.Radar }, // Explorer Supreme
    "AT-003" to { Icons.Outlined.MultipleStop }, // Route Master
    "RF-001" to { Icons.Outlined.Mood }, // Mood Keeper
    "RF-002" to { Icons.Outlined.EmojiEmotions }, // Feel-Good Five
    "RF-003" to { Icons.Outlined.Favorite }, // Well Done, Me
    "RF-004" to { Icons.Outlined.EditNote }, // Notebook Explorer
    "RF-005" to { Icons.Outlined.Yard }, // Small But Real
    "RF-007" to { Icons.Outlined.Restore }, // Comeback Kid
    "RF-008" to { Icons.Outlined.Autorenew }, // Back Again Again
    "RF-009" to { Icons.Outlined.SportsScore }, // First Goal Met
    "RF-010" to { Icons.Outlined.GpsFixed }, // Five Goals Met
    "RF-011" to { Icons.Outlined.TrackChanges }, // Ten Goals Met
    "RF-012" to { Icons.Outlined.SelfImprovement }, // Slow and Steady
    "RF-013" to { Icons.Outlined.Interests }, // Mixed Adventure
    "RF-014" to { Icons.Outlined.SentimentSatisfiedAlt }, // Happy Hundred
    "CH-001" to { Icons.Outlined.Pets }, // Chapter 1: The First Footprint
    "CH-002" to { Icons.Outlined.Fence }, // Chapter 2: The Hidden Path
    "CH-003" to { Icons.Outlined.Forest }, // Chapter 3: The Forest Gate
    "CH-004" to { Icons.Outlined.Grain }, // Chapter 4: The Sandy Start
    "CH-005" to { Icons.Outlined.Campaign }, // Chapter 5: Canyon of Echoes
    "CH-006" to { Icons.Outlined.WbTwilight }, // Chapter 6: The Sunset Beacon
    "CH-007" to { Icons.Outlined.Cloud }, // Chapter 7: Clouds Below
    "CH-008" to { Icons.Outlined.Waves }, // Chapter 8: The Floating Bridge
    "CH-009" to { Icons.Outlined.CellTower }, // Chapter 9: The Star Tower
    "CH-010" to { Icons.Outlined.FlightLand }, // Chapter 10: Landing Day
    "CH-011" to { Icons.Outlined.Looks }, // Chapter 11: The Rainbow Rings
    "CH-012" to { Icons.Outlined.Celebration }, // Chapter 12: The Great ruuun! Festival
    "QS-001" to { Icons.Outlined.OutlinedFlag }, // Quest Starter
    "QS-002" to { Icons.Outlined.School }, // Quest Apprentice
    "QS-003" to { Icons.AutoMirrored.Outlined.AltRoute }, // Side Quest Hero
    "QS-004" to { Icons.Outlined.Inventory2 }, // Quest Collector
    "QS-005" to { Icons.Outlined.MilitaryTech }, // Quest Veteran
    "QS-006" to { Icons.Outlined.Key }, // Quest Master
    "QS-007" to { Icons.Outlined.Checklist }, // Trail Task Expert
    "QS-008" to { Icons.Outlined.Castle }, // Quest Legend
    "MO-001" to { Icons.Outlined.AcUnit }, // Fresh Start
    "MO-002" to { Icons.Outlined.VolunteerActivism }, // Heart of Adventure
    "MO-003" to { Icons.Outlined.Spa }, // Spring Steps
    "MO-004" to { Icons.Outlined.Umbrella }, // April Explorer
    "MO-005" to { Icons.Outlined.FilterVintage }, // May Momentum
    "MO-006" to { Icons.Outlined.WbSunny }, // Sunny Trails
    "MO-007" to { Icons.Outlined.Icecream }, // Summer Scout
    "MO-008" to { Icons.Outlined.Surfing }, // August Adventure
    "MO-009" to { Icons.Outlined.Backpack }, // Back to the Path
    "MO-010" to { Icons.Outlined.DarkMode }, // Spooky Steps
    "MO-011" to { Icons.Outlined.Eco }, // Autumn Tracks
    "MO-012" to { Icons.Outlined.NotificationsActive }, // Jingle Jogger
    "SE-001" to { Icons.Outlined.BackHand }, // Hidden High Five
    "SE-002" to { Icons.Outlined.EscalatorWarning }, // Family Trail
    "SE-003" to { Icons.Outlined.Toys }, // Little Steps
    "SE-004" to { Icons.AutoMirrored.Outlined.KeyboardReturn }, // The Return
    "SE-005" to { Icons.Outlined.NaturePeople }, // Park Explorer
    "SE-006" to { Icons.AutoMirrored.Outlined.CallSplit }, // Three Routes
    "SE-007" to { Icons.Outlined.Equalizer }, // Every Effort Counts
)
