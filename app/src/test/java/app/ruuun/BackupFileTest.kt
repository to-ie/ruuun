package app.ruuun

import app.ruuun.backup.BackupFile
import app.ruuun.data.Effort
import app.ruuun.data.RunMode
import app.ruuun.data.RunTag
import app.ruuun.logic.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BackupFileTest {

    private val settings = TestData.settings(currentTargetM = 425)
    private val logs = listOf(
        TestData.log(
            date = LocalDate.of(2026, 11, 1),
            distanceM = 450,
            effort = Effort.EASY,
            targetMAtLog = 425,
            mode = RunMode.WALK_JOG,
            tags = setOf(RunTag.PARK, RunTag.FAMILY),
            note = "Nice walk",
            durationMinutes = 9,
            id = "fixed-id-1",
        ),
        TestData.log(
            date = LocalDate.of(2026, 10, 28),
            distanceM = 300,
            effort = Effort.HARD,
            targetMAtLog = 400,
            id = "fixed-id-2",
        ),
    )

    /** QA 15: a round trip restores settings and logs exactly. */
    @Test
    fun `export then import restores everything, including computed state`() {
        val text = BackupFile.write(settings, logs, exportedAt = "2026-11-01T18:00:00Z")
        val result = BackupFile.parse(text)
        assertTrue(result is BackupFile.ParseResult.Ok)
        val payload = (result as BackupFile.ParseResult.Ok).payload

        assertEquals(settings, payload.settings)
        assertEquals(logs.sortedBy { it.id }, payload.logs.sortedBy { it.id })
        assertEquals(
            StatsCalculator.snapshot(logs, settings),
            StatsCalculator.snapshot(payload.logs, payload.settings),
        )
    }

    @Test
    fun `the envelope carries the format and schema version`() {
        val text = BackupFile.write(settings, logs, exportedAt = "2026-11-01T18:00:00Z")
        assertTrue(text.contains("\"format\": \"ruuun-offline-backup\""))
        assertTrue(text.contains("\"schemaVersion\": 1"))
        assertTrue("No derived state is exported", !text.contains("\"badges\""))
        assertTrue("No derived state is exported", !text.contains("\"xp\""))
    }

    @Test
    fun `the suggested file name is dated`() {
        assertEquals(
            "ruuun-backup-2026-11-01.json",
            BackupFile.suggestedFileName(LocalDate.of(2026, 11, 1)),
        )
    }

    @Test
    fun `malformed or foreign files are rejected with a readable reason`() {
        val cases = listOf(
            "not json at all",
            "{}",
            """{"format":"something-else","schemaVersion":1}""",
            """{"format":"ruuun-offline-backup","schemaVersion":99}""",
            """{"format":"ruuun-offline-backup","schemaVersion":1}""",
        )
        cases.forEach { text ->
            val result = BackupFile.parse(text)
            assertTrue(text, result is BackupFile.ParseResult.Invalid)
            assertTrue((result as BackupFile.ParseResult.Invalid).reason.isNotBlank())
        }
    }

    @Test
    fun `duplicate run ids are rejected`() {
        val duplicated = logs + logs.first()
        val text = BackupFile.write(settings, duplicated, exportedAt = "2026-11-01T18:00:00Z")
        val result = BackupFile.parse(text)
        assertTrue(result is BackupFile.ParseResult.Invalid)
    }

    @Test
    fun `out-of-range values are rejected rather than clamped`() {
        val base = BackupFile.write(settings, logs, exportedAt = "2026-11-01T18:00:00Z")
        val badDistance = base.replace("\"distanceM\": 450", "\"distanceM\": 0")
        assertTrue(BackupFile.parse(badDistance) is BackupFile.ParseResult.Invalid)

        val badEffort = base.replace("\"effort\": \"EASY\"", "\"effort\": \"SPRINTING\"")
        assertTrue(BackupFile.parse(badEffort) is BackupFile.ParseResult.Invalid)

        val badGoal = base.replace("\"weeklyGoal\": 2", "\"weeklyGoal\": 9")
        assertTrue(BackupFile.parse(badGoal) is BackupFile.ParseResult.Invalid)

        val badDate = base.replace("\"date\": \"2026-11-01\"", "\"date\": \"not-a-date\"")
        assertTrue(BackupFile.parse(badDate) is BackupFile.ParseResult.Invalid)
    }

    @Test
    fun `empty notes and tags survive a round trip`() {
        val plain = listOf(TestData.log(LocalDate.of(2026, 11, 1), id = "plain"))
        val text = BackupFile.write(settings, plain, exportedAt = "2026-11-01T18:00:00Z")
        val payload = (BackupFile.parse(text) as BackupFile.ParseResult.Ok).payload
        assertEquals(null, payload.logs.single().note)
        assertTrue(payload.logs.single().tags.isEmpty())
        assertEquals(null, payload.logs.single().mode)
    }

    @Test
    fun `an empty journey exports and imports cleanly`() {
        val text = BackupFile.write(settings, emptyList(), exportedAt = "2026-11-01T18:00:00Z")
        val payload = (BackupFile.parse(text) as BackupFile.ParseResult.Ok).payload
        assertTrue(payload.logs.isEmpty())
        assertEquals(settings, payload.settings)
    }
}
