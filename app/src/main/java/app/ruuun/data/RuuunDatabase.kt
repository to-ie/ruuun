package app.ruuun.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RunnerSettings::class, RunLog::class], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class RuuunDatabase : RoomDatabase() {
    abstract fun dao(): RuuunDao

    companion object {
        @Volatile private var instance: RuuunDatabase? = null

        /**
         * 1.3: stops on each run, the stop limit that lets the distance grow, and the
         * longest suggested mission default raised from 3 km to 5 km. Only rows still on the old default move; a cap a parent chose
         * themselves is left alone.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE run_log ADD COLUMN stops INTEGER")
                db.execSQL("ALTER TABLE run_log ADD COLUMN stopsGoalAtLog INTEGER")
                db.execSQL(
                    "ALTER TABLE runner_settings ADD COLUMN lowStopsLimit INTEGER NOT NULL " +
                        "DEFAULT ${RunnerSettings.DEFAULT_LOW_STOPS}",
                )
                db.execSQL(
                    "UPDATE runner_settings SET maxTargetM = ${RunnerSettings.DEFAULT_MAX_TARGET_M} " +
                        "WHERE maxTargetM = ${RunnerSettings.OLD_DEFAULT_MAX_TARGET_M}",
                )
            }
        }

        /**
         * 1.3.1, data only: 1.3.0 held the mission when a run beat it with high or
         * uncounted stops (a 1.1 km run left a 300 m mission). The rule now catches the
         * mission up to the distance run. This applies that once to the latest run, only
         * if the mission was never moved after it. The schema does not change.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    UPDATE runner_settings SET currentTargetM = (
                        SELECT MIN((l.distanceM / 25) * 25, runner_settings.maxTargetM)
                        FROM run_log l ORDER BY l.date DESC, l.createdAt DESC LIMIT 1
                    )
                    WHERE EXISTS (
                        SELECT 1 FROM (
                            SELECT * FROM run_log ORDER BY date DESC, createdAt DESC LIMIT 1
                        ) l
                        WHERE l.distanceM > l.targetMAtLog
                          AND (l.stops IS NULL OR l.stops > runner_settings.lowStopsLimit)
                          AND runner_settings.currentTargetM = l.targetMAtLog
                          AND (l.distanceM / 25) * 25 > runner_settings.currentTargetM
                    )
                    """.trimIndent(),
                )
            }
        }

        fun get(context: Context): RuuunDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RuuunDatabase::class.java,
                "ruuun.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
    }
}
