package app.ruuun

import app.ruuun.update.UpdateInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The version.json attached to each release, and what the app will accept from it. */
class UpdateInfoTest {

    private val sha = "a".repeat(64)
    private val apk = "https://github.com/to-ie/ruuun/releases/download/v1.1/ruuun.apk"

    private fun json(
        versionCode: Any = 7,
        versionName: String = "1.1",
        apkUrl: String = apk,
        sha256: String = sha,
    ) = """{"versionCode": $versionCode, "versionName": "$versionName", "apkUrl": "$apkUrl", "sha256": "$sha256"}"""

    @Test
    fun `a well formed release file parses`() {
        val info = UpdateInfo.parse(json())
        assertEquals(UpdateInfo(7, "1.1", apk, sha), info)
    }

    @Test
    fun `newer means a higher version code, nothing else`() {
        val info = UpdateInfo.parse(json(versionCode = 7))!!
        assertTrue(info.isNewerThan(6))
        assertFalse(info.isNewerThan(7))
        assertFalse(info.isNewerThan(8))
    }

    @Test
    fun `the checksum is accepted in either case and stored lower case`() {
        assertEquals(sha, UpdateInfo.parse(json(sha256 = sha.uppercase()))!!.sha256)
    }

    @Test
    fun `an APK from anywhere but this repository's releases is refused`() {
        listOf(
            "https://example.com/ruuun.apk",
            "http://github.com/to-ie/ruuun/releases/download/v1.1/ruuun.apk",
            "https://github.com/someone-else/ruuun/releases/download/v1.1/ruuun.apk",
            "https://github.com/to-ie/ruuun/releases/download/../../../evil/ruuun.apk",
            "https://github.com/to-ie/ruuun/releases/download/",
            "https://github.com/to-ie/ruuun/releases/download/v1.1/ruuun.apk?x=@evil.com",
        ).forEach { assertNull(it, UpdateInfo.parse(json(apkUrl = it))) }
    }

    @Test
    fun `malformed or incomplete files are refused`() {
        assertNull(UpdateInfo.parse("not json"))
        assertNull(UpdateInfo.parse("{}"))
        assertNull(UpdateInfo.parse(json(versionCode = "\"seven\"")))
        assertNull(UpdateInfo.parse(json(versionCode = 0)))
        assertNull(UpdateInfo.parse(json(versionName = "")))
        assertNull(UpdateInfo.parse(json(sha256 = "abc")))
        assertNull(UpdateInfo.parse(json(sha256 = "z".repeat(64))))
    }
}
