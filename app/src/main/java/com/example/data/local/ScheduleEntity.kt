package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "target",
    val colorHex: String = "#58CC02",
    val isDefault: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:00",
    val reminderMinutesBefore: Int = 0, // 0 = tepat waktu, 5, 10, 15, 30, 60
    val createdAt: Long = System.currentTimeMillis()
)
