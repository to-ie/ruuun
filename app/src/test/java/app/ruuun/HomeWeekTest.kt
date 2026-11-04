package app.ruuun

import app.ruuun.logic.JourneyView
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** The Monday to Sunday row and the pill under Ruu on Home. */
class HomeWeekTest {

    private val monday = LocalDate.of(2026, 11, 2)

    @Test
    fun `the week row marks the days with a run and today`() {
        val logs = listOf(
            TestData.log(monday),
            TestData.log(monday.plusDays(2)),
            TestData.log(monday.plusDays(2)),
            TestData.log(monday.minusDays(1)),
        )
        val days = JourneyView.weekDays(logs, monday, today = monday.plusDays(3))
        assertEquals(7, days.size)
        assertEquals(listOf(true, false, true, false, false, false, false), days.map { it.ran })
        assertEquals(3, days.indexOfFirst { it.isToday })
        assertEquals("Ran on Monday and Wednesday", JourneyView.weekDaysDescription(days))
    }

    @Test
    fun `an empty week says so`() {
        val days = JourneyView.weekDays(emptyList(), monday, today = monday)
        assertEquals("No runs yet this week", JourneyView.weekDaysDescription(days))
    }

    @Test
    fun `the pill names the stop goal, or explains the rule before stops are counted`() {
        assertEquals(
            "2 stops or fewer · from 3 · breaks are fine",
            JourneyView.homePill(stopGoal = 2, lastStops = 3, lowStopsLimit = 2, atCap = false, welcomeBack = false),
        )
        assertEquals(
            "2 stops or fewer · breaks are fine",
            JourneyView.homePill(stopGoal = 2, lastStops = 2, lowStopsLimit = 2, atCap = false, welcomeBack = false),
        )
        assertEquals(
            "Non-stop · from 1 · breaks are fine",
            JourneyView.homePill(stopGoal = 0, lastStops = 1, lowStopsLimit = 2, atCap = false, welcomeBack = false),
        )
        assertEquals(
            "Breaks are fine · longer after 2 stops or fewer",
            JourneyView.homePill(stopGoal = null, lastStops = null, lowStopsLimit = 2, atCap = false, welcomeBack = false),
        )
        assertEquals(
            "Repeat this one, or make it easier",
            JourneyView.homePill(stopGoal = 2, lastStops = 3, lowStopsLimit = 2, atCap = false, welcomeBack = true),
        )
    }

    @Test
    fun `the weekly goal reads as one day, not one days`() {
        assertEquals("Run on 1 day this week", JourneyView.weeklyGoalText(1))
        assertEquals("Run on 3 days this week", JourneyView.weeklyGoalText(3))
    }
}
