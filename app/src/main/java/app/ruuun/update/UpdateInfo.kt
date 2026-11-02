package app.ruuun.update

import org.json.JSONException
import org.json.JSONObject

/**
 * What the newest GitHub release says about itself, read from the `version.json` file
 * attached to every release. Pure Kotlin so the rules can be unit tested.
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
) {
    fun isNewerThan(installedVersionCode: Int): Boolean = versionCode > installedVersionCode

    companion object {
        /** GitHub redirects this to the file on the newest full (not pre-) release. */
        const val MANIFEST_URL =
            "https://github.com/to-ie/ruuun/releases/latest/download/version.json"

        /** APKs are only ever downloaded from this repository's releases. */
        const val APK_URL_PREFIX = "https://github.com/to-ie/ruuun/releases/download/"

        private val SHA256 = Regex("^[0-9a-f]{64}$")
        private val SAFE_PATH = Regex("^[A-Za-z0-9._/-]+$")

        /**
         * Null for anything malformed, or for an APK link that points anywhere other
         * than this repository's releases. Android still refuses any APK signed with a
         * different key, so this is a second lock, not the only one.
         */
        fun parse(json: String): UpdateInfo? {
            val info = try {
                val o = JSONObject(json)
                UpdateInfo(
                    versionCode = o.getInt("versionCode"),
                    versionName = o.getString("versionName").trim(),
                    apkUrl = o.getString("apkUrl").trim(),
                    sha256 = o.getString("sha256").trim().lowercase(),
                )
            } catch (e: JSONException) {
                return null
            }
            val path = info.apkUrl.removePrefix(APK_URL_PREFIX)
            val valid = info.versionCode > 0 &&
                info.versionName.isNotEmpty() &&
                info.versionName.length <= 40 &&
                info.apkUrl.startsWith(APK_URL_PREFIX) &&
                path.isNotEmpty() &&
                SAFE_PATH.matches(path) &&
                ".." !in path &&
                SHA256.matches(info.sha256)
            return info.takeIf { valid }
        }
    }
}
