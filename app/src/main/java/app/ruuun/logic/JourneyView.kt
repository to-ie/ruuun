package app.ruuun.logic

import app.ruuun.content.BadgeDef
import app.ruuun.content.Chapter
import app.ruuun.content.Chapters
import app.ruuun.data.RunLog
import app.ruuun.data.RunnerSettings
import java.time.LocalDate

/** Where the child is in the story. [chapter] is null once all twelve are complete. */
data class ChapterProgress(
    val chapter: Chapter?,
    val runDays: Int,
    val runDaysNeeded: Int,
    val previousChapterTarget: Int,
) {
    val isFinale: Boolean get() = chapter == null
    val title: String get() = chapter?.title ?: Chapters.FINALE_TITLE
    val teaser: String get() = chapter?.teaser ?: Chapters.FINALE_TEASER
    val worldTitle: String get() = chapter?.world?.title ?: "Your own legend"
    val fraction: Float
        get() = if (isFinale || runDaysNeeded <= 0) 1f
        else (runDays.toFloat() / runDaysNeeded).coerceIn(0f, 1f)
}

/** The four numbers in the History header. */
data class HistoryStats(
    val totalDistanceM: Long,
    val outingDays: Int,
    val longestOutingM: Int,
    val daysOutThisMonth: Int,
)

/** Read-only views over the logs, shared by the screens. */
object JourneyView {

    fun chapterProgress(stats: StatsSnapshot, settings: RunnerSettings): ChapterProgress {
        val completed = stats.completedChapters
        val next = Chapters.ALL.getOrNull(completed)
        val previousTarget = Chapters.ALL.getOrNull(completed - 1)
            ?.let { Chapters.runDaysNeeded(it, settings.initialStoryPaceOnePerWeek) } ?: 0
        return ChapterProgress(
            chapter = next,
            runDays = stats.outingDays,
            runDaysNeeded = next?.let {
                Chapters.runDaysNeeded(it, settings.initialStoryPaceOnePerWeek)
            } ?: 0,
            previousChapterTarget = previousTarget,
        )
    }

    fun thisWeek(
        logs: List<RunLog>,
        settings: RunnerSettings,
        today: LocalDate,
    ): WeekCalculator.WeekProgress = WeekCalculator.weekProgress(
        logs = logs,
        onboardedDate = settings.onboardedDate,
        weeklyGoal = settings.weeklyGoal,
        monday = WeekCalculator.weekStart(today),
    )

    /** Journey weeks 1..[upToJourneyWeek], including weeks with no logs. */
    fun journeyWeeks(
        logs: List<RunLog>,
        settings: RunnerSettings,
        upToJourneyWeek: Int,
    ): List<WeekCalculator.WeekProgress> {
        val firstMonday = WeekCalculator.weekStart(settings.onboardedDate)
        return (1..upToJourneyWeek.coerceAtLeast(1)).map { week ->
            WeekCalculator.weekProgress(
                logs = logs,
                onboardedDate = settings.onboardedDate,
                weeklyGoal = settings.weeklyGoal,
                monday = firstMonday.plusWeeks((week - 1).toLong()),
            )
        }
    }

    /** The single reachable badge shown on Home: the locked one closest to done. */
    fun upcomingBadge(stats: StatsSnapshot): BadgeDef? =
        BadgeEvaluator.closestLocked(stats, 1).firstOrNull()

    fun badgesWithinReach(stats: StatsSnapshot, limit: Int = 3): List<BadgeDef> =
        BadgeEvaluator.closestLocked(stats, limit)

    fun historyStats(logs: List<RunLog>, today: LocalDate): HistoryStats = HistoryStats(
        totalDistanceM = logs.sumOf { it.distanceM.toLong() },
        outingDays = StatsCalculator.outingDays(logs),
        longestOutingM = logs.maxOfOrNull { it.distanceM } ?: 0,
        daysOutThisMonth = logs
            .filter { it.date.year == today.year && it.date.month == today.month }
            .map { it.date }
            .distinct()
            .size,
    )

    /** Supportive one-liner beside the next mission on the Run saved screen. */
    fun nextMissionNote(outcome: SaveOutcome, settings: RunnerSettings): String = when {
        outcome.nextTargetM < outcome.previousTargetM -> "Nice and gentle next time."
        outcome.nextTargetM > outcome.previousTargetM -> "Your mission grew a little. Walk breaks still welcome."
        outcome.log.effort == app.ruuun.data.Effort.EASY &&
            outcome.log.distanceM >= outcome.log.targetMAtLog ->
            "One more easy outing at ${Formatters.distance(outcome.nextTargetM)} and it can grow a little."
        else -> "Same mission next time. Repeating builds confidence."
    }.let { if (settings.currentTargetM >= settings.maxTargetM) "This is your longest suggested mission." else it }

    /** Cheerful line under the big check on the Run saved screen. */
    fun savedHeadline(nickname: String): String = "Nice one, $nickname!"
}
