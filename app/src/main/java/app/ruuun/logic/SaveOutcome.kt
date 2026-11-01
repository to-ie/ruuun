package app.ruuun.logic

import app.ruuun.content.BadgeDef
import app.ruuun.content.Chapter
import app.ruuun.data.RunLog

/**
 * What to celebrate after a save, computed by diffing the snapshot before and after.
 * Nothing here is stored: reopening the app cannot re-trigger it.
 */
data class SaveOutcome(
    val log: RunLog,
    val before: StatsSnapshot,
    val after: StatsSnapshot,
    val newBadges: List<BadgeDef>,
    val completedChapter: Chapter?,
    val nextTargetM: Int,
    val previousTargetM: Int,
) {
    /** A second log on a day already recorded adds distance, but not an outing day. */
    val isNewOutingDay: Boolean get() = after.outingDays > before.outingDays

    val leveledUp: Boolean get() = after.level > before.level

    companion object {
        /** At most three badges are shown in the result sheet. */
        const val MAX_BADGES_SHOWN = 3
    }
}
