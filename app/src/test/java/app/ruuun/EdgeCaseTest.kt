package app.ruuun

import app.ruuun.backup.BackupFile
import app.ruuun.content.BadgeMetric
import app.ruuun.data.RunnerSettings
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.TargetRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Awkward inputs that should bend rather than break. */
class EdgeCaseTest {

    @Test
    fun `a backup that omits the optional keys entirely still imports`() {
        val text = """
            {
              "format": "ruuun-offline-backup",
              "schemaVersion": 1,
              "exportedAt": "2026-11-01T18:00:00Z",
              "settings": {
                "nickname": "Runner",
                "onboardedDate": "2026-11-01",
                "startingTargetM": 300,
                "currentTargetM": 300,
                "maxTargetM": 3000,
                "weeklyGoal": 2
              },
              "logs": [
                { "id": "a", "date": "2026-11-01", "distanceM": 300,
                  "effort": "OKAY", "targetMAtLog": 300 }
              ]
            }
        """.trimIndent()
        val result = BackupFile.parse(text)
        assertTrue(result.toString(), result is BackupFile.ParseResult.Ok)
        val log = (result as BackupFile.ParseResult.Ok).payload.logs.single()
        assertNull(log.durationMinutes)
        assertNull(log.mode)
        assertNull(log.note)
        assertTrue(log.tags.isEmpty())
    }

    @Test
    fun `unknown fields in a backup are ignored, not fatal`() {
        val settings = TestData.settings()
        val logs = listOf(TestData.log(LocalDate.of(2026, 11, 1), id = "keep-me"))
        val text = BackupFile.write(settings, logs, exportedAt = "2026-11-01T18:00:00Z")
            .replaceFirst("{", "{\n  \"somethingNew\": 42,")
        val result = BackupFile.parse(text)
        assertTrue(result is BackupFile.ParseResult.Ok)
        assertEquals("keep-me", (result as BackupFile.ParseResult.Ok).payload.logs.single().id)
    }

    @Test
    fun `a backup with an impossible start date is refused`() {
        val envelope = { date: String -> """
            {"format":"ruuun-offline-backup","schemaVersion":1,"exportedAt":"x",
             "settings":{"nickname":"R","onboardedDate":"$date","startingTargetM":300,
             "currentTargetM":300,"maxTargetM":3000,"weeklyGoal":2},"logs":[]}
        """.trimIndent() }
        val today = LocalDate.of(2026, 11, 1)
        listOf("0001-01-01", "1900-01-01", "2200-01-01").forEach { date ->
            assertTrue(date, BackupFile.parse(envelope(date), today) is BackupFile.ParseResult.Invalid)
        }
        assertTrue(BackupFile.parse(envelope("2026-01-01"), today) is BackupFile.ParseResult.Ok)
    }

    @Test
    fun `a backup with absurdly many runs is refused`() {
        val one = { i: Int -> """{"id":"id$i","date":"2026-01-01","distanceM":300,
            "effort":"OKAY","targetMAtLog":300}""" }
        val many = (0..BackupFile.MAX_LOGS).joinToString(",") { one(it) }
        val text = """
            {"format":"ruuun-offline-backup","schemaVersion":1,"exportedAt":"x",
             "settings":{"nickname":"R","onboardedDate":"2026-01-01","startingTargetM":300,
             "currentTargetM":300,"maxTargetM":3000,"weeklyGoal":2},"logs":[$many]}
        """.trimIndent()
        val result = BackupFile.parse(text, LocalDate.of(2026, 11, 1))
        assertTrue(result is BackupFile.ParseResult.Invalid)
    }

    @Test
    fun `tags and modes from a newer version are skipped, not fatal`() {
        val text = """
            {"format":"ruuun-offline-backup","schemaVersion":1,"exportedAt":"x",
             "settings":{"nickname":"R","onboardedDate":"2026-01-01","startingTargetM":300,
             "currentTargetM":300,"maxTargetM":3000,"weeklyGoal":2},
             "logs":[{"id":"a","date":"2026-01-02","distanceM":300,"effort":"OKAY",
                      "targetMAtLog":300,"tags":["PARK","SOMETHING_NEW"],"mode":"SPRINT"}]}
        """.trimIndent()
        val result = BackupFile.parse(text, LocalDate.of(2026, 11, 1))
        assertTrue(result.toString(), result is BackupFile.ParseResult.Ok)
        val log = (result as BackupFile.ParseResult.Ok).payload.logs.single()
        assertEquals(setOf(app.ruuun.data.RunTag.PARK), log.tags)
        assertNull("an unknown mode becomes no mode", log.mode)
    }

    @Test
    fun `an unknown effort is still fatal, because it drives the rules`() {
        val text = """
            {"format":"ruuun-offline-backup","schemaVersion":1,"exportedAt":"x",
             "settings":{"nickname":"R","onboardedDate":"2026-01-01","startingTargetM":300,
             "currentTargetM":300,"maxTargetM":3000,"weeklyGoal":2},
             "logs":[{"id":"a","date":"2026-01-02","distanceM":300,"effort":"BRUTAL",
                      "targetMAtLog":300}]}
        """.trimIndent()
        assertTrue(
            BackupFile.parse(text, LocalDate.of(2026, 11, 1)) is BackupFile.ParseResult.Invalid,
        )
    }

    @Test
    fun `an imported future onboarding date does not break the week list`() {
        val settings = TestData.settings(onboardedDate = LocalDate.of(2030, 1, 7))
        val weeks = JourneyView.journeyWeeks(emptyList(), settings, upToJourneyWeek = -4)
        assertEquals(1, weeks.size)
        assertEquals(1, weeks.single().journeyWeek)
    }

    @Test
    fun `runs logged before onboarding count for totals but not for weekly quests`() {
        val settings = TestData.settings(weeklyGoal = 1)
        val logs = listOf(
            TestData.log(TestData.ONBOARDED.minusDays(30)),
            TestData.log(TestData.ONBOARDED.minusDays(29)),
            TestData.log(TestData.ONBOARDED),
        )
        val stats = StatsCalculator.snapshot(logs, settings)
        assertEquals("every run counts towards totals", 3, stats.runDays)
        assertEquals("only the journey week counts", 1, stats.weeksMet)
        assertEquals(2, stats.metric(BadgeMetric.ACTIVE_MONTHS))
    }

    @Test
    fun `raising the weekly goal can lower weeks met, as settings warns`() {
        val logs = (0 until 3).map { TestData.log(TestData.ONBOARDED.plusWeeks(it.toLong())) }
        assertEquals(3, StatsCalculator.snapshot(logs, TestData.settings(weeklyGoal = 1)).weeksMet)
        assertEquals(0, StatsCalculator.snapshot(logs, TestData.settings(weeklyGoal = 2)).weeksMet)
    }

    @Test
    fun `the target never escapes the cap, even when the cap is lowered`() {
        val lowered = TestData.settings(startingTargetM = 800, maxTargetM = 500)
        val reset = lowered.startingTargetM
            .coerceIn(RunnerSettings.MIN_TARGET_M, lowered.maxTargetM)
        assertEquals(500, reset)
        assertEquals(500, TargetRecommender.increase(500, lowered.maxTargetM))
    }

    @Test
    fun `a one-metre effort is a real run`() {
        val stats = StatsCalculator.snapshot(
            listOf(TestData.log(TestData.ONBOARDED, distanceM = 1, targetMAtLog = 300)),
            TestData.settings(),
        )
        assertEquals(1, stats.runDays)
        assertEquals(50, stats.xp)
        assertTrue("FS-001" in stats.earnedBadgeIds)
    }

    @Test
    fun `logs spanning a decade stay ordered and countable`() {
        val logs = (0 until 10).map {
            TestData.log(LocalDate.of(2020 + it, 3, 1), distanceM = 500)
        }
        val stats = StatsCalculator.snapshot(logs, TestData.settings(onboardedDate = LocalDate.of(2020, 3, 1)))
        assertEquals(10, stats.runDays)
        assertEquals(10, stats.metric(BadgeMetric.ACTIVE_MONTHS))
        assertEquals(10, stats.metric(BadgeMetric.RUNS_IN_MONTH_03))
        assertEquals(9, stats.metric(BadgeMetric.COMEBACK_GAPS_30D))
    }
}
