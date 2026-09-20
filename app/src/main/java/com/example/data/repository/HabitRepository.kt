package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>> {
        return habitDao.getLogsForDate(dateString)
    }

    suspend fun addHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
        // clean logs for this habit
    }

    suspend fun getHabitLog(habitId: Long, dateString: String): HabitLogEntity? {
        return habitDao.getLogForHabitAndDate(habitId, dateString)
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String, note: String = ""): Boolean {
        val existingLog = habitDao.getLogForHabitAndDate(habitId, dateString)
        return if (existingLog != null) {
            habitDao.deleteLog(habitId, dateString)
            false // Now incomplete
        } else {
            habitDao.insertLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    completedAtMs = System.currentTimeMillis(),
                    note = note
                )
            )
            true // Now completed
        }
    }

    suspend fun seedInitialHabitsIfEmpty() {
        val habits = allHabits.first()
        // Ensure default sample habits are removed to keep user schedule clean by default
        val defaultTitles = setOf(
            "Minum Air 2 Liter",
            "Olahraga ringan 15 Min",
            "Membaca Buku 20 Halaman",
            "Meditasi & De-stress",
            "Minum Air 2L"
        )
        for (h in habits) {
            if (h.title in defaultTitles) {
                habitDao.deleteHabit(h)
            }
        }
    }

    suspend fun clearAllData() {
        habitDao.deleteAllHabits()
        habitDao.deleteAllLogs()
    }

    // Google & Phone Calendar Integration Helper
    suspend fun importEventsFromDeviceCalendar(context: Context): Int {
        var countImported = 0
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART
            )
            val selection = "${CalendarContract.Events.DELETED} = 0"
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                null,
                "${CalendarContract.Events.DTSTART} DESC LIMIT 15"
            )

            cursor?.use {
                val titleIdx = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descIdx = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val dtStartIdx = it.getColumnIndex(CalendarContract.Events.DTSTART)

                while (it.moveToNext()) {
                    val title = if (titleIdx >= 0) it.getString(titleIdx) else null
                    if (!title.isNullOrBlank() && !title.startsWith("🔥 Routina Habit:")) {
                        val desc = if (descIdx >= 0) it.getString(descIdx) ?: "Diimpor dari Kalender HP" else "Diimpor dari Kalender HP"
                        val dtStart = if (dtStartIdx >= 0) it.getLong(dtStartIdx) else System.currentTimeMillis()
                        val cal = Calendar.getInstance().apply { timeInMillis = dtStart }
                        val hour = cal.get(Calendar.HOUR_OF_DAY)
                        val minute = cal.get(Calendar.MINUTE)
                        val timeOfDayStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

                        val timeCat = when (hour) {
                            in 5..11 -> "Pagi"
                            in 12..17 -> "Siang"
                            else -> "Malam"
                        }

                        val newHabit = HabitEntity(
                            title = title,
                            description = desc,
                            timeCategory = timeCat,
                            timeOfDay = timeOfDayStr,
                            frequencyType = "HARIAN",
                            isGoogleCalendarSynced = true,
                            iconName = "calendar"
                        )
                        habitDao.insertHabit(newHabit)
                        countImported++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return countImported
    }

    fun addToGoogleCalendarIntent(context: Context, habitTitle: String, description: String, timeHHmm: String) {
        try {
            val parts = timeHHmm.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val beginTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            val endTime = (beginTime.clone() as Calendar).apply {
                add(Calendar.MINUTE, 30)
            }

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
                putExtra(CalendarContract.Events.TITLE, "Routina Habit: $habitTitle")
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY")
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Aplikasi Routina")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Share Habit / Schedule via Android Share Sheet
    fun shareHabitsIntent(context: Context, habitList: List<HabitEntity>) {
        val habitText = StringBuilder("Jadwal & Habit Harian Saya di Routina:\n\n")
        habitList.forEachIndexed { index, habit ->
            habitText.append("${index + 1}. [${habit.timeCategory}] ${habit.title} (${habit.timeOfDay})\n")
        }
        habitText.append("\nAyo bangun kebiasaan positif bersama Routina.")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Jadwal Habit Routina Saya")
            putExtra(Intent.EXTRA_TEXT, habitText.toString())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Jadwal Habit Ke..."))
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun formatDateToIndonesian(dateString: String): String {
            return try {
                val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdfInput.parse(dateString) ?: return dateString
                val sdfOutput = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
                sdfOutput.format(date)
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
