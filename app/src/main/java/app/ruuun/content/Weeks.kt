package app.ruuun.content

/** The eight fixed optional side quests. No random generation, no quest state. */
enum class SideQuest(val description: String) {
    ANY_RUN("Enjoy any outing"),
    EASY("Enjoy an easy-feeling outing"),
    WALK("Try a walking adventure"),
    NOTE("Save a tiny memory"),
    PARK("Visit a park"),
    FAMILY("Go with family"),
    NEW_ROUTE("Try a new safe path"),
    NATURE("Notice nature"),
}

data class WeekTheme(
    val number: Int,
    val title: String,
    val flavour: String,
    val sideQuest: SideQuest,
)

object Weeks {

    val ALL: List<WeekTheme> = listOf(
        WeekTheme(1, "Launch Week", "First steps", SideQuest.ANY_RUN),
        WeekTheme(2, "Back for More", "Comfort over distance", SideQuest.NOTE),
        WeekTheme(3, "Tiny Victories", "Small steps are real", SideQuest.EASY),
        WeekTheme(4, "Finding Your Feet", "Repeat with confidence", SideQuest.PARK),
        WeekTheme(5, "Explore the Park", "A new view", SideQuest.ANY_RUN),
        WeekTheme(6, "Forest Friends", "Buddy week", SideQuest.FAMILY),
        WeekTheme(7, "The Hidden Path", "Try something different", SideQuest.NEW_ROUTE),
        WeekTheme(8, "The Forest Gate", "World finale preview", SideQuest.NATURE),
        WeekTheme(9, "Footprints and Stories", "Build a journal", SideQuest.WALK),
        WeekTheme(10, "Canyon Calling", "Fresh scenery", SideQuest.NOTE),
        WeekTheme(11, "The Sunny Shortcut", "Find your rhythm", SideQuest.EASY),
        WeekTheme(12, "Calm Counts", "Rest and return", SideQuest.ANY_RUN),
        WeekTheme(13, "Pace Is Yours", "No race necessary", SideQuest.PARK),
        WeekTheme(14, "Gecko Week", "Meet the new friend", SideQuest.ANY_RUN),
        WeekTheme(15, "Little Longer, If Ready", "Flexible target", SideQuest.NOTE),
        WeekTheme(16, "Repeat Champion", "Repeating helps", SideQuest.EASY),
        WeekTheme(17, "Find the Beacon", "Look ahead", SideQuest.PARK),
        WeekTheme(18, "Canyon Stories", "Notice the world", SideQuest.ANY_RUN),
        WeekTheme(19, "Halfway to Sky", "New chapter energy", SideQuest.FAMILY),
        WeekTheme(20, "Something Different", "Switch the scene", SideQuest.NEW_ROUTE),
        WeekTheme(21, "Slow-Down Superpower", "Choose kindness", SideQuest.NATURE),
        WeekTheme(22, "Sidekick Week", "Family matters", SideQuest.WALK),
        WeekTheme(23, "Floating Thoughts", "Keep memories", SideQuest.NOTE),
        WeekTheme(24, "Cloud Explorer", "Gentle exploration", SideQuest.EASY),
        WeekTheme(25, "Six Months of Adventure", "Half-year celebration", SideQuest.ANY_RUN),
        WeekTheme(26, "Bridge Builder", "Small pieces make progress", SideQuest.PARK),
        WeekTheme(27, "Easy Wins", "Confidence week", SideQuest.ANY_RUN),
        WeekTheme(28, "The Long View", "Look back", SideQuest.NOTE),
        WeekTheme(29, "Trophy Room", "Celebrate yourself", SideQuest.EASY),
        WeekTheme(30, "Sky Lanterns", "Enjoy the journey", SideQuest.PARK),
        WeekTheme(31, "A Change of Scene", "New path week", SideQuest.ANY_RUN),
        WeekTheme(32, "Tower Time", "Keep showing up", SideQuest.FAMILY),
        WeekTheme(33, "Just Because", "No pressure week", SideQuest.NEW_ROUTE),
        WeekTheme(34, "First Landing", "Start the last world", SideQuest.NATURE),
        WeekTheme(35, "Rocket Shoes", "Playful momentum", SideQuest.WALK),
        WeekTheme(36, "Space Friend", "Take a sidekick", SideQuest.NOTE),
        WeekTheme(37, "Rainbow Rings", "Colourful journey", SideQuest.EASY),
        WeekTheme(38, "Story Collector", "Write it down", SideQuest.ANY_RUN),
        WeekTheme(39, "The Gentle Quest", "A kind challenge", SideQuest.PARK),
        WeekTheme(40, "Cosmic Paths", "Explore again", SideQuest.ANY_RUN),
        WeekTheme(41, "Comet Week", "Bright energy", SideQuest.NOTE),
        WeekTheme(42, "Trail of Memories", "Remember a moment", SideQuest.EASY),
        WeekTheme(43, "The Home Stretch", "Progress is personal", SideQuest.PARK),
        WeekTheme(44, "Planet Party", "Fun first", SideQuest.ANY_RUN),
        WeekTheme(45, "Mystery Week", "A surprise", SideQuest.FAMILY),
        WeekTheme(46, "The Grand Trail", "Your own path", SideQuest.NEW_ROUTE),
        WeekTheme(47, "Welcome-Back Window", "No pressure", SideQuest.NATURE),
        WeekTheme(48, "Festival Preparations", "Get ready", SideQuest.WALK),
        WeekTheme(49, "Look How Far", "Reflect on total", SideQuest.NOTE),
        WeekTheme(50, "One More Memory", "Make it special", SideQuest.EASY),
        WeekTheme(51, "The Last Chapter", "Finish your story", SideQuest.ANY_RUN),
        WeekTheme(52, "ruuun! Anniversary", "Celebrate the year", SideQuest.PARK),
    )

    init {
        require(ALL.size == 52) { "Expected 52 weekly themes, found ${ALL.size}" }
    }

    /**
     * Theme for a 1-based journey week number. Week 53 rotates back to the week-1
     * theme; the journey week number itself keeps counting up.
     */
    fun themeFor(journeyWeek: Int): WeekTheme {
        val index = ((journeyWeek - 1).mod(ALL.size))
        return ALL[index]
    }
}
