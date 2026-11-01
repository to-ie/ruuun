package app.ruuun

import app.ruuun.content.SideQuest
import app.ruuun.content.Weeks
import app.ruuun.data.Effort
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.logic.WeekCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WeekCalculatorTest {

    @Test
    fun `weeks start on Monday`() {
        val sunday = LocalDate.of(2026, 11, 1)
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 26), WeekCalculator.weekStart(sunday))
        assertEquals(
            LocalDate.of(2026, 10, 26),
            WeekCalculator.weekStart(LocalDate.of(2026, 10, 26)),
        )
    }

    /** QA 7: a Sunday and the following Monday are different weeks. */
    @Test
    fun `a sunday and the next monday are two different weeks`() {
        val sunday = LocalDate.of(2026, 11, 1)
        val monday = LocalDate.of(2026, 11, 2)
        val logs = listOf(TestData.log(sunday), TestData.log(monday))
        assertEquals(1, WeekCalculator.outingDaysInWeek(logs, WeekCalculator.weekStart(sunday)))
        assertEquals(1, WeekCalculator.outingDaysInWeek(logs, WeekCalculator.weekStart(monday)))
    }

    /** QA 13: the December/January boundary must not drift. */
    @Test
    fun `week identity survives the december to january rollover`() {
        val dec31 = LocalDate.of(2026, 12, 31) // Thursday
        val jan1 = LocalDate.of(2027, 1, 1) // Friday, same ISO week
        assertEquals(WeekCalculator.weekStart(dec31), WeekCalculator.weekStart(jan1))

        val jan4 = LocalDate.of(2027, 1, 4) // Monday, next week
        assertFalse(WeekCalculator.weekStart(dec31) == WeekCalculator.weekStart(jan4))
    }

    @Test
    fun `leap day sits in its own ordinary week`() {
        val leapDay = LocalDate.of(2028, 2, 29)
        assertEquals(LocalDate.of(2028, 2, 28), WeekCalculator.weekStart(leapDay))
        val onboarded = LocalDate.of(2028, 2, 28)
        assertEquals(1, WeekCalculator.journeyWeek(onboarded, leapDay))
    }

    @Test
    fun `journey weeks count from the onboarding week`() {
        val onboarded = LocalDate.of(2026, 9, 23) // Wednesday
        assertEquals(1, WeekCalculator.journeyWeek(onboarded, LocalDate.of(2026, 9, 27)))
        assertEquals(2, WeekCalculator.journeyWeek(onboarded, LocalDate.of(2026, 9, 28)))
        assertEquals(52, WeekCalculator.journeyWeek(onboarded, LocalDate.of(2027, 9, 13)))
        assertEquals(53, WeekCalculator.journeyWeek(onboarded, LocalDate.of(2027, 9, 20)))
    }

    /** QA 8: week 53 reuses the week 1 theme without resetting anything. */
    @Test
    fun `week 53 rotates back to the first theme`() {
        assertEquals(52, Weeks.ALL.size)
        assertEquals(Weeks.ALL[0], Weeks.themeFor(53))
        assertEquals(Weeks.ALL[1], Weeks.themeFor(54))
        assertEquals(Weeks.ALL[51], Weeks.themeFor(52))
        assertEquals(Weeks.ALL[0], Weeks.themeFor(105))
    }

    @Test
    fun `side quests read only the logs in their own week`() {
        val monday = LocalDate.of(2026, 11, 2)
        val weekLogs = listOf(
            TestData.log(monday, effort = Effort.EASY, mode = RunMode.WALK_ONLY),
            TestData.log(monday.plusDays(2), tags = setOf(RunTag.PARK), note = "Nice"),
        )
        assertTrue(WeekCalculator.sideQuestMet(weekLogs, SideQuest.ANY_RUN))
        assertTrue(WeekCalculator.sideQuestMet(weekLogs, SideQuest.EASY))
        assertTrue(WeekCalculator.sideQuestMet(weekLogs, SideQuest.WALK))
        assertTrue(WeekCalculator.sideQuestMet(weekLogs, SideQuest.NOTE))
        assertTrue(WeekCalculator.sideQuestMet(weekLogs, SideQuest.PARK))
        assertFalse(WeekCalculator.sideQuestMet(weekLogs, SideQuest.FAMILY))
        assertFalse(WeekCalculator.sideQuestMet(weekLogs, SideQuest.NATURE))
        assertFalse(WeekCalculator.sideQuestMet(emptyList(), SideQuest.ANY_RUN))
    }

    @Test
    fun `main quest uses distinct days, not log count`() {
        val monday = LocalDate.of(2026, 11, 2)
        val settings = TestData.settings(weeklyGoal = 2, onboardedDate = monday)
        val sameDayTwice = listOf(TestData.log(monday), TestData.log(monday))
        val progress = WeekCalculator.weekProgress(sameDayTwice, monday, 2, monday)
        assertEquals(1, progress.outingDays)
        assertFalse(progress.mainQuestMet)
        assertEquals(2, settings.weeklyGoal)
    }

    @Test
    fun `weeks before the journey start are not journey weeks`() {
        val onboarded = LocalDate.of(2026, 11, 2)
        val logs = listOf(
            TestData.log(LocalDate.of(2026, 10, 20)),
            TestData.log(LocalDate.of(2026, 11, 3)),
        )
        val weeks = WeekCalculator.journeyWeeksWithLogs(logs, onboarded)
        assertEquals(listOf(LocalDate.of(2026, 11, 2)), weeks)
    }
}
