package app.ruuun

import app.ruuun.content.BadgeMetric
import app.ruuun.data.Effort
import app.ruuun.logic.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorTest {

    private val settings = TestData.settings()

    @Test
    fun `empty data is a complete, zeroed state`() {
        val stats = StatsCalculator.snapshot(emptyList(), settings)
        assertEquals(0, stats.outingDays)
        assertEquals(0, stats.xp)
        assertEquals(1, stats.level)
        assertEquals(0L, stats.totalDistanceM)
        assertEquals(0, stats.completedChapters)
        assertTrue(stats.earnedBadgeIds.isEmpty())
    }

    /** QA 3: one 250 m Okay outing against a 300 m mission. */
    @Test
    fun `first 250m okay outing gives one day, 50 XP and no met-target badge`() {
        val logs = listOf(
            TestData.log(TestData.ONBOARDED, distanceM = 250, effort = Effort.OKAY, targetMAtLog = 300),
        )
        val stats = StatsCalculator.snapshot(logs, settings)
        assertEquals(1, stats.outingDays)
        assertEquals(250L, stats.totalDistanceM)
        assertEquals(50, stats.xp)
        assertEquals(1, stats.level)
        assertTrue("FS-001" in stats.earnedBadgeIds)
        assertFalse("RF-009 must need an actual met target", "RF-009" in stats.earnedBadgeIds)
        assertEquals(1, stats.metric(BadgeMetric.BELOW_TARGET_LOGS))
    }

    /** QA 6: same-day grinding adds distance but not days. */
    @Test
    fun `three logs on one date count once for days, XP and chapters`() {
        val date = TestData.ONBOARDED
        val logs = listOf(
            TestData.log(date, distanceM = 200),
            TestData.log(date, distanceM = 300),
            TestData.log(date, distanceM = 150),
        )
        val stats = StatsCalculator.snapshot(logs, settings)
        assertEquals(1, stats.outingDays)
        assertEquals(650L, stats.totalDistanceM)
        assertEquals(50, stats.xp)
        assertEquals(0, stats.completedChapters)
        assertEquals(3, stats.metric(BadgeMetric.RUN_LOGS_WITH_EFFORT))
    }

    @Test
    fun `xp and level follow the five-day rule exactly`() {
        listOf(0 to 1, 4 to 1, 5 to 2, 9 to 2, 10 to 3, 100 to 21).forEach { (days, level) ->
            val stats = StatsCalculator.snapshot(TestData.run(days), settings)
            assertEquals("days=$days", 50 * days, stats.xp)
            assertEquals("days=$days", level, stats.level)
        }
    }

    /** QA 11: nothing can be farmed by recomputing. */
    @Test
    fun `snapshots are pure, so recomputing changes nothing`() {
        val logs = TestData.run(7)
        val first = StatsCalculator.snapshot(logs, settings)
        val second = StatsCalculator.snapshot(logs, settings)
        assertEquals(first, second)
        assertEquals(first.earnedBadgeIds, second.earnedBadgeIds)
    }

    /** QA 12: deleting a log recomputes downwards. */
    @Test
    fun `removing a log lowers totals, level and badges`() {
        val logs = TestData.run(5)
        val before = StatsCalculator.snapshot(logs, settings)
        val after = StatsCalculator.snapshot(logs.dropLast(1), settings)
        assertEquals(2, before.level)
        assertEquals(1, after.level)
        assertTrue("RC-003" in before.earnedBadgeIds)
        assertFalse("RC-003" in after.earnedBadgeIds)
    }

    @Test
    fun `comeback gaps count returns, not absences`() {
        val logs = listOf(
            TestData.log(LocalDate.of(2026, 10, 1)),
            TestData.log(LocalDate.of(2026, 10, 20)), // 19 days later
            TestData.log(LocalDate.of(2026, 10, 21)),
            TestData.log(LocalDate.of(2026, 12, 1)), // 41 days later
        )
        val stats = StatsCalculator.snapshot(logs, settings)
        assertEquals(2, stats.metric(BadgeMetric.COMEBACK_GAPS_14D))
        assertEquals(1, stats.metric(BadgeMetric.COMEBACK_GAPS_30D))
    }

    @Test
    fun `active months counts distinct year-months, month badges count months of year`() {
        val logs = listOf(
            TestData.log(LocalDate.of(2026, 11, 2)),
            TestData.log(LocalDate.of(2026, 11, 20)),
            TestData.log(LocalDate.of(2027, 11, 3)),
            TestData.log(LocalDate.of(2027, 1, 5)),
        )
        val stats = StatsCalculator.snapshot(logs, settings)
        assertEquals(3, stats.metric(BadgeMetric.ACTIVE_MONTHS))
        assertEquals(3, stats.metric(BadgeMetric.RUNS_IN_MONTH_11))
        assertEquals(1, stats.metric(BadgeMetric.RUNS_IN_MONTH_01))
    }

    @Test
    fun `all three efforts recorded is a zero-or-one metric`() {
        val base = listOf(
            TestData.log(TestData.ONBOARDED, effort = Effort.EASY),
            TestData.log(TestData.ONBOARDED.plusDays(1), effort = Effort.OKAY),
        )
        assertEquals(0, StatsCalculator.snapshot(base, settings).metric(BadgeMetric.ALL_THREE_EFFORTS_RECORDED))
        val withHard = base + TestData.log(TestData.ONBOARDED.plusDays(2), effort = Effort.HARD)
        assertEquals(1, StatsCalculator.snapshot(withHard, settings).metric(BadgeMetric.ALL_THREE_EFFORTS_RECORDED))
        assertTrue("SE-007" in StatsCalculator.snapshot(withHard, settings).earnedBadgeIds)
    }

    @Test
    fun `level names are stable and cap at Trail Hero`() {
        assertEquals("First Steps", StatsCalculator.levelName(1))
        assertEquals("Trail Finder", StatsCalculator.levelName(3))
        assertEquals("Trail Hero", StatsCalculator.levelName(8))
        assertEquals("Trail Hero", StatsCalculator.levelName(25))
    }
}
