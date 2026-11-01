package app.ruuun.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RuuunDao {

    @Query("SELECT * FROM runner_settings WHERE id = 1")
    fun observeSettings(): Flow<RunnerSettings?>

    @Query("SELECT * FROM runner_settings WHERE id = 1")
    suspend fun getSettings(): RunnerSettings?

    @Upsert
    suspend fun upsertSettings(settings: RunnerSettings)

    @Query("SELECT * FROM run_log ORDER BY date DESC, createdAt DESC")
    fun observeLogs(): Flow<List<RunLog>>

    @Query("SELECT * FROM run_log ORDER BY date DESC, createdAt DESC")
    suspend fun getLogs(): List<RunLog>

    @Query("SELECT * FROM run_log WHERE id = :id")
    suspend fun getLog(id: String): RunLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: RunLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<RunLog>)

    @Upsert
    suspend fun upsertLog(log: RunLog)

    @Delete
    suspend fun deleteLog(log: RunLog)

    @Query("DELETE FROM run_log")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM runner_settings")
    suspend fun deleteAllSettings()

    /** Import replaces everything or nothing. */
    @Transaction
    suspend fun replaceAll(settings: RunnerSettings, logs: List<RunLog>) {
        deleteAllLogs()
        deleteAllSettings()
        upsertSettings(settings)
        insertLogs(logs)
    }

    @Transaction
    suspend fun deleteEverything() {
        deleteAllLogs()
        deleteAllSettings()
    }
}
