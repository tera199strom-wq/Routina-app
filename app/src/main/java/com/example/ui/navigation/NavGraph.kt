package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.HabitEntity
import com.example.data.local.QuestionRouteEntity
import com.example.data.local.ScheduleEntity
import com.example.ui.screens.AboutAppScreen
import com.example.ui.screens.AddEditHabitScreen
import com.example.ui.screens.GoogleAuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MascotConfigurationScreen
import com.example.ui.screens.MascotSettingsScreen
import com.example.ui.screens.MonthlyStatsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuestionnaireScreen
import com.example.ui.screens.ScheduleListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TermsAndConditionsScreen
import com.example.ui.viewmodel.HabitViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val GOOGLE_AUTH = "google_auth"
    const val QUESTIONNAIRE = "questionnaire"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val ADD_EDIT_HABIT = "add_edit_habit"
    const val MONTHLY_STATS = "monthly_stats"
    const val MASCOT = "mascot"
    const val MASCOT_CONFIGURATION = "mascot_configuration"
    const val SETTINGS = "settings"
    const val SCHEDULE_LIST = "schedule_list"
    const val ABOUT_APP = "about_app"
    const val TERMS_CONDITIONS = "terms_conditions"
}

@Composable
fun RoutinaNavGraph(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val logsForToday by viewModel.logsForToday.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val mascotEvents by viewModel.mascotEvents.collectAsStateWithLifecycle()
    val appOpenEvent by viewModel.appOpenEvent.collectAsStateWithLifecycle()
    val leaveStreakEvent by viewModel.leaveStreakEvent.collectAsStateWithLifecycle()
    val hasShownAppOpenGreeting by viewModel.hasShownAppOpenGreeting.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val currentActiveSchedule by viewModel.currentActiveSchedule.collectAsStateWithLifecycle()
    val todayScheduleSelection by viewModel.todayScheduleSelection.collectAsStateWithLifecycle()
    val activeScheduleQuestionEvent by viewModel.activeScheduleQuestionEvent.collectAsStateWithLifecycle()
    val questionRoutes by viewModel.questionRoutes.collectAsStateWithLifecycle()

    var selectedHabitForEdit by remember { mutableStateOf<HabitEntity?>(null) }

    if (!userSettings.isInitialized) {
        // Smooth placeholder screen matching background while preferences load from disk
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    val startDestination = when {
        !userSettings.isOnboardingCompleted -> Routes.ONBOARDING
        !userSettings.isLoggedIn && !userSettings.isGuestMode -> "${Routes.GOOGLE_AUTH}?initialTab=0"
        !userSettings.isQuestionnaireCompleted -> Routes.QUESTIONNAIRE
        else -> Routes.HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateToRegister = {
                    viewModel.completeOnboarding()
                    navController.navigate("${Routes.GOOGLE_AUTH}?initialTab=0") {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    viewModel.completeOnboarding()
                    navController.navigate("${Routes.GOOGLE_AUTH}?initialTab=1") {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onSkip = {
                    viewModel.continueAsGuest()
                    if (!userSettings.isQuestionnaireCompleted) {
                        navController.navigate(Routes.QUESTIONNAIRE) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = "${Routes.GOOGLE_AUTH}?initialTab={initialTab}",
            arguments = listOf(
                navArgument("initialTab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            GoogleAuthScreen(
                initialTab = initialTab,
                webClientId = userSettings.googleClientId,
                supabaseUrl = userSettings.supabaseUrl,
                supabaseAnonKey = userSettings.supabaseAnonKey,
                onGoogleSignInSuccess = { name, email, isNewUser, phone, receiveUpdates ->
                    viewModel.setLoggedIn(name, email, phone, receiveUpdates)
                    if (isNewUser && !userSettings.isQuestionnaireCompleted) {
                        navController.navigate(Routes.QUESTIONNAIRE) {
                            popUpTo(Routes.GOOGLE_AUTH) { inclusive = true }
                        }
                    } else {
                        // User sudah punya akun / login biasa -> langsung masuk ke HOME tanpa kuesioner
                        viewModel.saveQuestionnaireResponse("Google/Login", "Meningkatkan Disiplin")
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.GOOGLE_AUTH) { inclusive = true }
                            }
                        }
                    }
                },
                onSkipOrGuest = {
                    viewModel.continueAsGuest()
                    if (!userSettings.isQuestionnaireCompleted) {
                        navController.navigate(Routes.QUESTIONNAIRE) {
                            popUpTo(Routes.GOOGLE_AUTH) { inclusive = true }
                        }
                    } else if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.GOOGLE_AUTH) { inclusive = true }
                        }
                    }
                },
                onNavigateToTerms = {
                    navController.navigate(Routes.TERMS_CONDITIONS)
                },
                onBackClick = if (userSettings.isQuestionnaireCompleted) {
                    {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.GOOGLE_AUTH) { inclusive = true }
                            }
                        }
                    }
                } else null
            )
        }

        composable(Routes.QUESTIONNAIRE) {
            QuestionnaireScreen(
                onQuestionnaireCompleted = { sourceInfo, usageGoal ->
                    viewModel.saveQuestionnaireResponse(sourceInfo, usageGoal)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.QUESTIONNAIRE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                habits = habits,
                logsForToday = logsForToday,
                allLogs = allLogs,
                isDarkMode = userSettings.isDarkMode,
                userName = userSettings.userName,
                customCategories = userSettings.customCategories,
                schedules = schedules,
                currentActiveSchedule = currentActiveSchedule,
                todayScheduleSelection = todayScheduleSelection,
                activeScheduleQuestionEvent = activeScheduleQuestionEvent,
                appOpenEvent = appOpenEvent,
                hasShownAppOpenGreeting = hasShownAppOpenGreeting,
                mascotName = userSettings.mascotName,
                onAnswerScheduleQuestion = { eventId, answer ->
                    viewModel.answerScheduleQuestion(eventId, answer)
                },
                onSelectScheduleManually = { scheduleId ->
                    viewModel.selectScheduleManually(scheduleId)
                },
                onResetScheduleQuestion = {
                    viewModel.resetScheduleQuestion()
                },
                onNavigateToSchedules = {
                    navController.navigate(Routes.SCHEDULE_LIST)
                },
                onDismissAppOpenGreeting = { viewModel.dismissAppOpenGreeting() },
                onAddCustomCategory = { cat -> viewModel.addCustomCategory(cat) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onToggleHabitComplete = { habitId -> viewModel.toggleHabitComplete(habitId) },
                onAddHabitClick = {
                    selectedHabitForEdit = null
                    navController.navigate(Routes.ADD_EDIT_HABIT)
                },
                onEditHabitClick = { habit ->
                    selectedHabitForEdit = habit
                    navController.navigate(Routes.ADD_EDIT_HABIT)
                },
                onNavigateToStats = {
                    navController.navigate(Routes.MONTHLY_STATS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMascot = {
                    navController.navigate(Routes.MASCOT) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            val activeSchedule = currentActiveSchedule
            val activeScheduleHabits = if (activeSchedule != null) habits.filter { it.scheduleId == null || it.scheduleId == activeSchedule.id } else habits
            val activeHabitIds = activeScheduleHabits.map { it.id }.toSet()
            val filteredLogs = if (activeSchedule != null) allLogs.filter { activeHabitIds.contains(it.habitId) } else allLogs
            val filteredTodayLogs = logsForToday.filter { activeHabitIds.contains(it.habitId) }
            val isSyncing by viewModel.isSyncing.collectAsState()

            ProfileScreen(
                isLoggedIn = userSettings.isLoggedIn,
                userName = userSettings.userName,
                userEmail = userSettings.userEmail,
                userPhone = userSettings.userPhone,
                lastSyncTimeMs = userSettings.lastSyncTimeMs,
                isSyncing = isSyncing,
                habits = activeScheduleHabits,
                completedTodayCount = filteredTodayLogs.size,
                totalLogsCount = filteredLogs.size,
                allLogs = filteredLogs,
                onSyncNow = { viewModel.syncNow() },
                onNavigateToLogin = { navController.navigate(Routes.GOOGLE_AUTH) },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToStats = {
                    navController.navigate(Routes.MONTHLY_STATS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMascot = {
                    navController.navigate(Routes.MASCOT) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    viewModel.logout()
                }
            )
        }

        composable(Routes.MASCOT) {
            MascotSettingsScreen(
                mascotEvents = mascotEvents,
                schedules = schedules,
                questionRoutes = questionRoutes,
                mascotName = userSettings.mascotName,
                onUpdateMascotName = { viewModel.updateMascotName(it) },
                onAddEvent = { eventType, title, desc ->
                    viewModel.addMascotEvent(eventType, title, desc)
                },
                onToggleEvent = { event ->
                    viewModel.toggleMascotEvent(event)
                },
                onUpdateEventSettings = { event, showInNotification, showOnScreenOverlay ->
                    viewModel.updateMascotEventSettings(event, showInNotification, showOnScreenOverlay)
                },
                onDeleteEvent = { event ->
                    viewModel.deleteMascotEvent(event)
                },
                onSaveQuestionRoute = { parentEventId, answer, nextEventId, targetScheduleId ->
                    viewModel.saveQuestionRoute(parentEventId, answer, nextEventId, targetScheduleId)
                },
                onDeleteQuestionRoute = { parentEventId, answer ->
                    viewModel.deleteQuestionRoute(parentEventId, answer)
                },
                onAddMessage = { eventId, text, charName, uri, rot, scale, ox, oy, box, boy, soundUri, soundName, order ->
                    viewModel.addMascotMessage(eventId, text, charName, uri, rot, scale, ox, oy, box, boy, soundUri, soundName, order)
                },
                onUpdateMessage = { message ->
                    viewModel.updateMascotMessage(message)
                },
                onDeleteMessage = { message ->
                    viewModel.deleteMascotMessage(message)
                },
                onMoveMessageUp = { message ->
                    viewModel.moveMessageUp(message)
                },
                onMoveMessageDown = { message ->
                    viewModel.moveMessageDown(message)
                },
                onReorderMessages = { eventId, messages ->
                    viewModel.reorderMessages(eventId, messages)
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToStats = {
                    navController.navigate(Routes.MONTHLY_STATS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Routes.ADD_EDIT_HABIT) {
            AddEditHabitScreen(
                initialHabit = selectedHabitForEdit,
                customCategories = userSettings.customCategories,
                schedules = schedules,
                mascotEvents = mascotEvents,
                isLoggedIn = userSettings.isLoggedIn,
                onAddCustomCategory = { cat -> viewModel.addCustomCategory(cat) },
                onSaveHabit = { habitToSave, syncCalendar ->
                    viewModel.saveHabit(habitToSave, syncCalendar)
                    navController.popBackStack()
                },
                onDeleteHabit = { habitToDelete ->
                    viewModel.deleteHabit(habitToDelete)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToMascotConfiguration = {
                    navController.navigate(Routes.MASCOT_CONFIGURATION)
                }
            )
        }

        composable(Routes.SCHEDULE_LIST) {
            ScheduleListScreen(
                schedules = schedules,
                onAddSchedule = { name, icon, colorHex, isDefault ->
                    viewModel.addSchedule(name, icon, colorHex, isDefault)
                },
                onUpdateSchedule = { schedule ->
                    viewModel.updateSchedule(schedule)
                },
                onDeleteSchedule = { schedule ->
                    viewModel.deleteSchedule(schedule)
                },
                onSetDefaultSchedule = { scheduleId ->
                    viewModel.setDefaultSchedule(scheduleId)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MONTHLY_STATS) {
            val activeSchedule = currentActiveSchedule
            val activeScheduleHabits = if (activeSchedule != null) habits.filter { it.scheduleId == null || it.scheduleId == activeSchedule.id } else habits
            val activeHabitIds = activeScheduleHabits.map { it.id }.toSet()
            val filteredLogs = if (activeSchedule != null) allLogs.filter { activeHabitIds.contains(it.habitId) } else allLogs

            MonthlyStatsScreen(
                habits = activeScheduleHabits,
                logs = filteredLogs,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToStats = { /* already in stats */ },
                onNavigateToMascot = {
                    navController.navigate(Routes.MASCOT) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val isSyncing by viewModel.isSyncing.collectAsState()

            SettingsScreen(
                isLoggedIn = userSettings.isLoggedIn,
                userName = userSettings.userName,
                userEmail = userSettings.userEmail,
                isDarkMode = userSettings.isDarkMode,
                isSyncing = isSyncing,
                lastSyncTimeMs = userSettings.lastSyncTimeMs,
                habits = habits,
                onSyncNow = { viewModel.syncNow() },
                onSendFeedback = { feedbackText ->
                    viewModel.sendFeedback(feedbackText) {}
                },
                onSendTestimonial = { rating, text ->
                    viewModel.sendTestimonial(rating, text) {}
                },
                onAddToGoogleCalendar = { habit ->
                    viewModel.addToGoogleCalendar(context, habit)
                },
                onImportDeviceCalendar = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        viewModel.importCalendarEvents(context) { count ->
                            if (count > 0) {
                                Toast.makeText(context, "$count agenda berhasil diimpor dari Kalender HP.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Tidak ditemukan agenda baru di Kalender HP.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Izinkan akses Kalender di Pengaturan perangkat untuk membaca agenda.", Toast.LENGTH_LONG).show()
                    }
                },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onLogout = {
                    viewModel.logout()
                },
                onNavigateToGoogleAuth = {
                    navController.navigate("${Routes.GOOGLE_AUTH}?initialTab=1")
                },
                onNavigateToAbout = {
                    navController.navigate(Routes.ABOUT_APP)
                },
                onNavigateToTerms = {
                    navController.navigate(Routes.TERMS_CONDITIONS)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT_APP) {
            AboutAppScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.TERMS_CONDITIONS) {
            TermsAndConditionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MASCOT_CONFIGURATION) {
            MascotConfigurationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
