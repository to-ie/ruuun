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

    /** One day in the Monday to Sunday row on Home. */
    data class WeekDay(val date: LocalDate, val ran: Boolean, val isToday: Boolean)

    /** The seven days of the week starting [weekStart], with the days that had a run. */
    fun weekDays(logs: List<RunLog>, weekStart: LocalDate, today: LocalDate): List<WeekDay> {
        val ranOn = logs.map { it.date }.toSet()
        return (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            WeekDay(date = date, ran = date in ranOn, isToday = date == today)
        }
    }

    /** "Ran on Monday and Wednesday", or "No runs yet" for TalkBack. */
    fun weekDaysDescription(days: List<WeekDay>): String {
        val names = days.filter { it.ran }.map {
            it.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.UK)
        }
        return when (names.size) {
            0 -> "No runs yet this week"
            1 -> "Ran on ${names[0]}"
            else -> "Ran on ${names.dropLast(1).joinToString(", ")} and ${names.last()}"
        }
    }

    /**
     * The navy pill under Ruu on Home: the stop goal if there is one, and that breaks
     * are fine. Short, because it sits on one line under the fox.
     */
    fun homePill(stopGoal: Int?, lastStops: Int?, lowStopsLimit: Int, atCap: Boolean, welcomeBack: Boolean): String {
        if (welcomeBack) return "Repeat this one, or make it easier"
        if (stopGoal == null) {
            return if (atCap) "Breaks are fine · your longest mission setting"
            else "Breaks are fine · longer after ${stopsOrFewer(lowStopsLimit)}"
        }
        val goal = StopGoal.goalLabel(stopGoal)
        val from = lastStops?.takeIf { it > stopGoal }?.let { " · from $it" } ?: ""
        return "$goal$from · breaks are fine"
    }

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
        val limit = settings.lowStopsLimit
        val was = Formatters.mission(outcome.previousTargetM)
        val beat = log.distanceM > outcome.previousTargetM
        val matched = !beat && log.distanceM >= log.targetMAtLog && log.effort != Effort.HARD
        val went = outcome.nextTargetM.compareTo(outcome.previousTargetM)
        val atCap = outcome.nextTargetM >= settings.maxTargetM
        val capNote = " That is your longest mission setting."
        return when {
            outcome.backdated -> "Runs on earlier days don't change the mission."
            // Caught up to the distance run, without stretching further.
            went > 0 && beat && !TargetRecommender.lowStops(log, limit) -> {
                val now = Formatters.mission(outcome.nextTargetM)
                "You beat $was, so the new mission is $now. " + when (val stops = log.stops) {
                    null -> "Count your stops: it gets longer after a run with ${stopsOrFewer(limit)}."
                    else -> "Now try it with fewer stops: ${fewerToGo(stops - limit)} and it gets longer."
                } + if (atCap) capNote else ""
            }
            went > 0 && beat -> when (log.effort) {
                Effort.EASY -> "You beat $was and it felt easy, so it goes up."
                Effort.OKAY -> "You beat $was and it felt okay, so it goes up a step."
                Effort.HARD -> "You beat $was. It felt hard, so it only goes up a little."
            } + if (atCap) capNote else ""
            went > 0 -> "Two days at $was with ${stopsOrFewer(limit)}, so it goes up a step." +
                if (atCap) capNote else ""
            went < 0 -> "That felt hard, so next time is a bit shorter."
            atCap -> "You are at your longest mission setting."
            // Would have gone up, but the stops were not low enough (or not counted).
            (beat || matched) && !TargetRecommender.lowStops(log, limit) -> when (val stops = log.stops) {
                null -> "Same distance again. Count your stops: it gets longer after a run " +
                    "with ${stopsOrFewer(limit)}."
                else -> (if (beat) "You beat $was! " else "") +
                    "With ${StopGoal.stopsLabel(stops)} the distance stays the same. " +
                    fewerToGo(stops - limit) + " and it gets longer."
            }
            log.distanceM < log.targetMAtLog -> "Same again next time. Turning up still counts."
            // Below 250 m one step is more than 10%, so matching never steps it up there.
            matched && TargetRecommender.increase(outcome.previousTargetM, settings.maxTargetM) ==
                outcome.previousTargetM -> "Same again next time. Go a little further to move it on."
            matched -> "One more run at $was with ${stopsOrFewer(limit)} and it goes up."
            else -> "Same again next time. Repeating is how it sticks."
        }
    }

    /**
     * The line beside the stop count on the Run saved screen, or null when this run's
     * stops were not counted. Stopping is never treated as failing.
     */
    fun stopNote(outcome: SaveOutcome): String? {
        val log = outcome.log
        val stops = log.stops ?: return null
        val label = StopGoal.stopsLabel(stops)
        val goal = outcome.previousStopGoal
        val next = outcome.nextStopGoal
        val nextLine = when {
            next == null -> ""
            next == 0 -> " Next time, try it non-stop."
            log.effort == Effort.HARD && next == stops ->
                " It felt hard, so the goal stays at ${goalText(next)}."
            // Within the parent's limit: the distance grows and the stops only need to hold.
            next == stops -> " Keep it at ${goalText(next)} while the distance grows."
            else -> " Next time, try ${goalText(next)}."
        }
        return when {
            stops == 0 -> "Non-stop! You went the whole way."
            outcome.backdated -> "$label. Runs on earlier days don't change the stop goal."
            !StopGoal.countsForGoal(log) -> "$label. Short runs don't change the stop goal."
            !outcome.setsStopGoal -> "$label on this extra run. Your longest run today sets the stop goal."
            goal == null -> "First count: $label.$nextLine"
            stops < goal -> "$label. You beat your stop goal!$nextLine"
            stops == goal -> "$label. Stop goal met!$nextLine"
            else -> "$label this time. Breaks are fine.$nextLine"
        }
    }

    /** New personal bests from this run, for the Run saved screen. Empty on the first run. */
    fun records(outcome: SaveOutcome): List<String> {
        val before = outcome.before
        val after = outcome.after
        if (before.runDays == 0 && before.maxSingleDistanceM == 0) return emptyList()
        return buildList {
            if (after.maxSingleDistanceM > before.maxSingleDistanceM) {
                add("New longest run: ${Formatters.distance(after.maxSingleDistanceM)}")
            }
            val nonStopBefore = before.metric(app.ruuun.content.BadgeMetric.NONSTOP_MAX_M)
            val nonStopAfter = after.metric(app.ruuun.content.BadgeMetric.NONSTOP_MAX_M)
            if (nonStopAfter > nonStopBefore) {
                add("New longest non-stop run: ${Formatters.distance(nonStopAfter)}")
            }
        }
    }

    /** "2 stops or fewer", "non-stop" for a limit of zero. */
    fun stopsOrFewer(limit: Int): String =
        if (limit == 0) "no stops" else "${StopGoal.stopsLabel(limit)} or fewer"

    private fun fewerToGo(n: Int): String = if (n == 1) "1 fewer stop" else "$n fewer stops"

    private fun goalText(goal: Int): String = StopGoal.goalLabel(goal).lowercase()

    /** Cheerful line under the big check on the Run saved screen. */
    fun savedHeadline(nickname: String): String = "Nice one, $nickname!"
}
