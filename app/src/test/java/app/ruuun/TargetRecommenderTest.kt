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

    /** QA 4: two Easy runs at target raise 300 m to 325 m, never 350 m. */
    @Test
    fun `two easy met-target days step up by one 25m increment`() {
        val settings = TestData.settings(currentTargetM = 300)
        val first = TestData.log(day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(
            "one easy day is not enough",
            300,
            TargetRecommender.afterLog(settings, listOf(first), first),
        )

        val second = TestData.log(day2, distanceM = 310, effort = Effort.EASY, targetMAtLog = 300)
        val next = TargetRecommender.afterLog(settings, listOf(first, second), second)
        assertEquals(325, next)
    }

    @Test
    fun `two easy logs on the same day do not step up`() {
        val settings = TestData.settings(currentTargetM = 300)
        val a = TestData.log(day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        val b = TestData.log(day1, distanceM = 320, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(300, TargetRecommender.afterLog(settings, listOf(a, b), b))
    }

    /** QA 5: Hard never raises the target. */
    @Test
    fun `hard effort below the mission eases off ten percent, rounded down to 25m`() {
        val settings = TestData.settings(currentTargetM = 300)
        val hard = TestData.log(day1, distanceM = 270, effort = Effort.HARD, targetMAtLog = 300)
        assertEquals(250, TargetRecommender.afterLog(settings, listOf(hard), hard))
    }

    /** Distance wins: beating the mission raises it even when it felt hard. */
    @Test
    fun `a hard run that beat the mission still raises it to that distance`() {
        val settings = TestData.settings(currentTargetM = 500)
        val hard = TestData.log(day1, distanceM = 900, effort = Effort.HARD, targetMAtLog = 500)
        assertEquals(900, TargetRecommender.afterLog(settings, listOf(hard), hard))
    }

    @Test
    fun `a hard run that fell short eases the mission off`() {
        val settings = TestData.settings(currentTargetM = 500)
        val hard = TestData.log(day1, distanceM = 300, effort = Effort.HARD, targetMAtLog = 500)
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
        val matched = TestData.log(day1, distanceM = 400, effort = Effort.OKAY, targetMAtLog = 400)
        assertEquals(400, TargetRecommender.afterLog(settings, listOf(matched), matched))

        val short = TestData.log(day2, distanceM = 200, effort = Effort.EASY, targetMAtLog = 400)
        assertEquals(400, TargetRecommender.afterLog(settings, listOf(matched, short), short))
    }

    /** Beating the mission moves it to what was actually run, not one small step. */
    @Test
    fun `running further than the mission raises it to that distance`() {
        val settings = TestData.settings(currentTargetM = 300)
        val big = TestData.log(day1, distanceM = 350, effort = Effort.EASY, targetMAtLog = 300)
        assertEquals(350, TargetRecommender.afterLog(settings, listOf(big), big))

        // Okay effort counts too: the mission follows the distance, not the mood.
        val okay = TestData.log(day1, distanceM = 480, effort = Effort.OKAY, targetMAtLog = 300)
        assertEquals(475, TargetRecommender.afterLog(settings, listOf(okay), okay))

        // Beating it by less than 25 m is not enough to move it on its own.
        val nearly = TestData.log(day1, distanceM = 310, effort = Effort.OKAY, targetMAtLog = 300)
        assertEquals(300, TargetRecommender.afterLog(settings, listOf(nearly), nearly))
    }

    /** The sequence the user hit: 200 m mission, ran 250 m, rated it hard. */
    @Test
    fun `beating a 200 m mission with 250 m sets the next mission to 250 m`() {
        val settings = TestData.settings(currentTargetM = 200)
        listOf(Effort.EASY, Effort.OKAY, Effort.HARD).forEach { effort ->
            val log = TestData.log(day1, distanceM = 250, effort = effort, targetMAtLog = 200)
            assertEquals(
                "a $effort run of 250 m should set the mission to 250 m",
                250,
                TargetRecommender.afterLog(settings, listOf(log), log),
            )
        }
    }

    @Test
    fun `a short hard run after a long one eases from the raised mission`() {
        val settings = TestData.settings(currentTargetM = 250)
        val short = TestData.log(day2, distanceM = 50, effort = Effort.HARD, targetMAtLog = 250)
        assertEquals(225, TargetRecommender.afterLog(settings, listOf(short), short))
    }

    @Test
    fun `beating the mission never jumps past the cap`() {
        val settings = TestData.settings(currentTargetM = 900, maxTargetM = 1_000)
        val huge = TestData.log(day1, distanceM = 4_000, effort = Effort.EASY, targetMAtLog = 900)
        assertEquals(1_000, TargetRecommender.afterLog(settings, listOf(huge), huge))
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
        val recent = TestData.log(day2, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300)
        val backdated = TestData.log(
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
        val beforeTheDip = TestData.log(
            day1, distanceM = 300, effort = Effort.EASY, targetMAtLog = 300,
        )
        val atALowerTarget = TestData.log(
            day1.plusDays(4), distanceM = 260, effort = Effort.EASY, targetMAtLog = 250,
        )
        val backAtThreeHundred = TestData.log(
            day1.plusDays(9), distanceM = 320, effort = Effort.EASY, targetMAtLog = 300,
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
        val second = TestData.log(
            day1.plusDays(11), distanceM = 305, effort = Effort.EASY, targetMAtLog = 300,
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
            TestData.log(day1, targetMAtLog = 400),
            TestData.log(day2, targetMAtLog = 500),
        )
        assertEquals(500, TargetRecommender.afterCorrection(settings, remaining))
        assertEquals(300, TargetRecommender.afterCorrection(settings, emptyList()))
    }

    @Test
    fun `welcome back appears only after fourteen quiet days`() {
        val logs = listOf(TestData.log(LocalDate.of(2026, 10, 1)))
        assertFalse(TargetRecommender.needsWelcomeBack(logs, LocalDate.of(2026, 10, 14)))
        assertTrue(TargetRecommender.needsWelcomeBack(logs, LocalDate.of(2026, 10, 15)))
        assertFalse(TargetRecommender.needsWelcomeBack(emptyList(), LocalDate.of(2026, 10, 15)))
    }
}
