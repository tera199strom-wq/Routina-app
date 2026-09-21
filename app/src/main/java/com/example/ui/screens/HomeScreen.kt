package com.example.ui.screens

import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.rememberUiScale
import com.example.R
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.repository.HabitRepository
import com.example.ui.components.HabitItemCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.BadgeType
import com.example.ui.components.StreakCounterBadge
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.components.TactileProgressBar
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GrayText
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenShadow
import com.example.ui.theme.NavText

import com.example.util.HabitStatsHelper
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.DailyScheduleSelectionEntity
import com.example.data.local.MascotEventWithMessages
import com.example.data.local.ScheduleEntity
import com.example.ui.components.MascotDialogueOverlay
import com.example.ui.screens.MascotBottomNavBar
import com.example.util.SoundHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    habits: List<HabitEntity>,
    logsForToday: List<HabitLogEntity>,
    allLogs: List<HabitLogEntity> = emptyList(),
    isDarkMode: Boolean,
    userName: String,
    customCategories: List<String> = emptyList(),
    schedules: List<ScheduleEntity> = emptyList(),
    currentActiveSchedule: ScheduleEntity? = null,
    todayScheduleSelection: DailyScheduleSelectionEntity? = null,
    activeScheduleQuestionEvent: MascotEventWithMessages? = null,
    appOpenEvent: MascotEventWithMessages? = null,
    hasShownAppOpenGreeting: Boolean = true,
    mascotName: String = "Karakter",
    onAnswerScheduleQuestion: (Long, String) -> Unit = { _, _ -> },
    onSelectScheduleManually: (Long) -> Unit = {},
    onResetScheduleQuestion: () -> Unit = {},
    onNavigateToSchedules: () -> Unit = {},
    onDismissAppOpenGreeting: () -> Unit = {},
    onAddCustomCategory: (String) -> Unit = {},
    onToggleDarkMode: () -> Unit,
    onToggleHabitComplete: (Long) -> Unit,
    onAddHabitClick: () -> Unit,
    onEditHabitClick: (HabitEntity) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToMascot: () -> Unit = {},
    onNavigateToSyncShare: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotifPermission by remember(context) {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPermission = isGranted
    }

    val hasActiveReminderHabits = remember(habits) {
        habits.any { it.reminderEnabled }
    }

    var selectedTab by remember { mutableStateOf("Umum") }
    var answeredCondition by remember { mutableStateOf<Boolean?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showScheduleSwitcherDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val allTabs = remember(customCategories) {
        val base = listOf("Umum")
        (base + customCategories).distinct()
    }

    val activeQuestionHabit = remember(habits) {
        habits.firstOrNull { !it.questionTrigger.isNullOrBlank() }
    }
    val activeQuestionText = activeQuestionHabit?.questionTrigger

    val todayDateStr = remember { HabitRepository.getTodayDateString() }
    val formattedDate = remember { HabitRepository.formatDateToIndonesian(todayDateStr) }

    val completedHabitIds = remember(logsForToday) {
        logsForToday.map { it.habitId }.toSet()
    }

    // Filter habits by current active schedule (null scheduleId = global/all schedules)
    val scheduleScopedHabits = remember(habits, currentActiveSchedule) {
        if (currentActiveSchedule != null) {
            habits.filter { it.scheduleId == null || it.scheduleId == currentActiveSchedule.id }
        } else {
            habits
        }
    }

    val filteredHabits = remember(scheduleScopedHabits, selectedTab, answeredCondition) {
        val tabFiltered = scheduleScopedHabits.filter {
            it.timeCategory.contains(selectedTab, ignoreCase = true) ||
            (selectedTab.equals("Umum", ignoreCase = true) && (it.timeCategory.isBlank() || it.timeCategory.contains("Umum", ignoreCase = true)))
        }

        when (answeredCondition) {
            true -> tabFiltered.filter { it.activeCondition == "IF_TRUE" || it.activeCondition == "ALL" }
            false -> tabFiltered.filter { it.activeCondition == "IF_FALSE" || it.activeCondition == "ALL" }
            null -> tabFiltered
        }
    }

    val completedCount = scheduleScopedHabits.count { completedHabitIds.contains(it.id) }
    val progressRatio = if (scheduleScopedHabits.isNotEmpty()) completedCount.toFloat() / scheduleScopedHabits.size else 0f
    val currentStreak = remember(allLogs) { HabitStatsHelper.calculateCurrentStreak(allLogs) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val appName = stringResource(id = R.string.app_name)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user_app_icon),
                            contentDescription = "Routina Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = appName.lowercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BlueAccent,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    StreakCounterBadge(
                        streakCount = currentStreak,
                        progress = progressRatio
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Mode Gelap",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            MascotBottomNavBar(
                currentTab = "HOME",
                onNavigateToHome = { /* already home */ },
                onNavigateToStats = onNavigateToStats,
                onNavigateToMascot = onNavigateToMascot,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(60.dp)
                    .testTag("add_habit_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Jadwal Baru",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.background == DarkBackground

            // Notification Permission Banner for existing habits
            if (Build.VERSION.SDK_INT >= 33 && hasActiveReminderHabits && !hasNotifPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) MaterialTheme.colorScheme.surface else Color.White)
                        .border(1.dp, if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable {
                            notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isDark) GreenPrimary else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Izin Notifikasi Belum Aktif",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = "Ketuk untuk mengaktifkan izin agar pengingat dapat muncul di HP kamu",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
            // Target Harian Card with Circular Progress Ring matching Vibrant Palette HTML
            val targetCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFAFBFC)
            val targetCardBorder = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(targetCardBg)
                    .border(1.dp, targetCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Circular Progress Canvas Widget
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color(0xFFE8ECEF)
                        val progressColor = GreenPrimary

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 5.5.dp.toPx()
                            val arcRadius = (size.minDimension - strokeWidth) / 2f
                            drawCircle(
                                color = trackColor,
                                radius = arcRadius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )
                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = progressRatio * 360f,
                                useCenter = false,
                                topLeft = Offset((size.width - arcRadius * 2) / 2, (size.height - arcRadius * 2) / 2),
                                size = Size(arcRadius * 2, arcRadius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Target Harian",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount dari ${scheduleScopedHabits.size} kebiasaan selesai!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayLight
                        )
                    }
                }
            }

            // Multi-Schedule Active Status & Switcher Bar with Current Date
            val todayDateFormatted = remember {
                try {
                    val sdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
                    sdf.format(Date())
                } catch (_: Exception) {
                    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date())
                }
            }

            if (schedules.isNotEmpty()) {
                val scheduleColor = currentActiveSchedule?.colorHex?.let {
                    try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { GreenPrimary }
                } ?: GreenPrimary

                val schedCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFAFBFC)
                val schedCardBorder = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(schedCardBg)
                        .border(1.dp, schedCardBorder, RoundedCornerShape(14.dp))
                        .clickable { showScheduleSwitcherDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(scheduleColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "JADWAL HARI INI • $todayDateFormatted",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GrayLight,
                                    letterSpacing = 0.2.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentActiveSchedule?.name ?: "Semua Kebiasaan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF1F3F5))
                                    .border(1.dp, if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Ganti Jadwal",
                                        tint = scheduleColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Ganti",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = scheduleColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF1F3F5))
                                    .border(1.dp, if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF), RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToSchedules() }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Kelola",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrayLight,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                allTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) GreenPrimary else if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE9ECEF)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else if (isDark) Color(0xFFD0D0D0) else Color(0xFF495057)
                        )
                    }
                }

                // Plus Button to Add Custom Category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE9ECEF))
                        .clickable {
                            newCategoryInput = ""
                            showAddCategoryDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Kategori",
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Kategori",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }
            }

            // Dialog for Adding Custom Category
            if (showAddCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryDialog = false },
                    title = {
                        Text(
                            text = "Tambah Kategori Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Masukkan nama kategori custom (misal: Sore, Olahraga, Belajar, Kerja):",
                                fontSize = 13.sp,
                                color = GrayLight
                            )
                            OutlinedTextField(
                                value = newCategoryInput,
                                onValueChange = { newCategoryInput = it },
                                placeholder = { Text("Nama Kategori") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newCategoryInput.isNotBlank()) {
                                    val cat = newCategoryInput.trim()
                                    onAddCustomCategory(cat)
                                    selectedTab = cat.uppercase()
                                    showAddCategoryDialog = false
                                }
                            }
                        ) {
                            Text("SIMPAN", fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("BATAL", color = GrayLight)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Habit List Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KEBIASAAN HARI INI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                if (answeredCondition != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary.copy(alpha = 0.15f))
                            .clickable { answeredCondition = null }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (answeredCondition == true) "Jawaban: Ya" else "Jawaban: Tidak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Habit List
            if (filteredHabits.isEmpty() && (activeQuestionText == null || answeredCondition != null)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "belum ada jadwal nih",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = GrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_schedule),
                            contentDescription = "belum ada jadwal nih",
                            modifier = Modifier
                                .size(220.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Multi-schedule Mascot Branching Question Card (If Mascot is asking for today's schedule)
                    if (activeScheduleQuestionEvent != null && activeScheduleQuestionEvent.messages.isNotEmpty()) {
                        item(key = "schedule_branching_question_${activeScheduleQuestionEvent.event.id}") {
                            MascotScheduleBranchingQuestionCard(
                                eventWithMessages = activeScheduleQuestionEvent,
                                onAnswer = { answer ->
                                    onAnswerScheduleQuestion(activeScheduleQuestionEvent.event.id, answer)
                                }
                            )
                        }
                    }

                    if (activeQuestionText != null && answeredCondition == null) {
                        item(key = "character_question_card") {
                            CharacterQuestionCard(
                                questionText = activeQuestionText,
                                onAnswerYes = { answeredCondition = true },
                                onAnswerNo = { answeredCondition = false }
                            )
                        }
                    }

                    items(filteredHabits, key = { it.id }) { habit ->
                        val isDone = completedHabitIds.contains(habit.id)
                        HabitItemCard(
                            habit = habit,
                            isCompleted = isDone,
                            onToggleComplete = { onToggleHabitComplete(habit.id) },
                            onCardClick = { onEditHabitClick(habit) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Schedule Switcher Dialog
    if (showScheduleSwitcherDialog) {
        ScheduleSwitcherDialog(
            schedules = schedules,
            currentScheduleId = currentActiveSchedule?.id,
            onSelectSchedule = { scheduleId ->
                onSelectScheduleManually(scheduleId)
                showScheduleSwitcherDialog = false
            },
            onManageSchedules = {
                showScheduleSwitcherDialog = false
                onNavigateToSchedules()
            },
            onDismiss = { showScheduleSwitcherDialog = false }
        )
    }

    // Interactive Mascot Dialogue Overlay for App Open Event (Only triggers ONCE per session on app open)
    if (!hasShownAppOpenGreeting && appOpenEvent != null && appOpenEvent.event.isEnabled && appOpenEvent.messages.isNotEmpty()) {
        MascotDialogueOverlay(
            messages = appOpenEvent.messages,
            characterName = mascotName,
            onDismiss = { onDismissAppOpenGreeting() }
        )
    }
}

@Composable
fun MascotScheduleBranchingQuestionCard(
    eventWithMessages: MascotEventWithMessages,
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = rememberUiScale()
    val context = LocalContext.current
    val messages = eventWithMessages.messages
    if (messages.isEmpty()) return

    var currentMsgIndex by remember(eventWithMessages.event.id) { mutableIntStateOf(0) }
    val message = messages.getOrNull(currentMsgIndex) ?: messages.first()

    // Sound effect on show
    androidx.compose.runtime.LaunchedEffect(message.messageId, message.soundUri) {
        if (!message.soundUri.isNullOrBlank()) {
            SoundHelper.playSound(context, message.soundUri)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, GreenPrimary, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with event title and optional message step selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = eventWithMessages.event.title.ifBlank { "Tanya Jadwal Hari Ini" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary
                )

                if (messages.size > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentMsgIndex == 0) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { currentMsgIndex = 0 }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Pesan 1",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentMsgIndex == 0) Color.White else GrayLight
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentMsgIndex == messages.size - 1) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { currentMsgIndex = messages.size - 1 }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Pertanyaan",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentMsgIndex == messages.size - 1) Color.White else GrayLight
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mascot Custom Avatar/Image
                Box(
                    modifier = Modifier
                        .size((60 * scale).dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.15f))
                        .border(2.dp, GreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!message.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(message.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Maskot Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .rotate(message.rotation)
                                .scale(message.scale)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_onboarding_hero),
                            contentDescription = "Maskot Routina",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .rotate(message.rotation)
                                .scale(message.scale)
                        )
                    }
                }

                Spacer(modifier = Modifier.width((10 * scale).dp))

                // Speech Bubble Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, GreenPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tanya Jadwal",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            if (!message.soundUri.isNullOrBlank()) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Audio Sound",
                                    tint = GreenPrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { SoundHelper.playSound(context, message.soundUri) }
                                )
                            }
                        }
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Tactile3DButtons Row (YA / TIDAK)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Tactile3DButton(
                        text = "YA",
                        onClick = { onAnswer("YA") },
                        type = TactileButtonType.PRIMARY
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Tactile3DButton(
                        text = "TIDAK",
                        onClick = { onAnswer("TIDAK") },
                        type = TactileButtonType.SECONDARY
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleSwitcherDialog(
    schedules: List<ScheduleEntity>,
    currentScheduleId: Long?,
    onSelectSchedule: (Long) -> Unit,
    onManageSchedules: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Pilih Jadwal Hari Ini", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pilih langsung jadwal yang ingin kamu aktifkan untuk hari ini:",
                    fontSize = 13.sp,
                    color = GrayLight
                )

                Spacer(modifier = Modifier.height(4.dp))

                val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.background == DarkBackground

                schedules.forEach { schedule ->
                    val isCurrent = schedule.id == currentScheduleId
                    val scheduleColor = try {
                        Color(android.graphics.Color.parseColor(schedule.colorHex))
                    } catch (_: Exception) {
                        GreenPrimary
                    }

                    val dialogItemBg = if (isCurrent) scheduleColor.copy(alpha = 0.15f) else if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFAFBFC)
                    val dialogItemBorder = if (isCurrent) scheduleColor else if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(dialogItemBg)
                            .border(
                                1.dp,
                                dialogItemBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectSchedule(schedule.id) }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(scheduleColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = schedule.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (schedule.isDefault) {
                                        Text(
                                            text = "Jadwal Utama (Default)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = GrayLight
                                        )
                                    }
                                }
                            }

                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Aktif",
                                    tint = scheduleColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onManageSchedules) {
                Text("KELOLA JADWAL", fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("TUTUP", color = GrayLight)
            }
        }
    )
}

@Composable
fun CharacterQuestionCard(
    questionText: String,
    onAnswerYes: () -> Unit,
    onAnswerNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = rememberUiScale()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mascot Avatar
                Box(
                    modifier = Modifier
                        .size((64 * scale).dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.2f))
                        .border(2.dp, GreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding_hero),
                        contentDescription = "Maskot Routina",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width((12 * scale).dp))

                // Speech Bubble Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, GreenPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Pertanyaan Maskot:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary
                        )
                        Text(
                            text = questionText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Tactile3DButtons Row (YA / TIDAK)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Tactile3DButton(
                        text = "YA",
                        onClick = onAnswerYes,
                        type = TactileButtonType.PRIMARY
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Tactile3DButton(
                        text = "TIDAK",
                        onClick = onAnswerNo,
                        type = TactileButtonType.SECONDARY
                    )
                }
            }
        }
    }
}

