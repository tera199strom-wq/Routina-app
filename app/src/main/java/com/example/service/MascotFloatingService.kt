package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.MascotMessageEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.components.MascotDialogueContent
import com.example.ui.theme.RoutinaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

data class MascotTriggerData(
    val eventId: Long = -1L,
    val habitTitle: String = "",
    val habitTime: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class MascotFloatingService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val CHANNEL_ID = "routina_mascot_overlay_channel"
        const val NOTIFICATION_ID = 9921
        const val ACTION_START = "ACTION_START_MASCOT"
        const val ACTION_STOP = "ACTION_STOP_MASCOT"

        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        const val EXTRA_HABIT_TIME = "extra_habit_time"

        private val _currentTrigger = MutableStateFlow<MascotTriggerData?>(null)
        val currentTrigger = _currentTrigger.asStateFlow()

        var isRunning = false
            private set

        fun start(context: Context, eventId: Long = -1L, habitTitle: String = "", habitTime: String = "") {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, MascotFloatingService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_EVENT_ID, eventId)
                    putExtra(EXTRA_HABIT_TITLE, habitTitle)
                    putExtra(EXTRA_HABIT_TIME, habitTime)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MascotFloatingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        isRunning = true

        val eventId = intent?.getLongExtra(EXTRA_EVENT_ID, -1L) ?: -1L
        val habitTitle = intent?.getStringExtra(EXTRA_HABIT_TITLE) ?: ""
        val habitTime = intent?.getStringExtra(EXTRA_HABIT_TIME) ?: ""

        _currentTrigger.value = MascotTriggerData(
            eventId = eventId,
            habitTitle = habitTitle,
            habitTime = habitTime,
            timestamp = System.currentTimeMillis()
        )

        setupFloatingView()

        return START_STICKY
    }

    private fun setupFloatingView() {
        if (floatingView != null) return

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MascotFloatingService)
            setViewTreeSavedStateRegistryOwner(this@MascotFloatingService)

            setContent {
                RoutinaTheme {
                    val trigger by currentTrigger.collectAsState()
                    val context = LocalContext.current
                    val db = remember { AppDatabase.getDatabase(context) }
                    val userPrefs = remember { UserPreferencesRepository(context) }
                    val userSettings by userPrefs.userSettings.collectAsState(initial = null)
                    var messages by remember { mutableStateOf<List<MascotMessageEntity>>(emptyList()) }

                    LaunchedEffect(trigger) {
                        val current = trigger
                        val eventId = current?.eventId ?: -1L
                        val habitTitle = current?.habitTitle ?: ""
                        val habitTime = current?.habitTime ?: ""

                        if (eventId > 0) {
                            val eventWithMessages = db.mascotDao().getEventWithMessagesById(eventId).firstOrNull()
                            if (eventWithMessages != null && eventWithMessages.messages.isNotEmpty()) {
                                messages = eventWithMessages.messages
                                return@LaunchedEffect
                            }
                        }

                        if (habitTitle.isNotBlank()) {
                            val timeText = if (habitTime.isNotBlank()) " jam $habitTime" else ""
                            messages = listOf(
                                MascotMessageEntity(
                                    eventId = 1,
                                    text = "Waktunya $habitTitle$timeText. Yuk selesaikan habit ini sekarang.",
                                    orderIndex = 0
                                ),
                                MascotMessageEntity(
                                    eventId = 1,
                                    text = "Konsistensi kecil setiap hari akan membawa hasil luar biasa.",
                                    orderIndex = 1
                                )
                            )
                            return@LaunchedEffect
                        }

                        // Fallback default messages (e.g. APP_OPEN)
                        val eventWithMessages = db.mascotDao().getEventWithMessagesByType("APP_OPEN").firstOrNull()
                        if (eventWithMessages != null && eventWithMessages.messages.isNotEmpty()) {
                            messages = eventWithMessages.messages
                        } else {
                            messages = listOf(
                                MascotMessageEntity(
                                    eventId = 1,
                                    text = "Semangat harimu! Selesaikan kebiasaanmu hari ini.",
                                    orderIndex = 0
                                )
                            )
                        }
                    }

                    if (messages.isNotEmpty()) {
                        MascotDialogueContent(
                            messages = messages,
                            characterName = userSettings?.mascotName ?: "Karakter",
                            onDismiss = { stopSelf() }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Karakter Maskot Routina Aktif")
            .setContentText("Karakter muncul di layar untuk mengingatkan jadwal kebiasaan.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Maskot Melayang Routina",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status karakter melayang di luar aplikasi"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            floatingView = null
        }
    }
}
