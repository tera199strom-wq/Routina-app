package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        MascotEventEntity::class,
        MascotMessageEntity::class,
        ScheduleEntity::class,
        QuestionRouteEntity::class,
        DailyScheduleSelectionEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun mascotDao(): MascotDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun questionRouteDao(): QuestionRouteDao
    abstract fun dailyScheduleSelectionDao(): DailyScheduleSelectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routina_habit_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
