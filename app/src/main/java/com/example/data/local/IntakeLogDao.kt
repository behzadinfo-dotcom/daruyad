package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {
    @Query("SELECT * FROM intake_logs WHERE logDateString = :dateString ORDER BY scheduledTime ASC")
    fun getLogsForDate(dateString: String): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE logDateString = :dateString AND batchHour = :hour AND batchMinute = :minute")
    fun getLogsForBatch(dateString: String, hour: Int, minute: Int): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE logDateString = :dateString AND batchHour = :hour AND batchMinute = :minute")
    suspend fun getLogsForBatchSync(dateString: String, hour: Int, minute: Int): List<IntakeLogEntity>

    @Query("SELECT * FROM intake_logs WHERE medicineId = :medicineId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun findLog(medicineId: Long, scheduledTime: Long): IntakeLogEntity?

    @Query("SELECT * FROM intake_logs ORDER BY scheduledTime DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<IntakeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: IntakeLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLogsIfNotExist(logs: List<IntakeLogEntity>)

    @Update
    suspend fun updateLog(log: IntakeLogEntity)

    @Query("UPDATE intake_logs SET status = :status, takenTime = :takenTime WHERE id = :id")
    suspend fun updateLogStatus(id: Long, status: String, takenTime: Long?)

    @Query("UPDATE intake_logs SET status = :status, takenTime = :takenTime WHERE logDateString = :dateString AND batchHour = :hour AND batchMinute = :minute")
    suspend fun markAllInBatch(dateString: String, hour: Int, minute: Int, status: String, takenTime: Long?)

    @Query("SELECT COUNT(*) FROM intake_logs WHERE status = 'TAKEN'")
    fun getTakenCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM intake_logs WHERE status != 'PENDING'")
    fun getTotalDecidedCountFlow(): Flow<Int>
}
