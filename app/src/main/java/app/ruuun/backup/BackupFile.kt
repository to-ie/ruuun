package app.ruuun.backup

import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.data.RunnerSettings
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * The manual JSON backup. Badges, XP, quests and chapters are never exported: they
 * are recomputed from the logs, so the file only carries the two stored records.
 */
object BackupFile {

    const val FORMAT = "ruuun-offline-backup"
    /** 2 added stops in 1.3. Version 1 files still restore; their runs have no stops. */
    const val SCHEMA_VERSION = 2
    private val READABLE_VERSIONS = 1..SCHEMA_VERSION

    /** Roughly 55 years of daily running. Past this, something is wrong with the file. */
    const val MAX_LOGS = 20_000

    /** The journey cannot have started before the app existed, nor in the future. */
    private val EARLIEST_START: LocalDate = LocalDate.of(2024, 1, 1)

    data class Payload(val settings: RunnerSettings, val logs: List<RunLog>)

    sealed interface ParseResult {
        data class Ok(val payload: Payload) : ParseResult
        data class Invalid(val reason: String) : ParseResult
    }

    fun suggestedFileName(today: LocalDate): String = "ruuun-backup-$today.json"

    fun write(settings: RunnerSettings, logs: List<RunLog>, exportedAt: String): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", exportedAt)
        root.put(
            "settings",
            JSONObject().apply {
                put("nickname", settings.nickname)
                put("onboardedDate", settings.onboardedDate.toString())
                put("startingTargetM", settings.startingTargetM)
                put("currentTargetM", settings.currentTargetM)
                put("maxTargetM", settings.maxTargetM)
                put("weeklyGoal", settings.weeklyGoal)
                put("initialStoryPaceOnePerWeek", settings.initialStoryPaceOnePerWeek)
                put("onboardingComplete", settings.onboardingComplete)
                put("lowStopsLimit", settings.lowStopsLimit)
            },
        )
        val array = JSONArray()
        logs.sortedBy { it.date }.forEach { log ->
            array.put(
                JSONObject().apply {
                    put("id", log.id)
                    put("date", log.date.toString())
                    put("distanceM", log.distanceM)
                    put("effort", log.effort.name)
                    put("targetMAtLog", log.targetMAtLog)
                    put("durationMinutes", log.durationMinutes ?: JSONObject.NULL)
                    put("mode", log.mode?.name ?: JSONObject.NULL)
                    put("tags", JSONArray(log.tags.map { it.name }.sorted()))
                    put("note", log.note ?: JSONObject.NULL)
                    put("stops", log.stops ?: JSONObject.NULL)
                    put("stopsGoalAtLog", log.stopsGoalAtLog ?: JSONObject.NULL)
                    put("createdAt", log.createdAt)
                    put("updatedAt", log.updatedAt)
                },
            )
        }
        root.put("logs", array)
        return root.toString(2)
    }

    fun parse(text: String): ParseResult = parse(text, today = LocalDate.now())

    @Suppress("ReturnCount")
    fun parse(text: String, today: LocalDate): ParseResult {
        val root = try {
            JSONObject(text)
        } catch (e: JSONException) {
            return ParseResult.Invalid("That file isn't a ruuun! backup.")
        }

        if (root.optString("format") != FORMAT) {
            return ParseResult.Invalid("That file isn't a ruuun! backup.")
        }
        val version = root.optInt("schemaVersion", -1)
        if (version !in READABLE_VERSIONS) {
            return ParseResult.Invalid("This backup was made by a different version of ruuun!.")
        }

        val settingsJson = root.optJSONObject("settings")
            ?: return ParseResult.Invalid("The backup is missing its settings.")

        val onboardedDate = parseDate(settingsJson.optString("onboardedDate"))
            ?: return ParseResult.Invalid("The backup has an unreadable start date.")
        // A wild start date would make the weekly journey span thousands of weeks.
        if (onboardedDate.isBefore(EARLIEST_START) || onboardedDate.isAfter(today)) {
            return ParseResult.Invalid("The backup has a start date that cannot be right.")
        }

        val startingTargetM = settingsJson.optInt("startingTargetM", -1)
        val currentTargetM = settingsJson.optInt("currentTargetM", -1)
        val savedMax = settingsJson.optInt("maxTargetM", RunnerSettings.DEFAULT_MAX_TARGET_M)
        // A 1.2 backup still on the old 3 km default moves up, as the upgrade does.
        val maxTargetM = if (version == 1 && savedMax == RunnerSettings.OLD_DEFAULT_MAX_TARGET_M) {
            RunnerSettings.DEFAULT_MAX_TARGET_M
        } else {
            savedMax
        }
        val lowStopsLimit = settingsJson.optInt("lowStopsLimit", RunnerSettings.DEFAULT_LOW_STOPS)
        val weeklyGoal = settingsJson.optInt("weeklyGoal", -1)
        if (!inTargetRange(startingTargetM) || !inTargetRange(currentTargetM) ||
            !inTargetRange(maxTargetM) || weeklyGoal !in 1..3 ||
            lowStopsLimit !in 0..RunnerSettings.MAX_LOW_STOPS
        ) {
            return ParseResult.Invalid("The backup has settings outside the allowed range.")
        }

        val nickname = settingsJson.optString("nickname").ifBlank { RunnerSettings.DEFAULT_NICKNAME }
        val settings = RunnerSettings(
            id = 1,
            nickname = nickname.take(MAX_NICKNAME),
            onboardedDate = onboardedDate,
            startingTargetM = startingTargetM,
            currentTargetM = currentTargetM,
            maxTargetM = maxTargetM,
            weeklyGoal = weeklyGoal,
            initialStoryPaceOnePerWeek = settingsJson.optBoolean("initialStoryPaceOnePerWeek", false),
            onboardingComplete = settingsJson.optBoolean("onboardingComplete", true),
            lowStopsLimit = lowStopsLimit,
        )

        val logsJson = root.optJSONArray("logs")
            ?: return ParseResult.Invalid("The backup is missing its runs.")

        if (logsJson.length() > MAX_LOGS) {
            return ParseResult.Invalid("That backup holds too many runs to import.")
        }

        val logs = ArrayList<RunLog>(logsJson.length())
        val seenIds = HashSet<String>()
        for (index in 0 until logsJson.length()) {
            val item = logsJson.optJSONObject(index)
                ?: return ParseResult.Invalid("Run ${index + 1} in the backup is unreadable.")
            val log = parseLog(item, index) ?: return ParseResult.Invalid(
                "Run ${index + 1} in the backup is unreadable.",
            )
            if (!seenIds.add(log.id)) {
                return ParseResult.Invalid("The backup lists the same run twice.")
            }
            logs.add(log)
        }

        return ParseResult.Ok(Payload(settings, logs))
    }

    private const val MAX_NICKNAME = 20

    private fun inTargetRange(value: Int) =
        value in RunnerSettings.MIN_TARGET_M..RunnerSettings.MAX_ALLOWED_TARGET_M

    private fun parseDate(raw: String?): LocalDate? =
        if (raw.isNullOrBlank()) null
        else try {
            LocalDate.parse(raw)
        } catch (e: DateTimeParseException) {
            null
        }

    private fun parseLog(item: JSONObject, index: Int): RunLog? {
        val id = item.optString("id").ifBlank { return null }
        val date = parseDate(item.optString("date")) ?: return null
        val distanceM = item.optInt("distanceM", -1)
        if (distanceM !in 1..RunLog.MAX_DISTANCE_M) return null
        val effort = enumOrNull<Effort>(item.optString("effort")) ?: return null
        val targetMAtLog = item.optInt("targetMAtLog", -1)
        if (!inTargetRange(targetMAtLog)) return null

        val duration = if (item.isNull("durationMinutes")) null else item.optInt("durationMinutes", -1)
        if (duration != null && duration <= 0) return null

        val mode = if (item.isNull("mode")) null else enumOrNull<RunMode>(item.optString("mode"))

        // Tags and mode are decorative, so a value from a newer version of the app is
        // skipped rather than failing the whole restore. Effort drives the rules, so an
        // unknown effort is still rejected above.
        val tagsJson = item.optJSONArray("tags")
        val tags = LinkedHashSet<RunTag>()
        if (tagsJson != null) {
            for (t in 0 until tagsJson.length()) {
                enumOrNull<RunTag>(tagsJson.optString(t))?.let { tags.add(it) }
            }
        }

        val note = if (item.isNull("note")) null else item.optString("note").ifBlank { null }
        if (note != null && note.length > RunLog.MAX_NOTE_LENGTH) return null

        val stops = optionalStops(item, "stops") ?: return null
        val stopsGoal = optionalStops(item, "stopsGoalAtLog") ?: return null

        return RunLog(
            id = id,
            date = date,
            distanceM = distanceM,
            effort = effort,
            targetMAtLog = targetMAtLog,
            durationMinutes = duration,
            mode = mode,
            tags = tags,
            note = note,
            stops = stops.value,
            stopsGoalAtLog = stopsGoal.value,
            createdAt = item.optLong("createdAt", 0L),
            updatedAt = item.optLong("updatedAt", 0L),
        )
    }

    /** A field that may be missing or null; [value] is null then. Null result = invalid. */
    private class Optional(val value: Int?)

    private fun optionalStops(item: JSONObject, key: String): Optional? {
        if (!item.has(key) || item.isNull(key)) return Optional(null)
        val value = item.optInt(key, -1)
        return if (value in 0..RunLog.MAX_STOPS) Optional(value) else null
    }

    private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
        if (name.isNullOrBlank()) null
        else enumValues<T>().firstOrNull { it.name == name }
}
