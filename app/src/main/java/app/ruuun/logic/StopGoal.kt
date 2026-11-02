package app.ruuun.logic

import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunnerSettings

/**
 * The second half of a mission: how many stops to aim for. Like the distance, it is a
 * suggestion, never a rule, and it is derived from the logs rather than stored.
 *
 * Counting stops is optional. Until a run has its stops counted there is no stop goal.
 */
object StopGoal {

    /**
     * One counted run per day, oldest first, for history and badges. When a day has
     * several counted runs, the longest stands for the day, and runs shorter than half
     * their mission never count, so a short extra lap cannot make a better (or worse) day.
     */
    fun countedDays(logs: List<RunLog>): List<RunLog> = logs
        .filter { it.stops != null && countsForGoal(it) }
        .groupBy { it.date }
        .map { (_, day) -> day.longest() }
        .sortedBy { it.date }

    /**
     * Ids of runs entered for an earlier date than a run already in the log at the time.
     * Worked out from the order the runs were entered, so it survives a backup.
     */
    fun backdatedIds(logs: List<RunLog>): Set<String> {
        val ids = HashSet<String>()
        var latest = java.time.LocalDate.MIN
        // Runs entered at the same instant (an old import) are not backdated against
        // each other, so they are handled as one batch.
        val batches = logs.sortedBy { it.createdAt }.groupBy { it.createdAt }.values
        for (batch in batches) {
            for (log in batch) if (log.date.isBefore(latest)) ids += log.id
            val batchLatest = batch.maxOf { it.date }
            if (batchLatest.isAfter(latest)) latest = batchLatest
        }
        return ids
    }

    /**
     * The run the stop goal is built on: the most recent day with a counted run that was
     * not entered for an earlier date and was at least half the mission it was logged
     * against. Short extra runs and back-filled runs never move the goal.
     */
    fun goalSource(logs: List<RunLog>): RunLog? {
        val backdated = backdatedIds(logs)
        return logs
            .filter { it.stops != null && it.id !in backdated && countsForGoal(it) }
            .groupBy { it.date }
            .maxByOrNull { (date, _) -> date }
            ?.value
            ?.longest()
    }

    /** A run shorter than half its mission is a warm-up or an extra lap. */
    fun countsForGoal(log: RunLog): Boolean = log.distanceM * 2 >= log.targetMAtLog

    /**
     * The stop goal for the next run.
     *
     * One fewer than the run it is built on, never below zero. It stays the same instead
     * when that run felt Hard, or when its stops were already within the parent's limit:
     * from there the distance does the growing, so the stops only need to hold. At the
     * longest mission setting the distance cannot grow, so it asks for one fewer again.
     */
    fun next(logs: List<RunLog>, settings: RunnerSettings): Int? {
        val source = goalSource(logs) ?: return null
        val stops = source.stops ?: return null
        val atCap = settings.currentTargetM >= settings.maxTargetM
        val distancePhase = stops <= settings.lowStopsLimit && !atCap
        return if (source.effort == Effort.HARD || distancePhase) stops else (stops - 1).coerceAtLeast(0)
    }

    /** True when this run had a stop goal and stayed within it; null when it had none. */
    fun met(log: RunLog): Boolean? {
        val goal = log.stopsGoalAtLog ?: return null
        val stops = log.stops ?: return null
        return stops <= goal
    }

    /** Days on which at least one run met its stop goal. Extra laps cannot add more. */
    fun daysGoalMet(logs: List<RunLog>): Int =
        logs.filter { met(it) == true && countsForGoal(it) }.map { it.date }.distinct().size

    /** Counted days with fewer stops than the counted day before. */
    fun fewerThanBefore(logs: List<RunLog>): Int =
        countedDays(logs).zipWithNext().count { (before, after) ->
            (after.stops ?: 0) < (before.stops ?: 0)
        }

    /** "1 stop", "8 stops". */
    fun stopsLabel(stops: Int): String = if (stops == 1) "1 stop" else "$stops stops"

    /** How the goal reads on Home and in the run details. */
    fun goalLabel(goal: Int): String =
        if (goal == 0) "Non-stop" else "${stopsLabel(goal)} or fewer"

    private fun List<RunLog>.longest(): RunLog =
        maxWith(compareBy<RunLog> { it.distanceM }.thenBy { it.createdAt })
}
