package app.ruuun.logic

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Display helpers. Storage is always whole metres. */
object Formatters {

    /** `300 m`, `950 m`, `1.2 km`, `4 km`. */
    fun distance(metres: Int): String =
        if (metres < 1_000) "$metres m" else "${km(metres)} km"

    /** Kilometres to one decimal, with a trailing `.0` trimmed. */
    fun km(metres: Int): String = km(metres.toLong())

    fun km(metres: Long): String {
        val tenths = (metres / 100.0).roundToInt()
        val whole = tenths / 10
        val remainder = tenths % 10
        return if (remainder == 0) "$whole" else "$whole.$remainder"
    }

    /** Thousands-separated metres, for settings values like `3,000 m`. */
    fun metres(metres: Int): String = String.format(Locale.UK, "%,d m", metres)

    private val dayMonth = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK)
    private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)
    private val weekday = DateTimeFormatter.ofPattern("EEE", Locale.UK)
    private val monthName = DateTimeFormatter.ofPattern("MMMM", Locale.UK)

    fun dayMonth(date: LocalDate): String = date.format(dayMonth)
    fun monthYear(date: LocalDate): String = date.format(monthYear).uppercase(Locale.UK)
    fun weekday(date: LocalDate): String = date.format(weekday).uppercase(Locale.UK)
    fun monthName(date: LocalDate): String = date.format(monthName)

    /** `Today · Sun 1 Nov` when the date is today, otherwise just the date. */
    fun runDate(date: LocalDate, today: LocalDate): String =
        if (date == today) "Today · ${dayMonth(date)}" else dayMonth(date)
}
