package app.ruuun.ui.screens

import app.ruuun.ui.components.SubScreenHeader
import android.content.Context
import java.io.ByteArrayOutputStream
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import app.ruuun.ui.components.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ruuun.BuildConfig
import app.ruuun.backup.BackupFile
import app.ruuun.data.RunnerSettings
import app.ruuun.logic.Formatters
import app.ruuun.logic.JourneyView
import app.ruuun.ui.AppState
import app.ruuun.ui.RuuunViewModel
import app.ruuun.ui.components.InfoPanel
import app.ruuun.ui.components.Overline
import app.ruuun.ui.components.RuuunCard
import app.ruuun.ui.components.SecondaryButton
import app.ruuun.ui.components.SegmentedPicker
import app.ruuun.ui.components.SettingsRow
import app.ruuun.ui.theme.Border
import app.ruuun.ui.theme.Destructive
import app.ruuun.ui.theme.Dimens
import app.ruuun.ui.theme.InputBorder
import app.ruuun.ui.theme.Navy
import app.ruuun.ui.theme.RuuunText
import app.ruuun.ui.theme.Shapes
import app.ruuun.ui.theme.TextMuted
import app.ruuun.ui.theme.Surface as SurfaceColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    state: AppState,
    viewModel: RuuunViewModel,
    onBack: () -> Unit,
) {
    val settings = state.settings ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pendingImport by viewModel.pendingImport.collectAsStateWithLifecycle()
    val checkingForUpdate by viewModel.checkingForUpdate.collectAsStateWithLifecycle()

    var nickname by remember(settings.nickname) { mutableStateOf(settings.nickname) }

    // Commit once the typing stops: every write recomputes the whole snapshot.
    LaunchedEffect(nickname) {
        if (nickname != settings.nickname) {
            delay(NICKNAME_COMMIT_DELAY_MS)
            viewModel.setNickname(nickname.ifBlank { RunnerSettings.DEFAULT_NICKNAME })
        }
    }
    var showMaxDialog by remember { mutableStateOf(false) }
    var showStopsDialog by remember { mutableStateOf(false) }
    var deleteStage by remember { mutableIntStateOf(0) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = viewModel.buildExport()
            val saved = text != null && withContext(Dispatchers.IO) { writeText(context, uri, text) }
            viewModel.notify(
                when {
                    text == null -> "Nothing to export yet"
                    saved -> "Backup saved"
                    else -> "Could not save that file"
                },
            )
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.previewImport { readText(context, uri) }
    }

    Column(Modifier.fillMaxSize()) {
        SubScreenHeader(
            title = "Settings",
            onBack = onBack,
            backDescription = "Back to Home",
            subtitle = "Best changed with a grown-up",
        )

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
        ) {
            Overline("Your plan")
            Spacer(Modifier.height(10.dp))
            RuuunCard {
                Text("Nickname", style = RuuunText.BodyStrong, color = Navy)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(20) },
                    singleLine = true,
                    shape = Shapes.Input,
                    textStyle = RuuunText.BodyLarge,
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.SectionGap))
                Text("Weekly goal", style = RuuunText.BodyStrong, color = Navy)
                Spacer(Modifier.height(8.dp))
                SegmentedPicker(
                    options = listOf(1, 2, 3),
                    selected = settings.weeklyGoal,
                    onSelect = viewModel::setWeeklyGoal,
                    label = { "$it" },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "How many days to run each week. Changing this can change past weekly-goal " +
                        "badges. Story progress stays.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )

                Spacer(Modifier.height(Dimens.SectionGap))
                HorizontalDivider(color = Border)
                SettingsRow(
                    title = "Longest suggested mission",
                    subtitle = "Missions never go above this",
                    trailing = {
                        SecondaryButton(
                            text = Formatters.metres(settings.maxTargetM),
                            onClick = { showMaxDialog = true },
                            lowEmphasis = true,
                        )
                    },
                )
                HorizontalDivider(color = Border)
                SettingsRow(
                    title = "Distance goes up after",
                    subtitle = "A run with this many stops or fewer",
                    trailing = {
                        SecondaryButton(
                            text = JourneyView.stopsOrFewer(settings.lowStopsLimit),
                            onClick = { showStopsDialog = true },
                            lowEmphasis = true,
                        )
                    },
                )
                HorizontalDivider(color = Border)
                SettingsRow(
                    title = "Story pace",
                    subtitle = "Set at the start, can't be changed",
                    trailing = {
                        Text(
                            if (settings.initialStoryPaceOnePerWeek) "Gentle" else "Normal",
                            style = RuuunText.BodyStrong,
                            color = TextMuted,
                        )
                    },
                )
                Spacer(Modifier.height(Dimens.SectionGap))
                SecondaryButton(
                    text = "Reset next mission to ${Formatters.mission(settings.startingTargetM)}",
                    onClick = viewModel::resetTargetToStart,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            Overline("Your data")
            Spacer(Modifier.height(10.dp))
            RuuunCard {
                SettingsRow(
                    title = "Export progress",
                    subtitle = "Save a backup file. Keep it somewhere safe.",
                    leading = Icons.Rounded.Upload,
                    onClick = {
                        exportLauncher.launch(BackupFile.suggestedFileName(viewModel.today()))
                    },
                )
                HorizontalDivider(color = Border)
                SettingsRow(
                    title = "Import progress",
                    subtitle = "Replaces everything on this phone. You'll see a preview first.",
                    leading = Icons.Rounded.Download,
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                )
                HorizontalDivider(color = Border)
                SettingsRow(
                    title = "Delete all progress",
                    subtitle = "Asks twice before anything is removed",
                    leading = Icons.Rounded.DeleteOutline,
                    tint = Destructive,
                    onClick = { deleteStage = 1 },
                )
            }

            Spacer(Modifier.height(Dimens.SectionGap))
            InfoPanel(
                "ruuun! has no account, no location and no ads. Everything you log stays " +
                    "on this phone unless you export it. The app only goes online to check " +
                    "GitHub for a new version of itself.",
            )
            Spacer(Modifier.height(Dimens.ItemGap))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    text = if (checkingForUpdate) "Checking" else "Check for updates",
                    onClick = viewModel::checkForUpdates,
                    enabled = !checkingForUpdate,
                    lowEmphasis = true,
                )
            }
            Spacer(Modifier.height(Dimens.ScreenPadding))
        }
    }

    if (showStopsDialog) {
        LowStopsDialog(
            current = settings.lowStopsLimit,
            onDismiss = { showStopsDialog = false },
            onConfirm = {
                viewModel.setLowStopsLimit(it)
                showStopsDialog = false
            },
        )
    }

    if (showMaxDialog) {
        MaxTargetDialog(
            current = settings.maxTargetM,
            onDismiss = { showMaxDialog = false },
            onConfirm = {
                viewModel.setMaxTarget(it)
                showMaxDialog = false
            },
        )
    }

    val preview = pendingImport
    if (preview != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelImport,
            containerColor = SurfaceColor,
            title = { Text("Import this backup?", style = RuuunText.SubTitle, color = Navy) },
            text = {
                Text(
                    "${preview.settings.nickname} · ${preview.logs.size} " +
                        (if (preview.logs.size == 1) "run" else "runs") + ".\n\n" +
                        "This replaces everything on this phone.",
                    style = RuuunText.Body,
                    color = Navy,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmImport) {
                    Text("Replace everything", color = Navy, style = RuuunText.BodyStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelImport) {
                    Text("Cancel", color = TextMuted, style = RuuunText.Body)
                }
            },
        )
    }

    if (deleteStage > 0) {
        AlertDialog(
            onDismissRequest = { deleteStage = 0 },
            containerColor = SurfaceColor,
            title = {
                Text(
                    if (deleteStage == 1) "Delete all progress?" else "Really delete everything?",
                    style = RuuunText.SubTitle,
                    color = Navy,
                )
            },
            text = {
                Text(
                    if (deleteStage == 1) {
                        "Every run, badge and chapter goes back to the start. " +
                            "Export a backup first if you want to keep it."
                    } else {
                        "This cannot be undone."
                    },
                    style = RuuunText.Body,
                    color = Navy,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (deleteStage == 1) {
                        deleteStage = 2
                    } else {
                        deleteStage = 0
                        viewModel.deleteEverything()
                        onBack()
                    }
                }) {
                    Text(
                        if (deleteStage == 1) "Continue" else "Delete everything",
                        color = Destructive,
                        style = RuuunText.BodyStrong,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStage = 0 }) {
                    Text("Keep it", color = Navy, style = RuuunText.BodyStrong)
                }
            },
        )
    }
}


@Composable
private fun LowStopsDialog(current: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var choice by remember { mutableIntStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text("Distance goes up after", style = RuuunText.SubTitle, color = Navy) },
        text = {
            Column {
                SegmentedPicker(
                    options = (0..RunnerSettings.MAX_LOW_STOPS).toList(),
                    selected = choice,
                    onSelect = { choice = it },
                    label = { "$it" },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "The mission only gets longer after a run with " +
                        "${JourneyView.stopsOrFewer(choice)}. Lower is stricter. 2 is a good start.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(choice) }) {
                Text("Save", color = Navy, style = RuuunText.BodyStrong)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted, style = RuuunText.Body)
            }
        },
    )
}

@Composable
private fun MaxTargetDialog(current: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf(current.toString()) }
    val parsed = text.toIntOrNull()
    val valid = parsed != null &&
        parsed in RunnerSettings.MIN_TARGET_M..RunnerSettings.MAX_ALLOWED_TARGET_M
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text("Longest suggested mission", style = RuuunText.SubTitle, color = Navy) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    singleLine = true,
                    isError = !valid,
                    suffix = { Text("m", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = Shapes.Input,
                    colors = fieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Between 100 m and 50,000 m.",
                    style = RuuunText.Secondary,
                    color = TextMuted,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(parsed ?: current) }) {
                Text("Save", color = Navy, style = RuuunText.BodyStrong)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted, style = RuuunText.Body)
            }
        },
    )
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = SurfaceColor,
    unfocusedContainerColor = SurfaceColor,
    focusedIndicatorColor = Navy,
    unfocusedIndicatorColor = InputBorder,
    focusedTextColor = Navy,
    unfocusedTextColor = Navy,
)

private fun writeText(context: Context, uri: Uri, text: String): Boolean = try {
    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) } != null
} catch (e: Exception) {
    false
}

/** Backups are small; refuse to pull an arbitrarily large file into memory. */
private const val MAX_IMPORT_BYTES = 32 * 1024 * 1024

private fun readText(context: Context, uri: Uri): String? = try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        val collected = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        var bounded = true
        while (true) {
            val read = stream.read(buffer)
            if (read < 0) break
            total += read
            if (total > MAX_IMPORT_BYTES) {
                bounded = false
                break
            }
            collected.write(buffer, 0, read)
        }
        if (bounded) collected.toByteArray().decodeToString() else null
    }
} catch (e: Exception) {
    null
}

private const val NICKNAME_COMMIT_DELAY_MS = 400L
