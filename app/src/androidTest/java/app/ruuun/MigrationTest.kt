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

    /** A 1.3.0 database: the latest run beat its mission and [currentTargetM] is what is stored now. */
    private fun v2(name: String, currentTargetM: Int, stops: String, lowStopsLimit: Int = 2) {
        helper.createDatabase(name, 2).apply {
            execSQL(
                "INSERT INTO runner_settings VALUES " +
                    "(1,'Runner','2026-11-02',300,$currentTargetM,5000,2,0,1,$lowStopsLimit)",
            )
            execSQL(
                "INSERT INTO run_log VALUES " +
                    "('a','2026-11-02',1100,'OKAY',300,NULL,NULL,'',NULL,$stops,NULL,1,1)",
            )
            close()
        }
    }

    private fun mission(name: String): Int {
        val db = helper.runMigrationsAndValidate(name, 3, true, RuuunDatabase.MIGRATION_2_3)
        db.query("SELECT currentTargetM FROM runner_settings").use {
            assertTrue(it.moveToFirst())
            return it.getInt(0)
        }
    }

    @Test
    fun two_to_three_catches_a_stuck_mission_up_to_the_run() {
        v2("s1.db", currentTargetM = 300, stops = "8")
        assertEquals(1_100, mission("s1.db"))
    }

    @Test
    fun two_to_three_also_catches_up_an_uncounted_run() {
        v2("s2.db", currentTargetM = 300, stops = "NULL")
        assertEquals(1_100, mission("s2.db"))
    }

    @Test
    fun two_to_three_leaves_a_mission_moved_by_hand() {
        v2("s3.db", currentTargetM = 325, stops = "8")
        assertEquals(325, mission("s3.db"))
    }

    @Test
    fun two_to_three_leaves_a_low_stop_run_alone() {
        // 1.3.0 already stretched these; this one was then set by hand back to 300.
        v2("s4.db", currentTargetM = 300, stops = "1")
        assertEquals(300, mission("s4.db"))
    }

    @Test
    fun one_to_three_keeps_a_12_phone_as_it_was() {
        v1("s5.db", maxTargetM = 3_000)
        helper.runMigrationsAndValidate("s5.db", 2, true, RuuunDatabase.MIGRATION_1_2).close()
        assertEquals("the 1.2 mission had already moved past the run's target", 1_200, mission("s5.db"))
    }
}
