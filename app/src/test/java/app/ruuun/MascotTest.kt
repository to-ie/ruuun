package app.ruuun

import app.ruuun.content.Achievements
import app.ruuun.content.Chapters
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
import app.ruuun.ui.Mascot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Which Ruu shows after a save, and that every chapter has its own picture. */
class MascotTest {

    private val settings = TestData.settings()
    private val earlier = TestData.run(days = 6, distanceM = 400)
    private val day = earlier.last().date.plusDays(1)

    private fun outcome(
        log: RunLog,
        before: List<RunLog> = earlier,
        nextTargetM: Int = 300,
        previousTargetM: Int = 300,
        backdated: Boolean = false,
        newBadges: Boolean = false,
    ): SaveOutcome {
        val beforeStats = StatsCalculator.snapshot(before, settings)
        val afterStats = StatsCalculator.snapshot(before + log, settings)
        return SaveOutcome(
            log = log,
            before = beforeStats,
            after = afterStats,
            newBadges = if (newBadges) listOf(Achievements.ALL.first()) else emptyList(),
            completedChapter = null,
            nextTargetM = nextTargetM,
            previousTargetM = previousTargetM,
            backdated = backdated,
        )
    }

    @Test
    fun `a stop goal met is a cheer`() {
        val log = TestData.log(day, distanceM = 300, stops = 2, stopsGoalAtLog = 3)
        assertEquals(Mascot.CHEER, Mascot.afterSave(outcome(log)))
    }

    @Test
    fun `a new badge is a cheer, even after a hard run`() {
        val log = TestData.log(day, distanceM = 300, effort = Effort.HARD)
        assertEquals(Mascot.CHEER, Mascot.afterSave(outcome(log, newBadges = true)))
    }

    @Test
    fun `a hard run with nothing new is tired but proud`() {
        val log = TestData.log(day, distanceM = 300, effort = Effort.HARD)
        assertEquals(Mascot.TIRED_PROUD, Mascot.afterSave(outcome(log, nextTargetM = 325)))
    }

    @Test
    fun `an ordinary run is proud, whether or not the mission grows`() {
        val log = TestData.log(day, distanceM = 300, stops = 5, stopsGoalAtLog = 4)
        assertEquals(Mascot.PROUD, Mascot.afterSave(outcome(log)))
        assertEquals(Mascot.PROUD, Mascot.afterSave(outcome(log, nextTargetM = 325)))
    }

    @Test
    fun `a short warm up that meets the stop number is not a cheer`() {
        val log = TestData.log(day, distanceM = 50, stops = 0, stopsGoalAtLog = 3)
        assertEquals(Mascot.PROUD, Mascot.afterSave(outcome(log)))
    }

    @Test
    fun `an extra run that does not set the stop goal is not a cheer`() {
        val log = TestData.log(day, distanceM = 300, stops = 1, stopsGoalAtLog = 3)
        assertEquals(Mascot.PROUD, Mascot.afterSave(outcome(log).copy(setsStopGoal = false)))
    }

    @Test
    fun `every chapter has its own picture`() {
        val pictures = Chapters.ALL.map { Mascot.chapter(it.number) }
        assertEquals(Chapters.ALL.size, pictures.toSet().size)
        assertNotEquals(Mascot.chapter(1), Mascot.chapter(24))
    }
}
