package app.ruuun.data

import app.ruuun.content.Chapters
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StopGoal
import app.ruuun.logic.TargetRecommender
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

/**
 * The only writer. Every mutation recomputes the derived state from the logs, so the
 * database never holds XP, badges, quests or chapter rows.
 */
class RuuunRepository(private val dao: RuuunDao) {

    val settings: Flow<RunnerSettings?> = dao.observeSettings()
    val logs: Flow<List<RunLog>> = dao.observeLogs()

    suspend fun completeSetup(
        nickname: String,
        startingTargetM: Int,
        weeklyGoal: Int,
        today: LocalDate,
    ) {
        dao.upsertSettings(
            RunnerSettings(
                id = 1,
                nickname = nickname.ifBlank { RunnerSettings.DEFAULT_NICKNAME },
                onboardedDate = today,
                startingTargetM = startingTargetM,
                currentTargetM = startingTargetM,
                maxTargetM = RunnerSettings.DEFAULT_MAX_TARGET_M,
                weeklyGoal = weeklyGoal,
                initialStoryPaceOnePerWeek = weeklyGoal == 1,
                onboardingComplete = true,
            ),
        )
    }

    suspend fun updateSettings(transform: (RunnerSettings) -> RunnerSettings) {
        val current = dao.getSettings() ?: return
        dao.upsertSettings(transform(current))
    }

    /** Saves a new run and returns everything the celebration screen needs. */
    suspend fun saveRun(
        date: LocalDate,
        distanceM: Int,
        effort: Effort,
        durationMinutes: Int?,
        mode: RunMode?,
        tags: Set<RunTag>,
        note: String?,
        stops: Int?,
        now: Long,
    ): SaveOutcome? {
        val settings = dao.getSettings() ?: return null
        val existing = dao.getLogs()
        val backdated = existing.maxOfOrNull { it.date }?.let { date.isBefore(it) } ?: false
        // A run on an earlier day was not run against today's goal.
        val stopGoal = if (backdated) null else StopGoal.next(existing, settings)
        val log = RunLog(
            id = UUID.randomUUID().toString(),
            date = date,
            distanceM = distanceM,
            effort = effort,
            targetMAtLog = settings.currentTargetM,
            durationMinutes = durationMinutes,
            mode = mode,
            tags = tags,
            note = note,
            stops = stops,
            stopsGoalAtLog = stopGoal,
            createdAt = now,
            updatedAt = now,
        )

        val before = StatsCalculator.snapshot(existing, settings)
        val after = StatsCalculator.snapshot(existing + log, settings)
        val nextTarget = TargetRecommender.afterLog(settings, existing + log, log)

        dao.insertLog(log)
        if (nextTarget != settings.currentTargetM) {
            dao.upsertSettings(settings.copy(currentTargetM = nextTarget))
        }

        return SaveOutcome(
            log = log,
            before = before,
            after = after,
            newBadges = app.ruuun.logic.BadgeEvaluator.newlyEarned(before, after),
            completedChapter = newlyCompletedChapter(before, after),
            nextTargetM = nextTarget,
            previousTargetM = settings.currentTargetM,
            previousStopGoal = stopGoal,
            nextStopGoal = StopGoal.next(existing + log, settings.copy(currentTargetM = nextTarget)),
            setsStopGoal = StopGoal.goalSource(existing + log)?.id == log.id,
            backdated = backdated,
        )
    }

    /**
     * Editing recomputes everything that is derived. The mission only falls back to a
     * safe repeat when the corrected run is, or becomes, the latest one: fixing an older
     * run must not undo what newer runs (or Easier and Harder) set.
     */
    suspend fun updateRun(
        id: String,
        date: LocalDate,
        distanceM: Int,
        effort: Effort,
        durationMinutes: Int?,
        mode: RunMode?,
        tags: Set<RunTag>,
        note: String?,
        stops: Int?,
        now: Long,
    ) {
        val settings = dao.getSettings() ?: return
        val existing = dao.getLog(id) ?: return
        val wasLatest = latestOf(dao.getLogs())?.id == existing.id
        val updated = existing.copy(
            date = date,
            distanceM = distanceM,
            effort = effort,
            durationMinutes = durationMinutes,
            mode = mode,
            tags = tags,
            note = note,
            stops = stops,
            updatedAt = now,
        )
        dao.upsertLog(updated)
        // Only the date, distance and effort reset the mission. Adding a note, tags or a
        // stop count to an old run must not knock the next mission back.
        val missionInputsChanged = existing.date != date ||
            existing.distanceM != distanceM ||
            existing.effort != effort
        if (missionInputsChanged) {
            if (wasLatest || latestOf(dao.getLogs())?.id == updated.id) {
                resetTargetAfterCorrection(settings)
            }
            return
        }
        // A corrected stop count on the latest run that crosses the low-stops limit
        // replays the rules for that run, as if it had been logged that way.
        val limit = settings.lowStopsLimit
        val crossed = TargetRecommender.lowStops(existing, limit) != TargetRecommender.lowStops(updated, limit)
        val all = dao.getLogs()
        val latest = all.maxWithOrNull(compareBy<RunLog> { it.date }.thenBy { it.createdAt })
        if (crossed && latest?.id == updated.id) {
            val atLog = settings.copy(currentTargetM = updated.targetMAtLog)
            // What the run set when it was saved. If the mission is something else now, a
            // parent changed it by hand since (Easier, Harder or reset), so leave it alone.
            val before = TargetRecommender.afterLog(
                atLog,
                all.map { if (it.id == existing.id) existing else it },
                existing,
            )
            val replayed = TargetRecommender.afterLog(atLog, all, updated)
                .coerceIn(RunnerSettings.MIN_TARGET_M, settings.maxTargetM)
            if (settings.currentTargetM == before && replayed != settings.currentTargetM) {
                dao.upsertSettings(settings.copy(currentTargetM = replayed))
            }
        }
    }

    /** Deleting the latest run falls back to a safe repeat; an older one keeps the mission. */
    suspend fun deleteRun(log: RunLog) {
        val settings = dao.getSettings() ?: return
        val wasLatest = latestOf(dao.getLogs())?.id == log.id
        dao.deleteLog(log)
        if (wasLatest) resetTargetAfterCorrection(settings)
    }

    private fun latestOf(logs: List<RunLog>): RunLog? =
        logs.maxWithOrNull(compareBy<RunLog> { it.date }.thenBy { it.createdAt })

    private suspend fun resetTargetAfterCorrection(settings: RunnerSettings) {
        val remaining = dao.getLogs()
        val target = TargetRecommender.afterCorrection(settings, remaining)
        if (target != settings.currentTargetM) {
            dao.upsertSettings(settings.copy(currentTargetM = target))
        }
    }

    suspend fun makeEasier() {
        val settings = dao.getSettings() ?: return
        dao.upsertSettings(
            settings.copy(currentTargetM = TargetRecommender.makeEasier(settings.currentTargetM)),
        )
    }

    suspend fun makeHarder() {
        val settings = dao.getSettings() ?: return
        dao.upsertSettings(
            settings.copy(
                currentTargetM = TargetRecommender.makeHarder(
                    settings.currentTargetM,
                    settings.maxTargetM,
                ),
            ),
        )
    }

    suspend fun resetTargetToStart() {
        val settings = dao.getSettings() ?: return
        // The cap may have been lowered below the original starting distance.
        val target = settings.startingTargetM
            .coerceIn(RunnerSettings.MIN_TARGET_M, settings.maxTargetM)
        dao.upsertSettings(settings.copy(currentTargetM = target))
    }

    suspend fun snapshotForExport(): Pair<RunnerSettings, List<RunLog>>? {
        val settings = dao.getSettings() ?: return null
        return settings to dao.getLogs()
    }

    suspend fun replaceAll(settings: RunnerSettings, logs: List<RunLog>) {
        dao.replaceAll(settings.copy(id = 1), logs)
    }

    suspend fun deleteEverything() {
        dao.deleteEverything()
    }

    private fun newlyCompletedChapter(
        before: app.ruuun.logic.StatsSnapshot,
        after: app.ruuun.logic.StatsSnapshot,
    ) = if (after.completedChapters > before.completedChapters) {
        Chapters.ALL.getOrNull(after.completedChapters - 1)
    } else {
        null
    }
}
