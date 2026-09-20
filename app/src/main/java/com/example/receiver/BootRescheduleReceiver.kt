package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeHabits = db.habitDao().getActiveReminderHabitsOnce()
                    activeHabits.forEach { habit ->
                        HabitNotificationReceiver.scheduleHabitReminder(
                            context = context,
                            habitId = habit.id,
                            title = habit.title,
                            timeHHmm = habit.timeOfDay,
                            minutesBefore = habit.reminderMinutesBefore,
                            showInNotification = habit.showInNotification,
                            showOnScreenOverlay = habit.showOnScreenOverlay,
                            linkedMascotEventId = habit.linkedMascotEventId
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
