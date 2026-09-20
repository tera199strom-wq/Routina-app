package com.example.data.repository

import com.example.data.local.DailyScheduleSelectionDao
import com.example.data.local.DailyScheduleSelectionEntity
import com.example.data.local.ScheduleDao
import com.example.data.local.ScheduleEntity
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(
    private val scheduleDao: ScheduleDao,
    private val dailySelectionDao: DailyScheduleSelectionDao
) {
    val allSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()

    fun getSelectionForDate(date: String): Flow<DailyScheduleSelectionEntity?> {
        return dailySelectionDao.getSelectionForDate(date)
    }

    suspend fun getSelectionForDateSync(date: String): DailyScheduleSelectionEntity? {
        return dailySelectionDao.getSelectionForDateSync(date)
    }

    suspend fun selectScheduleForDate(date: String, scheduleId: Long, isAuto: Boolean = false) {
        dailySelectionDao.insertOrUpdate(
            DailyScheduleSelectionEntity(
                date = date,
                scheduleId = scheduleId,
                isAutoSelected = isAuto,
                selectedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearSelectionForDate(date: String) {
        dailySelectionDao.clearSelectionForDate(date)
    }

    suspend fun getScheduleById(id: Long): ScheduleEntity? {
        return scheduleDao.getScheduleById(id)
    }

    suspend fun getDefaultSchedule(): ScheduleEntity? {
        return scheduleDao.getDefaultSchedule()
    }

    suspend fun addSchedule(schedule: ScheduleEntity): Long {
        val count = scheduleDao.getScheduleCount()
        val toInsert = if (count == 0) schedule.copy(isDefault = true) else schedule
        val id = scheduleDao.insertSchedule(toInsert)
        if (toInsert.isDefault) {
            scheduleDao.setDefaultSchedule(id)
        }
        return id
    }

    suspend fun updateSchedule(schedule: ScheduleEntity) {
        scheduleDao.updateSchedule(schedule)
        if (schedule.isDefault) {
            scheduleDao.setDefaultSchedule(schedule.id)
        }
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }

    suspend fun setDefaultSchedule(scheduleId: Long) {
        scheduleDao.setDefaultSchedule(scheduleId)
    }

    suspend fun seedInitialSchedulesIfEmpty() {
        val allSchedules = scheduleDao.getAllSchedulesList()
        if (allSchedules.isEmpty()) {
            val defaultScheduleId = scheduleDao.insertSchedule(
                ScheduleEntity(
                    name = "Jadwal Default",
                    iconName = "target",
                    colorHex = "#58CC02",
                    isDefault = true
                )
            )
            scheduleDao.setDefaultSchedule(defaultScheduleId)
        } else {
            // Rename legacy "Jadwal Kerja" to "Jadwal Default" and remove seeded "Jadwal Libur" if still in initial pair
            allSchedules.forEach { schedule ->
                if (schedule.name == "Jadwal Kerja") {
                    scheduleDao.updateSchedule(schedule.copy(name = "Jadwal Default", isDefault = true))
                }
                if (schedule.name == "Jadwal Libur" && allSchedules.size == 2 && allSchedules.any { it.name == "Jadwal Kerja" || it.name == "Jadwal Default" }) {
                    scheduleDao.deleteSchedule(schedule)
                }
            }
        }
    }
}
