package app.ruuun

import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.data.RunnerSettings
import java.time.LocalDate

/** Shared builders so each test reads as the scenario it describes. */
object TestData {

    val ONBOARDED: LocalDate = LocalDate.of(2026, 9, 21) // a Monday

    fun settings(
        startingTargetM: Int = 300,
        currentTargetM: Int = startingTargetM,
        weeklyGoal: Int = 2,
        onePerWeekPace: Boolean = false,
        maxTargetM: Int = RunnerSettings.DEFAULT_MAX_TARGET_M,
        onboardedDate: LocalDate = ONBOARDED,
    ) = RunnerSettings(
        id = 1,
        nickname = "Runner",
        onboardedDate = onboardedDate,
        startingTargetM = startingTargetM,
        currentTargetM = currentTargetM,
        maxTargetM = maxTargetM,
        weeklyGoal = weeklyGoal,
        initialStoryPaceOnePerWeek = onePerWeekPace,
        onboardingComplete = true,
    )

    private var counter = 0

    fun log(
        date: LocalDate,
        distanceM: Int = 300,
        effort: Effort = Effort.OKAY,
        targetMAtLog: Int = 300,
        mode: RunMode? = null,
        tags: Set<RunTag> = emptySet(),
        note: String? = null,
        durationMinutes: Int? = null,
        id: String = "log-${counter++}",
    ) = RunLog(
        id = id,
        date = date,
        distanceM = distanceM,
        effort = effort,
        targetMAtLog = targetMAtLog,
        durationMinutes = durationMinutes,
        mode = mode,
        tags = tags,
        note = note,
        createdAt = counter.toLong(),
        updatedAt = counter.toLong(),
    )

    /** [days] distinct outing days, one per day starting at [from]. */
    fun run(days: Int, from: LocalDate = ONBOARDED, distanceM: Int = 300): List<RunLog> =
        (0 until days).map { log(from.plusDays(it.toLong()), distanceM = distanceM) }
}
