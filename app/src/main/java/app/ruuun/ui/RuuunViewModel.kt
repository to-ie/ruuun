package app.ruuun.ui

import android.app.Application
import android.content.Intent
import app.ruuun.BuildConfig
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
import app.ruuun.update.AppUpdater
import app.ruuun.update.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import app.ruuun.logic.FilledDots

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

    private val updater = AppUpdater(application)

    private val _update = MutableStateFlow<UpdateState>(UpdateState.None)
    val update: StateFlow<UpdateState> = _update.asStateFlow()

    private val _checkingForUpdate = MutableStateFlow(false)
    val checkingForUpdate: StateFlow<Boolean> = _checkingForUpdate.asStateFlow()

    init {
        // One quiet check per launch. Offline simply means no banner.
        viewModelScope.launch {
            updater.cleanUp(BuildConfig.VERSION_CODE)
            checkForUpdate(manual = false)
        }
    }

    fun today(): LocalDate = LocalDate.now()

    private val filledDots = FilledDots()

    /** Weekly goal dots filled since Home last showed them, so they can bounce in. */
    fun newlyFilledDots(weekStart: LocalDate, runDays: Int): IntRange =
        filledDots.newlyFilled(weekStart, runDays)

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
        stops: Int?,
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
                    stops = stops,
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
        stops: Int?,
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
                    stops = stops,
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

    fun makeHarder() {
        viewModelScope.launch { repository.makeHarder() }
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

    fun setLowStopsLimit(limit: Int) {
        val safe = limit.coerceIn(0, RunnerSettings.MAX_LOW_STOPS)
        viewModelScope.launch { repository.updateSettings { it.copy(lowStopsLimit = safe) } }
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

    /**
     * Builds the export text, or null when there is nothing to export yet. Serialising a
     * long journey is slow enough to drop frames, so it stays off the main thread.
     */
    suspend fun buildExport(): String? = withContext(Dispatchers.IO) {
        val snapshot = repository.snapshotForExport() ?: return@withContext null
        BackupFile.write(
            snapshot.first,
            snapshot.second,
            exportedAt = java.time.Instant.now().toString(),
        )
    }

    /** Reads and validates a chosen file. All of it off the main thread. */
    fun previewImport(read: suspend () -> String?) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val text = read() ?: return@withContext null
                BackupFile.parse(text)
            }
            when (result) {
                null -> notify("Could not read that file")
                is BackupFile.ParseResult.Ok -> _pendingImport.value = result.payload
                is BackupFile.ParseResult.Invalid -> notify(result.reason)
            }
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

    // --- Updates ------------------------------------------------------------

    /** The Settings button: like the launch check, but says what it found. */
    fun checkForUpdates() {
        viewModelScope.launch { checkForUpdate(manual = true) }
    }

    private suspend fun checkForUpdate(manual: Boolean) {
        if (_checkingForUpdate.value) return
        _checkingForUpdate.value = true
        try {
            val info = updater.fetchLatest()
            when {
                info == null -> if (manual) notify("Could not check for updates. Are you online?")
                info.isNewerThan(BuildConfig.VERSION_CODE) -> {
                    val busy = _update.value is UpdateState.Downloading ||
                        _update.value is UpdateState.Ready
                    if (!busy) _update.value = UpdateState.Available(info)
                }
                else -> if (manual) notify("You have the newest version")
            }
        } finally {
            _checkingForUpdate.value = false
        }
    }

    fun downloadUpdate() {
        val info = when (val current = _update.value) {
            is UpdateState.Available -> current.info
            is UpdateState.Failed -> current.info
            else -> return
        }
        _update.value = UpdateState.Downloading(info, 0f)
        viewModelScope.launch {
            val file = updater.download(info) { progress ->
                _update.value = UpdateState.Downloading(info, progress)
            }
            _update.value = if (file != null) {
                UpdateState.Ready(info, file)
            } else {
                UpdateState.Failed(info)
            }
        }
    }

    /**
     * What to open when Install is tapped: Android's installer, or first the switch
     * that lets ruuun install apps. Null when there is nothing ready.
     */
    fun installIntent(): Intent? {
        val ready = _update.value as? UpdateState.Ready ?: return null
        return if (updater.canInstall()) {
            updater.installIntent(ready.file)
        } else {
            notify("Turn on Allow from this source, then come back and tap Install")
            updater.installPermissionIntent()
        }
    }

    /** "Later": hidden until the next launch or a check from Settings. */
    fun dismissUpdate() {
        if (_update.value !is UpdateState.Downloading) _update.value = UpdateState.None
    }

    fun deleteEverything() {
        viewModelScope.launch {
            repository.deleteEverything()
            _lastSave.value = null
            notify("Everything removed")
        }
    }
}
