package app.ruuun.logic

import app.ruuun.content.BadgeMetric
import app.ruuun.content.Chapters
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.data.RunnerSettings
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Everything the app shows, derived from the logs. Nothing here is ever persisted:
 * correcting a log simply produces a different snapshot.
 */
data class StatsSnapshot(
    val runDays: Int,
    val totalDistanceM: Long,
    val maxSingleDistanceM: Int,
    val weeksMet: Int,
    val questsCompleted: Int,
    val completedChapters: Int,
    val metrics: Map<BadgeMetric, Int>,
    val earnedBadgeIds: Set<String>,
) {
    /** 50 XP per distinct run day. Nothing else grants XP. */
    val xp: Int get() = XP_PER_RUN_DAY * runDays

    /** Five run days per level. */
    val level: Int get() = 1 + runDays / RUN_DAYS_PER_LEVEL

    /** Run days completed inside the current level, 0 to 4. */
    val daysIntoLevel: Int get() = runDays % RUN_DAYS_PER_LEVEL

    /** 0f to 1f progress towards the next level. */
    val levelProgress: Float get() = daysIntoLevel.toFloat() / RUN_DAYS_PER_LEVEL

    /** Run days still needed to reach the next level. */
    val runsToNextLevel: Int get() = RUN_DAYS_PER_LEVEL - daysIntoLevel

    fun metric(metric: BadgeMetric): Int = metrics[metric] ?: 0

    companion object {
        const val XP_PER_RUN_DAY = 50
        const val RUN_DAYS_PER_LEVEL = 5

        val EMPTY = StatsSnapshot(0, 0L, 0, 0, 0, 0, emptyMap(), emptySet())
    }
}

object StatsCalculator {

    /** Level titles. Descriptive only; they unlock nothing. */
    fun levelName(level: Int): String = when (level) {
        1 -> "First Steps"
        2 -> "Rookie"
        3 -> "Trail Finder"
        4 -> "Steady Strider"
        5 -> "Path Maker"
        6 -> "Explorer"
        7 -> "Adventure Ace"
        else -> "Trail Hero"
    }

    fun runDays(logs: List<RunLog>): Int = logs.map { it.date }.distinct().size

    /**
     * Build the full snapshot. [today] only bounds nothing by itself: weeks are taken
     * from the recorded dates, so the result is deterministic and testable.
     */
    fun snapshot(logs: List<RunLog>, settings: RunnerSettings): StatsSnapshot {
        val dates = logs.map { it.date }.distinct().sorted()
        val runDays = dates.size
        val totalM = logs.sumOf { it.distanceM.toLong() }
        val maxSingle = logs.maxOfOrNull { it.distanceM } ?: 0

        val weeks = WeekCalculator.journeyWeeksWithLogs(logs, settings.onboardedDate)
            .map { WeekCalculator.weekProgress(logs, settings.onboardedDate, settings.weeklyGoal, it) }
        val weeksMet = weeks.count { it.mainQuestMet }
        val questsCompleted = weeks.sumOf { it.tasksDone }

        // Includes weeks before the journey start: "most days in any week" is a plain fact.
        val daysPerWeek = logs.groupBy { WeekCalculator.weekStart(it.date) }
            .mapValues { (_, weekLogs) -> weekLogs.map { it.date }.distinct().size }
        val notesPerWeek = logs.filter { !it.note.isNullOrBlank() }
            .groupBy { WeekCalculator.weekStart(it.date) }
            .mapValues { (_, weekLogs) -> weekLogs.size }

        val completedChapters = Chapters.ALL.count { chapter ->
            runDays >= Chapters.runDaysNeeded(chapter, settings.initialStoryPaceOnePerWeek)
        }

        val metrics = buildMap {
            put(BadgeMetric.RUN_DAYS, runDays)
            put(BadgeMetric.MAX_SESSION_M, maxSingle)
            put(BadgeMetric.TOTAL_M, totalM.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            put(BadgeMetric.WEEKS_MET, weeksMet)
            put(BadgeMetric.MAX_DAYS_IN_ANY_WEEK, daysPerWeek.values.maxOrNull() ?: 0)
            put(BadgeMetric.MAX_NOTED_LOGS_IN_WEEK, notesPerWeek.values.maxOrNull() ?: 0)
            put(BadgeMetric.ACTIVE_MONTHS, dates.map { it.year * 12 + it.monthValue }.distinct().size)
            put(BadgeMetric.QUEST_COMPLETIONS, questsCompleted)
            put(BadgeMetric.CHAPTER_COMPLETED, completedChapters)

            put(BadgeMetric.TAG_PARK, logs.count { RunTag.PARK in it.tags })
            put(BadgeMetric.TAG_FAMILY, logs.count { RunTag.FAMILY in it.tags })
            put(BadgeMetric.TAG_NEW_ROUTE, logs.count { RunTag.NEW_ROUTE in it.tags })
            put(BadgeMetric.TAG_NATURE, logs.count { RunTag.NATURE in it.tags })

            put(BadgeMetric.MODE_WALK_ONLY, logs.count { it.mode == RunMode.WALK_ONLY })
            put(BadgeMetric.MODE_WALK_JOG, logs.count { it.mode == RunMode.WALK_JOG })

            val easy = logs.count { it.effort == Effort.EASY }
            val okay = logs.count { it.effort == Effort.OKAY }
            val hard = logs.count { it.effort == Effort.HARD }
            put(BadgeMetric.EFFORT_EASY, easy)
            put(BadgeMetric.EFFORT_OKAY, okay)
            put(BadgeMetric.EFFORT_HARD, hard)
            put(BadgeMetric.RUN_LOGS_WITH_EFFORT, logs.size)
            put(
                BadgeMetric.ALL_THREE_EFFORTS_RECORDED,
                if (easy > 0 && okay > 0 && hard > 0) 1 else 0,
            )

            put(BadgeMetric.NOTED_LOGS, logs.count { !it.note.isNullOrBlank() })
            put(BadgeMetric.MET_TARGET_LOGS, logs.count { it.distanceM >= it.targetMAtLog })
            put(BadgeMetric.BELOW_TARGET_LOGS, logs.count { it.distanceM < it.targetMAtLog })
            put(BadgeMetric.LOGS_AT_LEAST_100M, logs.count { it.distanceM >= 100 })
            put(BadgeMetric.LOGS_AT_MOST_500M, logs.count { it.distanceM <= 500 })

            put(BadgeMetric.COMEBACK_GAPS_14D, comebackGaps(dates, 14))
            put(BadgeMetric.COMEBACK_GAPS_30D, comebackGaps(dates, 30))

            val monthMetrics = listOf(
                BadgeMetric.RUNS_IN_MONTH_01, BadgeMetric.RUNS_IN_MONTH_02,
                BadgeMetric.RUNS_IN_MONTH_03, BadgeMetric.RUNS_IN_MONTH_04,
                BadgeMetric.RUNS_IN_MONTH_05, BadgeMetric.RUNS_IN_MONTH_06,
                BadgeMetric.RUNS_IN_MONTH_07, BadgeMetric.RUNS_IN_MONTH_08,
                BadgeMetric.RUNS_IN_MONTH_09, BadgeMetric.RUNS_IN_MONTH_10,
                BadgeMetric.RUNS_IN_MONTH_11, BadgeMetric.RUNS_IN_MONTH_12,
            )
            monthMetrics.forEachIndexed { index, metric ->
                put(metric, logs.count { it.date.monthValue == index + 1 })
            }
        }

        return StatsSnapshot(
            runDays = runDays,
            totalDistanceM = totalM,
            maxSingleDistanceM = maxSingle,
            weeksMet = weeksMet,
            questsCompleted = questsCompleted,
            completedChapters = completedChapters,
            metrics = metrics,
            earnedBadgeIds = BadgeEvaluator.earnedBadgeIds(metrics),
        )
    }

    /**
     * Gaps of at least [days] calendar days between consecutive distinct run dates.
     * The return run counts once per gap.
     */
    private fun comebackGaps(sortedDates: List<LocalDate>, days: Int): Int =
        sortedDates.zipWithNext().count { (previous, next) ->
            ChronoUnit.DAYS.between(previous, next) >= days
        }

    /** Days since the most recent run, or null when nothing has been logged. */
    fun daysSinceLastRun(logs: List<RunLog>, today: LocalDate): Long? =
        logs.maxOfOrNull { it.date }?.let { ChronoUnit.DAYS.between(it, today) }
}
