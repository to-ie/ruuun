package app.ruuun.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RunnerSettings::class, RunLog::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class RuuunDatabase : RoomDatabase() {
    abstract fun dao(): RuuunDao

    companion object {
        @Volatile private var instance: RuuunDatabase? = null

        fun get(context: Context): RuuunDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RuuunDatabase::class.java,
                "ruuun.db",
            ).build().also { instance = it }
        }
    }
}
