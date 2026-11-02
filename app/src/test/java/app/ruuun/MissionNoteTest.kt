package app.ruuun

import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.logic.JourneyView
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsSnapshot
import app.ruuun.logic.TargetRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Run saved screen says why the mission moved, using the recommender's own result. */
class MissionNoteTest {

    private val day1 = TestData.ONBOARDED
    private val day2 = day1.plusDays(1)

    /** Save [log] on top of [earlier] and return the note the screen would show. */
    private fun note(
        log: RunLog,
        earlier: List<RunLog> = emptyList(),
        currentTargetM: Int = 300,
        maxTargetM: Int = 3_000,
        backdated: Boolean = false,
    ): String {
        val settings = TestData.settings(currentTargetM = currentTargetM, maxTargetM = maxTargetM)
        val next = TargetRecommender.afterLog(settings, earlier + log, log)
        val outcome = SaveOutcome(
            log = log,
            before = StatsSnapshot.EMPTY,
            after = StatsSnapshot.EMPTY,
            newBadges = emptyList(),
            completedChapter = null,
            nextTargetM = next,
            previousTargetM = currentTargetM,
            backdated = backdated,
        )
        return JourneyView.nextMissionNote(outcome, settings.copy(currentTargetM = next))
    }

    @Test
    fun `beating the mission names the old distance and the effort`() {
        assertEquals(
            "You beat 300 m and it felt easy, so it goes up.",
            note(TestData.log(day1, distanceM = 320, effort = Effort.EASY)),
        )
        assertEquals(
            "You beat 300 m and it felt okay, so it goes up a step.",
            note(TestData.log(day1, distanceM = 320, effort = Effort.OKAY)),
        )
        assertEquals(
            "You beat 300 m. It felt hard, so it only goes up a little.",
            note(TestData.log(day1, distanceM = 320, effort = Effort.HARD)),
        )
    }

    @Test
    fun `two easy days at the mission explain the step up`() {
        val first = TestData.log(day1, distanceM = 300, effort = Effort.EASY)
        assertEquals(
            "Two easy days at 300 m, so it goes up a step.",
            note(TestData.log(day2, distanceM = 300, effort = Effort.EASY), earlier = listOf(first)),
        )
    }

    @Test
    fun `one easy day at the mission says what is still needed`() {
        assertEquals(
            "One more easy day at 300 m and it goes up.",
            note(TestData.log(day1, distanceM = 300, effort = Effort.EASY)),
        )
    }

    @Test
    fun `hard and short eases off, short otherwise repeats`() {
        assertEquals(
            "That felt hard, so next time is a bit shorter.",
            note(TestData.log(day1, distanceM = 200, effort = Effort.HARD)),
        )
        assertEquals(
            "Same again next time. Turning up still counts.",
            note(TestData.log(day1, distanceM = 200, effort = Effort.OKAY)),
        )
        assertEquals(
            "Same again next time. Repeating is how it sticks.",
            note(TestData.log(day1, distanceM = 300, effort = Effort.OKAY)),
        )
    }

    @Test
    fun `a backdated run says it cannot move the mission`() {
        assertEquals(
            "Runs on earlier days don't change the mission.",
            note(TestData.log(day1, distanceM = 900, effort = Effort.EASY), backdated = true),
        )
    }

    @Test
    fun `the cap is named when it holds the mission back`() {
        assertEquals(
            "You are at your longest mission setting.",
            note(
                TestData.log(day1, distanceM = 3_500, effort = Effort.EASY, targetMAtLog = 3_000),
                currentTargetM = 3_000,
            ),
        )
        assertTrue(
            note(
                TestData.log(day1, distanceM = 2_960, effort = Effort.EASY, targetMAtLog = 2_950),
                currentTargetM = 2_950,
            ).endsWith("That is your longest mission setting."),
        )
    }
}
