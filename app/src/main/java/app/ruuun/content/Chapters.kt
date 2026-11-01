package app.ruuun.content

/** The four worlds. Banner colours and icons live in the UI layer. */
enum class World(val title: String) {
    ROOKIE_WOODS("Rookie Woods"),
    DESERT_DASH("Desert Dash"),
    SKY_KINGDOM("Sky Kingdom"),
    PLANET_ZOOM("Planet Zoom"),
}

/**
 * A story chapter. [runDaysNeeded] is the default 2+ runs/week pacing; a journey
 * started on the 1 run/week plan uses ceil(runDaysNeeded / 2).
 */
data class Chapter(
    val number: Int,
    val world: World,
    val title: String,
    val runDaysNeeded: Int,
    val revealText: String,
    val teaser: String,
)

object Chapters {

    /** Shown once all twelve chapters are complete. */
    const val FINALE_TITLE = "Legend of the Trail"
    const val FINALE_TEASER = "Story finished. Every run from here is yours."

    val ALL: List<Chapter> = listOf(
        Chapter(1, World.ROOKIE_WOODS, "The First Footprint", 4,
            "The adventure has begun.", "It starts with one run."),
        Chapter(2, World.ROOKIE_WOODS, "The Hidden Path", 8,
            "A new trail appears.", "There is a trail hidden in the trees."),
        Chapter(3, World.ROOKIE_WOODS, "The Forest Gate", 12,
            "The forest gate opens.", "The gate out of the woods is close."),
        Chapter(4, World.DESERT_DASH, "The Sandy Start", 18,
            "The desert is yours to explore.", "Open sand, nothing in your way."),
        Chapter(5, World.DESERT_DASH, "Canyon of Echoes", 24,
            "Your footsteps echo through the canyon.", "Shout once, the canyon answers twice."),
        Chapter(6, World.DESERT_DASH, "The Sunset Beacon", 32,
            "The beacon lights up.", "There is a beacon on the ridge to light."),
        Chapter(7, World.SKY_KINGDOM, "Clouds Below", 40,
            "You made it into the clouds.", "The ground is a long way down now."),
        Chapter(8, World.SKY_KINGDOM, "The Floating Bridge", 50,
            "The bridge is ready to cross.", "The bridge builds itself, one run at a time."),
        Chapter(9, World.SKY_KINGDOM, "The Star Tower", 60,
            "The tower shines brightly.", "The tower takes on more light every run."),
        Chapter(10, World.PLANET_ZOOM, "Landing Day", 72,
            "Welcome to a brand-new planet.", "A new planet is coming into view."),
        Chapter(11, World.PLANET_ZOOM, "The Rainbow Rings", 84,
            "The rings come alive with colour.", "The rings are starting to glow."),
        Chapter(12, World.PLANET_ZOOM, "The Great ruuun! Festival", 100,
            "A big celebration of every step.", "They are setting up the festival for you."),
    )

    init {
        require(ALL.size == 12) { "Expected 12 chapters, found ${ALL.size}" }
    }

    /** Run days needed for [chapter] under the journey's frozen story pace. */
    fun runDaysNeeded(chapter: Chapter, onePerWeekPace: Boolean): Int =
        if (onePerWeekPace) (chapter.runDaysNeeded + 1) / 2 else chapter.runDaysNeeded
}
