package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.service.MascotFloatingService
import java.util.Calendar

class HabitNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Waktunya Habit Harian!"
        val habitTime = intent.getStringExtra(EXTRA_HABIT_TIME) ?: ""
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)
        val showInNotif = intent.getBooleanExtra(EXTRA_SHOW_IN_NOTIFICATION, true)
        val showOverlay = intent.getBooleanExtra(EXTRA_SHOW_ON_SCREEN_OVERLAY, true)
        val mascotEventId = intent.getLongExtra(EXTRA_LINKED_MASCOT_EVENT_ID, -1L)

        if (showInNotif) {
            showNotification(context, habitTitle, habitTime, minutesBefore)
        }

        if (showOverlay) {
            try {
                MascotFloatingService.start(
                    context = context,
                    eventId = mascotEventId,
                    habitTitle = habitTitle,
                    habitTime = habitTime
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Reschedule for next day if habitId and time are valid
        if (habitId != -1L && habitTime.isNotBlank()) {
            scheduleHabitReminder(context, habitId, habitTitle, habitTime, minutesBefore, showInNotif, showOverlay, if (mascotEventId != -1L) mascotEventId else null)
        }
    }

    private fun showNotification(context: Context, habitTitle: String, time: String, minutesBefore: Int = 0) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "habit_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Habit Routina",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk mengingatkan jadwal habit harian Anda"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mascotBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_onboarding_hero)
        } catch (e: Exception) {
            null
        }

        val reminderMessage = if (minutesBefore > 0) {
            "$minutesBefore menit lagi. Waktunya $habitTitle jam $time. Bersiap-siap ya."
        } else {
            "Waktunya $habitTitle jam $time. Jangan lupa diselesaikan ya."
        }

        val customLayout = RemoteViews(context.packageName, R.layout.custom_notification).apply {
            setTextViewText(R.id.notification_title, "Pesan Maskot: $habitTitle")
            setTextViewText(R.id.notification_message, reminderMessage)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pesan Maskot: $habitTitle")
            .setContentText(reminderMessage)
            .setCustomContentView(customLayout)
            .setCustomBigContentView(customLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setColor(android.graphics.Color.WHITE)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (mascotBitmap != null) {
            notificationBuilder.setLargeIcon(mascotBitmap)
        }

        notificationManager.notify(habitTitle.hashCode(), notificationBuilder.build())
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        const val EXTRA_HABIT_TIME = "extra_habit_time"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val EXTRA_SHOW_IN_NOTIFICATION = "extra_show_in_notification"
        const val EXTRA_SHOW_ON_SCREEN_OVERLAY = "extra_show_on_screen_overlay"
        const val EXTRA_LINKED_MASCOT_EVENT_ID = "extra_linked_mascot_event_id"

        fun triggerImmediateTestNotification(context: Context, title: String = "Minum Air 2 Liter") {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "habit_reminder_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Pengingat Habit Routina",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifikasi pengingat konsistensi harian"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val contentIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val mascotBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.img_onboarding_hero)
            } catch (e: Exception) {
                null
            }

            val reminderMessage = "Waktunya $title sekarang. Jangan lupa diselesaikan ya."

            val customLayout = RemoteViews(context.packageName, R.layout.custom_notification).apply {
                setTextViewText(R.id.notification_title, "Pesan Maskot: $title")
                setTextViewText(R.id.notification_message, reminderMessage)
            }

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Pesan Maskot: $title")
                .setContentText(reminderMessage)
                .setCustomContentView(customLayout)
                .setCustomBigContentView(customLayout)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setColor(android.graphics.Color.WHITE)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            if (mascotBitmap != null) {
                notificationBuilder.setLargeIcon(mascotBitmap)
            }

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notificationBuilder.build())
        }

        fun scheduleHabitReminder(
            context: Context,
            habitId: Long,
            title: String,
            timeHHmm: String,
            minutesBefore: Int = 0,
            showInNotification: Boolean = true,
            showOnScreenOverlay: Boolean = true,
            linkedMascotEventId: Long? = null
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_HABIT_TITLE, title)
                putExtra(EXTRA_HABIT_TIME, timeHHmm)
                putExtra(EXTRA_MINUTES_BEFORE, minutesBefore)
                putExtra(EXTRA_SHOW_IN_NOTIFICATION, showInNotification)
                putExtra(EXTRA_SHOW_ON_SCREEN_OVERLAY, showOnScreenOverlay)
                if (linkedMascotEventId != null) {
                    putExtra(EXTRA_LINKED_MASCOT_EVENT_ID, linkedMascotEventId)
                }
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habitId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                val timeParts = timeHHmm.split(":")
                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (minutesBefore > 0) {
                        add(Calendar.MINUTE, -minutesBefore)
                    }
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancelHabitReminder(context: Context, habitId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HabitNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                habitId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
