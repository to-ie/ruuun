package app.ruuun.logic

import app.ruuun.content.BadgeDef
import app.ruuun.content.Chapter
import app.ruuun.content.Chapters
import app.ruuun.content.World
import app.ruuun.data.Effort
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

    /** Says what moves the story: days run, not distance. */
    val progressLabel: String
        get() = if (isFinale) "Every chapter complete"
        else "$runDays of $runDaysNeeded runs · any distance counts"
}

/** One chapter's standing in the overall journey. */
data class ChapterStatus(
    val chapter: Chapter,
    val runDaysNeeded: Int,
    val runDays: Int,
    val isComplete: Boolean,
    val isCurrent: Boolean,
) {
    val fraction: Float
        get() = if (runDaysNeeded <= 0) 1f else (runDays.toFloat() / runDaysNeeded).coerceIn(0f, 1f)
}

/** A world and the three chapters inside it. */
data class WorldStatus(
    val world: World,
    val chapters: List<ChapterStatus>,
) {
    val completed: Int get() = chapters.count { it.isComplete }
    val total: Int get() = chapters.size
    val isComplete: Boolean get() = completed == total
    val isCurrent: Boolean get() = chapters.any { it.isCurrent }
    val isLocked: Boolean get() = completed == 0 && !isCurrent
}

/** The four numbers in the History header. */
data class HistoryStats(
    val totalDistanceM: Long,
    val runDays: Int,
    val longestRunM: Int,
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
            runDays = stats.runDays,
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

    /**
     * The whole story at a glance: four worlds, twelve chapters, with what is done,
     * what is in progress and what is still ahead.
     */
    fun journeyMap(stats: StatsSnapshot, settings: RunnerSettings): List<WorldStatus> {
        val currentNumber = Chapters.ALL.getOrNull(stats.completedChapters)?.number
        return Chapters.ALL
            .groupBy { it.world }
            .map { (world, chapters) ->
                WorldStatus(
                    world = world,
                    chapters = chapters.map { chapter ->
                        val needed = Chapters.runDaysNeeded(
                            chapter,
                            settings.initialStoryPaceOnePerWeek,
                        )
                        ChapterStatus(
                            chapter = chapter,
                            runDaysNeeded = needed,
                            runDays = stats.runDays,
                            isComplete = stats.runDays >= needed,
                            isCurrent = chapter.number == currentNumber,
                        )
                    },
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
        runDays = StatsCalculator.runDays(logs),
        longestRunM = logs.maxOfOrNull { it.distanceM } ?: 0,
        daysOutThisMonth = logs
            .filter { it.date.year == today.year && it.date.month == today.month }
            .map { it.date }
            .distinct()
            .size,
    )

    /**
     * The line beside the next mission on the Run saved screen: what happened to the
     * mission and why, following the same rules as [TargetRecommender.afterLog].
     */
    fun nextMissionNote(outcome: SaveOutcome, settings: RunnerSettings): String {
        val log = outcome.log
        val was = Formatters.distance(outcome.previousTargetM)
        val beat = log.distanceM > outcome.previousTargetM
        val went = outcome.nextTargetM.compareTo(outcome.previousTargetM)
        val atCap = outcome.nextTargetM >= settings.maxTargetM
        val capNote = " That is your longest mission setting."
        return when {
            outcome.backdated -> "Runs on earlier days don't change the mission."
            went > 0 && beat -> when (log.effort) {
                Effort.EASY -> "You beat $was and it felt easy, so it goes up."
                Effort.OKAY -> "You beat $was and it felt okay, so it goes up a step."
                Effort.HARD -> "You beat $was. It felt hard, so it only goes up a little."
            } + if (atCap) capNote else ""
            went > 0 -> "Two easy days at $was, so it goes up a step." + if (atCap) capNote else ""
            went < 0 -> "That felt hard, so next time is a bit shorter."
            atCap -> "You are at your longest mission setting."
            log.distanceM < log.targetMAtLog -> "Same again next time. Turning up still counts."
            log.effort == Effort.EASY -> "One more easy day at $was and it goes up."
            else -> "Same again next time. Repeating is how it sticks."
        }
    }

    /** Cheerful line under the big check on the Run saved screen. */
    fun savedHeadline(nickname: String): String = "Nice one, $nickname!"
}
