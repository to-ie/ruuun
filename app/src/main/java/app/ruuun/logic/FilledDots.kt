package app.ruuun.logic

import java.time.LocalDate

/**
 * Remembers how many weekly goal dots Home last showed, so the ones filled since then
 * can bounce in. Memory only: a fresh launch starts quiet.
 */
class FilledDots {
    private var shown: Pair<LocalDate, Int>? = null

    /** Dot indexes filled since the last call. Empty first time, in a new week, or unchanged. */
    fun newlyFilled(weekStart: LocalDate, runDays: Int): IntRange {
        val before = shown?.takeIf { it.first == weekStart }?.second
        shown = weekStart to runDays
        return if (before == null || runDays <= before) IntRange.EMPTY else before until runDays
    }
}
