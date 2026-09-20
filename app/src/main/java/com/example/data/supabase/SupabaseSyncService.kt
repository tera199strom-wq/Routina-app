package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.DailyScheduleSelectionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.MascotEventEntity
import com.example.data.local.MascotMessageEntity
import com.example.data.local.QuestionRouteEntity
import com.example.data.local.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class SyncResult(
    val isSuccess: Boolean,
    val habitsSynced: Int = 0,
    val logsSynced: Int = 0,
    val schedulesSynced: Int = 0,
    val mascotEventsSynced: Int = 0,
    val errorMessage: String? = null
)

class SupabaseSyncService(
    private val database: AppDatabase
) {
    private val tag = "SupabaseSyncService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val defaultSupabaseUrl = try {
        BuildConfig::class.java.getField("SUPABASE_URL").get(null) as? String
    } catch (e: Exception) {
        null
    } ?: "https://znsrlbftjlsetopcfyyn.supabase.co"

    private val defaultSupabaseAnonKey = try {
        BuildConfig::class.java.getField("SUPABASE_ANON_KEY").get(null) as? String
    } catch (e: Exception) {
        null
    } ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpuc3JsYmZ0amxzZXRvcGNmeXluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUzNDgzNzYsImV4cCI6MjEwMDkyNDM3Nn0.EzOC1i92T0FbecnIhOUw1Eqnqf86_8k5BOPbIPT69IE"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private fun getUrl(customUrl: String?): String =
        customUrl?.takeIf { it.isNotBlank() } ?: defaultSupabaseUrl

    private fun getKey(customAnonKey: String?): String =
        customAnonKey?.takeIf { it.isNotBlank() } ?: defaultSupabaseAnonKey

    /**
     * Melakukan sinkronisasi dua arah secara menyeluruh (Full Two-Way Sync):
     * 1. Mengunduh (Pull) data dari Supabase cloud ke SQLite lokal
     * 2. Mengunggah (Push) data SQLite lokal ke Supabase cloud
     */
    suspend fun performFullTwoWaySync(
        userEmail: String,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email pengguna kosong."))
        }

        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)

        Log.d(tag, "Memulai Full Two-Way Sync untuk akun: $userEmail")

        var habitsCount = 0
        var logsCount = 0
        var schedulesCount = 0
        var mascotCount = 0

        try {
            // 1. SYNC SCHEDULES
            try {
                pullSchedules(url, key, userEmail)
                pushSchedules(url, key, userEmail)
                schedulesCount = database.scheduleDao().getAllSchedulesList().size
            } catch (e: Exception) {
                Log.w(tag, "Warning saat sinkronisasi schedules: ${e.message}")
            }

            // 2. SYNC HABITS
            try {
                pullHabits(url, key, userEmail)
                pushHabits(url, key, userEmail)
                habitsCount = database.habitDao().getAllHabitsList().size
            } catch (e: Exception) {
                Log.w(tag, "Warning saat sinkronisasi habits: ${e.message}")
            }

            // 3. SYNC HABIT LOGS (Riwayat checklist & streak)
            try {
                pullHabitLogs(url, key, userEmail)
                pushHabitLogs(url, key, userEmail)
                logsCount = database.habitDao().getAllLogsList().size
            } catch (e: Exception) {
                Log.w(tag, "Warning saat sinkronisasi habit logs: ${e.message}")
            }

            // 4. SYNC DAILY SCHEDULE SELECTIONS
            try {
                pullDailySelections(url, key, userEmail)
                pushDailySelections(url, key, userEmail)
            } catch (e: Exception) {
                Log.w(tag, "Warning saat sinkronisasi daily selections: ${e.message}")
            }

            // 5. SYNC MASCOT EVENTS & MESSAGES
            try {
                pullMascotEvents(url, key, userEmail)
                pushMascotEvents(url, key, userEmail)
                mascotCount = database.mascotDao().getAllEventsWithMessagesList().size
            } catch (e: Exception) {
                Log.w(tag, "Warning saat sinkronisasi mascot events: ${e.message}")
            }

            // 6. SYNC QUESTION ROUTES
            try {
                pullQuestionRoutes(url, key, userEmail)
                pushQuestionRoutes(url, key, userEmail)
            } catch (e: Exception) {
                Log.w(tag, "Warning saat sinkronisasi question routes: ${e.message}")
            }

            Log.d(tag, "Full Two-Way Sync Berhasil untuk $userEmail! (Habits: $habitsCount, Logs: $logsCount, Schedules: $schedulesCount)")
            Result.success(
                SyncResult(
                    isSuccess = true,
                    habitsSynced = habitsCount,
                    logsSynced = logsCount,
                    schedulesSynced = schedulesCount,
                    mascotEventsSynced = mascotCount
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Full sync error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 1. SCHEDULES SYNC
    // =========================================================================

    private suspend fun pullSchedules(url: String, key: String, userEmail: String) {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val request = Request.Builder()
            .url("$url/rest/v1/schedules?user_email=eq.$encodedEmail")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<ScheduleEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        ScheduleEntity(
                            id = obj.optLong("id"),
                            name = obj.optString("name", "Jadwal"),
                            iconName = obj.optString("icon_name", "calendar"),
                            colorHex = obj.optString("color_hex", "#58CC02"),
                            isDefault = obj.optBoolean("is_default", false),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.scheduleDao().insertAll(list)
                }
            }
        }
    }

    private suspend fun pushSchedules(url: String, key: String, userEmail: String) {
        val localList = database.scheduleDao().getAllSchedulesList()
        if (localList.isEmpty()) return

        val array = JSONArray()
        for (item in localList) {
            array.put(JSONObject().apply {
                put("id", item.id)
                put("user_email", userEmail)
                put("name", item.name)
                put("description", "")
                put("color_hex", item.colorHex)
                put("icon_name", item.iconName)
                put("is_default", item.isDefault)
                put("created_at", item.createdAt)
            })
        }

        val body = array.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$url/rest/v1/schedules")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }

    // =========================================================================
    // 2. HABITS SYNC
    // =========================================================================

    private suspend fun pullHabits(url: String, key: String, userEmail: String) {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val request = Request.Builder()
            .url("$url/rest/v1/habits?user_email=eq.$encodedEmail")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<HabitEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        HabitEntity(
                            id = obj.optLong("id"),
                            title = obj.optString("title", "Kebiasaan"),
                            description = obj.optString("description", ""),
                            timeOfDay = obj.optString("time_of_day", "08:00"),
                            timeCategory = obj.optString("time_category", "Pagi"),
                            iconName = obj.optString("icon_name", "fitness"),
                            colorHex = obj.optString("color_hex", "#58CC02"),
                            reminderEnabled = obj.optBoolean("reminder_enabled", true),
                            scheduleId = if (obj.has("schedule_id") && !obj.isNull("schedule_id")) obj.optLong("schedule_id") else null,
                            createdAtMs = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.habitDao().insertAllHabits(list)
                }
            }
        }
    }

    private suspend fun pushHabits(url: String, key: String, userEmail: String) {
        val localList = database.habitDao().getAllHabitsList()
        if (localList.isEmpty()) return

        val array = JSONArray()
        for (item in localList) {
            array.put(JSONObject().apply {
                put("id", item.id)
                put("user_email", userEmail)
                put("title", item.title)
                put("description", item.description)
                put("time_of_day", item.timeOfDay)
                put("time_category", item.timeCategory)
                put("icon_name", item.iconName)
                put("color_hex", item.colorHex)
                put("reminder_enabled", item.reminderEnabled)
                if (item.scheduleId != null) {
                    put("schedule_id", item.scheduleId)
                } else {
                    put("schedule_id", JSONObject.NULL)
                }
                put("created_at", item.createdAtMs)
            })
        }

        val body = array.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$url/rest/v1/habits")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }

    // =========================================================================
    // 3. HABIT LOGS SYNC
    // =========================================================================

    private suspend fun pullHabitLogs(url: String, key: String, userEmail: String) {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val request = Request.Builder()
            .url("$url/rest/v1/habit_logs?user_email=eq.$encodedEmail")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<HabitLogEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        HabitLogEntity(
                            logId = obj.optLong("id"),
                            habitId = obj.optLong("habit_id"),
                            dateString = obj.optString("date"),
                            completedAtMs = obj.optLong("completed_at_ms", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.habitDao().insertAllLogs(list)
                }
            }
        }
    }

    private suspend fun pushHabitLogs(url: String, key: String, userEmail: String) {
        val localList = database.habitDao().getAllLogsList()
        if (localList.isEmpty()) return

        val array = JSONArray()
        for (item in localList) {
            array.put(JSONObject().apply {
                put("id", item.logId)
                put("user_email", userEmail)
                put("habit_id", item.habitId)
                put("date", item.dateString)
                put("is_completed", true)
                put("completed_at_ms", item.completedAtMs)
            })
        }

        val body = array.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$url/rest/v1/habit_logs")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }

    // =========================================================================
    // 4. DAILY SCHEDULE SELECTIONS SYNC
    // =========================================================================

    private suspend fun pullDailySelections(url: String, key: String, userEmail: String) {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val request = Request.Builder()
            .url("$url/rest/v1/daily_schedule_selections?user_email=eq.$encodedEmail")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<DailyScheduleSelectionEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        DailyScheduleSelectionEntity(
                            date = obj.optString("date"),
                            scheduleId = obj.optLong("schedule_id"),
                            isAutoSelected = obj.optBoolean("is_auto_selected", true),
                            selectedAt = obj.optLong("selected_at", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.dailyScheduleSelectionDao().insertAll(list)
                }
            }
        }
    }

    private suspend fun pushDailySelections(url: String, key: String, userEmail: String) {
        val localList = database.dailyScheduleSelectionDao().getAllSelectionsList()
        if (localList.isEmpty()) return

        val array = JSONArray()
        for (item in localList) {
            val pseudoId = abs(item.date.hashCode().toLong())
            array.put(JSONObject().apply {
                put("id", pseudoId)
                put("user_email", userEmail)
                put("schedule_id", item.scheduleId)
                put("date", item.date)
                put("is_auto_selected", item.isAutoSelected)
                put("selected_at", item.selectedAt)
            })
        }

        val body = array.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$url/rest/v1/daily_schedule_selections")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }

    // =========================================================================
    // 5. MASCOT EVENTS & CUSTOMIZATIONS SYNC
    // =========================================================================

    private suspend fun pullMascotEvents(url: String, key: String, userEmail: String) {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val request = Request.Builder()
            .url("$url/rest/v1/mascot_events?user_email=eq.$encodedEmail")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val events = mutableListOf<MascotEventEntity>()
                val messages = mutableListOf<MascotMessageEntity>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val eventId = obj.optString("id").toLongOrNull() ?: (i + 1).toLong()

                    events.add(
                        MascotEventEntity(
                            id = eventId,
                            eventType = obj.optString("event_type", "APP_OPEN"),
                            title = obj.optString("title", "Pesan Maskot"),
                            description = "",
                            isEnabled = true,
                            createdAtMs = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )

                    messages.add(
                        MascotMessageEntity(
                            messageId = eventId,
                            eventId = eventId,
                            orderIndex = 0,
                            text = obj.optString("message", "Halo!"),
                            characterName = obj.optString("character_name", "Karakter"),
                            imageUri = if (obj.isNull("image_uri")) null else obj.optString("image_uri"),
                            soundUri = if (obj.isNull("sound_uri")) null else obj.optString("sound_uri"),
                            soundName = if (obj.isNull("sound_name")) null else obj.optString("sound_name"),
                            offsetX = obj.optDouble("offset_x", 0.0).toFloat(),
                            offsetY = obj.optDouble("offset_y", 0.0).toFloat(),
                            rotation = obj.optDouble("rotation", 0.0).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            bubbleOffsetX = obj.optDouble("bubble_offset_x", 0.0).toFloat(),
                            bubbleOffsetY = obj.optDouble("bubble_offset_y", 0.0).toFloat()
                        )
                    )
                }

                if (events.isNotEmpty()) {
                    database.mascotDao().insertEvents(events)
                    database.mascotDao().insertMessages(messages)
                }
            }
        }
    }

    private suspend fun pushMascotEvents(url: String, key: String, userEmail: String) {
        val localList = database.mascotDao().getAllEventsWithMessagesList()
        if (localList.isEmpty()) return

        val array = JSONArray()
        for (item in localList) {
            val firstMsg = item.messages.firstOrNull()
            array.put(JSONObject().apply {
                put("id", item.event.id.toString())
                put("user_email", userEmail)
                put("title", item.event.title)
                put("message", firstMsg?.text ?: "")
                put("character_name", firstMsg?.characterName ?: "Karakter")
                put("image_uri", firstMsg?.imageUri ?: JSONObject.NULL)
                put("sound_uri", firstMsg?.soundUri ?: JSONObject.NULL)
                put("sound_name", firstMsg?.soundName ?: JSONObject.NULL)
                put("event_type", item.event.eventType)
                put("rotation", (firstMsg?.rotation ?: 0f).toDouble())
                put("scale", (firstMsg?.scale ?: 1f).toDouble())
                put("offset_x", (firstMsg?.offsetX ?: 0f).toDouble())
                put("offset_y", (firstMsg?.offsetY ?: 0f).toDouble())
                put("bubble_offset_x", (firstMsg?.bubbleOffsetX ?: 0f).toDouble())
                put("bubble_offset_y", (firstMsg?.bubbleOffsetY ?: 0f).toDouble())
                put("created_at", item.event.createdAtMs)
            })
        }

        val body = array.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$url/rest/v1/mascot_events")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }

    // =========================================================================
    // 6. QUESTION ROUTES SYNC
    // =========================================================================

    private suspend fun pullQuestionRoutes(url: String, key: String, userEmail: String) {
        val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
        val request = Request.Builder()
            .url("$url/rest/v1/question_routes?user_email=eq.$encodedEmail")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<QuestionRouteEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        QuestionRouteEntity(
                            id = obj.optLong("id"),
                            parentEventId = obj.optString("parent_event_id").toLongOrNull() ?: 0L,
                            answerCondition = obj.optString("answer_label", "YES"),
                            nextEventId = if (obj.has("next_event_id") && !obj.isNull("next_event_id")) obj.optString("next_event_id").toLongOrNull() else null,
                            targetScheduleId = if (obj.has("target_schedule_id") && !obj.isNull("target_schedule_id")) obj.optLong("target_schedule_id") else null,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    database.questionRouteDao().insertAllRoutes(list)
                }
            }
        }
    }

    private suspend fun pushQuestionRoutes(url: String, key: String, userEmail: String) {
        val localList = database.questionRouteDao().getAllRoutesList()
        if (localList.isEmpty()) return

        val array = JSONArray()
        for (item in localList) {
            array.put(JSONObject().apply {
                put("id", item.id)
                put("user_email", userEmail)
                put("parent_event_id", item.parentEventId.toString())
                put("answer_label", item.answerCondition)
                if (item.nextEventId != null) {
                    put("next_event_id", item.nextEventId.toString())
                } else {
                    put("next_event_id", JSONObject.NULL)
                }
                if (item.targetScheduleId != null) {
                    put("target_schedule_id", item.targetScheduleId)
                } else {
                    put("target_schedule_id", JSONObject.NULL)
                }
                put("action_type", if (item.targetScheduleId != null) "SET_SCHEDULE" else "NEXT_QUESTION")
            })
        }

        val body = array.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$url/rest/v1/question_routes")
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }

    // =========================================================================
    // INSTANT SINGLE-ENTITY MUTATION HELPERS
    // =========================================================================

    suspend fun syncHabitInstant(habit: HabitEntity, userEmail: String, customUrl: String? = null, customAnonKey: String? = null) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)
        try {
            val json = JSONObject().apply {
                put("id", habit.id)
                put("user_email", userEmail)
                put("title", habit.title)
                put("description", habit.description)
                put("time_of_day", habit.timeOfDay)
                put("time_category", habit.timeCategory)
                put("icon_name", habit.iconName)
                put("color_hex", habit.colorHex)
                put("reminder_enabled", habit.reminderEnabled)
                if (habit.scheduleId != null) put("schedule_id", habit.scheduleId) else put("schedule_id", JSONObject.NULL)
                put("created_at", habit.createdAtMs)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/rest/v1/habits")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(tag, "Instant sync habit failed: ${e.message}")
        }
    }

    suspend fun deleteHabitInstant(habitId: Long, userEmail: String, customUrl: String? = null, customAnonKey: String? = null) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)
        try {
            val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
            val request = Request.Builder()
                .url("$url/rest/v1/habits?id=eq.$habitId&user_email=eq.$encodedEmail")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(tag, "Instant delete habit failed: ${e.message}")
        }
    }

    suspend fun syncHabitLogInstant(log: HabitLogEntity, userEmail: String, customUrl: String? = null, customAnonKey: String? = null) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)
        try {
            val json = JSONObject().apply {
                put("id", log.logId)
                put("user_email", userEmail)
                put("habit_id", log.habitId)
                put("date", log.dateString)
                put("is_completed", true)
                put("completed_at_ms", log.completedAtMs)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/rest/v1/habit_logs")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(tag, "Instant sync habit log failed: ${e.message}")
        }
    }

    suspend fun deleteHabitLogInstant(habitId: Long, dateString: String, userEmail: String, customUrl: String? = null, customAnonKey: String? = null) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)
        try {
            val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
            val encodedDate = URLEncoder.encode(dateString, "UTF-8")
            val request = Request.Builder()
                .url("$url/rest/v1/habit_logs?habit_id=eq.$habitId&date=eq.$encodedDate&user_email=eq.$encodedEmail")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(tag, "Instant delete habit log failed: ${e.message}")
        }
    }

    suspend fun syncScheduleInstant(schedule: ScheduleEntity, userEmail: String, customUrl: String? = null, customAnonKey: String? = null) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)
        try {
            val json = JSONObject().apply {
                put("id", schedule.id)
                put("user_email", userEmail)
                put("name", schedule.name)
                put("description", "")
                put("color_hex", schedule.colorHex)
                put("icon_name", schedule.iconName)
                put("is_default", schedule.isDefault)
                put("created_at", schedule.createdAt)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/rest/v1/schedules")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(tag, "Instant sync schedule failed: ${e.message}")
        }
    }

    suspend fun deleteScheduleInstant(scheduleId: Long, userEmail: String, customUrl: String? = null, customAnonKey: String? = null) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        val url = getUrl(customUrl)
        val key = getKey(customAnonKey)
        try {
            val encodedEmail = URLEncoder.encode(userEmail, "UTF-8")
            val request = Request.Builder()
                .url("$url/rest/v1/schedules?id=eq.$scheduleId&user_email=eq.$encodedEmail")
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e(tag, "Instant delete schedule failed: ${e.message}")
        }
    }
}
