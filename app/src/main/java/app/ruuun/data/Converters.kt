package app.ruuun.data

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room converters. Dates are stored as local ISO calendar days, never as UTC instants. */
class Converters {
    @TypeConverter fun dateToString(value: LocalDate): String = value.toString()
    @TypeConverter fun stringToDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter fun effortToString(value: Effort): String = value.name
    @TypeConverter fun stringToEffort(value: String): Effort = Effort.valueOf(value)

    @TypeConverter fun modeToString(value: RunMode?): String? = value?.name
    @TypeConverter fun stringToMode(value: String?): RunMode? = value?.let { RunMode.valueOf(it) }

    @TypeConverter
    fun tagsToString(value: Set<RunTag>): String =
        value.map { it.name }.sorted().joinToString(",")

    @TypeConverter
    fun stringToTags(value: String): Set<RunTag> =
        if (value.isBlank()) emptySet()
        else value.split(",").mapNotNull { name ->
            runCatching { RunTag.valueOf(name.trim()) }.getOrNull()
        }.toSet()
}
