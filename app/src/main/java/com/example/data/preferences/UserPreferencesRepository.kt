package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "routina_user_prefs")

data class UserSettings(
    val isInitialized: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val userName: String = "Pengguna Routina",
    val userEmail: String = "",
    val userPhone: String = "",
    val receiveUpdates: Boolean = true,
    val userPhotoUrl: String = "",
    val isDarkMode: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isQuestionnaireCompleted: Boolean = false,
    val appSourceInfo: String = "",
    val appUsageGoal: String = "",
    val isCloudSyncEnabled: Boolean = true,
    val lastSyncTimeMs: Long = System.currentTimeMillis(),
    val supabaseUrl: String = "https://znsrlbftjlsetopcfyyn.supabase.co",
    val supabaseAnonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpuc3JsYmZ0amxzZXRvcGNmeXluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUzNDgzNzYsImV4cCI6MjEwMDkyNDM3Nn0.EzOC1i92T0FbecnIhOUw1Eqnqf86_8k5BOPbIPT69IE",
    val googleClientId: String = "302781807275-b99k3ntgl0i3j0dj8rt286d4t55hhknd.apps.googleusercontent.com",
    val customCategories: List<String> = emptyList(),
    val mascotName: String = "Karakter"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val RECEIVE_UPDATES = booleanPreferencesKey("receive_updates")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_QUESTIONNAIRE_COMPLETED = booleanPreferencesKey("is_questionnaire_completed")
        val APP_SOURCE_INFO = stringPreferencesKey("app_source_info")
        val APP_USAGE_GOAL = stringPreferencesKey("app_usage_goal")
        val IS_CLOUD_SYNC_ENABLED = booleanPreferencesKey("is_cloud_sync_enabled")
        val LAST_SYNC_TIME_MS = longPreferencesKey("last_sync_time_ms")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        val GOOGLE_CLIENT_ID = stringPreferencesKey("google_client_id")
        val CUSTOM_CATEGORIES = stringPreferencesKey("custom_categories")
        val MASCOT_NAME = stringPreferencesKey("mascot_name")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val catString = prefs[Keys.CUSTOM_CATEGORIES] ?: ""
        val catList = if (catString.isBlank()) emptyList() else catString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        UserSettings(
            isInitialized = true,
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            isGuestMode = prefs[Keys.IS_GUEST_MODE] ?: false,
            userName = prefs[Keys.USER_NAME] ?: "Pengguna Routina",
            userEmail = prefs[Keys.USER_EMAIL] ?: "",
            userPhone = prefs[Keys.USER_PHONE] ?: "",
            receiveUpdates = prefs[Keys.RECEIVE_UPDATES] ?: true,
            userPhotoUrl = prefs[Keys.USER_PHOTO_URL] ?: "",
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: false,
            isOnboardingCompleted = prefs[Keys.IS_ONBOARDING_COMPLETED] ?: false,
            isQuestionnaireCompleted = prefs[Keys.IS_QUESTIONNAIRE_COMPLETED] ?: false,
            appSourceInfo = prefs[Keys.APP_SOURCE_INFO] ?: "",
            appUsageGoal = prefs[Keys.APP_USAGE_GOAL] ?: "",
            isCloudSyncEnabled = prefs[Keys.IS_CLOUD_SYNC_ENABLED] ?: true,
            lastSyncTimeMs = prefs[Keys.LAST_SYNC_TIME_MS] ?: System.currentTimeMillis(),
            supabaseUrl = prefs[Keys.SUPABASE_URL] ?: "https://znsrlbftjlsetopcfyyn.supabase.co",
            supabaseAnonKey = prefs[Keys.SUPABASE_ANON_KEY] ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpuc3JsYmZ0amxzZXRvcGNmeXluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUzNDgzNzYsImV4cCI6MjEwMDkyNDM3Nn0.EzOC1i92T0FbecnIhOUw1Eqnqf86_8k5BOPbIPT69IE",
            googleClientId = prefs[Keys.GOOGLE_CLIENT_ID] ?: "302781807275-b99k3ntgl0i3j0dj8rt286d4t55hhknd.apps.googleusercontent.com",
            customCategories = catList,
            mascotName = prefs[Keys.MASCOT_NAME] ?: "Karakter"
        )
    }

    suspend fun setMascotName(name: String) {
        val trimmed = name.trim()
        context.dataStore.edit { prefs ->
            prefs[Keys.MASCOT_NAME] = if (trimmed.isBlank()) "Karakter" else trimmed
        }
    }

    suspend fun addCustomCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CUSTOM_CATEGORIES] ?: ""
            val list = if (current.isBlank()) mutableListOf() else current.split(",").map { it.trim() }.toMutableList()
            if (!list.contains(trimmed)) {
                list.add(trimmed)
                prefs[Keys.CUSTOM_CATEGORIES] = list.joinToString(",")
            }
        }
    }

    suspend fun setLoggedIn(
        isLoggedIn: Boolean,
        name: String = "Pengguna Routina",
        email: String = "",
        photoUrl: String = "",
        phone: String = "",
        receiveUpdates: Boolean = true
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = isLoggedIn
            prefs[Keys.IS_GUEST_MODE] = false
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.USER_PHOTO_URL] = photoUrl
            if (phone.isNotBlank()) {
                prefs[Keys.USER_PHONE] = phone.trim()
            }
            prefs[Keys.RECEIVE_UPDATES] = receiveUpdates
        }
    }

    suspend fun updateUserPhone(phone: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_PHONE] = phone.trim()
        }
    }

    suspend fun setReceiveUpdates(receive: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RECEIVE_UPDATES] = receive
        }
    }

    suspend fun continueAsGuest() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs[Keys.IS_GUEST_MODE] = true
            prefs[Keys.USER_NAME] = "Pengguna Routina"
            prefs[Keys.USER_EMAIL] = ""
            prefs[Keys.USER_PHOTO_URL] = ""
            prefs[Keys.IS_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = isDarkMode
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setQuestionnaireResponse(sourceInfo: String, usageGoal: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_QUESTIONNAIRE_COMPLETED] = true
            prefs[Keys.APP_SOURCE_INFO] = sourceInfo
            prefs[Keys.APP_USAGE_GOAL] = usageGoal
        }
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_CLOUD_SYNC_ENABLED] = enabled
        }
    }

    suspend fun updateLastSyncTime() {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME_MS] = System.currentTimeMillis()
        }
    }

    suspend fun setSupabaseConfig(url: String, anonKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUPABASE_URL] = url
            prefs[Keys.SUPABASE_ANON_KEY] = anonKey
        }
    }

    suspend fun setGoogleClientId(clientId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GOOGLE_CLIENT_ID] = clientId
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs[Keys.IS_GUEST_MODE] = false
            prefs[Keys.USER_NAME] = "Pengguna Routina"
            prefs[Keys.USER_EMAIL] = ""
            prefs[Keys.USER_PHOTO_URL] = ""
            prefs[Keys.USER_PHONE] = ""
            prefs[Keys.RECEIVE_UPDATES] = true
        }
    }
}
