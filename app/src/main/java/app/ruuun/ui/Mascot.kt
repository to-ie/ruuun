package app.ruuun.ui

import androidx.annotation.DrawableRes
import app.ruuun.R
import app.ruuun.content.Chapters
import app.ruuun.content.World
import app.ruuun.data.Effort
import app.ruuun.logic.JourneyView
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StopGoal

/**
 * Ruu, the fox, and the pictures of the story. All of them are decoration: whatever a
 * picture shows is also said in words next to it, so they carry no content description.
 *
 * Every Ruu picture shares one canvas with the feet on the same line, so swapping one
 * pose for another never makes the layout jump.
 */
object Mascot {

    @DrawableRes val READY = R.drawable.ruu_ready
    @DrawableRes val CHEER = R.drawable.ruu_cheer
    @DrawableRes val PROUD = R.drawable.ruu_proud
    @DrawableRes val TIRED_PROUD = R.drawable.ruu_tired_proud
    @DrawableRes val WELCOME_BACK = R.drawable.ruu_welcome_back
    @DrawableRes val NOTEBOOK = R.drawable.ruu_notebook
    @DrawableRes val BADGE_PROUD = R.drawable.ruu_badge_proud

    /** Width over height of every Ruu picture. */
    const val RUU_ASPECT = 425f / 480f

    /** Width over height of the world scenes and the chapter pictures. */
    const val SCENE_ASPECT = 16f / 9f
    const val CHAPTER_ASPECT = 3f / 2f

    /** The scene shown once the whole story is finished. */
    @DrawableRes val FINALE_SCENE = R.drawable.scene_legend_of_the_trail

    /**
     * Ruu after a save. A cheer for anything new (a badge, a level, a record, a finished
     * chapter, or a stop goal met on a run that sets the next one), tired but proud after
     * a Hard run, and plain proud otherwise.
     */
    @DrawableRes
    fun afterSave(outcome: SaveOutcome): Int = when {
        outcome.newBadges.isNotEmpty() ||
            outcome.leveledUp ||
            outcome.completedChapter != null ||
            JourneyView.records(outcome).isNotEmpty() ||
            stopGoalMet(outcome) -> CHEER
        outcome.log.effort == Effort.HARD -> TIRED_PROUD
        else -> PROUD
    }

    /** A warm-up lap or an extra run that day can meet the number without it counting. */
    private fun stopGoalMet(outcome: SaveOutcome): Boolean =
        StopGoal.met(outcome.log) == true &&
            StopGoal.countsForGoal(outcome.log) &&
            outcome.setsStopGoal &&
            !outcome.backdated

    /** The picture for chapter [number], 1 to 24, shown once it is finished. */
    @DrawableRes
    fun chapter(number: Int): Int = CHAPTER_PICTURES[(number - 1).coerceIn(0, CHAPTER_PICTURES.lastIndex)]

    /** A small copy of chapter [number]'s picture, for lists. */
    @DrawableRes
    fun chapterThumb(number: Int): Int = CHAPTER_THUMBS[(number - 1).coerceIn(0, CHAPTER_THUMBS.lastIndex)]

    private val CHAPTER_THUMBS = intArrayOf(
        R.drawable.chapter_01_thumb, R.drawable.chapter_02_thumb, R.drawable.chapter_03_thumb,
        R.drawable.chapter_04_thumb, R.drawable.chapter_05_thumb, R.drawable.chapter_06_thumb,
        R.drawable.chapter_07_thumb, R.drawable.chapter_08_thumb, R.drawable.chapter_09_thumb,
        R.drawable.chapter_10_thumb, R.drawable.chapter_11_thumb, R.drawable.chapter_12_thumb,
        R.drawable.chapter_13_thumb, R.drawable.chapter_14_thumb, R.drawable.chapter_15_thumb,
        R.drawable.chapter_16_thumb, R.drawable.chapter_17_thumb, R.drawable.chapter_18_thumb,
        R.drawable.chapter_19_thumb, R.drawable.chapter_20_thumb, R.drawable.chapter_21_thumb,
        R.drawable.chapter_22_thumb, R.drawable.chapter_23_thumb, R.drawable.chapter_24_thumb,
    )

    private val CHAPTER_PICTURES = intArrayOf(
        R.drawable.chapter_01, R.drawable.chapter_02, R.drawable.chapter_03,
        R.drawable.chapter_04, R.drawable.chapter_05, R.drawable.chapter_06,
        R.drawable.chapter_07, R.drawable.chapter_08, R.drawable.chapter_09,
        R.drawable.chapter_10, R.drawable.chapter_11, R.drawable.chapter_12,
        R.drawable.chapter_13, R.drawable.chapter_14, R.drawable.chapter_15,
        R.drawable.chapter_16, R.drawable.chapter_17, R.drawable.chapter_18,
        R.drawable.chapter_19, R.drawable.chapter_20, R.drawable.chapter_21,
        R.drawable.chapter_22, R.drawable.chapter_23, R.drawable.chapter_24,
    )

    init {
        require(CHAPTER_PICTURES.size == Chapters.ALL.size) { "One picture per chapter" }
        require(CHAPTER_THUMBS.size == Chapters.ALL.size) { "One thumbnail per chapter" }
    }
}

/** The world's scene, a wide picture of the place. */
@DrawableRes
fun World.scene(): Int = when (this) {
    World.ROOKIE_WOODS -> R.drawable.scene_rookie_woods
    World.DESERT_DASH -> R.drawable.scene_desert_dash
    World.SKY_KINGDOM -> R.drawable.scene_sky_kingdom
    World.PLANET_ZOOM -> R.drawable.scene_planet_zoom
    World.CORAL_COAST -> R.drawable.scene_coral_coast
    World.JUNGLE_RUN -> R.drawable.scene_jungle_run
    World.FROSTY_PEAKS -> R.drawable.scene_frosty_peaks
    World.COMET_RUN -> R.drawable.scene_comet_run
}

/** Ruu dressed for the world. */
@DrawableRes
fun World.ruu(): Int = when (this) {
    World.ROOKIE_WOODS -> R.drawable.ruu_world_rookie_woods
    World.DESERT_DASH -> R.drawable.ruu_world_desert_dash
    World.SKY_KINGDOM -> R.drawable.ruu_world_sky_kingdom
    World.PLANET_ZOOM -> R.drawable.ruu_world_planet_zoom
    World.CORAL_COAST -> R.drawable.ruu_world_coral_coast
    World.JUNGLE_RUN -> R.drawable.ruu_world_jungle_run
    World.FROSTY_PEAKS -> R.drawable.ruu_world_frosty_peaks
    World.COMET_RUN -> R.drawable.ruu_world_comet_run
}
