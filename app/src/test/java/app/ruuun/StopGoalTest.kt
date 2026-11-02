package app.ruuun

import app.ruuun.backup.BackupFile
import app.ruuun.content.BadgeMetric
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunnerSettings
import app.ruuun.logic.JourneyView
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StatsSnapshot
import app.ruuun.logic.StopGoal
import app.ruuun.logic.TargetRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Stops: the second half of a mission, and the Non-stop badges that follow it. */
class StopGoalTest {

    private val day1 = TestData.ONBOARDED
    private val day2 = day1.plusDays(2)
    private val day3 = day1.plusDays(4)

    @Test
    fun `no stop goal until stops have been counted`() {
        assertNull(StopGoal.next(emptyList(), TestData.settings(currentTargetM = 300)))
        assertNull(StopGoal.next(listOf(TestData.log(day1)), TestData.settings(currentTargetM = 300)))
    }

    @Test
    fun `the first real run - 1_1 km with 8 stops - sets a goal of 7`() {
        val first = TestData.log(day1, distanceM = 1_100, stops = 8)
        assertEquals(7, StopGoal.next(listOf(first), TestData.settings(currentTargetM = 300)))
    }

    @Test
    fun `after a hard run the goal stays the same`() {
        val hard = TestData.log(day1, effort = Effort.HARD, stops = 8)
        assertEquals(8, StopGoal.next(listOf(hard), TestData.settings(currentTargetM = 300)))
    }

    @Test
    fun `within the stop limit the goal holds, at the cap it asks for one fewer`() {
        val run = listOf(TestData.log(day1, stops = 1))
        assertEquals("distance grows instead", 1, StopGoal.next(run, TestData.settings(currentTargetM = 300)))
        val atCap = TestData.settings(currentTargetM = 5_000)
        assertEquals("at the cap, never below non-stop", 0, StopGoal.next(run, atCap))
        assertEquals(0, StopGoal.next(listOf(TestData.log(day1, stops = 0)), atCap))
        assertEquals(
            "a stricter limit keeps asking",
            0,
            StopGoal.next(run, TestData.settings(currentTargetM = 300).copy(lowStopsLimit = 0)),
        )
    }

    @Test
    fun `a worse day relaxes the goal instead of piling on`() {
        val logs = listOf(
            TestData.log(day1, stops = 5),
            TestData.log(day2, stops = 9),
        )
        assertEquals(8, StopGoal.next(logs, TestData.settings(currentTargetM = 300)))
    }

    @Test
    fun `an uncounted later run leaves the goal where it was`() {
        val logs = listOf(TestData.log(day1, stops = 6), TestData.log(day2))
        assertEquals(5, StopGoal.next(logs, TestData.settings(currentTargetM = 300)))
    }

    @Test
    fun `a short extra lap on the same day does not set the goal`() {
        val logs = listOf(
            TestData.log(day1, distanceM = 1_100, stops = 8),
            TestData.log(day1, distanceM = 200, stops = 0),
        )
        assertEquals(7, StopGoal.next(logs, TestData.settings(currentTargetM = 300)))
        assertEquals(1, StopGoal.countedDays(logs).size)
    }

    @Test
    fun `met compares with the goal that was shown at the time`() {
        assertEquals(true, StopGoal.met(TestData.log(day1, stops = 7, stopsGoalAtLog = 7)))
        assertEquals(false, StopGoal.met(TestData.log(day1, stops = 8, stopsGoalAtLog = 7)))
        assertNull(StopGoal.met(TestData.log(day1, stops = 8)))
        assertNull(StopGoal.met(TestData.log(day1, stopsGoalAtLog = 7)))
    }

    @Test
    fun `labels read naturally`() {
        assertEquals("1 stop", StopGoal.stopsLabel(1))
        assertEquals("8 stops", StopGoal.stopsLabel(8))
        assertEquals("7 stops or fewer", StopGoal.goalLabel(7))
        assertEquals("Non-stop", StopGoal.goalLabel(0))
    }

    @Test
    fun `stop metrics feed the Non-stop badges`() {
        val logs = listOf(
            TestData.log(day1, distanceM = 1_100, stops = 8),
            TestData.log(day2, distanceM = 1_150, stops = 6, stopsGoalAtLog = 7),
            TestData.log(day3, distanceM = 1_200, stops = 0, stopsGoalAtLog = 5),
            TestData.log(day3.plusDays(1), distanceM = 50, stops = 0),
        )
        val stats = StatsCalculator.snapshot(logs, TestData.settings())
        assertEquals("the 50 m lap is too short to count", 3, stats.metric(BadgeMetric.STOPS_COUNTED_DAYS))
        assertEquals(2, stats.metric(BadgeMetric.FEWER_STOPS_DAYS))
        assertEquals(2, stats.metric(BadgeMetric.STOP_GOALS_MET))
        // The 50 m lap is too short to count as a non-stop run.
        assertEquals(1, stats.metric(BadgeMetric.NONSTOP_RUNS))
        assertEquals(1_200, stats.metric(BadgeMetric.NONSTOP_MAX_M))
        listOf("NS-001", "NS-003", "NS-006", "NS-009", "NS-011", "NS-012").forEach {
            assertTrue(it, it in stats.earnedBadgeIds)
        }
        assertFalse("NS-013" in stats.earnedBadgeIds)
    }

    @Test
    fun `runs without stops earn no Non-stop badges`() {
        val stats = StatsCalculator.snapshot(TestData.run(30), TestData.settings())
        assertTrue(stats.earnedBadgeIds.none { it.startsWith("NS-") })
    }

    // --- The Run saved note ------------------------------------------------

    private fun note(log: RunLog, earlier: List<RunLog> = emptyList(), backdated: Boolean = false): String? {
        val outcome = SaveOutcome(
            log = log,
            before = StatsSnapshot.EMPTY,
            after = StatsSnapshot.EMPTY,
            newBadges = emptyList(),
            completedChapter = null,
            nextTargetM = 1_100,
            previousTargetM = 1_100,
            previousStopGoal = log.stopsGoalAtLog,
            nextStopGoal = StopGoal.next(earlier + log, TestData.settings()),
            backdated = backdated,
        )
        return JourneyView.stopNote(outcome)
    }

    @Test
    fun `the note celebrates without scolding`() {
        assertNull(note(TestData.log(day1)))
        assertEquals(
            "First count: 8 stops. Next time, try 7 stops or fewer.",
            note(TestData.log(day1, stops = 8)),
        )
        assertEquals(
            "6 stops. You beat your stop goal! Next time, try 5 stops or fewer.",
            note(TestData.log(day2, stops = 6, stopsGoalAtLog = 7)),
        )
        assertEquals(
            "7 stops. Stop goal met! Next time, try 6 stops or fewer.",
            note(TestData.log(day2, stops = 7, stopsGoalAtLog = 7)),
        )
        assertEquals(
            "9 stops this time. Breaks are fine. Next time, try 8 stops or fewer.",
            note(TestData.log(day2, stops = 9, stopsGoalAtLog = 7)),
        )
        assertEquals(
            "9 stops this time. Breaks are fine. It felt hard, so the goal stays at 9 stops or fewer.",
            note(TestData.log(day2, stops = 9, stopsGoalAtLog = 7, effort = Effort.HARD)),
        )
        assertEquals("Non-stop! You went the whole way.", note(TestData.log(day2, stops = 0, stopsGoalAtLog = 1)))
        assertEquals(
            "2 stops. You beat your stop goal! Keep it at 2 stops or fewer while the distance grows.",
            note(TestData.log(day2, stops = 2, stopsGoalAtLog = 3)),
        )
        assertEquals(
            "1 stop. Stop goal met! Keep it at 1 stop or fewer while the distance grows.",
            note(TestData.log(day2, stops = 1, stopsGoalAtLog = 1)),
        )
    }

    // --- Backups ------------------------------------------------------------

    @Test
    fun `stops survive a backup round trip`() {
        val logs = listOf(
            TestData.log(day1, stops = 8, id = "a"),
            TestData.log(day2, stops = 6, stopsGoalAtLog = 7, id = "b"),
            TestData.log(day3, id = "c"),
        )
        val text = BackupFile.write(TestData.settings(), logs, exportedAt = "2026-11-02T18:00:00Z")
        val payload = (BackupFile.parse(text, today = day3.plusDays(1)) as BackupFile.ParseResult.Ok).payload
        assertEquals(logs.sortedBy { it.id }, payload.logs.sortedBy { it.id })
    }

    @Test
    fun `a backup from 1_2 still restores, with no stops`() {
        val v1 = """
            {"format":"ruuun-offline-backup","schemaVersion":1,"exportedAt":"2026-11-01T18:00:00Z",
             "settings":{"nickname":"Runner","onboardedDate":"2026-09-21","startingTargetM":300,
             "currentTargetM":1200,"maxTargetM":3000,"weeklyGoal":2,
             "initialStoryPaceOnePerWeek":false,"onboardingComplete":true},
             "logs":[{"id":"x","date":"2026-11-01","distanceM":1100,"effort":"OKAY",
             "targetMAtLog":300,"durationMinutes":null,"mode":null,"tags":[],"note":null,
             "createdAt":1,"updatedAt":1}]}
        """.trimIndent()
        val result = BackupFile.parse(v1, today = day3.plusDays(60))
        assertTrue(result is BackupFile.ParseResult.Ok)
        val log = (result as BackupFile.ParseResult.Ok).payload.logs.single()
        assertNull(log.stops)
        assertNull(log.stopsGoalAtLog)
    }

    @Test
    fun `impossible stop counts are rejected`() {
        val text = BackupFile.write(
            TestData.settings(),
            listOf(TestData.log(day1, stops = 8, id = "a")),
            exportedAt = "2026-11-02T18:00:00Z",
        ).replace("\"stops\": 8", "\"stops\": -3")
        assertTrue(BackupFile.parse(text, today = day3) is BackupFile.ParseResult.Invalid)
    }

    @Test
    fun `the longest mission now defaults to 5 km`() {
        assertEquals(5_000, RunnerSettings.DEFAULT_MAX_TARGET_M)
    }

    // --- Distance only grows with low stops ----------------------------------

    private fun nextMission(log: RunLog, earlier: List<RunLog> = emptyList(), current: Int = 1_200): Int {
        val settings = TestData.settings(currentTargetM = current)
        return TargetRecommender.afterLog(settings, earlier + log, log)
    }

    @Test
    fun `beating the mission with many stops catches up to the distance run, no further`() {
        val run = TestData.log(day1, distanceM = 1_300, effort = Effort.EASY, targetMAtLog = 1_200, stops = 6)
        assertEquals(1_300, nextMission(run))
    }

    @Test
    fun `a 1_1 km run with 8 stops on a 300 m mission makes the mission 1_1 km`() {
        val run = TestData.log(day1, distanceM = 1_100, effort = Effort.OKAY, targetMAtLog = 300, stops = 8)
        assertEquals(1_100, nextMission(run, current = 300))
        assertEquals("then the same distance, with fewer stops", 7, StopGoal.next(listOf(run), TestData.settings(currentTargetM = 1_100)))
        val again = TestData.log(day2, distanceM = 1_100, effort = Effort.OKAY, targetMAtLog = 1_100, stops = 7)
        assertEquals("matching it with 7 stops holds it", 1_100, nextMission(again, listOf(run), current = 1_100))
    }

    @Test
    fun `beating the mission with 2 stops or fewer lets it grow`() {
        listOf(0, 1, 2).forEach { stops ->
            val run = TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200, stops = stops)
            assertEquals("with $stops stops", 1_350, nextMission(run))
        }
        val three = TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 3)
        assertEquals("3 stops: caught up, not stretched", 1_300, nextMission(three))
    }

    @Test
    fun `an uncounted run catches up to the distance run, no further`() {
        val run = TestData.log(day1, distanceM = 1_300, effort = Effort.EASY, targetMAtLog = 1_200)
        assertEquals(1_300, nextMission(run))
    }

    @Test
    fun `a hard run still eases off, whatever the stops`() {
        val run = TestData.log(day1, distanceM = 900, effort = Effort.HARD, targetMAtLog = 1_200, stops = 9)
        assertEquals(1_075, nextMission(run))
    }

    @Test
    fun `two easy days at the mission only step up when both had low stops`() {
        val first = TestData.log(day1, distanceM = 1_200, effort = Effort.EASY, targetMAtLog = 1_200, stops = 5)
        val second = TestData.log(day2, distanceM = 1_200, effort = Effort.EASY, targetMAtLog = 1_200, stops = 1)
        assertEquals("the first day had too many stops", 1_200, nextMission(second, listOf(first)))

        val lowFirst = first.copy(stops = 2)
        assertEquals(1_250, nextMission(second, listOf(lowFirst)))
    }

    private fun missionNote(log: RunLog, current: Int = 1_200): String {
        val settings = TestData.settings(currentTargetM = current)
        val next = TargetRecommender.afterLog(settings, listOf(log), log)
        val outcome = SaveOutcome(
            log = log,
            before = StatsSnapshot.EMPTY,
            after = StatsSnapshot.EMPTY,
            newBadges = emptyList(),
            completedChapter = null,
            nextTargetM = next,
            previousTargetM = current,
        )
        return JourneyView.nextMissionNote(outcome, settings.copy(currentTargetM = next))
    }

    @Test
    fun `the mission note says why the distance held`() {
        assertEquals(
            "You beat 1.2 km, so the new mission is 1.3 km. Now try it with fewer stops: 4 fewer stops and it gets longer.",
            missionNote(TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 6)),
        )
        assertEquals(
            "You beat 1.2 km, so the new mission is 1.3 km. Count your stops: it gets longer after a run with 2 stops or fewer.",
            missionNote(TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200)),
        )
        assertEquals(
            "You beat 1.2 km and it felt okay, so it goes up a step.",
            missionNote(TestData.log(day1, distanceM = 1_300, effort = Effort.OKAY, targetMAtLog = 1_200, stops = 2)),
        )
    }
}
