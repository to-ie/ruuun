package app.ruuun

import app.ruuun.backup.BackupFile
import app.ruuun.content.Achievements
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.data.RunnerSettings
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StopGoal
import app.ruuun.logic.TargetRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

/**
 * Randomised journeys, checked against the rules after every run. This is the net that
 * catches a rule change quietly breaking an unrelated one; the fixed-scenario tests
 * elsewhere say what the rules *are*.
 */
class InvariantSweepTest {

    @Test
    fun `a thousand random journeys keep every rule`() {
        val rng = Random(20261101)
        repeat(1_000) { seed ->
            var settings = RunnerSettings(
                id = 1,
                nickname = "R",
                onboardedDate = LocalDate.of(2026, 1, 5).plusDays(rng.nextInt(0, 400).toLong()),
                startingTargetM = listOf(300, 500, 800).random(rng),
                currentTargetM = 0,
                maxTargetM = listOf(1_000, 3_000, 50_000).random(rng),
                weeklyGoal = rng.nextInt(1, 4),
                initialStoryPaceOnePerWeek = rng.nextBoolean(),
                onboardingComplete = true,
            ).let { it.copy(currentTargetM = it.startingTargetM) }

            val logs = mutableListOf<RunLog>()
            var date = settings.onboardedDate
            repeat(rng.nextInt(1, 40)) { step ->
                date = date.plusDays(rng.nextInt(0, 40).toLong())
                val log = RunLog(
                    id = "s$seed-$step",
                    date = date,
                    distanceM = rng.nextInt(1, 6_000),
                    effort = Effort.entries.random(rng),
                    targetMAtLog = settings.currentTargetM,
                    durationMinutes = if (rng.nextBoolean()) rng.nextInt(1, 200) else null,
                    mode = if (rng.nextBoolean()) RunMode.entries.random(rng) else null,
                    tags = RunTag.entries.filter { rng.nextInt(5) == 0 }.toSet(),
                    note = if (rng.nextBoolean()) "n" else null,
                    stops = if (rng.nextInt(4) == 0) null else rng.nextInt(0, 13),
                    stopsGoalAtLog = StopGoal.next(logs, settings),
                    createdAt = step.toLong(),
                    updatedAt = step.toLong(),
                )
                logs += log

                val was = settings.currentTargetM
                val next = TargetRecommender.afterLog(settings, logs, log)
                val why = "seed $seed step $step: ran ${log.distanceM} ${log.effort} " +
                    "with ${log.stops} stops " +
                    "on a $was mission (cap ${settings.maxTargetM}) -> $next"

                assertTrue("$why: below the floor", next >= RunnerSettings.MIN_TARGET_M)
                assertTrue("$why: above the cap", next <= settings.maxTargetM)
                assertEquals("$why: not a round 25 m", 0, next % 25)
                if (!TargetRecommender.lowStops(log, settings.lowStopsLimit)) {
                    assertTrue("$why: rose without low stops", next <= was)
                }
                if (next < settings.maxTargetM && log.distanceM > was && TargetRecommender.lowStops(log, settings.lowStopsLimit)) {
                    assertTrue("$why: beat the mission but it did not move past", next > log.distanceM)
                }
                if (log.effort == Effort.HARD && log.distanceM < was) {
                    assertTrue("$why: hard and short yet it rose", next <= was)
                }
                if (log.distanceM <= was && log.effort != Effort.EASY) {
                    assertTrue("$why: rose without beating it or feeling easy", next <= was)
                }
                settings = settings.copy(currentTargetM = next)

                val s = StatsCalculator.snapshot(logs, settings)
                assertEquals("seed $seed: xp", 50 * s.runDays, s.xp)
                assertEquals("seed $seed: level", 1 + s.runDays / 5, s.level)
                assertTrue("seed $seed: chapters", s.completedChapters in 0..12)
                assertTrue("seed $seed: runs to next level", s.runsToNextLevel in 1..5)
                assertTrue("seed $seed: level progress", s.levelProgress in 0f..1f)
                Achievements.ALL.forEach { badge ->
                    val f = BadgeEvaluator.progressFraction(badge, s)
                    assertTrue("seed $seed: ${badge.id} fraction $f", !f.isNaN() && f in 0f..1f)
                }
                val week = JourneyView.thisWeek(logs, settings, date)
                assertTrue("seed $seed: week tasks", week.tasksDone in 0..2)
                val chapter = JourneyView.chapterProgress(s, settings)
                assertTrue("seed $seed: chapter fraction", chapter.fraction in 0f..1f)
            }

            // Whatever the app exports, it must be able to import back to the same state.
            val text = BackupFile.write(settings, logs, "2026-11-01T00:00:00Z")
            val parsed = BackupFile.parse(text, LocalDate.of(2099, 1, 1))
            assertTrue("seed $seed: own export was rejected", parsed is BackupFile.ParseResult.Ok)
            val payload = (parsed as BackupFile.ParseResult.Ok).payload
            assertEquals(
                "seed $seed: round trip changed the computed state",
                StatsCalculator.snapshot(logs, settings),
                StatsCalculator.snapshot(payload.logs, payload.settings),
            )
        }
    }
}
