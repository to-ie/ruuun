package app.ruuun

import app.ruuun.content.Achievements
import app.ruuun.data.Effort
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.logic.BadgeEvaluator
import app.ruuun.logic.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BadgeEvaluatorTest {

    private val settings = TestData.settings()

    @Test
    fun `the very first outing earns Liftoff and nothing that needs more`() {
        val stats = StatsCalculator.snapshot(
            listOf(TestData.log(TestData.ONBOARDED, distanceM = 300, targetMAtLog = 300)),
            settings,
        )
        assertTrue("FS-001" in stats.earnedBadgeIds)
        assertFalse("FS-002" in stats.earnedBadgeIds)
        assertTrue("FS-003" in stats.earnedBadgeIds)
        assertTrue("RF-009" in stats.earnedBadgeIds)
    }

    @Test
    fun `tag, mode and effort badges come from the matching logs`() {
        val logs = listOf(
            TestData.log(TestData.ONBOARDED, mode = RunMode.WALK_ONLY, tags = setOf(RunTag.PARK)),
            TestData.log(
                TestData.ONBOARDED.plusDays(1),
                mode = RunMode.WALK_JOG,
                tags = setOf(RunTag.FAMILY, RunTag.NATURE),
                effort = Effort.EASY,
            ),
            TestData.log(TestData.ONBOARDED.plusDays(2), tags = setOf(RunTag.NEW_ROUTE), note = "Hi"),
        )
        val earned = StatsCalculator.snapshot(logs, settings).earnedBadgeIds
        listOf("FS-004", "FS-005", "FS-006", "FS-007", "FS-009", "AD-001", "AD-002", "AD-003", "AD-004")
            .forEach { assertTrue(it, it in earned) }
        assertFalse("AT-001 needs three new routes", "AT-001" in earned)
    }

    @Test
    fun `newly earned is the difference between two snapshots`() {
        val existing = TestData.run(2)
        val before = StatsCalculator.snapshot(existing, settings)
        val after = StatsCalculator.snapshot(existing + TestData.log(TestData.ONBOARDED.plusDays(2)), settings)
        val fresh = BadgeEvaluator.newlyEarned(before, after).map { it.id }
        assertTrue("RC-001" in fresh)
        assertFalse("FS-001" in fresh)
    }

    @Test
    fun `badge progress is capped and fractional progress stays in range`() {
        val stats = StatsCalculator.snapshot(TestData.run(30), settings)
        val century = Achievements.BY_ID.getValue("RC-018")
        assertEquals(30, BadgeEvaluator.progress(century, stats))
        assertEquals(0.3f, BadgeEvaluator.progressFraction(century, stats), 0.001f)

        val liftoff = Achievements.BY_ID.getValue("FS-001")
        assertEquals(1, BadgeEvaluator.progress(liftoff, stats))
        assertEquals(1f, BadgeEvaluator.progressFraction(liftoff, stats), 0.001f)
    }

    @Test
    fun `secret badges never appear in the within-reach list`() {
        val stats = StatsCalculator.snapshot(TestData.run(4), settings)
        val reachable = BadgeEvaluator.closestLocked(stats, 20)
        assertTrue(reachable.none { it.secret })
        assertTrue(reachable.none { it.id in stats.earnedBadgeIds })
    }

    /** QA 16: a long journey still resolves every rule. */
    @Test
    fun `a 150-run journey evaluates the whole catalogue without gaps`() {
        val logs = (0 until 150).map { index ->
            TestData.log(
                date = LocalDate.of(2026, 1, 1).plusDays(index.toLong() * 2),
                distanceM = 300 + index * 10,
                effort = Effort.entries[index % 3],
                targetMAtLog = 300,
                mode = RunMode.entries[index % 3],
                tags = if (index % 4 == 0) setOf(RunTag.PARK, RunTag.FAMILY) else emptySet(),
                note = if (index % 5 == 0) "A memory" else null,
            )
        }
        val stats = StatsCalculator.snapshot(logs, TestData.settings(onboardedDate = LocalDate.of(2026, 1, 1)))
        assertEquals(150, stats.outingDays)
        assertEquals(7_500, stats.xp)
        assertTrue("RC-020" in stats.earnedBadgeIds)
        assertTrue(stats.earnedBadgeIds.size > 100)
        Achievements.ALL.forEach { badge ->
            // Every rule resolves to a number; nothing throws or goes missing.
            assertTrue(badge.id, BadgeEvaluator.progressFraction(badge, stats) in 0f..1f)
        }
    }
}
