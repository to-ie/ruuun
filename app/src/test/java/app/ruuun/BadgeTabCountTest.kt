package app.ruuun

import app.ruuun.content.Achievements
import app.ruuun.content.BadgeCategory
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Badges tab lists every badge: each category on screen adds up to all 193. */
class BadgeTabCountTest {
    @Test
    fun `every badge is in a category shown on the Badges tab`() {
        val shown = BadgeCategory.entries.filter { Achievements.BY_CATEGORY[it]?.isNotEmpty() == true }
        val perCategory = shown.associateWith { Achievements.BY_CATEGORY[it].orEmpty().size }
        assertEquals(Achievements.ALL.size, perCategory.values.sum())
        assertEquals(193, Achievements.ALL.size)
    }
}
