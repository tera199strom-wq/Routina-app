package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.HabitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseAuthResult(
    val isSuccess: Boolean,
    val userId: String? = null,
    val email: String? = null,
    val name: String? = null,
    val token: String? = null,
    val errorMessage: String? = null,
    val needsEmailVerification: Boolean = false
)

class SupabaseRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Default Supabase project credentials (can be overridden via BuildConfig / Secrets)
    private val supabaseUrl = try {
        BuildConfig::class.java.getField("SUPABASE_URL").get(null) as? String
    } catch (e: Exception) {
        null
    } ?: "https://znsrlbftjlsetopcfyyn.supabase.co"

    private val supabaseAnonKey = try {
        BuildConfig::class.java.getField("SUPABASE_ANON_KEY").get(null) as? String
    } catch (e: Exception) {
        null
    } ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpuc3JsYmZ0amxzZXRvcGNmeXluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUzNDgzNzYsImV4cCI6MjEwMDkyNDM3Nn0.EzOC1i92T0FbecnIhOUw1Eqnqf86_8k5BOPbIPT69IE"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        phone: String = "",
        receiveUpdates: Boolean = true,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): SupabaseAuthResult = withContext(Dispatchers.IO) {
        val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
        val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("full_name", name)
                    if (phone.isNotBlank()) {
                        put("phone", phone)
                        put("phone_number", phone)
                    }
                    put("receive_updates", receiveUpdates)
                })
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/auth/v1/signup")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respObj = JSONObject(responseStr)
                    val userObj = respObj.optJSONObject("user") ?: respObj
                    val userId = userObj.optString("id")
                    val userEmail = userObj.optString("email", email)
                    val metadata = userObj.optJSONObject("user_metadata")
                    val userName = metadata?.optString("full_name", name) ?: name
                    val token = respObj.optString("access_token", null).takeIf { !it.isNullOrBlank() }
                    val confirmedAt = userObj.optString("confirmed_at", null).takeIf { !it.isNullOrBlank() }
                    val identities = userObj.optJSONArray("identities")
                    val isRepeatedSignup = identities != null && identities.length() == 0

                    if (isRepeatedSignup) {
                        Log.d("SupabaseRepository", "User already registered (repeated signup detected): $userEmail")
                        return@use SupabaseAuthResult(
                            isSuccess = false,
                            errorMessage = "Email '$userEmail' sudah terdaftar. Silakan beralih ke tab 'Masuk' atau gunakan fitur lupa sandi."
                        )
                    }

                    val isAlreadyConfirmed = token != null || confirmedAt != null

                    Log.d("SupabaseRepository", "User registered successfully: $userEmail, isConfirmed=$isAlreadyConfirmed")
                    SupabaseAuthResult(
                        isSuccess = true,
                        userId = userId,
                        email = userEmail,
                        name = userName,
                        token = token,
                        needsEmailVerification = !isAlreadyConfirmed
                    )
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseStr)
                        errObj.optString("msg", errObj.optString("error_description", "Gagal mendaftarkan akun (HTTP ${response.code})."))
                    } catch (e: Exception) {
                        "Gagal mendaftarkan akun (HTTP ${response.code})."
                    }
                    val friendlyError = when {
                        errorMsg.contains("User already registered", ignoreCase = true) || errorMsg.contains("already exists", ignoreCase = true) ->
                            "Email ini sudah terdaftar. Silakan pindah ke tab 'Masuk' atau gunakan fitur lupa kata sandi."
                        errorMsg.contains("Error sending confirmation", ignoreCase = true) || errorMsg.contains("gomail", ignoreCase = true) || response.code == 500 ->
                            "Gagal mengirimkan kode OTP ke email. Pastikan pengaturan SMTP Brevo di Supabase sudah benar dan tersimpan."
                        else -> errorMsg
                    }
                    SupabaseAuthResult(isSuccess = false, errorMessage = friendlyError)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error sign up Supabase: ${e.message}", e)
            SupabaseAuthResult(
                isSuccess = false,
                errorMessage = "Gagal terhubung ke Supabase: ${e.localizedMessage ?: e.message}. Periksa koneksi internet atau URL Supabase."
            )
        }
    }

    suspend fun signInWithEmail(
        email: String,
        password: String,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): SupabaseAuthResult = withContext(Dispatchers.IO) {
        val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
        val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/auth/v1/token?grant_type=password")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respObj = JSONObject(responseStr)
                    val userObj = respObj.optJSONObject("user")
                    val userId = userObj?.optString("id")
                    val userEmail = userObj?.optString("email", email) ?: email
                    val metadata = userObj?.optJSONObject("user_metadata")
                    val rawName = metadata?.optString("full_name")
                    val userName = if (!rawName.isNullOrBlank()) rawName else {
                        email.substringBefore("@").replace(".", " ").split(" ")
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    }
                    val token = respObj.optString("access_token")
                    Log.d("SupabaseRepository", "User signed in successfully to Supabase Auth: $userEmail")
                    SupabaseAuthResult(
                        isSuccess = true,
                        userId = userId,
                        email = userEmail,
                        name = userName,
                        token = token
                    )
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseStr)
                        errObj.optString("error_description", errObj.optString("msg", ""))
                    } catch (e: Exception) {
                        ""
                    }
                    val friendlyError = if (errorMsg.contains("Invalid login credentials", ignoreCase = true) || response.code == 400) {
                        "Akun belum terdaftar di database Supabase atau kata sandi salah. Jika belum punya akun, silakan daftar di tab 'Daftar Akun' terlebih dahulu."
                    } else if (errorMsg.contains("Email not confirmed", ignoreCase = true)) {
                        "Email belum dikonfirmasi di Supabase. Silakan cek inbox email Anda atau nonaktifkan 'Confirm email' di pengaturan Supabase Auth."
                    } else if (errorMsg.isNotBlank()) {
                        errorMsg
                    } else {
                        "Gagal masuk (HTTP ${response.code})."
                    }
                    SupabaseAuthResult(isSuccess = false, errorMessage = friendlyError)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error sign in Supabase: ${e.message}", e)
            SupabaseAuthResult(
                isSuccess = false,
                errorMessage = "Gagal terhubung ke Supabase: ${e.localizedMessage ?: e.message}. Periksa koneksi internet atau URL Supabase."
            )
        }
    }

    suspend fun verifyOtp(
        email: String,
        token: String,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): SupabaseAuthResult = withContext(Dispatchers.IO) {
        val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
        val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey
        val cleanedToken = token.trim().filter { it.isDigit() || it.isLetter() }
        try {
            // First attempt: type = signup
            val json = JSONObject().apply {
                put("type", "signup")
                put("email", email)
                put("token", cleanedToken)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/auth/v1/verify")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            val firstCallResult = client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respObj = JSONObject(responseStr)
                    val userObj = respObj.optJSONObject("user") ?: respObj
                    val userId = userObj.optString("id")
                    val userEmail = userObj.optString("email", email)
                    val metadata = userObj.optJSONObject("user_metadata")
                    val userName = metadata?.optString("full_name") ?: userEmail.substringBefore("@")
                    val accessToken = respObj.optString("access_token", null)
                    Log.d("SupabaseRepository", "OTP verified successfully (signup): $userEmail")
                    SupabaseAuthResult(
                        isSuccess = true,
                        userId = userId,
                        email = userEmail,
                        name = userName,
                        token = accessToken
                    )
                } else {
                    null
                }
            }

            if (firstCallResult != null) {
                return@withContext firstCallResult
            }

            // Second attempt fallback: type = email
            val fallbackJson = JSONObject().apply {
                put("type", "email")
                put("email", email)
                put("token", cleanedToken)
            }
            val fallbackBody = fallbackJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val fallbackRequest = Request.Builder()
                .url("$url/auth/v1/verify")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(fallbackBody)
                .build()

            client.newCall(fallbackRequest).execute().use { fallbackResp ->
                val fallbackStr = fallbackResp.body?.string() ?: ""
                if (fallbackResp.isSuccessful) {
                    val respObj = JSONObject(fallbackStr)
                    val userObj = respObj.optJSONObject("user") ?: respObj
                    val userId = userObj.optString("id")
                    val userEmail = userObj.optString("email", email)
                    val metadata = userObj.optJSONObject("user_metadata")
                    val userName = metadata?.optString("full_name") ?: userEmail.substringBefore("@")
                    val accessToken = respObj.optString("access_token", null)
                    Log.d("SupabaseRepository", "OTP verified successfully (email fallback): $userEmail")
                    SupabaseAuthResult(
                        isSuccess = true,
                        userId = userId,
                        email = userEmail,
                        name = userName,
                        token = accessToken
                    )
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(fallbackStr)
                        errObj.optString("msg", errObj.optString("error_description", "Kode verifikasi salah atau sudah kedaluwarsa."))
                    } catch (e: Exception) {
                        "Kode verifikasi salah atau sudah kedaluwarsa."
                    }
                    val friendlyError = if (errorMsg.contains("Token has expired or is invalid", ignoreCase = true) || errorMsg.contains("invalid", ignoreCase = true)) {
                        "Kode verifikasi 6-digit salah atau sudah kedaluwarsa. Silakan periksa inbox email Anda atau klik 'Kirim Ulang Kode'."
                    } else {
                        errorMsg
                    }
                    SupabaseAuthResult(isSuccess = false, errorMessage = friendlyError)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error verify OTP: ${e.message}", e)
            SupabaseAuthResult(isSuccess = false, errorMessage = "Gagal memverifikasi kode: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun resendOtp(
        email: String,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
        val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey
        try {
            val json = JSONObject().apply {
                put("type", "signup")
                put("email", email)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/auth/v1/resend")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Kode verifikasi baru berhasil dikirim ke email Anda!")
                } else {
                    val respStr = response.body?.string() ?: ""
                    val err = try {
                        JSONObject(respStr).optString("msg", "Gagal mengirim ulang kode.")
                    } catch (e: Exception) {
                        "Gagal mengirim ulang kode."
                    }
                    Result.failure(Exception(err))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveOnboardingResponse(
        userEmail: String,
        userName: String,
        sourceInfo: String,
        usageGoal: String,
        userPhone: String = "",
        receiveUpdates: Boolean = true,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
            val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey

            val json = JSONObject().apply {
                put("user_email", userEmail)
                put("user_name", userName)
                put("phone_number", userPhone)
                put("receive_updates", receiveUpdates)
                put("source_info", sourceInfo)
                put("usage_goal", usageGoal)
                put("created_at", System.currentTimeMillis())
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)

            // 1. Sync ke tabel users_onboarding (Master User untuk Spreadsheet)
            try {
                val reqUsers = Request.Builder()
                    .url("$url/rest/v1/users_onboarding")
                    .header("apikey", anonKey)
                    .header("Authorization", "Bearer $anonKey")
                    .header("Content-Type", "application/json")
                    .header("Prefer", "resolution=merge-duplicates")
                    .post(body)
                    .build()
                client.newCall(reqUsers).execute().use { }
            } catch (e: Exception) {
                Log.w("SupabaseRepository", "users_onboarding sync: ${e.message}")
            }

            // 2. Sync ke tabel onboarding_responses (Tabel histori respons)
            try {
                val reqLegacy = Request.Builder()
                    .url("$url/rest/v1/onboarding_responses")
                    .header("apikey", anonKey)
                    .header("Authorization", "Bearer $anonKey")
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .post(body)
                    .build()
                client.newCall(reqLegacy).execute().use { }
            } catch (e: Exception) {
                Log.w("SupabaseRepository", "onboarding_responses sync: ${e.message}")
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Error syncing to Supabase: ${e.message}", e)
            Result.success(true)
        }
    }

    suspend fun syncHabit(
        habit: HabitEntity,
        userEmail: String,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
            val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey

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
                put("created_at", habit.createdAtMs)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/rest/v1/habits")
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("SupabaseRepository", "Habit synced to Supabase successfully.")
                    Result.success(true)
                } else {
                    Log.w("SupabaseRepository", "Supabase habit sync HTTP ${response.code}")
                    Result.success(true)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed syncing habit to Supabase: ${e.message}")
            Result.success(true)
        }
    }

    suspend fun submitFeedback(
        userEmail: String,
        userName: String,
        feedbackText: String,
        category: String = "Saran & Kritik",
        customUrl: String? = null,
        customAnonKey: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
        val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey
        try {
            val json = JSONObject().apply {
                put("user_email", userEmail.ifBlank { "anonymous@routina.app" })
                put("user_name", userName.ifBlank { "Pengguna Routina" })
                put("feedback_text", feedbackText.trim())
                put("category", category)
                put("created_at", System.currentTimeMillis())
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/rest/v1/user_feedbacks")
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("SupabaseRepository", "Feedback submitted successfully.")
                    Result.success(true)
                } else {
                    Log.w("SupabaseRepository", "Feedback HTTP error: ${response.code}")
                    Result.success(true)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to submit feedback: ${e.message}")
            Result.success(true)
        }
    }

    suspend fun submitTestimonial(
        userEmail: String,
        userName: String,
        rating: Int,
        testimonyText: String,
        customUrl: String? = null,
        customAnonKey: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val url = customUrl?.takeIf { it.isNotBlank() } ?: supabaseUrl
        val anonKey = customAnonKey?.takeIf { it.isNotBlank() } ?: supabaseAnonKey
        try {
            val json = JSONObject().apply {
                put("user_email", userEmail)
                put("user_name", userName.ifBlank { "Pengguna Routina" })
                put("rating", rating)
                put("testimony_text", testimonyText.trim())
                put("is_public", true)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$url/rest/v1/user_testimonials")
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("SupabaseRepository", "Testimonial submitted successfully.")
                    Result.success(true)
                } else {
                    Log.w("SupabaseRepository", "Testimonial HTTP error: ${response.code}")
                    Result.success(true)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to submit testimonial: ${e.message}")
            Result.success(true)
        }
    }
}
