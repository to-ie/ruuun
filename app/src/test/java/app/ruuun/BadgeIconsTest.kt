package app.ruuun

import app.ruuun.content.Achievements
import app.ruuun.ui.badgeIconIds
import app.ruuun.ui.earnedIcon
import org.junit.Assert.assertEquals
import org.junit.Test

/** Every badge shows its own icon once earned, and no two badges look the same. */
class BadgeIconsTest {

    @Test
    fun `every badge has an icon of its own`() {
        assertEquals(Achievements.ALL.map { it.id }.toSet(), badgeIconIds)
    }

    @Test
    fun `no two badges share an icon`() {
        val byIcon = Achievements.ALL.groupBy { it.earnedIcon().name }
        val shared = byIcon.filterValues { it.size > 1 }.mapValues { (_, badges) -> badges.map { it.id } }
        assertEquals("Badges sharing an icon: $shared", emptyMap<String, List<String>>(), shared)
    }
}
