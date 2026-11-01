package app.ruuun.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** How the run felt. Required on every log. */
enum class Effort { EASY, OKAY, HARD }

/** Optional self-reported movement mode. Never used for pacing or ranking. */
enum class RunMode { WALK_ONLY, WALK_JOG, MOSTLY_JOG }

/** Optional self-reported tags. Nothing here is checked remotely. */
enum class RunTag { PARK, FAMILY, NEW_ROUTE, NATURE, FRIEND, TRAIL, HILLS, BEACH }

/**
 * The single settings row. Everything a parent can adjust lives here; nothing
 * derived (XP, badges, chapters) is ever stored.
 */
@Entity(tableName = "runner_settings")
data class RunnerSettings(
    @PrimaryKey val id: Int = 1,
    val nickname: String = DEFAULT_NICKNAME,
    val onboardedDate: LocalDate,
    val startingTargetM: Int,
    val currentTargetM: Int,
    val maxTargetM: Int = DEFAULT_MAX_TARGET_M,
    val weeklyGoal: Int = DEFAULT_WEEKLY_GOAL,
    /** Frozen at setup: 1 run/week halves the chapter run-day thresholds. */
    val initialStoryPaceOnePerWeek: Boolean = false,
    val onboardingComplete: Boolean = false,
) {
    companion object {
        const val DEFAULT_NICKNAME = "Runner"
        const val DEFAULT_MAX_TARGET_M = 3_000
        const val DEFAULT_WEEKLY_GOAL = 2
        const val MIN_TARGET_M = 100
        const val MAX_ALLOWED_TARGET_M = 50_000

        /** The three starting plans offered at setup. */
        const val START_NEW_M = 300
        const val START_MOVING_M = 500
        const val START_JOGGING_M = 800
    }
}

/**
 * One manually logged run. Multiple rows may share a [date]; all distances
 * count towards totals but the date counts once for XP, the weekly goal,
 * run-day badges and chapter milestones.
 */
@Entity(tableName = "run_log")
data class RunLog(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val distanceM: Int,
    val effort: Effort,
    /** The suggested mission shown when this entry was created. */
    val targetMAtLog: Int,
    val durationMinutes: Int? = null,
    val mode: RunMode? = null,
    val tags: Set<RunTag> = emptySet(),
    val note: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    companion object {
        const val MAX_NOTE_LENGTH = 140
        const val IMPLAUSIBLE_DISTANCE_M = 10_000
        const val MAX_DISTANCE_M = 50_000
    }
}
