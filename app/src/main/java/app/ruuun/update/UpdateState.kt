package app.ruuun.update

import java.io.File

/** Where an update is, as shown by the banner at the top of the tabs. */
sealed interface UpdateState {
    /** Up to date, not checked yet, or dismissed with "Later". */
    data object None : UpdateState

    data class Available(val info: UpdateInfo) : UpdateState

    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState

    data class Ready(val info: UpdateInfo, val file: File) : UpdateState

    data class Failed(val info: UpdateInfo) : UpdateState
}
