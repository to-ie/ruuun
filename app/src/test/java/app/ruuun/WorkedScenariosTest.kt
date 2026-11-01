package app.ruuun

import app.ruuun.data.Effort
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.TargetRecommender
import app.ruuun.logic.WeekCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** The worked scenarios from Appendix D of the handoff, end to end over the pure logic. */
class WorkedScenariosTest {

    @Test
    fun `first run - 220m okay against a 300m mission`() {
        val settings = TestData.settings(currentTargetM = 300)
        val log = TestData.log(TestData.ONBOARDED, distanceM = 220, effort = Effort.OKAY, targetMAtLog = 300)
        val stats = StatsCalculator.snapshot(listOf(log), settings)

        assertEquals(1, stats.runDays)
        assertEquals(220L, stats.totalDistanceM)
        assertEquals(50, stats.xp)
        assertTrue("FS-001" in stats.earnedBadgeIds)
        assertEquals(300, TargetRecommender.afterLog(settings, listOf(log), log))
    }

    @Test
    fun `gradual progress - two easy 300m sessions on different days`() {
        val settings = TestData.settings(currentTargetM = 300)
        val a = TestData.log(TestData.ONBOARDED, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        val b = TestData.log(TestData.ONBOARDED.plusDays(3), distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(325, TargetRecommender.afterLog(settings, listOf(a, b), b))
    }

    @Test
    fun `hard day - 270m hard against 300m suggests 250m`() {
        val settings = TestData.settings(currentTargetM = 300)
        val log = TestData.log(TestData.ONBOARDED, distanceM = 270, effort = Effort.HARD, targetMAtLog = 300)
        val stats = StatsCalculator.snapshot(listOf(log), settings)
        assertEquals(1, stats.runDays)
        assertEquals(250, TargetRecommender.afterLog(settings, listOf(log), log))
    }

    @Test
    fun `two logs today - 200m plus 300m is one day and one 50 XP event`() {
        val settings = TestData.settings()
        val a = TestData.log(TestData.ONBOARDED, distanceM = 200)
        val b = TestData.log(TestData.ONBOARDED, distanceM = 300)
        val before = StatsCalculator.snapshot(listOf(a), settings)
        val after = StatsCalculator.snapshot(listOf(a, b), settings)
        assertEquals(500L, after.totalDistanceM)
        assertEquals(1, after.runDays)
        assertEquals(50, after.xp)
        assertFalse("the second log adds no run day", after.runDays > before.runDays)
        assertEquals(before.completedChapters, after.completedChapters)
    }

    @Test
    fun `comeback - a new run after sixteen days keeps everything`() {
        val settings = TestData.settings()
        val old = TestData.run(4)
        val comebackDate = old.last().date.plusDays(16)
        val before = StatsCalculator.snapshot(old, settings)
        val after = StatsCalculator.snapshot(old + TestData.log(comebackDate), settings)

        assertTrue(TargetRecommender.needsWelcomeBack(old, comebackDate))
        assertTrue(before.earnedBadgeIds.all { it in after.earnedBadgeIds })
        assertTrue(after.xp > before.xp)
        assertTrue("RF-007" in after.earnedBadgeIds)
    }

    @Test
    fun `backdated entry - last month counts for history but not for today's target`() {
        val settings = TestData.settings(currentTargetM = 500)
        val recent = TestData.log(LocalDate.of(2026, 11, 1), distanceM = 500, targetMAtLog = 500)
        val backdated = TestData.log(
            LocalDate.of(2026, 10, 2),
            distanceM = 800,
            effort = Effort.EASY,
            targetMAtLog = 500,
        )
        val stats = StatsCalculator.snapshot(listOf(recent, backdated), settings)
        assertEquals(2, stats.runDays)
        assertEquals(1_300L, stats.totalDistanceM)
        assertEquals(500, TargetRecommender.afterLog(settings, listOf(recent, backdated), backdated))
    }

    @Test
    fun `correction - changing 1500m to 150m recomputes every total`() {
        val settings = TestData.settings()
        val wrong = TestData.log(TestData.ONBOARDED, distanceM = 1_500, id = "fix-me")
        val before = StatsCalculator.snapshot(listOf(wrong), settings)
        assertTrue("SD-011" in before.earnedBadgeIds)

        val corrected = listOf(wrong.copy(distanceM = 150))
        val after = StatsCalculator.snapshot(corrected, settings)
        assertEquals(150L, after.totalDistanceM)
        assertFalse("SD-011" in after.earnedBadgeIds)
        assertEquals(1, after.runDays)
        assertEquals(50, after.xp)
    }

    @Test
    fun `one per week - 48 run days still finishes the story`() {
        val settings = TestData.settings(onePerWeekPace = true)
        val logs = (0 until 48).map { TestData.log(TestData.ONBOARDED.plusWeeks(it.toLong())) }
        val stats = StatsCalculator.snapshot(logs, settings)
        assertEquals(48, stats.runDays)
        assertEquals(11, stats.completedChapters)

        val twoMore = logs + listOf(
            TestData.log(TestData.ONBOARDED.plusWeeks(48)),
            TestData.log(TestData.ONBOARDED.plusWeeks(49)),
        )
        assertEquals(12, StatsCalculator.snapshot(twoMore, settings).completedChapters)
    }

    @Test
    fun `year two - the 53rd week reuses week one without resetting anything`() {
        val settings = TestData.settings()
        val logs = (0 until 60).map { TestData.log(TestData.ONBOARDED.plusWeeks(it.toLong())) }
        val stats = StatsCalculator.snapshot(logs, settings)
        val weekFiftyThree = TestData.ONBOARDED.plusWeeks(52)

        assertEquals(53, WeekCalculator.journeyWeek(settings.onboardedDate, weekFiftyThree))
        assertEquals(
            "Launch Week",
            WeekCalculator.themeFor(settings.onboardedDate, weekFiftyThree).title,
        )
        assertEquals(60, stats.runDays)
        // Chapter thresholds are run days, not weeks: 60 days clears chapters 1 to 9.
        assertEquals(9, stats.completedChapters)
    }

    @Test
    fun `the mockup sample child reconciles with the rules`() {
        // UI spec section 7: 11 run days and 4,050 m, then a 450 m run.
        val settings = TestData.settings(currentTargetM = 425)
        val before = (0 until 11).map {
            TestData.log(TestData.ONBOARDED.plusDays(it.toLong() * 3), distanceM = 4_050 / 11)
        }
        val saved = TestData.log(
            LocalDate.of(2026, 11, 1),
            distanceM = 450,
            effort = Effort.EASY,
            targetMAtLog = 425,
            mode = RunMode.WALK_JOG,
            tags = setOf(RunTag.PARK, RunTag.FAMILY),
        )
        val after = StatsCalculator.snapshot(before + saved, settings)
        assertEquals(12, after.runDays)
        assertEquals(600, after.xp)
        assertEquals(3, after.level)
        assertEquals("Trail Finder", StatsCalculator.levelName(after.level))
        assertEquals(3, after.completedChapters)
        assertEquals(
            "Chapter 4 is next after 12 days",
            4,
            JourneyView.chapterProgress(after, settings).chapter?.number,
        )
    }
}
