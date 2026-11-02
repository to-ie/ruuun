package app.ruuun

import app.ruuun.backup.BackupFile
import app.ruuun.content.BadgeMetric
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunnerSettings
import app.ruuun.logic.JourneyView
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StopGoal
import app.ruuun.logic.TargetRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The 1.3 review: same-day farming, back-filled runs, short runs and the stop limit. */
class StopEdgeCaseTest {

    private val day1 = TestData.ONBOARDED
    private val day2 = day1.plusDays(2)
    private val day3 = day1.plusDays(4)

    @Test
    fun `sixteen short non-stop laps on one day count as one day`() {
        val laps = (1..16).map { TestData.log(day1, distanceM = 100, stops = 0, stopsGoalAtLog = if (it == 1) null else 0) }
        val stats = StatsCalculator.snapshot(laps, TestData.settings())
        assertEquals(1, stats.metric(BadgeMetric.NONSTOP_RUNS))
        assertEquals("laps under half the mission never meet a goal", 0, stats.metric(BadgeMetric.STOP_GOALS_MET))
        assertTrue("NS-010" !in stats.earnedBadgeIds)
        assertTrue("NS-007" !in stats.earnedBadgeIds)
    }

    @Test
    fun `a run entered for an earlier date never moves the stop goal`() {
        val mon = TestData.log(day1, distanceM = 1_000, stops = 6, targetMAtLog = 1_000)
        val wed = TestData.log(day3, distanceM = 1_000, targetMAtLog = 1_000) // not counted
        val tueLater = TestData.log(day2, distanceM = 800, stops = 1, effort = Effort.EASY, targetMAtLog = 1_000)
        val logs = listOf(mon, wed, tueLater)
        assertEquals(setOf(tueLater.id), StopGoal.backdatedIds(logs))
        assertEquals("still built on Monday", 5, StopGoal.next(logs, TestData.settings(currentTargetM = 1_000)))
    }

    @Test
    fun `runs imported at the same moment are not backdated against each other`() {
        val a = TestData.log(day2, stops = 4).copy(createdAt = 5)
        val b = TestData.log(day1, stops = 6).copy(createdAt = 5)
        assertTrue(StopGoal.backdatedIds(listOf(a, b)).isEmpty())
    }

    @Test
    fun `a short run does not set the stop goal`() {
        val main = TestData.log(day1, distanceM = 1_200, stops = 6, targetMAtLog = 1_200)
        val short = TestData.log(day2, distanceM = 400, stops = 0, targetMAtLog = 1_200)
        assertEquals(5, StopGoal.next(listOf(main, short), TestData.settings(currentTargetM = 1_200)))
    }

    @Test
    fun `the first real run sets a goal of 7, whatever 1_2 did to the mission`() {
        val first = TestData.log(day1, distanceM = 1_100, stops = 8, targetMAtLog = 300)
        assertEquals(7, StopGoal.next(listOf(first), TestData.settings(currentTargetM = 1_200)))
    }

    @Test
    fun `two okay runs that match the mission with low stops step it up`() {
        val settings = TestData.settings(currentTargetM = 1_200)
        val a = TestData.log(day1, distanceM = 1_200, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 1)
        val b = TestData.log(day2, distanceM = 1_200, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 2)
        assertEquals(1_200, TargetRecommender.afterLog(settings, listOf(a), a))
        assertEquals(1_250, TargetRecommender.afterLog(settings, listOf(a, b), b))
    }

    @Test
    fun `the parent's stop limit is what the distance rule uses`() {
        val run = TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 4)
        val strict = TestData.settings(currentTargetM = 1_200)
        val loose = strict.copy(lowStopsLimit = 4)
        val nonStopOnly = strict.copy(lowStopsLimit = 0)
        assertEquals("catches up, no stretch", 1_300, TargetRecommender.afterLog(strict, listOf(run), run))
        assertEquals("low enough to stretch", 1_350, TargetRecommender.afterLog(loose, listOf(run), run))
        val zero = run.copy(stops = 0)
        assertEquals(1_350, TargetRecommender.afterLog(nonStopOnly, listOf(zero), zero))
        assertEquals(1_300, TargetRecommender.afterLog(nonStopOnly, listOf(run.copy(stops = 1)), run.copy(stops = 1)))
        assertEquals("no stops", JourneyView.stopsOrFewer(0))
        assertEquals("1 stop or fewer", JourneyView.stopsOrFewer(1))
    }

    // --- Notes --------------------------------------------------------------

    private fun outcome(
        log: RunLog,
        next: Int = 1_200,
        previous: Int = 1_200,
        nextGoal: Int? = null,
        setsStopGoal: Boolean = true,
    ) = SaveOutcome(
        log = log, before = StatsSnapshot0, after = StatsSnapshot0, newBadges = emptyList(),
        completedChapter = null, nextTargetM = next, previousTargetM = previous,
        previousStopGoal = log.stopsGoalAtLog, nextStopGoal = nextGoal, setsStopGoal = setsStopGoal,
    )

    private val StatsSnapshot0 = app.ruuun.logic.StatsSnapshot.EMPTY

    @Test
    fun `stop notes for extra, short and step-up runs`() {
        val extra = TestData.log(day1, distanceM = 800, stops = 1, targetMAtLog = 1_200, stopsGoalAtLog = 5)
        assertEquals(
            "1 stop on this extra run. Your longest run today sets the stop goal.",
            JourneyView.stopNote(outcome(extra, nextGoal = 3, setsStopGoal = false)),
        )
        val short = TestData.log(day1, distanceM = 300, stops = 1, targetMAtLog = 1_200, stopsGoalAtLog = 5)
        assertEquals("1 stop. Short runs don't change the stop goal.", JourneyView.stopNote(outcome(short)))
        val stepUp = TestData.log(day1, distanceM = 1_300, stops = 2, targetMAtLog = 1_200, stopsGoalAtLog = 3)
        assertEquals(
            "2 stops. You beat your stop goal! Keep it at 2 stops or fewer while the distance grows.",
            JourneyView.stopNote(outcome(stepUp, next = 1_400, nextGoal = 2)),
        )
    }

    @Test
    fun `one stop over the limit reads naturally`() {
        val settings = TestData.settings(currentTargetM = 1_200)
        val run = TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 3)
        assertEquals(
            "You beat 1.2 km! With 3 stops the distance stays the same. 1 fewer stop and it gets longer.",
            JourneyView.nextMissionNote(outcome(run), settings),
        )
    }

    // --- Personal bests -------------------------------------------------------

    @Test
    fun `personal bests show from the second run on`() {
        val settings = TestData.settings()
        val first = TestData.log(day1, distanceM = 1_100, stops = 8)
        val second = TestData.log(day2, distanceM = 1_300, stops = 0)
        val s0 = StatsCalculator.snapshot(emptyList(), settings)
        val s1 = StatsCalculator.snapshot(listOf(first), settings)
        val s2 = StatsCalculator.snapshot(listOf(first, second), settings)
        val firstOutcome = outcome(first).copy(before = s0, after = s1)
        assertTrue(JourneyView.records(firstOutcome).isEmpty())
        assertEquals(
            listOf("New longest run: 1.3 km", "New longest non-stop run: 1.3 km"),
            JourneyView.records(outcome(second).copy(before = s1, after = s2)),
        )
    }

    @Test
    fun `one year on counts days from the first run to the latest`() {
        val logs = listOf(TestData.log(day1), TestData.log(day1.plusDays(365)))
        val stats = StatsCalculator.snapshot(logs, TestData.settings())
        assertEquals(365, stats.metric(BadgeMetric.JOURNEY_DAYS))
        assertTrue("CO-006" in stats.earnedBadgeIds)
        assertTrue("CO-007" !in stats.earnedBadgeIds)
    }

    // --- Backups --------------------------------------------------------------

    private fun v1(maxTargetM: Int) = """
        {"format":"ruuun-offline-backup","schemaVersion":1,"exportedAt":"2026-11-01T18:00:00Z",
         "settings":{"nickname":"Runner","onboardedDate":"2026-09-21","startingTargetM":300,
         "currentTargetM":1200,"maxTargetM":$maxTargetM,"weeklyGoal":2,
         "initialStoryPaceOnePerWeek":false,"onboardingComplete":true},"logs":[]}
    """.trimIndent()

    private fun restored(text: String): RunnerSettings =
        (BackupFile.parse(text, today = day3.plusDays(60)) as BackupFile.ParseResult.Ok).payload.settings

    @Test
    fun `an old backup on the 3 km default restores with 5 km, a chosen cap stays`() {
        assertEquals(5_000, restored(v1(3_000)).maxTargetM)
        assertEquals(2_000, restored(v1(2_000)).maxTargetM)
        assertEquals(RunnerSettings.DEFAULT_LOW_STOPS, restored(v1(3_000)).lowStopsLimit)
    }

    @Test
    fun `the stop limit survives a backup, and nonsense is refused`() {
        val settings = TestData.settings().copy(lowStopsLimit = 4)
        val text = BackupFile.write(settings, emptyList(), "2026-11-02T18:00:00Z")
        assertEquals(4, restored(text).lowStopsLimit)
        val bad = text.replace("\"lowStopsLimit\": 4", "\"lowStopsLimit\": 40")
        assertTrue(BackupFile.parse(bad, day3.plusDays(60)) is BackupFile.ParseResult.Invalid)
    }

    @Test
    fun `no stop goal while nothing counted is long enough`() {
        assertNull(StopGoal.next(listOf(TestData.log(day1, distanceM = 100, stops = 0, targetMAtLog = 1_200)), TestData.settings(currentTargetM = 1_200)))
    }

    @Test
    fun `a short lap that meets the stop goal does not count as a goal day`() {
        val lap = TestData.log(day1, distanceM = 200, stops = 0, targetMAtLog = 1_200, stopsGoalAtLog = 5)
        val stats = StatsCalculator.snapshot(listOf(lap), TestData.settings())
        assertEquals(0, stats.metric(BadgeMetric.STOP_GOALS_MET))
        assertEquals(0, stats.metric(BadgeMetric.STOPS_COUNTED_DAYS))
    }

    @Test
    fun `below 250 m matching the mission does not promise a step`() {
        val settings = TestData.settings(currentTargetM = 225)
        val run = TestData.log(day1, distanceM = 225, effort = Effort.EASY, targetMAtLog = 225, stops = 0)
        assertEquals(225, TargetRecommender.afterLog(settings, listOf(run), run))
        assertEquals(
            "Same again next time. Go a little further to move it on.",
            JourneyView.nextMissionNote(outcome(run, next = 225, previous = 225), settings),
        )
    }
}
