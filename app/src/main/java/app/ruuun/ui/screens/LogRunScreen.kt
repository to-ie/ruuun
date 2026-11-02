package app.ruuun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.logic.DistanceUnit
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.logic.RunInput
import app.ruuun.logic.StopGoal
import app.ruuun.ui.AppState
import app.ruuun.ui.RuuunViewModel
import app.ruuun.ui.components.EffortButton
import app.ruuun.ui.components.InfoPanel
import app.ruuun.ui.components.PrimaryCta
import app.ruuun.ui.components.SegmentedPicker
import app.ruuun.ui.components.ToggleChip
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.Ground
import app.ruuun.ui.theme.InputBorder
import app.ruuun.ui.theme.Lime
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor
import java.time.LocalDate
import java.time.ZoneOffset

private val MODES = listOf(
    RunMode.WALK_ONLY to "Walk",
    RunMode.WALK_JOG to "Walk + jog",
    RunMode.MOSTLY_JOG to "Mostly jog",
)

private val TAGS = listOf(
    RunTag.PARK to "Park",
    RunTag.FAMILY to "Family",
    RunTag.FRIEND to "With a friend",
    RunTag.NEW_ROUTE to "New route",
    RunTag.NATURE to "Nature",
    RunTag.TRAIL to "Trail",
    RunTag.HILLS to "Hills",
    RunTag.BEACH to "Beach",
)

/** Add or edit one run. The same screen serves both; only the copy changes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRunScreen(
    state: AppState,
    viewModel: RuuunViewModel,
    editingId: String?,
    onClose: () -> Unit,
    onSaved: () -> Unit,
) {
    val settings = state.settings ?: return
    val today = viewModel.today()
    val editing: RunLog? = remember(editingId, state.logs) {
        editingId?.let { id -> state.logs.firstOrNull { it.id == id } }
    }
    val saving by viewModel.saving.collectAsStateWithLifecycle()

    // Saveable, so a rotation mid-entry does not throw away what has been typed.
    // Enums and dates are held as primitives because that is what the bundle can carry.
    val key = editing?.id ?: "new"
    var dateEpochDay by rememberSaveable(key) {
        mutableLongStateOf((editing?.date ?: today).toEpochDay())
    }
    val date: LocalDate = LocalDate.ofEpochDay(dateEpochDay)
    var unitName by rememberSaveable(key) { mutableStateOf(DistanceUnit.METRES.name) }
    val unit = DistanceUnit.valueOf(unitName)
    var distanceText by rememberSaveable(key) {
        mutableStateOf(editing?.distanceM?.toString() ?: "")
    }
    var effortName by rememberSaveable(key) { mutableStateOf(editing?.effort?.name ?: "") }
    val effort = effortName.takeIf { it.isNotEmpty() }?.let { Effort.valueOf(it) }
    var modeName by rememberSaveable(key) { mutableStateOf(editing?.mode?.name ?: "") }
    val mode = modeName.takeIf { it.isNotEmpty() }?.let { RunMode.valueOf(it) }
    var tagNames by rememberSaveable(key) {
        mutableStateOf(editing?.tags.orEmpty().joinToString(",") { it.name })
    }
    val tags = tagNames.split(",").filter { it.isNotEmpty() }.map { RunTag.valueOf(it) }.toSet()
    var minutesText by rememberSaveable(key) {
        mutableStateOf(editing?.durationMinutes?.toString() ?: "")
    }
    var note by rememberSaveable(key) { mutableStateOf(editing?.note ?: "") }
    // -1 while nobody has counted: stops are optional, and 0 means non-stop.
    var stopsCount by rememberSaveable(key) { mutableIntStateOf(editing?.stops ?: NOT_COUNTED) }
    val stops = stopsCount.takeIf { it != NOT_COUNTED }
    val latestDate = remember(state.logs) { state.logs.maxOfOrNull { it.date } }
    val stopGoal = remember(editing, state.logs, date, settings) {
        when {
            editing != null -> editing.stopsGoalAtLog
            // A run on an earlier date is saved without a stop goal, so none is shown.
            latestDate != null && date.isBefore(latestDate) -> null
            else -> StopGoal.next(state.logs, settings)
        }
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val distanceM = RunInput.parseDistanceM(distanceText, unit)
    val durationValid = RunInput.isDurationValid(minutesText)
    val canSave = distanceM != null && effort != null && durationValid && !saving

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onClose,
                shape = Shapes.Input,
                color = SurfaceColor,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Navy)
                }
            }
            Spacer(Modifier.width(4.dp))
            Text(
                if (editing == null) "Log a run" else "Edit run",
                style = RuuunText.SubTitle,
                color = Navy,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
        ) {
            Text("When", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = { showDatePicker = true },
                shape = Shapes.Input,
                color = SurfaceColor,
                border = androidx.compose.foundation.BorderStroke(2.dp, InputBorder),
                modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.InputHeight),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        Formatters.runDate(date, today),
                        style = RuuunText.BodyLarge,
                        color = Navy,
                        modifier = Modifier.weight(1f),
                    )
                    Text("Change", style = RuuunText.Body, color = app.ruuun.ui.theme.Link)
                }
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            Text("How far did you go?", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = distanceText,
                    onValueChange = { distanceText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    placeholder = {
                        Text(
                            when (unit) {
                                DistanceUnit.METRES -> "${settings.currentTargetM}"
                                DistanceUnit.KILOMETRES -> Formatters.km(settings.currentTargetM)
                            },
                            style = RuuunText.DistanceInput,
                            color = TextMuted,
                        )
                    },
                    singleLine = true,
                    textStyle = RuuunText.DistanceInput,
                    shape = Shapes.Input,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedIndicatorColor = Navy,
                        unfocusedIndicatorColor = Navy,
                        focusedTextColor = Navy,
                        unfocusedTextColor = Navy,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = Dimens.DistanceInputHeight),
                )
                Spacer(Modifier.width(10.dp))
                SegmentedPicker(
                    options = DistanceUnit.entries,
                    selected = unit,
                    onSelect = { unitName = it.name },
                    label = { it.label },
                    modifier = Modifier.width(126.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Your mission was ${Formatters.distance(settings.currentTargetM)}. " +
                    "Put in what you actually did. More or less, both count.",
                style = RuuunText.Secondary,
                color = TextMuted,
            )
            if (distanceM != null && RunInput.isImplausible(distanceM)) {
                Spacer(Modifier.height(10.dp))
                InfoPanel("That is a long way. Worth double-checking the number.")
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            Text("How many times did you stop?", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(10.dp))
            StopsStepper(
                stops = stops,
                onChange = { stopsCount = it ?: NOT_COUNTED },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    stops == 0 -> "Non-stop! The whole way."
                    stopGoal != null -> "Your stop goal: ${StopGoal.goalLabel(stopGoal).lowercase()}. " +
                        "Breaks are fine."
                    stops != null -> "The mission gets longer after a run with " +
                        "${JourneyView.stopsOrFewer(settings.lowStopsLimit)}."
                    else -> "Count each time you stopped to walk or rest. The mission gets " +
                        "longer after a run with ${JourneyView.stopsOrFewer(settings.lowStopsLimit)}."
                },
                style = RuuunText.Secondary,
                color = TextMuted,
            )

            Spacer(Modifier.height(Dimens.SectionGap))
            Text("How did it feel?", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemGap)) {
                Effort.entries.forEach { option ->
                    EffortButton(
                        effort = option,
                        selected = effort == option,
                        onSelect = { effortName = option.name },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(Dimens.SectionGap))
            Text("Extras (optional)", style = RuuunText.SectionTitle, color = Navy)

            Spacer(Modifier.height(14.dp))
            Text("Walk or jog?", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            FlowRowCompat {
                MODES.forEach { (value, label) ->
                    ToggleChip(
                        text = label,
                        selected = mode == value,
                        onToggle = { modeName = if (mode == value) "" else value.name },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Tags", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            FlowRowCompat {
                TAGS.forEach { (value, label) ->
                    val selected = value in tags
                    ToggleChip(
                        text = label,
                        selected = selected,
                        onToggle = {
                            val next = if (selected) tags - value else tags + value
                            tagNames = next.joinToString(",") { tag -> tag.name }
                        },
                        selectedFill = Lime,
                        selectedContent = Navy,
                        icon = if (selected) Icons.Outlined.Check else Icons.Outlined.Add,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Minutes", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter(Char::isDigit) },
                    singleLine = true,
                    isError = !durationValid,
                    shape = Shapes.Input,
                    textStyle = RuuunText.BodyLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedIndicatorColor = Navy,
                        unfocusedIndicatorColor = InputBorder,
                        focusedTextColor = Navy,
                        unfocusedTextColor = Navy,
                    ),
                    modifier = Modifier.width(130.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Just for your own record. Never turned into a pace.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))
            Text("Notes", style = RuuunText.BodyStrong, color = Navy)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(RunLog.MAX_NOTE_LENGTH) },
                placeholder = { Text("How did it go?", color = TextMuted) },
                minLines = 2,
                shape = Shapes.Input,
                textStyle = RuuunText.Body,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    focusedIndicatorColor = Navy,
                    unfocusedIndicatorColor = InputBorder,
                    focusedTextColor = Navy,
                    unfocusedTextColor = Navy,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${note.length}/${RunLog.MAX_NOTE_LENGTH}",
                style = RuuunText.Secondary,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }

        Surface(color = SurfaceColor, modifier = Modifier.fillMaxWidth()) {
            Column {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
                Box(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp)) {
                    PrimaryCta(
                        text = if (editing == null) "Save run" else "Save changes",
                        onClick = {
                            val metres = distanceM ?: return@PrimaryCta
                            val chosenEffort = effort ?: return@PrimaryCta
                            val minutes = RunInput.parseDurationMinutes(minutesText)
                            val cleanNote = RunInput.cleanNote(note)
                            if (editing == null) {
                                viewModel.saveRun(
                                    date = date,
                                    distanceM = metres,
                                    effort = chosenEffort,
                                    durationMinutes = minutes,
                                    mode = mode,
                                    tags = tags,
                                    note = cleanNote,
                                    stops = stops,
                                    onSaved = onSaved,
                                )
                            } else {
                                viewModel.updateRun(
                                    id = editing.id,
                                    date = date,
                                    distanceM = metres,
                                    effort = chosenEffort,
                                    durationMinutes = minutes,
                                    mode = mode,
                                    tags = tags,
                                    note = cleanNote,
                                    stops = stops,
                                    onSaved = onSaved,
                                )
                            }
                        },
                        icon = Icons.Outlined.Check,
                        enabled = canSave,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val todayUtcMillis = today.toEpochDay() * MILLIS_PER_DAY
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * MILLIS_PER_DAY,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
                override fun isSelectableYear(year: Int) = year <= today.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = java.time.Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        if (!picked.isAfter(today)) dateEpochDay = picked.toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("Choose", color = Navy, style = RuuunText.BodyStrong) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextMuted, style = RuuunText.Body)
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = Ground,
            ),
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
private const val NOT_COUNTED = -1

/**
 * Minus, the count, plus. Starts at "Not counted"; minus from there means non-stop,
 * plus means one stop. "Clear" goes back to not counted.
 */
@Composable
private fun StopsStepper(stops: Int?, onChange: (Int?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepButton(
            icon = Icons.Outlined.Remove,
            description = if (stops == null) "No stops, non-stop" else "One fewer stop",
            enabled = stops != 0,
            onClick = { onChange(if (stops == null) 0 else stops - 1) },
        )
        Box(
            Modifier
                .weight(1f)
                .heightIn(min = Dimens.InputHeight)
                .semantics {
                    contentDescription = when (stops) {
                        null -> "Stops not counted"
                        0 -> "No stops, non-stop"
                        else -> StopGoal.stopsLabel(stops)
                    }
                    // Read the new count out after each tap.
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            if (stops == null) {
                Text("Not counted", style = RuuunText.BodyLarge, color = TextMuted)
            } else {
                Text(
                    if (stops == 0) "0 · non-stop" else StopGoal.stopsLabel(stops),
                    style = RuuunText.CardTitle,
                    color = Navy,
                    textAlign = TextAlign.Center,
                )
            }
        }
        StepButton(
            icon = Icons.Outlined.Add,
            description = if (stops == null) "One stop" else "One more stop",
            enabled = (stops ?: 0) < RunLog.MAX_STOPS,
            onClick = { onChange(if (stops == null) 1 else stops + 1) },
        )
    }
    if (stops != null) {
        TextButton(onClick = { onChange(null) }) {
            Text("Clear, nobody counted", style = RuuunText.Body, color = app.ruuun.ui.theme.Link)
        }
    }
}

@Composable
private fun StepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = Shapes.Input,
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, if (enabled) Navy else InputBorder),
        modifier = Modifier.size(Dimens.InputHeight),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = if (enabled) Navy else TextMuted)
        }
    }
}

/** Chips wrap onto extra rows at large font scales. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowCompat(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}
