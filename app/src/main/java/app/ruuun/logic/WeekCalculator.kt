package app.ruuun.logic

import app.ruuun.content.SideQuest
import app.ruuun.content.WeekTheme
import app.ruuun.content.Weeks
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * ISO weeks run Monday to Sunday over the recorded local dates.
 *
 * A week is identified by its Monday, which sidesteps every ISO week-year rollover
 * problem: the Monday of the week containing 31 December 2026 is simply a date.
 */
object WeekCalculator {

    /** The Monday of the ISO week containing [date]. The absolute week identity. */
    fun weekStart(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /**
     * 1-based journey week: week 1 is the ISO week the child onboarded in.
     * Weeks before onboarding return zero or a negative number.
     */
    fun journeyWeek(onboardedDate: LocalDate, date: LocalDate): Int =
        ChronoUnit.WEEKS.between(weekStart(onboardedDate), weekStart(date)).toInt() + 1

    /** Authored theme for a date, rotating the content from journey week 53 onward. */
    fun themeFor(onboardedDate: LocalDate, date: LocalDate): WeekTheme =
        Weeks.themeFor(journeyWeek(onboardedDate, date).coerceAtLeast(1))

    /** Distinct outing dates in the ISO week starting on [monday]. */
    fun outingDaysInWeek(logs: List<RunLog>, monday: LocalDate): Int =
        logs.filter { weekStart(it.date) == monday }.map { it.date }.distinct().size

    /** Is the optional side quest satisfied by [logs] (already filtered to one week)? */
    fun sideQuestMet(weekLogs: List<RunLog>, quest: SideQuest): Boolean = when (quest) {
        SideQuest.ANY_RUN -> weekLogs.isNotEmpty()
        SideQuest.EASY -> weekLogs.any { it.effort == Effort.EASY }
        SideQuest.WALK -> weekLogs.any { it.mode == RunMode.WALK_ONLY }
        SideQuest.NOTE -> weekLogs.any { !it.note.isNullOrBlank() }
        SideQuest.PARK -> weekLogs.any { RunTag.PARK in it.tags }
        SideQuest.FAMILY -> weekLogs.any { RunTag.FAMILY in it.tags }
        SideQuest.NEW_ROUTE -> weekLogs.any { RunTag.NEW_ROUTE in it.tags }
        SideQuest.NATURE -> weekLogs.any { RunTag.NATURE in it.tags }
    }

    /** A single journey week, resolved against the logs. */
    data class WeekProgress(
        val journeyWeek: Int,
        val weekStart: LocalDate,
        val theme: WeekTheme,
        val outingDays: Int,
        val weeklyGoal: Int,
        val sideQuestMet: Boolean,
    ) {
        val mainQuestMet: Boolean get() = outingDays >= weeklyGoal
        val tasksDone: Int get() = (if (mainQuestMet) 1 else 0) + (if (sideQuestMet) 1 else 0)
    }

    fun weekProgress(
        logs: List<RunLog>,
        onboardedDate: LocalDate,
        weeklyGoal: Int,
        monday: LocalDate,
    ): WeekProgress {
        val weekLogs = logs.filter { weekStart(it.date) == monday }
        val week = journeyWeek(onboardedDate, monday)
        val theme = Weeks.themeFor(week.coerceAtLeast(1))
        return WeekProgress(
            journeyWeek = week,
            weekStart = monday,
            theme = theme,
            outingDays = weekLogs.map { it.date }.distinct().size,
            weeklyGoal = weeklyGoal,
            sideQuestMet = sideQuestMet(weekLogs, theme.sideQuest),
        )
    }

    /**
     * Journey weeks that contain at least one log, oldest first. Weeks before the
     * onboarding week are excluded: themes and quests only start at the journey start.
     */
    fun journeyWeeksWithLogs(logs: List<RunLog>, onboardedDate: LocalDate): List<LocalDate> {
        val firstWeek = weekStart(onboardedDate)
        return logs.map { weekStart(it.date) }
            .filter { !it.isBefore(firstWeek) }
            .distinct()
            .sorted()
    }
}
