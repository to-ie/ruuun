package app.ruuun.logic

import app.ruuun.data.RunLog
import kotlin.math.roundToInt

/** Metres or kilometres, chosen with the small segmented control on the log screen. */
enum class DistanceUnit(val label: String) { METRES("m"), KILOMETRES("km") }

/** Parsing and validation for the one free-text field in the app. */
object RunInput {

    /**
     * Parse typed distance into whole metres, or null when the text is not a usable
     * positive number. Decimal kilometres are converted accurately.
     *
     * Metres are whole numbers, so a comma there can only be a thousands separator;
     * kilometres are decimal, so a comma there is a decimal point.
     */
    fun parseDistanceM(text: String, unit: DistanceUnit): Int? {
        val trimmed = text.trim()
        val cleaned = when (unit) {
            DistanceUnit.METRES -> trimmed.replace(",", "")
            DistanceUnit.KILOMETRES -> trimmed.replace(',', '.')
        }
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (!value.isFinite() || value <= 0.0) return null
        val metres = when (unit) {
            DistanceUnit.METRES -> value
            DistanceUnit.KILOMETRES -> value * 1_000
        }
        val rounded = metres.roundToInt()
        if (rounded < 1 || rounded > RunLog.MAX_DISTANCE_M) return null
        return rounded
    }

    /** True for entries worth a gentle second look. Never blocks saving. */
    fun isImplausible(metres: Int): Boolean = metres > RunLog.IMPLAUSIBLE_DISTANCE_M

    fun parseDurationMinutes(text: String): Int? {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return null
        val value = cleaned.toIntOrNull() ?: return null
        return if (value > 0) value else null
    }

    fun isDurationValid(text: String): Boolean =
        text.isBlank() || parseDurationMinutes(text) != null

    fun cleanNote(text: String): String? =
        text.trim().take(RunLog.MAX_NOTE_LENGTH).ifBlank { null }

    fun cleanNickname(text: String): String =
        text.trim().take(20)
}
