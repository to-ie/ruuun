package app.ruuun.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ruuun.backup.BackupFile
import app.ruuun.data.Effort
import app.ruuun.data.RunLog
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.data.RunnerSettings
import app.ruuun.data.RuuunDatabase
import app.ruuun.data.RuuunRepository
import app.ruuun.logic.SaveOutcome
import app.ruuun.logic.StatsCalculator
import app.ruuun.logic.StatsSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Everything the UI reads, recomputed whenever the two tables change. */
data class AppState(
    val loading: Boolean = true,
    val settings: RunnerSettings? = null,
    val logs: List<RunLog> = emptyList(),
    val stats: StatsSnapshot = StatsSnapshot.EMPTY,
) {
    val needsSetup: Boolean get() = !loading && settings?.onboardingComplete != true
}

/** A one-shot message for a snackbar. */
data class Toast(val id: Long, val text: String)

class RuuunViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RuuunRepository(RuuunDatabase.get(application).dao())

    val state: StateFlow<AppState> =
        combine(repository.settings, repository.logs) { settings, logs ->
            AppState(
                loading = false,
                settings = settings,
                logs = logs,
                stats = settings?.let { StatsCalculator.snapshot(logs, it) } ?: StatsSnapshot.EMPTY,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

    private val _lastSave = MutableStateFlow<SaveOutcome?>(null)
    val lastSave: StateFlow<SaveOutcome?> = _lastSave.asStateFlow()

    private val _message = MutableStateFlow<Toast?>(null)
    val message: StateFlow<Toast?> = _message.asStateFlow()

    private val _pendingImport = MutableStateFlow<BackupFile.Payload?>(null)
    val pendingImport: StateFlow<BackupFile.Payload?> = _pendingImport.asStateFlow()

    /** Guards the save button against double taps. */
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    fun today(): LocalDate = LocalDate.now()

    fun completeSetup(nickname: String, startingTargetM: Int, weeklyGoal: Int) {
        viewModelScope.launch {
            repository.completeSetup(nickname, startingTargetM, weeklyGoal, today())
        }
    }

    fun saveRun(
        date: LocalDate,
        distanceM: Int,
        effort: Effort,
        durationMinutes: Int?,
        mode: RunMode?,
        tags: Set<RunTag>,
        note: String?,
        onSaved: () -> Unit,
    ) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                val outcome = repository.saveRun(
                    date = date,
                    distanceM = distanceM,
                    effort = effort,
                    durationMinutes = durationMinutes,
                    mode = mode,
                    tags = tags,
                    note = note,
                    now = System.currentTimeMillis(),
                )
                if (outcome != null) {
                    _lastSave.value = outcome
                    onSaved()
                }
            } finally {
                _saving.value = false
            }
        }
    }

    fun updateRun(
        id: String,
        date: LocalDate,
        distanceM: Int,
        effort: Effort,
        durationMinutes: Int?,
        mode: RunMode?,
        tags: Set<RunTag>,
        note: String?,
        onSaved: () -> Unit,
    ) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                repository.updateRun(
                    id = id,
                    date = date,
                    distanceM = distanceM,
                    effort = effort,
                    durationMinutes = durationMinutes,
                    mode = mode,
                    tags = tags,
                    note = note,
                    now = System.currentTimeMillis(),
                )
                notify("Run updated")
                onSaved()
            } finally {
                _saving.value = false
            }
        }
    }

    fun deleteRun(log: RunLog, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRun(log)
            notify("Run deleted")
            onDeleted()
        }
    }

    fun makeEasier() {
        viewModelScope.launch { repository.makeEasier() }
    }

    fun repeatTarget() {
        notify("Same mission next time. Repeating is a good plan.")
    }

    fun setNickname(nickname: String) {
        viewModelScope.launch { repository.updateSettings { it.copy(nickname = nickname) } }
    }

    fun setWeeklyGoal(goal: Int) {
        viewModelScope.launch { repository.updateSettings { it.copy(weeklyGoal = goal) } }
    }

    fun setMaxTarget(metres: Int) {
        val safe = metres.coerceIn(RunnerSettings.MIN_TARGET_M, RunnerSettings.MAX_ALLOWED_TARGET_M)
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(maxTargetM = safe, currentTargetM = minOf(it.currentTargetM, safe))
            }
        }
    }

    fun resetTargetToStart() {
        viewModelScope.launch {
            repository.resetTargetToStart()
            notify("Next mission reset")
        }
    }

    fun clearLastSave() {
        _lastSave.value = null
    }

    fun notify(text: String) {
        _message.value = Toast(System.currentTimeMillis(), text)
    }

    fun clearMessage() {
        _message.value = null
    }

    // --- Backup -------------------------------------------------------------

    /** Builds the export text, or null when there is nothing to export yet. */
    suspend fun buildExport(): String? {
        val (settings, logs) = repository.snapshotForExport() ?: return null
        return BackupFile.write(settings, logs, exportedAt = java.time.Instant.now().toString())
    }

    fun previewImport(text: String) {
        when (val result = BackupFile.parse(text)) {
            is BackupFile.ParseResult.Ok -> _pendingImport.value = result.payload
            is BackupFile.ParseResult.Invalid -> notify(result.reason)
        }
    }

    fun confirmImport() {
        val payload = _pendingImport.value ?: return
        viewModelScope.launch {
            repository.replaceAll(payload.settings, payload.logs)
            _pendingImport.value = null
            notify("Progress restored")
        }
    }

    fun cancelImport() {
        _pendingImport.value = null
    }

    fun deleteEverything() {
        viewModelScope.launch {
            repository.deleteEverything()
            _lastSave.value = null
            notify("Everything removed")
        }
    }
}
