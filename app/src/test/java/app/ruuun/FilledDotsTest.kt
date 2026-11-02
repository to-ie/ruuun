package app.ruuun

import app.ruuun.logic.FilledDots
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Only dots filled since Home last showed them should bounce in. */
class FilledDotsTest {

    private val week1 = LocalDate.of(2026, 11, 2)
    private val week2 = week1.plusWeeks(1)

    @Test
    fun `the first look after launch is quiet`() {
        assertEquals(IntRange.EMPTY, FilledDots().newlyFilled(week1, 2))
    }

    @Test
    fun `a run logged since last time fills the next dot`() {
        val dots = FilledDots()
        dots.newlyFilled(week1, 0)
        assertEquals(0 until 1, dots.newlyFilled(week1, 1))
        assertEquals(IntRange.EMPTY, dots.newlyFilled(week1, 1))
    }

    @Test
    fun `two runs since last time fill two dots in order`() {
        val dots = FilledDots()
        dots.newlyFilled(week1, 1)
        assertEquals(1 until 3, dots.newlyFilled(week1, 3))
    }

    @Test
    fun `a new week or a deleted run animates nothing`() {
        val dots = FilledDots()
        dots.newlyFilled(week1, 2)
        assertEquals(IntRange.EMPTY, dots.newlyFilled(week2, 1))
        assertEquals(IntRange.EMPTY, dots.newlyFilled(week2, 0))
    }
}
