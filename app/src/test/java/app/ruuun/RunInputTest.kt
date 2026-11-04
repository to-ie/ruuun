package app.ruuun

import app.ruuun.logic.DistanceUnit
import app.ruuun.logic.Formatters
import app.ruuun.logic.RunInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunInputTest {

    /** QA 14: malformed input cannot produce a saved run. */
    @Test
    fun `mission distances are exact, so every 25 m step reads differently`() {
        assertEquals("975 m", Formatters.mission(975))
        assertEquals("1 km", Formatters.mission(1_000))
        assertEquals("1.3 km", Formatters.mission(1_300))
        assertEquals("1.325 km", Formatters.mission(1_325))
        assertEquals("1.35 km", Formatters.mission(1_350))
        assertEquals("2.05 km", Formatters.mission(2_050))
        assertEquals("0.3", Formatters.exactKm(300))
        val steps = (1_000..1_500 step 25).map { Formatters.mission(it) }
        assertEquals(steps.size, steps.toSet().size)
    }

    @Test
    fun `bad distance text never parses`() {
        listOf("", "   ", "abc", "0", "-5", "0.0", "1e9", "50001", ".", "1.2.3").forEach {
            assertNull(it, RunInput.parseDistanceM(it, DistanceUnit.METRES))
        }
    }

    @Test
    fun `metres and decimal kilometres both convert accurately`() {
        assertEquals(450, RunInput.parseDistanceM("450", DistanceUnit.METRES))
        assertEquals(1_200, RunInput.parseDistanceM("1.2", DistanceUnit.KILOMETRES))
        assertEquals(1_200, RunInput.parseDistanceM("1,2", DistanceUnit.KILOMETRES))
        assertEquals(1_250, RunInput.parseDistanceM("1.25", DistanceUnit.KILOMETRES))
        assertEquals(1, RunInput.parseDistanceM("1", DistanceUnit.METRES))
    }

    @Test
    fun `a comma is a thousands separator in metres and a decimal point in km`() {
        assertEquals(1_200, RunInput.parseDistanceM("1,200", DistanceUnit.METRES))
        assertEquals(12_500, RunInput.parseDistanceM("12,500", DistanceUnit.METRES))
        assertEquals(1_200, RunInput.parseDistanceM("1,2", DistanceUnit.KILOMETRES))
        assertEquals(1_250, RunInput.parseDistanceM("1,25", DistanceUnit.KILOMETRES))
    }

    @Test
    fun `implausible distances warn but stay savable`() {
        assertFalse(RunInput.isImplausible(10_000))
        assertTrue(RunInput.isImplausible(10_001))
        assertEquals(12_000, RunInput.parseDistanceM("12", DistanceUnit.KILOMETRES))
    }

    @Test
    fun `duration is blank or a positive whole number`() {
        assertTrue(RunInput.isDurationValid(""))
        assertTrue(RunInput.isDurationValid("8"))
        assertFalse(RunInput.isDurationValid("0"))
        assertFalse(RunInput.isDurationValid("-3"))
        assertFalse(RunInput.isDurationValid("8.5"))
        assertEquals(8, RunInput.parseDurationMinutes("8"))
        assertNull(RunInput.parseDurationMinutes(""))
    }

    @Test
    fun `notes are trimmed, capped and blanked`() {
        assertNull(RunInput.cleanNote("   "))
        assertEquals("Hi", RunInput.cleanNote("  Hi  "))
        assertEquals(140, RunInput.cleanNote("x".repeat(500))!!.length)
    }

    @Test
    fun `nicknames are trimmed to twenty characters`() {
        assertEquals("Runner", RunInput.cleanNickname("  Runner "))
        assertEquals(20, RunInput.cleanNickname("y".repeat(40)).length)
    }

    @Test
    fun `distances display as metres below a kilometre and kilometres above`() {
        assertEquals("300 m", Formatters.distance(300))
        assertEquals("950 m", Formatters.distance(950))
        assertEquals("1 km", Formatters.distance(1_000))
        assertEquals("1.2 km", Formatters.distance(1_200))
        assertEquals("4.5 km", Formatters.distance(4_500))
        assertEquals("3,000 m", Formatters.metres(3_000))
        assertEquals("0", Formatters.km(0L))
        assertEquals("4.1", Formatters.km(4_050L))
    }
}
