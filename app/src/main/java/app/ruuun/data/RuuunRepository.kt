package app.ruuun.data

import app.ruuun.content.Chapters
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
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

    /** Saves a new outing and returns everything the celebration screen needs. */
    suspend fun saveRun(
        date: LocalDate,
        distanceM: Int,
        effort: Effort,
        durationMinutes: Int?,
        mode: RunMode?,
        tags: Set<RunTag>,
        note: String?,
        now: Long,
    ): SaveOutcome? {
        val settings = dao.getSettings() ?: return null
        val existing = dao.getLogs()
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
        )
    }

    /** Editing recomputes everything; the target falls back to a safe repeat. */
    suspend fun updateRun(
        id: String,
        date: LocalDate,
        distanceM: Int,
        effort: Effort,
        durationMinutes: Int?,
        mode: RunMode?,
        tags: Set<RunTag>,
        note: String?,
        now: Long,
    ) {
        val settings = dao.getSettings() ?: return
        val existing = dao.getLog(id) ?: return
        val updated = existing.copy(
            date = date,
            distanceM = distanceM,
            effort = effort,
            durationMinutes = durationMinutes,
            mode = mode,
            tags = tags,
            note = note,
            updatedAt = now,
        )
        dao.upsertLog(updated)
        resetTargetAfterCorrection(settings)
    }

    suspend fun deleteRun(log: RunLog) {
        val settings = dao.getSettings() ?: return
        dao.deleteLog(log)
        resetTargetAfterCorrection(settings)
    }

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
