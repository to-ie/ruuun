package app.ruuun

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import app.ruuun.data.RuuunDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Upgrading from 1.2 keeps every run and only moves a cap still on the old default. */
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RuuunDatabase::class.java,
    )

    private fun v1(name: String, maxTargetM: Int) {
        helper.createDatabase(name, 1).apply {
            execSQL(
                "INSERT INTO runner_settings VALUES (1,'Runner','2026-11-02',300,1200,$maxTargetM,2,0,1)",
            )
            execSQL(
                "INSERT INTO run_log VALUES ('first','2026-11-02',1100,'OKAY',300,NULL,NULL,'','First run!',1,1)",
            )
            close()
        }
    }

    @Test
    fun one_to_two_keeps_runs_adds_stops_and_raises_the_old_default_cap() {
        v1("m1.db", maxTargetM = 3_000)
        val db = helper.runMigrationsAndValidate("m1.db", 2, true, RuuunDatabase.MIGRATION_1_2)

        db.query("SELECT maxTargetM, currentTargetM, lowStopsLimit FROM runner_settings").use {
            assertTrue(it.moveToFirst())
            assertEquals(5_000, it.getInt(0))
            assertEquals(1_200, it.getInt(1))
            assertEquals("the stop limit starts at 2", 2, it.getInt(2))
        }
        db.query("SELECT distanceM, note, stops, stopsGoalAtLog FROM run_log").use {
            assertTrue(it.moveToFirst())
            assertEquals(1_100, it.getInt(0))
            assertEquals("First run!", it.getString(1))
            assertTrue("no stops were counted in 1.2", it.isNull(2))
            assertTrue(it.isNull(3))
        }
    }

    @Test
    fun a_cap_a_parent_chose_is_left_alone() {
        v1("m2.db", maxTargetM = 2_000)
        val db = helper.runMigrationsAndValidate("m2.db", 2, true, RuuunDatabase.MIGRATION_1_2)
        db.query("SELECT maxTargetM FROM runner_settings").use {
            assertTrue(it.moveToFirst())
            assertEquals(2_000, it.getInt(0))
        }
    }
}
