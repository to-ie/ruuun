package app.ruuun.content

/**
 * The worlds, four per season. Season 2 (added in 1.3) opens after the festival, so a
 * long journey still has somewhere to go. Banner colours and icons live in the UI layer.
 */
enum class World(val title: String, val season: Int) {
    ROOKIE_WOODS("Rookie Woods", 1),
    DESERT_DASH("Desert Dash", 1),
    SKY_KINGDOM("Sky Kingdom", 1),
    PLANET_ZOOM("Planet Zoom", 1),
    CORAL_COAST("Coral Coast", 2),
    JUNGLE_RUN("Jungle Run", 2),
    FROSTY_PEAKS("Frosty Peaks", 2),
    COMET_RUN("Comet Run", 2),
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

    /** Shown once every chapter, both seasons, is complete. */
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
        // Season 2
        Chapter(13, World.CORAL_COAST, "The Tide Pools", 112,
            "The sea has found you.", "Past the festival, the land runs down to the sea."),
        Chapter(14, World.CORAL_COAST, "Shell Beach", 124,
            "Every shell hums a little song of the sea.", "The beach is covered in shells to find."),
        Chapter(15, World.CORAL_COAST, "The Lighthouse", 136,
            "The lighthouse lamp is shining.", "A lighthouse waits at the end of the bay."),
        Chapter(16, World.JUNGLE_RUN, "Vines and Rivers", 150,
            "You are deep in the jungle now.", "Green everywhere, and the river is singing."),
        Chapter(17, World.JUNGLE_RUN, "The Waterfall Steps", 165,
            "You climbed the waterfall steps.", "Stone steps go up beside the waterfall."),
        Chapter(18, World.JUNGLE_RUN, "The Hidden Temple", 180,
            "The temple doors swing open.", "Something old is hiding in the leaves."),
        Chapter(19, World.FROSTY_PEAKS, "Snowy Switchbacks", 200,
            "Your footprints zigzag up the mountain.", "The path turns back and forth up the snow."),
        Chapter(20, World.FROSTY_PEAKS, "The Ice Cave", 220,
            "The ice cave glows blue.", "A cave of ice, glowing from inside."),
        Chapter(21, World.FROSTY_PEAKS, "Summit Lights", 240,
            "The sky lights up over the summit.", "From the top, the sky will dance."),
        Chapter(22, World.COMET_RUN, "The Launch Pad", 265,
            "Lift off!", "A launch pad is being built for you."),
        Chapter(23, World.COMET_RUN, "Riding the Comet", 290,
            "You are riding a comet's tail.", "A comet is coming close enough to catch."),
        Chapter(24, World.COMET_RUN, "The Grand Finish", 320,
            "Every world, every run: you did it.", "One last stretch to the grand finish."),
    )

    /** Chapters in the first season; the second opens once these are done. */
    const val SEASON_ONE_CHAPTERS = 12

    init {
        require(ALL.size == 24) { "Expected 24 chapters, found ${ALL.size}" }
    }

    /** Run days needed for [chapter] under the journey's frozen story pace. */
    fun runDaysNeeded(chapter: Chapter, onePerWeekPace: Boolean): Int =
        if (onePerWeekPace) (chapter.runDaysNeeded + 1) / 2 else chapter.runDaysNeeded
}
