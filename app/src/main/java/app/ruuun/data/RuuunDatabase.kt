package app.ruuun.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RunnerSettings::class, RunLog::class], version = 2, exportSchema = true)
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

        fun get(context: Context): RuuunDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RuuunDatabase::class.java,
                "ruuun.db",
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
