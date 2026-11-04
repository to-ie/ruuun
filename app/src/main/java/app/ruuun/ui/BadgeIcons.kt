package app.ruuun.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Attractions
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BackHand
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarViewMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Castle
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.EscalatorWarning
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Fence
import androidx.compose.material.icons.rounded.Filter9Plus
import androidx.compose.material.icons.rounded.FilterHdr
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.FrontHand
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Kayaking
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Looks
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.MultipleStop
import androidx.compose.material.icons.rounded.Nature
import androidx.compose.material.icons.rounded.NaturePeople
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.NordicWalking
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.OutlinedFlag
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlusOne
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Sailing
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Signpost
import androidx.compose.material.icons.rounded.Snowshoeing
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Sports
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Surfing
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Toll
import androidx.compose.material.icons.rounded.Tour
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Yard
import androidx.compose.ui.graphics.vector.ImageVector
import app.ruuun.content.BadgeDef
import androidx.compose.material.icons.rounded.AllOut
import androidx.compose.material.icons.rounded.Paragliding
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Moving
import androidx.compose.material.icons.rounded.Straight
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material.icons.rounded.Recommend
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Cyclone
import androidx.compose.material.icons.rounded.EmojiFlags
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.DownhillSkiing
import androidx.compose.material.icons.rounded.HourglassFull
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.EmojiTransportation
import androidx.compose.material.icons.rounded.ModeOfTravel
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material.icons.rounded.Festival
import androidx.compose.material.icons.rounded.Satellite
import androidx.compose.material.icons.rounded.SatelliteAlt
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.SevereCold
import androidx.compose.material.icons.rounded.Snowboarding
import androidx.compose.material.icons.rounded.TempleBuddhist
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.EmojiNature
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Kitesurfing
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Pentagon
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.ForkRight
import androidx.compose.material.icons.rounded.AirlineStops
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Fort

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
    "FS-001" to { Icons.Rounded.RocketLaunch }, // Liftoff!
    "FS-002" to { Icons.Rounded.Replay }, // Try Again
    "FS-003" to { Icons.Rounded.Start }, // Your First Footprint
    "FS-004" to { Icons.AutoMirrored.Rounded.DirectionsWalk }, // Walk On
    "FS-005" to { Icons.Rounded.Shuffle }, // Mix It Up
    "FS-006" to { Icons.Rounded.SentimentVerySatisfied }, // All Smiles
    "FS-007" to { Icons.Rounded.TaskAlt }, // Checked In
    "FS-008" to { Icons.Rounded.Shield }, // Brave Reporter
    "FS-009" to { Icons.Rounded.HistoryEdu }, // My Story
    "FS-010" to { Icons.Rounded.Flag }, // First Quest
    "RC-001" to { Icons.Rounded.Casino }, // Third Time Lucky
    "RC-002" to { Icons.Rounded.FastForward }, // Fourward!
    "RC-003" to { Icons.Rounded.FrontHand }, // High Five
    "RC-004" to { Icons.Rounded.Stairs }, // Six Steps Ahead
    "RC-005" to { Icons.Rounded.AutoAwesome }, // Lucky Seven
    "RC-006" to { Icons.Rounded.AllInclusive }, // Great Eight
    "RC-007" to { Icons.Rounded.Filter9Plus }, // Double Digits
    "RC-008" to { Icons.Rounded.Egg }, // The Dozen
    "RC-009" to { Icons.Rounded.Attractions }, // Fifteen Adventures
    "RC-010" to { Icons.AutoMirrored.Rounded.DirectionsRun }, // Running Rookie
    "RC-011" to { Icons.Rounded.Toll }, // Quarter Century
    "RC-012" to { Icons.Rounded.LocalFlorist }, // Thirty & Thriving
    "RC-013" to { Icons.Rounded.Snowshoeing }, // Forty Footprints
    "RC-014" to { Icons.Rounded.WorkspacePremium }, // Half-Century Hero
    "RC-015" to { Icons.Rounded.FlashOn }, // Super Sixty
    "RC-016" to { Icons.Rounded.FitnessCenter }, // Seventy-Five Strong
    "RC-017" to { Icons.Rounded.SportsMartialArts }, // Ninety Ninjas
    "RC-018" to { Icons.Rounded.EmojiEvents }, // Century Club
    "RC-019" to { Icons.Rounded.HourglassBottom }, // The Long Game
    "RC-020" to { Icons.Rounded.Diamond }, // Legend of the Trail
    "SD-001" to { Icons.Rounded.Rocket }, // Pocket Rocket
    "SD-002" to { Icons.Rounded.Bolt }, // Tiny but Mighty
    "SD-003" to { Icons.Rounded.Whatshot }, // Three Hundred Hero
    "SD-004" to { Icons.Rounded.Speed }, // Four Hundred Feet-First
    "SD-005" to { Icons.Rounded.Contrast }, // Half-K Hero
    "SD-006" to { Icons.Rounded.Stars }, // Six Hundred Superstar
    "SD-007" to { Icons.Rounded.DonutLarge }, // Three Quarter Quest
    "SD-008" to { Icons.Rounded.TravelExplore }, // Eight Hundred Explorer
    "SD-009" to { Icons.Rounded.Straighten }, // Kilometre Kid
    "SD-010" to { Icons.AutoMirrored.Rounded.TrendingUp }, // A Little Beyond
    "SD-011" to { Icons.Rounded.FlightTakeoff }, // Rocket Runner
    "SD-012" to { Icons.Rounded.Timeline }, // Longer Trail
    "SD-013" to { Icons.Rounded.Grade }, // Two-K Triumph
    "SD-014" to { Icons.Rounded.Sports }, // Two and a Half
    "SD-015" to { Icons.Rounded.Landscape }, // Three-K Trail
    "SD-016" to { Icons.Rounded.Flare }, // Fantastic Four-K
    "SD-017" to { Icons.Rounded.LocalFireDepartment }, // Fantastic Five
    "TD-001" to { Icons.Rounded.Signpost }, // First Half Kilometre
    "TD-002" to { Icons.Rounded.Handshake }, // One Kilometre Together
    "TD-003" to { Icons.Rounded.Map }, // Two Kilometres Travelled
    "TD-004" to { Icons.Rounded.Tram }, // Three Kilometres Travelled
    "TD-005" to { Icons.Rounded.Explore }, // Little Explorer
    "TD-006" to { Icons.Rounded.Place }, // Trail Collector
    "TD-007" to { Icons.Rounded.Language }, // World Wanderer
    "TD-008" to { Icons.Rounded.Groups }, // Fifteen-K Club
    "TD-009" to { Icons.Rounded.Loop }, // Long Way Round
    "TD-010" to { Icons.Rounded.Train }, // Quarter to a Hundred
    "TD-011" to { Icons.Rounded.Kayaking }, // Thirty-K Trek
    "TD-012" to { Icons.Rounded.Sailing }, // Forty-K Trek
    "TD-013" to { Icons.Rounded.Tour }, // Halfway to a Hundred
    "TD-014" to { Icons.Rounded.Flight }, // Seventy-Five K
    "TD-015" to { Icons.Rounded.Public }, // 100-K Adventurer
    "TD-016" to { Icons.Rounded.Luggage }, // Super Long Journey
    "WK-001" to { Icons.Rounded.EventAvailable }, // Weekly Warrior
    "WK-002" to { Icons.Rounded.DateRange }, // Double Weekly
    "WK-003" to { Icons.Rounded.ThumbUp }, // Three Good Weeks
    "WK-004" to { Icons.Rounded.CalendarViewMonth }, // Four Week Wonder
    "WK-005" to { Icons.Rounded.Balance }, // Steady Five
    "WK-006" to { Icons.Rounded.AutoStories }, // Six Weeks of Stories
    "WK-007" to { Icons.Rounded.Navigation }, // Eight Week Explorer
    "WK-008" to { Icons.Rounded.Verified }, // Ten Week Hero
    "WK-009" to { Icons.Rounded.EventRepeat }, // Twelve Week Quest
    "WK-010" to { Icons.AutoMirrored.Rounded.LibraryBooks }, // Sixteen Chapters of Effort
    "WK-011" to { Icons.Rounded.NordicWalking }, // Twenty Week Wanderer
    "WK-012" to { Icons.Rounded.CalendarMonth }, // Half-Year Hero
    "WK-013" to { Icons.AutoMirrored.Rounded.EventNote }, // Three-Quarter Year
    "WK-014" to { Icons.Rounded.Cake }, // Full-Year Footprints
    "CO-001" to { Icons.Rounded.PlusOne }, // One More This Week
    "CO-002" to { Icons.Rounded.SportsSoccer }, // Hat Trick Week
    "CO-003" to { Icons.Rounded.Today }, // New Month, New Start
    "CO-004" to { Icons.Rounded.Air }, // Across the Seasons
    "CO-005" to { Icons.Rounded.Redeem }, // A Whole Year of ruuun!
    "AD-001" to { Icons.Rounded.FamilyRestroom }, // Dynamic Duo
    "AD-002" to { Icons.Rounded.Park }, // Park Ranger
    "AD-003" to { Icons.Rounded.AddRoad }, // The Explorer
    "AD-004" to { Icons.Rounded.Grass }, // Nature Ninja
    "AD-005" to { Icons.Rounded.People }, // Running Buddy
    "AD-006" to { Icons.Rounded.Hiking }, // Off Road
    "AD-007" to { Icons.Rounded.Terrain }, // Hill Starter
    "AD-008" to { Icons.Rounded.BeachAccess }, // Sand Between Your Toes
    "AT-004" to { Icons.Rounded.FilterHdr }, // Hill Habit
    "AT-005" to { Icons.Rounded.Nature }, // Trail Regular
    "AT-001" to { Icons.Rounded.Directions }, // Route Discoverer
    "AT-002" to { Icons.Rounded.Radar }, // Explorer Supreme
    "AT-003" to { Icons.Rounded.MultipleStop }, // Route Master
    "RF-001" to { Icons.Rounded.Mood }, // Mood Keeper
    "RF-002" to { Icons.Rounded.EmojiEmotions }, // Feel-Good Five
    "RF-003" to { Icons.Rounded.Favorite }, // Well Done, Me
    "RF-004" to { Icons.Rounded.EditNote }, // Notebook Explorer
    "RF-005" to { Icons.Rounded.Yard }, // Small But Real
    "RF-007" to { Icons.Rounded.Restore }, // Comeback Kid
    "RF-008" to { Icons.Rounded.Autorenew }, // Back Again Again
    "RF-009" to { Icons.Rounded.SportsScore }, // First Goal Met
    "RF-010" to { Icons.Rounded.GpsFixed }, // Five Goals Met
    "RF-011" to { Icons.Rounded.TrackChanges }, // Ten Goals Met
    "RF-012" to { Icons.Rounded.SelfImprovement }, // Slow and Steady
    "RF-013" to { Icons.Rounded.Interests }, // Mixed Adventure
    "RF-014" to { Icons.Rounded.SentimentSatisfiedAlt }, // Happy Hundred
    "CH-001" to { Icons.Rounded.Pets }, // Chapter 1: The First Footprint
    "CH-002" to { Icons.Rounded.Fence }, // Chapter 2: The Hidden Path
    "CH-003" to { Icons.Rounded.Forest }, // Chapter 3: The Forest Gate
    "CH-004" to { Icons.Rounded.Grain }, // Chapter 4: The Sandy Start
    "CH-005" to { Icons.Rounded.Campaign }, // Chapter 5: Canyon of Echoes
    "CH-006" to { Icons.Rounded.WbTwilight }, // Chapter 6: The Sunset Beacon
    "CH-007" to { Icons.Rounded.Cloud }, // Chapter 7: Clouds Below
    "CH-008" to { Icons.Rounded.Waves }, // Chapter 8: The Floating Bridge
    "CH-009" to { Icons.Rounded.CellTower }, // Chapter 9: The Star Tower
    "CH-010" to { Icons.Rounded.FlightLand }, // Chapter 10: Landing Day
    "CH-011" to { Icons.Rounded.Looks }, // Chapter 11: The Rainbow Rings
    "CH-012" to { Icons.Rounded.Celebration }, // Chapter 12: The Great ruuun! Festival
    "QS-001" to { Icons.Rounded.OutlinedFlag }, // Quest Starter
    "QS-002" to { Icons.Rounded.School }, // Quest Apprentice
    "QS-003" to { Icons.AutoMirrored.Rounded.AltRoute }, // Side Quest Hero
    "QS-004" to { Icons.Rounded.Inventory2 }, // Quest Collector
    "QS-005" to { Icons.Rounded.MilitaryTech }, // Quest Veteran
    "QS-006" to { Icons.Rounded.Key }, // Quest Master
    "QS-007" to { Icons.Rounded.Checklist }, // Trail Task Expert
    "QS-008" to { Icons.Rounded.Castle }, // Quest Legend
    "MO-001" to { Icons.Rounded.AcUnit }, // Fresh Start
    "MO-002" to { Icons.Rounded.VolunteerActivism }, // Heart of Adventure
    "MO-003" to { Icons.Rounded.Spa }, // Spring Steps
    "MO-004" to { Icons.Rounded.Umbrella }, // April Explorer
    "MO-005" to { Icons.Rounded.FilterVintage }, // May Momentum
    "MO-006" to { Icons.Rounded.WbSunny }, // Sunny Trails
    "MO-007" to { Icons.Rounded.Icecream }, // Summer Scout
    "MO-008" to { Icons.Rounded.Surfing }, // August Adventure
    "MO-009" to { Icons.Rounded.Backpack }, // Back to the Path
    "MO-010" to { Icons.Rounded.DarkMode }, // Spooky Steps
    "MO-011" to { Icons.Rounded.Eco }, // Autumn Tracks
    "MO-012" to { Icons.Rounded.NotificationsActive }, // Jingle Jogger
    "SE-001" to { Icons.Rounded.BackHand }, // Hidden High Five
    "SE-002" to { Icons.Rounded.EscalatorWarning }, // Family Trail
    "SE-003" to { Icons.Rounded.Toys }, // Little Steps
    "SE-004" to { Icons.AutoMirrored.Rounded.KeyboardReturn }, // The Return
    "SE-005" to { Icons.Rounded.NaturePeople }, // Park Explorer
    "SE-006" to { Icons.AutoMirrored.Rounded.CallSplit }, // Three Routes
    "SE-007" to { Icons.Rounded.Equalizer }, // Every Effort Counts
    "NS-001" to { Icons.Rounded.Numbers }, // Stop Counter
    "NS-002" to { Icons.AutoMirrored.Rounded.PlaylistAddCheck }, // Keeping Count
    "NS-003" to { Icons.AutoMirrored.Rounded.TrendingDown }, // One Fewer
    "NS-004" to { Icons.Rounded.KeyboardDoubleArrowDown }, // Fewer and Fewer
    "NS-005" to { Icons.Rounded.Insights }, // Smooth Strider
    "NS-006" to { Icons.Rounded.Adjust }, // Stop Goal Star
    "NS-007" to { Icons.Rounded.GppGood }, // Goal Keeper
    "NS-008" to { Icons.Rounded.Recommend }, // Stop Goal Pro
    "NS-009" to { Icons.Rounded.DoubleArrow }, // Non-stop!
    "NS-010" to { Icons.Rounded.AutoGraph }, // Non-stop Habit
    "NS-011" to { Icons.Rounded.Straight }, // Non-stop 500
    "NS-012" to { Icons.Rounded.Moving }, // Non-stop Kilometre
    "NS-013" to { Icons.Rounded.ElectricBolt }, // Non-stop Two-K
    "NS-014" to { Icons.Rounded.Paragliding }, // Non-stop Three-K
    "NS-015" to { Icons.Rounded.AllOut }, // Non-stop Five-K
    "CH-013" to { Icons.Rounded.Water }, // Chapter 13: The Tide Pools
    "CH-014" to { Icons.Rounded.Kitesurfing }, // Chapter 14: Shell Beach
    "CH-015" to { Icons.Rounded.FlashlightOn }, // Chapter 15: The Lighthouse
    "CH-016" to { Icons.Rounded.EmojiNature }, // Chapter 16: Vines and Rivers
    "CH-017" to { Icons.Rounded.Pool }, // Chapter 17: The Waterfall Steps
    "CH-018" to { Icons.Rounded.TempleBuddhist }, // Chapter 18: The Hidden Temple
    "CH-019" to { Icons.Rounded.Snowboarding }, // Chapter 19: Snowy Switchbacks
    "CH-020" to { Icons.Rounded.SevereCold }, // Chapter 20: The Ice Cave
    "CH-021" to { Icons.Rounded.NightsStay }, // Chapter 21: Summit Lights
    "CH-022" to { Icons.Rounded.SatelliteAlt }, // Chapter 22: The Launch Pad
    "CH-023" to { Icons.Rounded.Satellite }, // Chapter 23: Riding the Comet
    "CH-024" to { Icons.Rounded.Festival }, // Chapter 24: The Grand Finish
    "RC-021" to { Icons.Rounded.Stadium }, // Three Hundred Club
    "RC-022" to { Icons.Rounded.CalendarToday }, // A Run for Every Day of the Year
    "TD-017" to { Icons.Rounded.ModeOfTravel }, // Three Hundred K Explorer
    "TD-018" to { Icons.Rounded.EmojiTransportation }, // Half a Thousand K
    "WK-015" to { Icons.Rounded.Event }, // Two Years of Weekly Goals
    "CO-006" to { Icons.Rounded.HourglassTop }, // One Year On
    "CO-007" to { Icons.Rounded.HourglassFull }, // Two Years On
    "NS-016" to { Icons.Rounded.DownhillSkiing }, // Non-stop One and a Half
    "NS-017" to { Icons.Rounded.Cached }, // Non-stop Four-K
    "NS-018" to { Icons.Rounded.EmojiFlags }, // Stop Goal Legend
    "NS-019" to { Icons.Rounded.Cyclone }, // Non-stop Hero
    "RC-023" to { Icons.Rounded.Fort }, // Still Going Strong
    "RC-024" to { Icons.Rounded.Cottage }, // Double Century
    "RC-025" to { Icons.Rounded.LocationCity }, // Two-Fifty Trailblazer
    "TD-019" to { Icons.Rounded.AirlineStops }, // Two Hundred K
    "TD-020" to { Icons.Rounded.ForkRight }, // Quarter of a Thousand K
    "WK-016" to { Icons.Rounded.Update }, // Sixty-Five Good Weeks
    "WK-017" to { Icons.Rounded.History }, // Year and a Half of Weeks
    "QS-009" to { Icons.Rounded.Pentagon }, // Quest Champion
)
