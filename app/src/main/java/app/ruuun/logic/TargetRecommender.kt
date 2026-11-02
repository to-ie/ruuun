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

    /**
     * True when this run's stops were counted and at most [limit] (the parent's setting,
     * 2 by default). Since 1.3 the distance only goes up after such a run: running more
     * of the way comes first, then it gets longer.
     */
    fun lowStops(log: RunLog, limit: Int): Boolean = log.stops?.let { it <= limit } ?: false
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

    /**
     * How much to add on top of a distance that beat the mission. The next mission is
     * always a little further than the run that earned it; how much further depends on
     * how the run felt.
     */
    fun bumpAfterBeating(distanceM: Int, effort: Effort): Int = when (effort) {
        Effort.HARD -> 25                      // a nudge
        Effort.OKAY -> stepFor(distanceM)      // one ordinary step
        Effort.EASY -> stepFor(distanceM) * 2  // room to stretch
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

        val limit = settings.lowStopsLimit

        // 1. Beat the mission? The next one is set a little beyond the distance just
        //    run, by an amount that depends on how hard it felt. Effort never drags it
        //    back below what was actually covered. Only with low stops, though: with
        //    more stops, or none counted, the mission holds.
        if (newLog.distanceM > current) {
            if (!lowStops(newLog, limit)) return current
            val base = maxOf(floorTo25(newLog.distanceM), current)
            val next = base + bumpAfterBeating(base, newLog.effort)
            return minOf(next, settings.maxTargetM).coerceAtLeast(current)
        }

        // 2. Hard, without beating the mission: ease off.
        if (newLog.effort == Effort.HARD) return easierAfterHard(current)

        // 3. Fell short of the mission: repeat it. Turning up still counts.
        if (newLog.distanceM < newLog.targetMAtLog) return current

        // 4. Matched the mission with low stops: step up after two such days at this
        //    distance, Easy or Okay (since 1.3; before, only two Easy days counted).
        //    Only days since the mission became this value count, so a day from before
        //    an earlier dip cannot carry over and skip the gate.
        return if (matchedLowStopDays(allLogs, current, limit) >= 2) {
            increase(current, settings.maxTargetM)
        } else {
            current
        }
    }

    /**
     * Distinct days at the [current] mission that matched it, did not feel Hard and had
     * low stops. Two of them step the mission up.
     */
    fun matchedLowStopDays(allLogs: List<RunLog>, current: Int, limit: Int): Int = allLogs
        .sortedWith(compareByDescending<RunLog> { it.date }.thenByDescending { it.createdAt })
        .takeWhile { it.targetMAtLog == current }
        .filter {
            it.effort != Effort.HARD && it.distanceM >= it.targetMAtLog && lowStops(it, limit)
        }
        .map { it.date }
        .distinct()
        .size

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
