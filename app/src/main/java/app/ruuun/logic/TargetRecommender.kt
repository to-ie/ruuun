package app.ruuun.logic

import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunnerSettings
import java.time.LocalDate
import kotlin.math.floor

/**
 * The gentle suggestion algorithm from the handoff, section 4. A suggestion, never a
 * schedule: the child may log any distance, and the target never rises after a `Hard`
 * run or because of a backdated entry.
 */
object TargetRecommender {

    const val MIN_TARGET_M = RunnerSettings.MIN_TARGET_M
    private const val HARD_FACTOR = 0.90
    private const val MAX_INCREASE_FRACTION = 0.10

    fun floorTo25(metres: Int): Int = (metres / 25) * 25

    /** +25 below 500 m, +50 to 1,499 m, +100 from 1,500 m up. */
    fun stepFor(target: Int): Int = when {
        target < 500 -> 25
        target < 1_500 -> 50
        else -> 100
    }

    /** The next target after a `Hard` run: 10% easier, rounded down to 25 m. */
    fun easierAfterHard(target: Int): Int =
        maxOf(MIN_TARGET_M, floorTo25(floor(target * HARD_FACTOR).toInt()))

    /** `Make it easier`: the nearest lower 25 m increment, never below 100 m. */
    fun makeEasier(target: Int): Int {
        val floored = floorTo25(target)
        val lower = if (floored < target) floored else target - 25
        return maxOf(MIN_TARGET_M, lower)
    }

    /** `Make it harder`: the nearest higher 25 m increment, never above the cap. */
    fun makeHarder(target: Int, maxTargetM: Int): Int {
        val higher = (target / 25 + 1) * 25
        return minOf(higher, maxTargetM).coerceAtLeast(target)
    }

    /**
     * One small step up, held steady when the step would exceed 10% or the cap.
     * 300 -> 325 m is valid; 300 -> 350 m is not.
     */
    fun increase(target: Int, maxTargetM: Int): Int {
        val step = stepFor(target)
        if (step > target * MAX_INCREASE_FRACTION) return target
        return minOf(target + step, maxTargetM).coerceAtLeast(target)
    }

    /**
     * The next suggested target after [newLog] was saved. [allLogs] must already
     * include it.
     *
     * Only a new log on or after the latest existing run date can move the target,
     * so backdating affects history and badges but never today's suggestion.
     */
    fun afterLog(
        settings: RunnerSettings,
        allLogs: List<RunLog>,
        newLog: RunLog,
    ): Int {
        val current = settings.currentTargetM
        val latestOtherDate = allLogs.filter { it.id != newLog.id }.maxOfOrNull { it.date }
        val isNewLatestDay = latestOtherDate == null || !newLog.date.isBefore(latestOtherDate)
        if (!isNewLatestDay) return current

        // 1. Ran further than the mission? The mission follows the distance actually
        //    covered, whatever the effort felt like. Running further never lowers it.
        val achieved = floorTo25(newLog.distanceM)
        if (achieved > current) return minOf(achieved, settings.maxTargetM)

        // 2. Hard, without beating the mission: ease off.
        if (newLog.effort == Effort.HARD) return easierAfterHard(current)

        // 3. Fell short of the mission: repeat it. Turning up still counts.
        if (newLog.distanceM < newLog.targetMAtLog) return current

        // 4. Okay, and matched the mission without beating it: repeat.
        if (newLog.effort == Effort.OKAY) return current

        // 5. Easy and matched the mission: step up only after two such days at this
        //    distance. Only days since the mission became this value count, so an Easy
        //    day from before an earlier dip cannot carry over and skip the gate.
        val easyMetDays = allLogs
            .sortedWith(compareByDescending<RunLog> { it.date }.thenByDescending { it.createdAt })
            .takeWhile { it.targetMAtLog == current }
            .filter { it.effort == Effort.EASY && it.distanceM >= it.targetMAtLog }
            .map { it.date }
            .distinct()
            .size
        return if (easyMetDays >= 2) increase(current, settings.maxTargetM) else current
    }

    /**
     * Target to use after an edit or a delete: a safe repeat of the most recent
     * remaining log's suggestion, or the onboarding starting distance when nothing
     * is left. Deliberately simple.
     */
    fun afterCorrection(settings: RunnerSettings, remainingLogs: List<RunLog>): Int {
        val latest = remainingLogs.maxWithOrNull(
            compareBy<RunLog> { it.date }.thenBy { it.createdAt },
        ) ?: return settings.startingTargetM.coerceIn(MIN_TARGET_M, settings.maxTargetM)
        return latest.targetMAtLog.coerceIn(MIN_TARGET_M, settings.maxTargetM)
    }

    /** 14 or more days without an run offers the gentle welcome-back choice. */
    fun needsWelcomeBack(logs: List<RunLog>, today: LocalDate): Boolean {
        val gap = StatsCalculator.daysSinceLastRun(logs, today) ?: return false
        return gap >= 14
    }
}
