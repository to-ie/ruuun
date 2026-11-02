package app.ruuun.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Checks GitHub for a newer release, downloads its APK into the app's own cache and
 * hands it to Android's installer. Nothing about the runner is ever sent: the only
 * requests are two plain GETs to github.com.
 */
class AppUpdater(context: Context) {

    private val context = context.applicationContext
    private val dir: File get() = File(context.cacheDir, UPDATE_DIR)

    /** The newest release, or null when offline or the file is not usable. */
    suspend fun fetchLatest(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val connection = open(UpdateInfo.MANIFEST_URL)
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                val bytes = connection.inputStream.use { input ->
                    val buffer = ByteArray(MAX_MANIFEST_BYTES + 1)
                    var total = 0
                    while (total < buffer.size) {
                        val n = input.read(buffer, total, buffer.size - total)
                        if (n < 0) break
                        total += n
                    }
                    if (total > MAX_MANIFEST_BYTES) return@withContext null
                    buffer.copyOf(total)
                }
                UpdateInfo.parse(String(bytes, Charsets.UTF_8))
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Downloads the APK for [info], checking its SHA-256 as it streams. Returns the
     * file, or null if anything went wrong; a half-finished download is thrown away.
     */
    suspend fun download(info: UpdateInfo, onProgress: (Float) -> Unit): File? =
        withContext(Dispatchers.IO) {
            dir.mkdirs()
            val target = File(dir, "ruuun-${info.versionCode}.apk")
            if (target.exists() && sha256(target) == info.sha256) return@withContext target

            val partial = File(dir, "download.part")
            try {
                val connection = open(info.apkUrl)
                try {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        return@withContext null
                    }
                    val expected = connection.contentLengthLong
                    if (expected > MAX_APK_BYTES) return@withContext null
                    val digest = MessageDigest.getInstance("SHA-256")
                    var received = 0L
                    connection.inputStream.use { input ->
                        partial.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                val n = input.read(buffer)
                                if (n < 0) break
                                received += n
                                if (received > MAX_APK_BYTES) return@withContext null
                                digest.update(buffer, 0, n)
                                output.write(buffer, 0, n)
                                if (expected > 0) onProgress(received.toFloat() / expected)
                            }
                        }
                    }
                    if (digest.digest().toHex() != info.sha256) return@withContext null
                    target.delete()
                    if (!partial.renameTo(target)) return@withContext null
                    target
                } finally {
                    connection.disconnect()
                }
            } catch (e: IOException) {
                null
            } finally {
                partial.delete()
            }
        }

    /** Android asks once per app before it will install anything from it. */
    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Opens the system switch that lets ruuun install apps. */
    fun installPermissionIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))

    /** Hands the downloaded APK to Android's installer, which asks the user to confirm. */
    fun installIntent(apk: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        return Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** Deletes downloaded APKs that are no newer than what is installed. */
    suspend fun cleanUp(installedVersionCode: Int) = withContext(Dispatchers.IO) {
        dir.listFiles()?.forEach { file ->
            val code = file.name.removePrefix("ruuun-").removeSuffix(".apk").toIntOrNull()
            if (code == null || code <= installedVersionCode) file.delete()
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "ruuun-app")
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private companion object {
        const val UPDATE_DIR = "updates"
        const val TIMEOUT_MS = 15_000
        const val MAX_MANIFEST_BYTES = 16 * 1024
        const val MAX_APK_BYTES = 100L * 1024 * 1024
    }
}
