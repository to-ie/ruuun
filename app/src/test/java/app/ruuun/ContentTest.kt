package app.ruuun

import app.ruuun.content.Achievements
import app.ruuun.content.BadgeCategory
import app.ruuun.content.BadgeMetric
import app.ruuun.content.Chapters
import app.ruuun.content.Weeks
import app.ruuun.logic.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentTest {

    @Test
    fun `the catalogue holds 147 unique badges, ids and all`() {
        val expected = 147   // the handoff's 141, plus six for the extra tags
        assertEquals(expected, Achievements.ALL.size)
        assertEquals(expected, Achievements.ALL.map { it.id }.toSet().size)
        assertEquals(expected, Achievements.BY_CATEGORY.values.sumOf { it.size })
    }

    /** Every id from the original handoff catalogue must survive. */
    @Test
    fun `the original badge ids are all still present`() {
        val counts = mapOf(
            "FS" to 10, "RC" to 20, "SD" to 17, "TD" to 16, "WK" to 14, "CO" to 5,
            "RF" to 13, "CH" to 12, "QS" to 8, "MO" to 12, "SE" to 7,
        )
        counts.forEach { (prefix, n) ->
            assertEquals(prefix, n, Achievements.ALL.count { it.id.startsWith("$prefix-") })
        }
    }

    @Test
    fun `every badge has a title, hint and a positive threshold`() {
        Achievements.ALL.forEach { badge ->
            assertTrue(badge.id, badge.title.isNotBlank())
            assertTrue(badge.id, badge.hint.isNotBlank())
            assertTrue("${badge.id} threshold=${badge.threshold}", badge.threshold >= 1)
        }
    }

    /** QA 10: no rule may depend on a feature the app does not have. */
    @Test
    fun `every badge rule is backed by a metric the calculator produces`() {
        val produced = StatsCalculator
            .snapshot(TestData.run(3), TestData.settings())
            .metrics
            .keys
        assertEquals(BadgeMetric.entries.toSet(), produced)
        Achievements.ALL.forEach { badge ->
            assertTrue("${badge.id} uses an uncomputed metric", badge.metric in produced)
        }
    }

    @Test
    fun `only the secret category hides its rule`() {
        Achievements.ALL.forEach { badge ->
            assertEquals(badge.category == BadgeCategory.SECRET, badge.secret)
        }
        assertEquals(7, Achievements.ALL.count { it.secret })
    }

    @Test
    fun `there are 52 weekly themes, each with a flavour and a side quest`() {
        assertEquals(52, Weeks.ALL.size)
        Weeks.ALL.forEachIndexed { index, week ->
            assertEquals(index + 1, week.number)
            assertTrue(week.title.isNotBlank())
            assertTrue(week.flavour.isNotBlank())
        }
    }

    /** QA 9: all twelve chapters are reachable, and the gentle pace halves them. */
    @Test
    fun `twelve chapters complete at their run-day thresholds`() {
        assertEquals(12, Chapters.ALL.size)
        val normal = TestData.settings(onePerWeekPace = false)
        val stats = StatsCalculator.snapshot(TestData.run(100), normal)
        assertEquals(12, stats.completedChapters)

        val oneLess = StatsCalculator.snapshot(TestData.run(99), normal)
        assertEquals(11, oneLess.completedChapters)
    }

    @Test
    fun `the one-per-week story pace halves every chapter requirement`() {
        val gentle = TestData.settings(onePerWeekPace = true)
        assertEquals(2, Chapters.runDaysNeeded(Chapters.ALL[0], true))
        assertEquals(50, Chapters.runDaysNeeded(Chapters.ALL[11], true))
        assertEquals(12, StatsCalculator.snapshot(TestData.run(50), gentle).completedChapters)
        assertEquals(11, StatsCalculator.snapshot(TestData.run(48), gentle).completedChapters)
    }

    @Test
    fun `chapter thresholds only ever increase`() {
        Chapters.ALL.zipWithNext().forEach { (a, b) ->
            assertTrue("${a.number} -> ${b.number}", b.runDaysNeeded > a.runDaysNeeded)
        }
    }

    @Test
    fun `chapter badges line up with the chapter list`() {
        val chapterBadges = Achievements.ALL.filter { it.metric == BadgeMetric.CHAPTER_COMPLETED }
        assertEquals(12, chapterBadges.size)
        chapterBadges.sortedBy { it.threshold }.forEachIndexed { index, badge ->
            assertEquals(index + 1, badge.threshold)
            assertTrue(badge.title, badge.title.contains(Chapters.ALL[index].title))
        }
    }

    @Test
    fun `no badge or theme uses discouraging language`() {
        val banned = listOf("failed", "behind", "lazy", "bad run", "burn calorie", "lose weight")
        val copy = buildList {
            Achievements.ALL.forEach { add(it.title); add(it.hint) }
            Weeks.ALL.forEach { add(it.title); add(it.flavour); add(it.sideQuest.description) }
            Chapters.ALL.forEach { add(it.title); add(it.revealText); add(it.teaser) }
        }
        copy.forEach { line ->
            banned.forEach { word ->
                assertFalse("\"$line\" contains \"$word\"", line.lowercase().contains(word))
            }
        }
    }
}
