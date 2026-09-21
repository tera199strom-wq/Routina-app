package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DailyScheduleSelectionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.MascotEventEntity
import com.example.data.local.MascotEventWithMessages
import com.example.data.local.MascotMessageEntity
import com.example.data.local.QuestionRouteEntity
import com.example.data.local.ScheduleEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.data.repository.HabitRepository
import com.example.data.repository.MascotRepository
import com.example.data.repository.QuestionRepository
import com.example.data.repository.ScheduleRepository
import com.example.data.supabase.SupabaseRepository
import com.example.data.supabase.SupabaseSyncService
import com.example.data.supabase.SyncResult
import com.example.receiver.HabitNotificationReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(db.habitDao())
    private val mascotRepository = MascotRepository(db.mascotDao())
    private val scheduleRepository = ScheduleRepository(db.scheduleDao(), db.dailyScheduleSelectionDao())
    private val questionRepository = QuestionRepository(db.questionRouteDao())
    private val userPreferences = UserPreferencesRepository(application)
    private val supabaseRepository = SupabaseRepository()
    private val syncService = SupabaseSyncService(db)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    // Track app open greeting so it ONLY triggers once per app session
    private val _hasShownAppOpenGreeting = MutableStateFlow(false)
    val hasShownAppOpenGreeting: StateFlow<Boolean> = _hasShownAppOpenGreeting.asStateFlow()

    fun dismissAppOpenGreeting() {
        _hasShownAppOpenGreeting.value = true
    }

    val schedules: StateFlow<List<ScheduleEntity>> = scheduleRepository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayScheduleSelection: StateFlow<DailyScheduleSelectionEntity?> =
        scheduleRepository.getSelectionForDate(HabitRepository.getTodayDateString())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val questionRoutes: StateFlow<List<QuestionRouteEntity>> = questionRepository.allRoutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentActiveSchedule: StateFlow<ScheduleEntity?> = combine(
        schedules,
        todayScheduleSelection
    ) { scheduleList, selection ->
        if (selection != null) {
            scheduleList.find { it.id == selection.scheduleId } ?: scheduleList.find { it.isDefault }
        } else {
            scheduleList.find { it.isDefault } ?: scheduleList.firstOrNull()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val scheduleQuestionEvents: StateFlow<List<MascotEventWithMessages>> =
        mascotRepository.getAllEventsWithMessagesByType("SCHEDULE_QUESTION")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _activeScheduleQuestionEvent = MutableStateFlow<MascotEventWithMessages?>(null)
    val activeScheduleQuestionEvent: StateFlow<MascotEventWithMessages?> = _activeScheduleQuestionEvent.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                scheduleQuestionEvents,
                todayScheduleSelection
            ) { questionEvents, selection ->
                if (selection == null && _activeScheduleQuestionEvent.value == null) {
                    val rootQuestion = questionEvents.firstOrNull { it.event.isEnabled && it.messages.isNotEmpty() }
                    _activeScheduleQuestionEvent.value = rootQuestion
                } else if (selection != null) {
                    _activeScheduleQuestionEvent.value = null
                }
            }.collect {}
        }
    }

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val logsForToday: StateFlow<List<HabitLogEntity>> = repository.getLogsForDate(HabitRepository.getTodayDateString())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettings> = userPreferences.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val mascotEvents: StateFlow<List<MascotEventWithMessages>> = mascotRepository.allEventsWithMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appOpenEvent: StateFlow<MascotEventWithMessages?> = mascotRepository.getEventWithMessagesByType("APP_OPEN")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val leaveStreakEvent: StateFlow<MascotEventWithMessages?> = mascotRepository.getEventWithMessagesByType("LEAVE_STREAK")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            repository.seedInitialHabitsIfEmpty()
            scheduleRepository.seedInitialSchedulesIfEmpty()
            mascotRepository.seedInitialMascotEventsIfEmpty()

            // Jika user telah masuk akun sebelumnya, lakukan background sync otomatis
            val settings = userPreferences.userSettings.first()
            if (settings.isLoggedIn && settings.userEmail.isNotBlank()) {
                syncNow()
            }
        }
    }

    fun toggleHabitComplete(habitId: Long) {
        viewModelScope.launch {
            val dateStr = HabitRepository.getTodayDateString()
            val wasNowCompleted = repository.toggleHabitCompletion(habitId, dateStr)

            // Instant sync habit log ke Supabase
            val email = userSettings.value.userEmail
            if (email.isNotBlank()) {
                if (wasNowCompleted) {
                    val log = repository.getHabitLog(habitId, dateStr)
                    if (log != null) {
                        syncService.syncHabitLogInstant(
                            log = log,
                            userEmail = email,
                            customUrl = userSettings.value.supabaseUrl,
                            customAnonKey = userSettings.value.supabaseAnonKey
                        )
                    }
                } else {
                    syncService.deleteHabitLogInstant(
                        habitId = habitId,
                        dateString = dateStr,
                        userEmail = email,
                        customUrl = userSettings.value.supabaseUrl,
                        customAnonKey = userSettings.value.supabaseAnonKey
                    )
                }
            }
        }
    }

    fun saveHabit(habit: HabitEntity, syncGoogleCalendar: Boolean) {
        viewModelScope.launch {
            val id = if (habit.id == 0L) {
                repository.addHabit(habit)
            } else {
                repository.updateHabit(habit)
                habit.id
            }

            // Selalu batalkan alarm lama terlebih dahulu agar idempotent
            HabitNotificationReceiver.cancelHabitReminder(getApplication(), id)

            if (habit.reminderEnabled) {
                HabitNotificationReceiver.scheduleHabitReminder(
                    context = getApplication(),
                    habitId = id,
                    title = habit.title,
                    timeHHmm = habit.timeOfDay,
                    minutesBefore = habit.reminderMinutesBefore,
                    showInNotification = habit.showInNotification,
                    showOnScreenOverlay = habit.showOnScreenOverlay,
                    linkedMascotEventId = habit.linkedMascotEventId
                )
            }

            if (syncGoogleCalendar) {
                repository.addToGoogleCalendarIntent(
                    context = getApplication(),
                    habitTitle = habit.title,
                    description = habit.description,
                    timeHHmm = habit.timeOfDay
                )
            }

            // Sync with Supabase Database
            val email = userSettings.value.userEmail
            if (email.isNotBlank()) {
                syncService.syncHabitInstant(
                    habit = habit.copy(id = id),
                    userEmail = email,
                    customUrl = userSettings.value.supabaseUrl,
                    customAnonKey = userSettings.value.supabaseAnonKey
                )
            }
        }
    }

    fun saveQuestionnaireResponse(sourceInfo: String, usageGoal: String) {
        viewModelScope.launch {
            userPreferences.setQuestionnaireResponse(sourceInfo, usageGoal)
            
            // Sync to Supabase Database
            val user = userSettings.value
            supabaseRepository.saveOnboardingResponse(
                userEmail = user.userEmail,
                userName = user.userName,
                sourceInfo = sourceInfo,
                usageGoal = usageGoal,
                userPhone = user.userPhone,
                receiveUpdates = user.receiveUpdates,
                customUrl = user.supabaseUrl,
                customAnonKey = user.supabaseAnonKey
            )
        }
    }

    fun saveSupabaseConfig(url: String, anonKey: String) {
        viewModelScope.launch {
            userPreferences.setSupabaseConfig(url, anonKey)
        }
    }

    fun saveGoogleClientId(clientId: String) {
        viewModelScope.launch {
            userPreferences.setGoogleClientId(clientId)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            HabitNotificationReceiver.cancelHabitReminder(getApplication(), habit.id)
            repository.deleteHabit(habit)

            val email = userSettings.value.userEmail
            if (email.isNotBlank()) {
                syncService.deleteHabitInstant(
                    habitId = habit.id,
                    userEmail = email,
                    customUrl = userSettings.value.supabaseUrl,
                    customAnonKey = userSettings.value.supabaseAnonKey
                )
            }
        }
    }

    fun addToGoogleCalendar(context: Context, habit: HabitEntity) {
        repository.addToGoogleCalendarIntent(
            context = context,
            habitTitle = habit.title,
            description = habit.description,
            timeHHmm = habit.timeOfDay
        )
    }

    fun importCalendarEvents(context: Context, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importEventsFromDeviceCalendar(context)
            onResult(count)
        }
    }

    fun addCustomCategory(category: String) {
        viewModelScope.launch {
            userPreferences.addCustomCategory(category)
        }
    }

    fun updateMascotName(name: String) {
        viewModelScope.launch {
            userPreferences.setMascotName(name)
        }
    }

    fun sendFeedback(feedbackText: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val email = userSettings.value.userEmail
            val name = userSettings.value.userName
            supabaseRepository.submitFeedback(
                userEmail = email,
                userName = name,
                feedbackText = feedbackText,
                customUrl = userSettings.value.supabaseUrl,
                customAnonKey = userSettings.value.supabaseAnonKey
            )
            onComplete()
        }
    }

    fun sendTestimonial(rating: Int, testimonyText: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val email = userSettings.value.userEmail
            val name = userSettings.value.userName
            supabaseRepository.submitTestimonial(
                userEmail = email,
                userName = name,
                rating = rating,
                testimonyText = testimonyText,
                customUrl = userSettings.value.supabaseUrl,
                customAnonKey = userSettings.value.supabaseAnonKey
            )
            onComplete()
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = userSettings.value.isDarkMode
            userPreferences.setDarkMode(!current)
        }
    }

    fun setLoggedIn(name: String, email: String, phone: String = "", receiveUpdates: Boolean = true) {
        viewModelScope.launch {
            userPreferences.setLoggedIn(
                isLoggedIn = true,
                name = name,
                email = email,
                phone = phone,
                receiveUpdates = receiveUpdates
            )
            userPreferences.setOnboardingCompleted(true)

            // Sinkronisasi data user ke users_onboarding Supabase
            val currentSettings = userSettings.value
            if (email.isNotBlank()) {
                supabaseRepository.saveOnboardingResponse(
                    userEmail = email,
                    userName = name,
                    sourceInfo = if (currentSettings.appSourceInfo.isNotBlank()) currentSettings.appSourceInfo else "Direct Auth",
                    usageGoal = if (currentSettings.appUsageGoal.isNotBlank()) currentSettings.appUsageGoal else "Meningkatkan Disiplin",
                    userPhone = phone,
                    receiveUpdates = receiveUpdates,
                    customUrl = currentSettings.supabaseUrl,
                    customAnonKey = currentSettings.supabaseAnonKey
                )
            }

            // Lakukan sinkronisasi dua arah seketika setelah pengguna berhasil login
            syncNow()
        }
    }

    fun syncNow(onResult: ((Boolean) -> Unit)? = null) {
        val email = userSettings.value.userEmail
        if (email.isBlank()) {
            onResult?.invoke(false)
            return
        }
        if (_isSyncing.value) {
            onResult?.invoke(false)
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            val result = syncService.performFullTwoWaySync(
                userEmail = email,
                customUrl = userSettings.value.supabaseUrl,
                customAnonKey = userSettings.value.supabaseAnonKey
            )
            _isSyncing.value = false
            if (result.isSuccess) {
                _lastSyncResult.value = result.getOrNull()
                userPreferences.updateLastSyncTime()
                onResult?.invoke(true)
            } else {
                onResult?.invoke(false)
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            userPreferences.continueAsGuest()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted(true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUser()
        }
    }

    fun updateSyncTime() {
        viewModelScope.launch {
            userPreferences.updateLastSyncTime()
        }
    }

    fun importHealthyPreset() {
        viewModelScope.launch {
            val presetHabits = listOf(
                HabitEntity(
                    title = "Jalan Kaki 5.000 Langkah",
                    description = "Menjaga stamina dan kesehatan jantung",
                    category = "Kesehatan",
                    timeOfDay = "06:30",
                    timeCategory = "Pagi",
                    reminderEnabled = false,
                    showInNotification = false,
                    iconName = "fitness"
                ),
                HabitEntity(
                    title = "Minum Jus Sayur & Buah",
                    description = "Asupan nutrisi dan asam folat alami",
                    category = "Kesehatan",
                    timeOfDay = "12:30",
                    timeCategory = "Siang",
                    reminderEnabled = false,
                    showInNotification = false,
                    iconName = "water"
                ),
                HabitEntity(
                    title = "Digital Detox 30 Min Sebelum Tidur",
                    description = "Matikan layar ponsel untuk kualitas tidur nyenyak",
                    category = "Mindfulness",
                    timeOfDay = "22:00",
                    timeCategory = "Malam",
                    reminderEnabled = false,
                    showInNotification = false,
                    iconName = "sleep"
                )
            )

            for (h in presetHabits) {
                repository.addHabit(h)
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    // --- MASCOT EVENT & MESSAGE METHODS ---

    fun addMascotEvent(
        eventType: String,
        title: String,
        description: String = "",
        showInNotification: Boolean = true,
        showOnScreenOverlay: Boolean = true
    ) {
        viewModelScope.launch {
            val eventId = mascotRepository.addEvent(
                MascotEventEntity(
                    eventType = eventType,
                    title = title,
                    description = description,
                    isEnabled = true,
                    showInNotification = showInNotification,
                    showOnScreenOverlay = showOnScreenOverlay
                )
            )
            // Add an initial default message for the new event
            mascotRepository.addMessage(
                MascotMessageEntity(
                    eventId = eventId,
                    orderIndex = 0,
                    text = "Halo! Ini pesan pertama untuk event $title.",
                    imageUri = null,
                    soundUri = "preset_meow",
                    soundName = "Meow Kucing",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0f,
                    scale = 1.0f
                )
            )
        }
    }

    fun updateMascotEvent(event: MascotEventEntity) {
        viewModelScope.launch {
            mascotRepository.updateEvent(event)
        }
    }

    fun updateMascotEventSettings(
        event: MascotEventEntity,
        showInNotification: Boolean,
        showOnScreenOverlay: Boolean
    ) {
        viewModelScope.launch {
            mascotRepository.updateEvent(
                event.copy(
                    showInNotification = showInNotification,
                    showOnScreenOverlay = showOnScreenOverlay
                )
            )
        }
    }

    fun toggleMascotEvent(event: MascotEventEntity) {
        viewModelScope.launch {
            mascotRepository.updateEvent(event.copy(isEnabled = !event.isEnabled))
        }
    }

    fun deleteMascotEvent(event: MascotEventEntity) {
        viewModelScope.launch {
            mascotRepository.deleteEvent(event)
        }
    }

    fun addMascotMessage(
        eventId: Long,
        text: String,
        characterName: String = "Karakter",
        imageUri: String?,
        rotation: Float = 0f,
        scale: Float = 1.0f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        bubbleOffsetX: Float = 0f,
        bubbleOffsetY: Float = 0f,
        soundUri: String? = null,
        soundName: String? = null,
        orderIndex: Int = 0
    ) {
        viewModelScope.launch {
            mascotRepository.addMessage(
                MascotMessageEntity(
                    eventId = eventId,
                    orderIndex = orderIndex,
                    text = text,
                    characterName = characterName.ifBlank { "Karakter" },
                    imageUri = imageUri,
                    soundUri = soundUri,
                    soundName = soundName,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    rotation = rotation,
                    scale = scale,
                    bubbleOffsetX = bubbleOffsetX,
                    bubbleOffsetY = bubbleOffsetY
                )
            )
        }
    }

    fun updateMascotMessage(message: MascotMessageEntity) {
        viewModelScope.launch {
            mascotRepository.updateMessage(message)
        }
    }

    fun deleteMascotMessage(message: MascotMessageEntity) {
        viewModelScope.launch {
            mascotRepository.deleteMessage(message)
        }
    }

    fun moveMessageUp(message: MascotMessageEntity) {
        viewModelScope.launch {
            val eventWithMessages = mascotEvents.value.find { it.event.id == message.eventId } ?: return@launch
            val list = eventWithMessages.messages.sortedBy { it.orderIndex }
            val currentIndex = list.indexOfFirst { it.messageId == message.messageId }
            if (currentIndex > 0) {
                val prev = list[currentIndex - 1]
                val updatedList = listOf(
                    message.copy(orderIndex = currentIndex - 1),
                    prev.copy(orderIndex = currentIndex)
                )
                mascotRepository.updateMessagesOrder(updatedList)
            }
        }
    }

    fun moveMessageDown(message: MascotMessageEntity) {
        viewModelScope.launch {
            val eventWithMessages = mascotEvents.value.find { it.event.id == message.eventId } ?: return@launch
            val list = eventWithMessages.messages.sortedBy { it.orderIndex }
            val currentIndex = list.indexOfFirst { it.messageId == message.messageId }
            if (currentIndex in 0 until list.size - 1) {
                val next = list[currentIndex + 1]
                val updatedList = listOf(
                    message.copy(orderIndex = currentIndex + 1),
                    next.copy(orderIndex = currentIndex)
                )
                mascotRepository.updateMessagesOrder(updatedList)
            }
        }
    }

    fun reorderMessages(eventId: Long, messages: List<MascotMessageEntity>) {
        viewModelScope.launch {
            val updated = messages.mapIndexed { idx, msg -> msg.copy(orderIndex = idx) }
            mascotRepository.updateMessagesOrder(updated)
        }
    }

    // --- SCHEDULE & QUESTION ROUTE METHODS ---

    fun selectScheduleForToday(scheduleId: Long, isAuto: Boolean = false) {
        viewModelScope.launch {
            val dateStr = HabitRepository.getTodayDateString()
            scheduleRepository.selectScheduleForDate(dateStr, scheduleId, isAuto)
        }
    }

    fun resetTodayScheduleSelection() {
        viewModelScope.launch {
            val dateStr = HabitRepository.getTodayDateString()
            scheduleRepository.clearSelectionForDate(dateStr)
        }
    }

    fun addSchedule(
        name: String,
        iconName: String = "target",
        colorHex: String = "#58CC02",
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            scheduleRepository.addSchedule(
                ScheduleEntity(
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    isDefault = isDefault
                )
            )
        }
    }

    fun updateSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
            val email = userSettings.value.userEmail
            if (email.isNotBlank()) {
                syncService.deleteScheduleInstant(
                    scheduleId = schedule.id,
                    userEmail = email,
                    customUrl = userSettings.value.supabaseUrl,
                    customAnonKey = userSettings.value.supabaseAnonKey
                )
            }
        }
    }

    fun setDefaultSchedule(scheduleId: Long) {
        viewModelScope.launch {
            scheduleRepository.setDefaultSchedule(scheduleId)
        }
    }

    fun saveQuestionRoute(
        parentEventId: Long,
        answer: String,
        nextEventId: Long?,
        targetScheduleId: Long?
    ) {
        viewModelScope.launch {
            questionRepository.saveRoute(
                parentEventId = parentEventId,
                answer = answer,
                nextEventId = nextEventId,
                targetScheduleId = targetScheduleId
            )
        }
    }

    fun deleteQuestionRoute(route: QuestionRouteEntity) {
        viewModelScope.launch {
            questionRepository.deleteRoute(route)
        }
    }

    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            val id = if (schedule.id == 0L) {
                scheduleRepository.addSchedule(schedule)
            } else {
                scheduleRepository.updateSchedule(schedule)
                schedule.id
            }

            val email = userSettings.value.userEmail
            if (email.isNotBlank()) {
                syncService.syncScheduleInstant(
                    schedule = schedule.copy(id = id),
                    userEmail = email,
                    customUrl = userSettings.value.supabaseUrl,
                    customAnonKey = userSettings.value.supabaseAnonKey
                )
            }
        }
    }

    fun selectScheduleManually(scheduleId: Long) {
        viewModelScope.launch {
            val dateStr = HabitRepository.getTodayDateString()
            scheduleRepository.selectScheduleForDate(dateStr, scheduleId, isAuto = false)
            _activeScheduleQuestionEvent.value = null
        }
    }

    fun resetScheduleQuestion() {
        viewModelScope.launch {
            val dateStr = HabitRepository.getTodayDateString()
            scheduleRepository.clearSelectionForDate(dateStr)
            val rootQuestion = scheduleQuestionEvents.value.firstOrNull { it.event.isEnabled && it.messages.isNotEmpty() }
            _activeScheduleQuestionEvent.value = rootQuestion
        }
    }

    fun deleteQuestionRoute(parentEventId: Long, answer: String) {
        viewModelScope.launch {
            questionRepository.deleteRouteByParentAndAnswer(parentEventId, answer)
        }
    }

    fun answerScheduleQuestion(
        parentEventId: Long,
        answer: String
    ) {
        viewModelScope.launch {
            // Check normalized answer string ("YA" / "YES", "TIDAK" / "NO")
            val normalizedAnswer = when (answer.uppercase()) {
                "YA", "YES" -> "YES"
                else -> "NO"
            }

            var route = questionRepository.getRouteForParentAndAnswer(parentEventId, normalizedAnswer)
            if (route == null) {
                route = questionRepository.getRouteForParentAndAnswer(parentEventId, answer.uppercase())
            }

            if (route != null) {
                if (route.targetScheduleId != null) {
                    val schedule = scheduleRepository.getScheduleById(route.targetScheduleId)
                    if (schedule != null) {
                        selectScheduleForToday(schedule.id, isAuto = true)
                        _activeScheduleQuestionEvent.value = null
                        return@launch
                    }
                } else if (route.nextEventId != null) {
                    val nextEvent = mascotRepository.getEventWithMessagesByIdSync(route.nextEventId)
                    if (nextEvent != null) {
                        _activeScheduleQuestionEvent.value = nextEvent
                        return@launch
                    }
                }
            }

            // Fallback if no explicit route configured for this answer:
            // If answer is YES/YA -> default schedule, if NO/TIDAK -> alternate schedule or default
            val defaultSched = scheduleRepository.getDefaultSchedule()
            val allScheds = schedules.value
            val chosen = if (normalizedAnswer == "YES") {
                defaultSched ?: allScheds.firstOrNull()
            } else {
                allScheds.find { !it.isDefault } ?: defaultSched ?: allScheds.firstOrNull()
            }

            if (chosen != null) {
                selectScheduleForToday(chosen.id, isAuto = true)
            }
            _activeScheduleQuestionEvent.value = null
        }
    }
}
