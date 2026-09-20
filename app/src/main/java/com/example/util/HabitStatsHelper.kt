package com.example.util

import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.repository.HabitRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class HeatmapDay(
    val dateString: String,
    val dayNumber: Int,
    val monthNumber: Int,
    val dayName: String,
    val isCompleted: Boolean,
    val completionCount: Int,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class MonthlyStatsSummary(
    val bestStreak: Int,
    val currentStreak: Int,
    val completionRate: Int,
    val activeDaysCount: Int,
    val totalCompletedCount: Int,
    val monthTitle: String,
    val heatmap30Days: List<HeatmapDay>,
    val currentMonthDays: List<HeatmapDay>
)

object HabitStatsHelper {

    fun parseUtcEpochDay(dateString: String): Long? {
        return try {
            val parts = dateString.split("-")
            if (parts.size != 3) return null
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(y, m - 1, d)
            }
            cal.timeInMillis / (1000L * 60 * 60 * 24)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Menghitung streak terbaik (jumlah hari berturut-turut terpanjang dengan minimal satu habit selesai).
     * Berdasarkan tanggal kalender unik, bukan jumlah log mentah.
     */
    fun calculateBestStreak(logs: List<HabitLogEntity>): Int {
        val activeDays = logs
            .mapNotNull { parseUtcEpochDay(it.dateString) }
            .distinct()
            .sorted()

        if (activeDays.isEmpty()) return 0

        var maxStreak = 1
        var currentRun = 1
        for (i in 1 until activeDays.size) {
            if (activeDays[i] == activeDays[i - 1] + 1L) {
                currentRun++
            } else if (activeDays[i] > activeDays[i - 1] + 1L) {
                currentRun = 1
            }
            if (currentRun > maxStreak) {
                maxStreak = currentRun
            }
        }
        return maxStreak
    }

    /**
     * Menghitung streak saat ini berdasarkan tanggal kalender.
     * Jika hari ini belum ada log tapi kemarin ada, streak tetap berlanjut (belum terputus hari ini).
     */
    fun calculateCurrentStreak(
        logs: List<HabitLogEntity>,
        todayDateString: String = HabitRepository.getTodayDateString()
    ): Int {
        val todayEpoch = parseUtcEpochDay(todayDateString) ?: return 0
        val activeSet = logs
            .mapNotNull { parseUtcEpochDay(it.dateString) }
            .toSet()

        if (activeSet.isEmpty()) return 0

        return if (activeSet.contains(todayEpoch)) {
            var streak = 0
            var day = todayEpoch
            while (activeSet.contains(day)) {
                streak++
                day--
            }
            streak
        } else if (activeSet.contains(todayEpoch - 1L)) {
            var streak = 0
            var day = todayEpoch - 1L
            while (activeSet.contains(day)) {
                streak++
                day--
            }
            streak
        } else {
            0
        }
    }

    /**
     * Menghitung ringkasan statistik bulanan berdasarkan data tanggal asli.
     */
    fun calculateMonthlyStats(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        referenceCalendar: Calendar = Calendar.getInstance()
    ): MonthlyStatsSummary {
        val currentYear = referenceCalendar.get(Calendar.YEAR)
        val currentMonth = referenceCalendar.get(Calendar.MONTH) + 1
        val currentMonthPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth)
        val todayDayOfMonth = referenceCalendar.get(Calendar.DAY_OF_MONTH)
        val monthTitle = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(referenceCalendar.time)

        val monthLogs = logs.filter { it.dateString.startsWith(currentMonthPrefix) }
        val totalCompleted = monthLogs.size
        val activeDaysCount = monthLogs.map { it.dateString }.distinct().size

        val habitsCount = habits.size
        val targetOpportunities = habitsCount * todayDayOfMonth
        val completionRate = if (targetOpportunities > 0 && habitsCount > 0) {
            ((totalCompleted.toFloat() / targetOpportunities) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val bestStreak = calculateBestStreak(logs)
        val currentStreak = calculateCurrentStreak(logs)

        val heatmap30Days = generate30DaysHeatmap(logs, referenceCalendar)
        val currentMonthDays = generateCurrentMonthHeatmap(logs, referenceCalendar)

        return MonthlyStatsSummary(
            bestStreak = bestStreak,
            currentStreak = currentStreak,
            completionRate = completionRate,
            activeDaysCount = activeDaysCount,
            totalCompletedCount = totalCompleted,
            monthTitle = monthTitle,
            heatmap30Days = heatmap30Days,
            currentMonthDays = currentMonthDays
        )
    }

    /**
     * Membangun 30 hari kalender terakhir yang berakhir hari ini.
     * Setiap kotak benar-benar mewakili tanggal kalender nyata.
     */
    fun generate30DaysHeatmap(
        logs: List<HabitLogEntity>,
        referenceCalendar: Calendar = Calendar.getInstance()
    ): List<HeatmapDay> {
        val calCopy = referenceCalendar.clone() as Calendar
        calCopy.add(Calendar.DAY_OF_YEAR, -29)
        val todayStr = HabitRepository.getTodayDateString()
        val dayNames = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        val result = ArrayList<HeatmapDay>(30)

        for (i in 0 until 30) {
            val y = calCopy.get(Calendar.YEAR)
            val m = calCopy.get(Calendar.MONTH) + 1
            val d = calCopy.get(Calendar.DAY_OF_MONTH)
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
            val dayOfWeekIdx = calCopy.get(Calendar.DAY_OF_WEEK) - 1
            val dayName = dayNames.getOrElse(dayOfWeekIdx.coerceIn(0, 6)) { "" }

            val count = logs.count { it.dateString == dateStr }
            result.add(
                HeatmapDay(
                    dateString = dateStr,
                    dayNumber = d,
                    monthNumber = m,
                    dayName = dayName,
                    isCompleted = count > 0,
                    completionCount = count,
                    isToday = (dateStr == todayStr),
                    isFuture = false
                )
            )
            calCopy.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    /**
     * Membangun hari-hari dalam bulan kalender saat ini (28, 29, 30, atau 31 hari).
     */
    fun generateCurrentMonthHeatmap(
        logs: List<HabitLogEntity>,
        referenceCalendar: Calendar = Calendar.getInstance()
    ): List<HeatmapDay> {
        val currentYear = referenceCalendar.get(Calendar.YEAR)
        val currentMonth = referenceCalendar.get(Calendar.MONTH) + 1
        val daysInMonth = referenceCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayDayOfMonth = referenceCalendar.get(Calendar.DAY_OF_MONTH)
        val todayStr = HabitRepository.getTodayDateString()

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
        }
        val dayNames = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        val result = ArrayList<HeatmapDay>(daysInMonth)

        for (d in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth, d)
            val dayOfWeekIdx = cal.get(Calendar.DAY_OF_WEEK) - 1
            val dayName = dayNames.getOrElse(dayOfWeekIdx.coerceIn(0, 6)) { "" }

            val count = logs.count { it.dateString == dateStr }
            result.add(
                HeatmapDay(
                    dateString = dateStr,
                    dayNumber = d,
                    monthNumber = currentMonth,
                    dayName = dayName,
                    isCompleted = count > 0,
                    completionCount = count,
                    isToday = (dateStr == todayStr),
                    isFuture = (d > todayDayOfMonth)
                )
            )
        }
        return result
    }
}
