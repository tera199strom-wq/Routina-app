package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyScheduleSelectionDao {

    @Query("SELECT * FROM daily_schedule_selections WHERE date = :date LIMIT 1")
    fun getSelectionForDate(date: String): Flow<DailyScheduleSelectionEntity?>

    @Query("SELECT * FROM daily_schedule_selections WHERE date = :date LIMIT 1")
    suspend fun getSelectionForDateSync(date: String): DailyScheduleSelectionEntity?

    @Query("SELECT * FROM daily_schedule_selections")
    suspend fun getAllSelectionsList(): List<DailyScheduleSelectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(selection: DailyScheduleSelectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(selections: List<DailyScheduleSelectionEntity>)

    @Query("DELETE FROM daily_schedule_selections WHERE date = :date")
    suspend fun clearSelectionForDate(date: String)

    @Query("DELETE FROM daily_schedule_selections")
    suspend fun clearAllSelections()
}
