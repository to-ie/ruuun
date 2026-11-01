package app.ruuun.logic

import app.ruuun.content.Achievements
import app.ruuun.content.BadgeDef
import app.ruuun.content.BadgeMetric

/**
 * Badges are not awarded, they are *true*. There is no award table: a badge is earned
 * whenever its rule holds against the current data, and locked again if it stops holding.
 */
object BadgeEvaluator {

    fun earnedBadgeIds(metrics: Map<BadgeMetric, Int>): Set<String> =
        Achievements.ALL.filter { (metrics[it.metric] ?: 0) >= it.threshold }
            .mapTo(LinkedHashSet()) { it.id }

    fun isEarned(badge: BadgeDef, snapshot: StatsSnapshot): Boolean =
        badge.id in snapshot.earnedBadgeIds

    /** Current value for the badge's rule, capped at its threshold for display. */
    fun progress(badge: BadgeDef, snapshot: StatsSnapshot): Int =
        snapshot.metric(badge.metric).coerceAtMost(badge.threshold)

    fun progressFraction(badge: BadgeDef, snapshot: StatsSnapshot): Float =
        if (badge.threshold <= 0) 1f
        else (snapshot.metric(badge.metric).toFloat() / badge.threshold).coerceIn(0f, 1f)

    /** Badges reached by moving from [before] to [after]. */
    fun newlyEarned(before: StatsSnapshot, after: StatsSnapshot): List<BadgeDef> =
        (after.earnedBadgeIds - before.earnedBadgeIds).mapNotNull { Achievements.BY_ID[it] }

    /**
     * Locked badges ordered by how close they are, closest first. Ties fall to the
     * smallest threshold, so a brand-new runner is pointed at `Liftoff!`. Secret badges
     * never appear: they must not show a countdown.
     */
    fun closestLocked(snapshot: StatsSnapshot, limit: Int): List<BadgeDef> =
        Achievements.ALL
            .filterNot { it.secret || it.id in snapshot.earnedBadgeIds }
            .sortedWith(
                compareByDescending<BadgeDef> { progressFraction(it, snapshot) }
                    .thenBy { it.threshold },
            )
            .take(limit)
}
