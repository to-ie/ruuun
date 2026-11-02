package app.ruuun

import app.ruuun.data.Effort
import app.ruuun.logic.TargetRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TargetRecommenderTest {

    private val day1 = TestData.ONBOARDED
    private val day2 = TestData.ONBOARDED.plusDays(2)

    /**
     * Matching the mission exactly, twice, steps up one band. Beating it is handled by
     * the bump rule instead, so this gate only covers runs that land exactly on target.
     */
    @Test
    fun `two easy days that exactly match the mission step up one band`() {
        val settings = TestData.settings(currentTargetM = 300)
        val first = TestData.lowStopsLog(day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(
            "one easy day is not enough",
            300,
            TargetRecommender.afterLog(settings, listOf(first), first),
        )

        val second = TestData.lowStopsLog(day2, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(325, TargetRecommender.afterLog(settings, listOf(first, second), second))
    }

    @Test
    fun `two exactly-on-target easy logs on the same day do not step up`() {
        val settings = TestData.settings(currentTargetM = 300)
        val a = TestData.lowStopsLog(day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        val b = TestData.lowStopsLog(day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(300, TargetRecommender.afterLog(settings, listOf(a, b), b))
    }

    /** QA 5: Hard never raises the target. */
    @Test
    fun `hard effort below the mission eases off ten percent, rounded down to 25m`() {
        val settings = TestData.settings(currentTargetM = 300)
        val hard = TestData.lowStopsLog(day1, distanceM = 270, effort = Effort.HARD, targetMAtLog = 300)
        assertEquals(250, TargetRecommender.afterLog(settings, listOf(hard), hard))
    }

    /** Distance wins: beating the mission raises it even when it felt hard. */
    @Test
    fun `a hard run that beat the mission still raises it past that distance`() {
        val settings = TestData.settings(currentTargetM = 500)
        val hard = TestData.lowStopsLog(day1, distanceM = 900, effort = Effort.HARD, targetMAtLog = 500)
        assertEquals(925, TargetRecommender.afterLog(settings, listOf(hard), hard))
    }

    @Test
    fun `a hard run that fell short eases the mission off`() {
        val settings = TestData.settings(currentTargetM = 500)
        val hard = TestData.lowStopsLog(day1, distanceM = 300, effort = Effort.HARD, targetMAtLog = 500)
        val next = TargetRecommender.afterLog(settings, listOf(hard), hard)
        assertTrue("next=$next", next < 500)
        assertEquals(450, next)
    }

    @Test
    fun `hard never falls below the 100m floor`() {
        assertEquals(100, TargetRecommender.easierAfterHard(100))
        assertEquals(100, TargetRecommender.easierAfterHard(110))
    }

    @Test
    fun `matching or falling short of the mission repeats it`() {
        val settings = TestData.settings(currentTargetM = 400)
        val matched = TestData.lowStopsLog(day1, distanceM = 400, effort = Effort.OKAY, targetMAtLog = 400)
        assertEquals(400, TargetRecommender.afterLog(settings, listOf(matched), matched))

        val short = TestData.lowStopsLog(day2, distanceM = 200, effort = Effort.EASY, targetMAtLog = 400)
        assertEquals(400, TargetRecommender.afterLog(settings, listOf(matched, short), short))
    }

    /** Beating the mission sets the next one beyond the distance just run. */
    @Test
    fun `beating the mission pushes past the distance, scaled by effort`() {
        val settings = TestData.settings(currentTargetM = 300)
        fun next(distance: Int, effort: Effort): Int {
            val log = TestData.lowStopsLog(day1, distanceM = distance, effort = effort, targetMAtLog = 300)
            return TargetRecommender.afterLog(settings, listOf(log), log)
        }
        // Ran 350 m against a 300 m mission.
        assertEquals("hard: a nudge", 375, next(350, Effort.HARD))
        assertEquals("okay: one step", 375, next(350, Effort.OKAY))
        assertEquals("easy: two steps", 400, next(350, Effort.EASY))

        // Above 500 m the band widens, so the gap between easy and hard widens too.
        val big = TestData.settings(currentTargetM = 600)
        fun nextBig(distance: Int, effort: Effort): Int {
            val log = TestData.lowStopsLog(day1, distanceM = distance, effort = effort, targetMAtLog = 600)
            return TargetRecommender.afterLog(big, listOf(log), log)
        }
        assertEquals(825, nextBig(800, Effort.HARD))
        assertEquals(850, nextBig(800, Effort.OKAY))
        assertEquals(900, nextBig(800, Effort.EASY))
    }

    @Test
    fun `the next mission is always further than the run that earned it`() {
        val settings = TestData.settings(currentTargetM = 200, maxTargetM = 50_000)
        listOf(201, 210, 249, 250, 251, 999, 1_000, 1_501).forEach { distance ->
            Effort.entries.forEach { effort ->
                val log = TestData.lowStopsLog(day1, distanceM = distance, effort = effort, targetMAtLog = 200)
                val next = TargetRecommender.afterLog(settings, listOf(log), log)
                assertTrue(
                    "$distance m $effort gave $next, which is not further",
                    next > distance,
                )
            }
        }
    }

    /** The sequence the user hit: 200 m mission, ran 250 m. */
    @Test
    fun `beating a 200 m mission with 250 m sets the next mission past 250 m`() {
        val settings = TestData.settings(currentTargetM = 200)
        mapOf(Effort.HARD to 275, Effort.OKAY to 275, Effort.EASY to 300).forEach { (effort, want) ->
            val log = TestData.lowStopsLog(day1, distanceM = 250, effort = effort, targetMAtLog = 200)
            assertEquals(
                "a $effort run of 250 m",
                want,
                TargetRecommender.afterLog(settings, listOf(log), log),
            )
        }
    }

    @Test
    fun `a short hard run after a long one eases from the raised mission`() {
        val settings = TestData.settings(currentTargetM = 250)
        val short = TestData.lowStopsLog(day2, distanceM = 50, effort = Effort.HARD, targetMAtLog = 250)
        assertEquals(225, TargetRecommender.afterLog(settings, listOf(short), short))
    }

    @Test
    fun `beating the mission never jumps past the cap`() {
        val settings = TestData.settings(currentTargetM = 900, maxTargetM = 1_000)
        val huge = TestData.lowStopsLog(day1, distanceM = 4_000, effort = Effort.EASY, targetMAtLog = 900)
        assertEquals(1_000, TargetRecommender.afterLog(settings, listOf(huge), huge))
    }

    @Test
    fun `the effort bump widens with the distance band`() {
        assertEquals(25, TargetRecommender.bumpAfterBeating(300, Effort.HARD))
        assertEquals(25, TargetRecommender.bumpAfterBeating(300, Effort.OKAY))
        assertEquals(50, TargetRecommender.bumpAfterBeating(300, Effort.EASY))
        assertEquals(100, TargetRecommender.bumpAfterBeating(1_000, Effort.EASY))
        assertEquals(200, TargetRecommender.bumpAfterBeating(2_000, Effort.EASY))
    }

    @Test
    fun `make it harder steps up to the next 25 m, never past the cap`() {
        assertEquals(325, TargetRecommender.makeHarder(300, 3_000))
        assertEquals(325, TargetRecommender.makeHarder(310, 3_000))
        assertEquals(1_000, TargetRecommender.makeHarder(990, 1_000))
        assertEquals(1_000, TargetRecommender.makeHarder(1_000, 1_000))
    }

    @Test
    fun `step size follows the three bands and the ten percent ceiling`() {
        assertEquals(25, TargetRecommender.stepFor(300))
        assertEquals(50, TargetRecommender.stepFor(500))
        assertEquals(50, TargetRecommender.stepFor(1_499))
        assertEquals(100, TargetRecommender.stepFor(1_500))

        // 25 m on 200 m would be 12.5%: hold steady instead.
        assertEquals(200, TargetRecommender.increase(200, 3_000))
        assertEquals(325, TargetRecommender.increase(300, 3_000))
        assertEquals(550, TargetRecommender.increase(500, 3_000))
        assertEquals(1_600, TargetRecommender.increase(1_500, 3_000))
    }

    @Test
    fun `increase never exceeds the configured cap`() {
        assertEquals(3_000, TargetRecommender.increase(2_950, 3_000))
        assertEquals(3_000, TargetRecommender.increase(3_000, 3_000))
    }

    @Test
    fun `make this easier drops to the next lower 25m, never below 100m`() {
        assertEquals(400, TargetRecommender.makeEasier(425))
        assertEquals(400, TargetRecommender.makeEasier(410))
        assertEquals(375, TargetRecommender.makeEasier(400))
        assertEquals(100, TargetRecommender.makeEasier(100))
        assertEquals(100, TargetRecommender.makeEasier(120))
    }

    /** Backdating must not move today's suggestion. */
    @Test
    fun `a backdated easy log cannot raise the target`() {
        val settings = TestData.settings(currentTargetM = 300)
        val recent = TestData.lowStopsLog(day2, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        val backdated = TestData.lowStopsLog(
            day1.minusDays(30),
            distanceM = 900,
            effort = Effort.EASY,
            targetMAtLog = 300,
        )
        assertEquals(
            300,
            TargetRecommender.afterLog(settings, listOf(recent, backdated), backdated),
        )
    }

    /** An Easy day from before the target dipped must not satisfy the two-day gate. */
    @Test
    fun `the easy gate only counts days since the target became this value`() {
        val settings = TestData.settings(currentTargetM = 300)
        val beforeTheDip = TestData.lowStopsLog(
            day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300,
        )
        val atALowerTarget = TestData.lowStopsLog(
            day1.plusDays(4), distanceM = 260, effort = Effort.EASY, targetMAtLog = 250,
        )
        val backAtThreeHundred = TestData.lowStopsLog(
            day1.plusDays(9), distanceM = 300, effort = Effort.EASY, targetMAtLog = 300,
        )
        assertEquals(
            300,
            TargetRecommender.afterLog(
                settings,
                listOf(beforeTheDip, atALowerTarget, backAtThreeHundred),
                backAtThreeHundred,
            ),
        )

        // A second Easy day at the restored target does step up, as it should.
        val second = TestData.lowStopsLog(
            day1.plusDays(11), distanceM = 300, effort = Effort.EASY, targetMAtLog = 300,
        )
        assertEquals(
            325,
            TargetRecommender.afterLog(
                settings,
                listOf(beforeTheDip, atALowerTarget, backAtThreeHundred, second),
                second,
            ),
        )
    }

    @Test
    fun `corrections fall back to the latest remaining log's suggestion`() {
        val settings = TestData.settings(startingTargetM = 300, currentTargetM = 800)
        val remaining = listOf(
            TestData.lowStopsLog(day1, targetMAtLog = 400),
            TestData.lowStopsLog(day2, targetMAtLog = 500),
        )
        assertEquals(500, TargetRecommender.afterCorrection(settings, remaining))
        assertEquals(300, TargetRecommender.afterCorrection(settings, emptyList()))
    }

    @Test
    fun `welcome back appears only after fourteen quiet days`() {
        val logs = listOf(TestData.lowStopsLog(LocalDate.of(2026, 10, 1)))
        assertFalse(TargetRecommender.needsWelcomeBack(logs, LocalDate.of(2026, 10, 14)))
        assertTrue(TargetRecommender.needsWelcomeBack(logs, LocalDate.of(2026, 10, 15)))
        assertFalse(TargetRecommender.needsWelcomeBack(emptyList(), LocalDate.of(2026, 10, 15)))
    }
}
