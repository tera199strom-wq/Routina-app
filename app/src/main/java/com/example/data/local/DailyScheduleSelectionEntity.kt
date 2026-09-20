package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_schedule_selections")
data class DailyScheduleSelectionEntity(
    @PrimaryKey
    val date: String, // format "yyyy-MM-dd"
    val scheduleId: Long,
    val isAutoSelected: Boolean = true, // true if determined from mascot question flow, false if manual
    val selectedAt: Long = System.currentTimeMillis()
)
