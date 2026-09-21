package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Kesehatan", // Kesehatan, Produktivitas, Belajar, Mindfulness, Olahraga
    val targetDaysPerWeek: Int = 7,
    val timeOfDay: String = "08:00", // "HH:mm"
    val timeCategory: String = "Pagi", // Pagi, Siang, Malam
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 0, // 0 = tepat waktu, 5, 10, 15, 30, 60 menit sebelumnya
    val showInNotification: Boolean = false,
    val showOnScreenOverlay: Boolean = true,
    val linkedMascotEventId: Long? = null,
    val iconName: String = "fitness", // fitness, book, water, meditate, code, sleep, target
    val colorHex: String = "#58CC02",
    val isSynced: Boolean = true,
    val createdAtMs: Long = System.currentTimeMillis(),
    val isGoogleCalendarSynced: Boolean = false,
    val questionTrigger: String? = null,
    val activeCondition: String = "ALL", // "ALL", "IF_TRUE", "IF_FALSE"
    val frequencyType: String = "HARIAN", // "HARIAN", "MINGGUAN", "BULANAN", "TAHUNAN", "GOOGLE_CALENDAR"
    val selectedDaysOfWeek: String = "1,2,3,4,5,6,7", // "1,2,3,4,5,6,7" (1=Senin..7=Minggu)
    val scheduleId: Long? = null // null means applicable to all schedules
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val habitId: Long,
    val dateString: String, // Format: YYYY-MM-DD
    val completedAtMs: Long = System.currentTimeMillis(),
    val note: String = ""
)
