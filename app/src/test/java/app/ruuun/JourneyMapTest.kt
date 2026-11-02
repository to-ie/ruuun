package app.ruuun

import app.ruuun.content.World
import app.ruuun.logic.JourneyView
import app.ruuun.logic.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyMapTest {

    private fun map(runDays: Int, gentle: Boolean = false) =
        JourneyView.journeyMap(
            StatsCalculator.snapshot(TestData.run(runDays), TestData.settings(onePerWeekPace = gentle)),
            TestData.settings(onePerWeekPace = gentle),
        )

    @Test
    fun `the map is four worlds of three chapters, in story order`() {
        val worlds = map(0)
        assertEquals(4, worlds.size)
        assertEquals(
            listOf(World.ROOKIE_WOODS, World.DESERT_DASH, World.SKY_KINGDOM, World.PLANET_ZOOM),
            worlds.map { it.world },
        )
        worlds.forEach { assertEquals(3, it.chapters.size) }
        assertEquals((1..12).toList(), worlds.flatMap { w -> w.chapters.map { it.chapter.number } })
    }

    @Test
    fun `a fresh runner has chapter one in progress and nothing complete`() {
        val worlds = map(0)
        assertEquals(0, worlds.sumOf { it.completed })
        val current = worlds.flatMap { it.chapters }.filter { it.isCurrent }
        assertEquals(1, current.size)
        assertEquals(1, current.single().chapter.number)
        assertTrue(worlds.first().isCurrent)
        assertTrue("later worlds are locked", worlds.last().isLocked)
    }

    @Test
    fun `exactly one chapter is ever current`() {
        (0..110).forEach { days ->
            assertTrue(
                "at $days run days",
                map(days).flatMap { it.chapters }.count { it.isCurrent } <= 1,
            )
        }
    }

    @Test
    fun `finishing the first world marks it complete and moves on`() {
        val worlds = map(12)
        assertTrue(worlds[0].isComplete)
        assertEquals(3, worlds[0].completed)
        assertFalse(worlds[0].isCurrent)
        assertTrue(worlds[1].isCurrent)
        assertEquals(4, worlds.flatMap { it.chapters }.single { it.isCurrent }.chapter.number)
    }

    @Test
    fun `after the last chapter nothing is current and all worlds are complete`() {
        val worlds = map(100)
        assertTrue(worlds.all { it.isComplete })
        assertTrue(worlds.flatMap { it.chapters }.none { it.isCurrent })
    }

    @Test
    fun `the gentle pace halves what each chapter asks for`() {
        val normal = map(0).flatMap { it.chapters }.map { it.runDaysNeeded }
        val gentle = map(0, gentle = true).flatMap { it.chapters }.map { it.runDaysNeeded }
        assertEquals(listOf(4, 8, 12, 18, 24, 32, 40, 50, 60, 72, 84, 100), normal)
        assertEquals(listOf(2, 4, 6, 9, 12, 16, 20, 25, 30, 36, 42, 50), gentle)
    }

    @Test
    fun `progress fractions stay in range`() {
        (0..110 step 7).forEach { days ->
            map(days).flatMap { it.chapters }.forEach {
                assertTrue("$days days, chapter ${it.chapter.number}", it.fraction in 0f..1f)
            }
        }
    }

    @Test
    fun `chapter progress says that any distance counts, until the finale`() {
        val settings = TestData.settings()
        val early = JourneyView.chapterProgress(StatsCalculator.snapshot(TestData.run(1), settings), settings)
        assertEquals("1 of 4 runs · any distance counts", early.progressLabel)
        val done = JourneyView.chapterProgress(StatsCalculator.snapshot(TestData.run(100), settings), settings)
        assertEquals("Every chapter complete", done.progressLabel)
    }
}
