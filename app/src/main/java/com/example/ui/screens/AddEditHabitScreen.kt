package com.example.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.HabitEntity
import com.example.data.local.MascotEventWithMessages
import com.example.data.local.ScheduleEntity
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.Tactile3DChip
import com.example.ui.components.Tactile3DIconButton
import com.example.ui.components.Tactile3DSmallButton
import com.example.ui.components.TactileButtonType
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    initialHabit: HabitEntity? = null,
    customCategories: List<String> = emptyList(),
    schedules: List<ScheduleEntity> = emptyList(),
    mascotEvents: List<MascotEventWithMessages> = emptyList(),
    onAddCustomCategory: (String) -> Unit = {},
    onSaveHabit: (HabitEntity, syncGoogleCalendar: Boolean) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Notification Permission Launcher
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                "Notifikasi mungkin tidak muncul tanpa izin ini, kamu bisa aktifkan nanti di Pengaturan HP.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Calendar Permissions Launcher
    var showCalendarAuthDialog by remember { mutableStateOf(false) }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.WRITE_CALENDAR] == true || permissions[Manifest.permission.READ_CALENDAR] == true
        if (isGranted) {
            Toast.makeText(context, "Izin akses Google Calendar berhasil diberikan! ✨", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Izin kalender ditolak. Jadwal tetap disimpan di aplikasi Routina.", Toast.LENGTH_SHORT).show()
        }
    }

    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }

    // Multi-select categories state
    val initialSelectedCategories = remember(initialHabit, customCategories) {
        val raw = initialHabit?.timeCategory
        if (!raw.isNullOrBlank()) {
            raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        } else {
            setOf(customCategories.firstOrNull() ?: "Umum")
        }
    }
    var selectedCategoriesSet by remember { mutableStateOf(initialSelectedCategories) }

    var selectedScheduleId by remember { mutableStateOf<Long?>(initialHabit?.scheduleId) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val allCategoryOptions = remember(customCategories) {
        val base = listOf("Umum")
        (base + customCategories).distinct()
    }

    var timeOfDay by remember { mutableStateOf(initialHabit?.timeOfDay ?: "08:00") }
    var frequencyType by remember { mutableStateOf(initialHabit?.frequencyType ?: "HARIAN") }
    var selectedDays by remember {
        val savedDays = initialHabit?.selectedDaysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet()
        mutableStateOf(savedDays ?: setOf(1, 2, 3, 4, 5, 6, 7))
    }
    var selectedMonthlyDates by remember { mutableStateOf(setOf(1, 15)) }
    var selectedYearlyMonths by remember {
        val raw = initialHabit?.selectedDaysOfWeek
        val months = if (raw?.contains("M:") == true) {
            val part = raw.substringAfter("M:").substringBefore(";")
            part.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else null
        mutableStateOf(if (!months.isNullOrEmpty()) months else setOf(1))
    }
    var selectedYearlyDates by remember {
        val raw = initialHabit?.selectedDaysOfWeek
        val dates = if (raw?.contains(";D:") == true) {
            val part = raw.substringAfter(";D:")
            part.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else null
        mutableStateOf(if (!dates.isNullOrEmpty()) dates else setOf(1))
    }
    var reminderEnabled by remember { mutableStateOf(initialHabit?.reminderEnabled ?: true) }
    var reminderMinutesBefore by remember { mutableStateOf(initialHabit?.reminderMinutesBefore ?: 0) }
    var showInNotification by remember { mutableStateOf(initialHabit?.showInNotification ?: true) }
    var linkedMascotEventId by remember { mutableStateOf<Long?>(initialHabit?.linkedMascotEventId) }
    var isEventDropdownExpanded by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconName ?: "fitness") }
    var syncGoogleCalendar by remember { mutableStateOf(initialHabit?.isGoogleCalendarSynced ?: true) }

    val isEditing = initialHabit != null
    val iconsList = listOf(
        Pair("water", "💧"),
        Pair("fitness", "🏃‍♂️"),
        Pair("book", "📚"),
        Pair("meditate", "🧘"),
        Pair("code", "💻"),
        Pair("sleep", "🌙"),
        Pair("target", "🎯")
    )

    fun checkAndRequestCalendarPermission() {
        val hasWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        if (!hasWrite || !hasRead) {
            showCalendarAuthDialog = true
        }
    }

    fun openTimePicker() {
        val parts = timeOfDay.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            context,
            { _, h, m ->
                val formatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)
                timeOfDay = formatted
            },
            hour,
            minute,
            true
        ).show()
    }

    fun openDatePicker(onDatePicked: (day: Int, month: Int, year: Int) -> Unit) {
        val cal = java.util.Calendar.getInstance()
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH)
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                onDatePicked(selectedDay, selectedMonth + 1, selectedYear)
            },
            y,
            m,
            d
        ).show()
    }

    // Calendar Authorization Dialog
    if (showCalendarAuthDialog) {
        AlertDialog(
            onDismissRequest = { showCalendarAuthDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Izin Google Calendar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Aplikasi Routina memerlukan izin akses Kalender untuk mengekspor jadwal dan menambahkan pengingat agenda ke Google Calendar perangkat Anda secara otomatis.",
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCalendarAuthDialog = false
                        calendarPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            )
                        )
                    }
                ) {
                    Text("IZINKAN & LANJUTKAN", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarAuthDialog = false }) {
                    Text("BATAL", color = GrayLight, fontSize = 13.sp)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Jadwal" else "Tambah Jadwal Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (isEditing && initialHabit != null) {
                        IconButton(onClick = { onDeleteHabit(initialHabit) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Jadwal",
                                tint = RedAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            Text(
                text = "NAMA HABIT & KEBIASAAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayLight,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Contoh: Minum Air 2 Liter", color = GrayLight) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Description Input
            Text(
                text = "DESKRIPSI / CATATAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayLight,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Contoh: 8 gelas sehari untuk menjaga kesehatan", color = GrayLight) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Category Selection (Dropdown Multi-select)
            Text(
                text = "KATEGORI",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayLight,
                letterSpacing = 1.sp
            )

            // Dropdown Selector Box (White background, border radius, click outside/inside to toggle)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayCategories = if (selectedCategoriesSet.isEmpty()) "Pilih Kategori" else selectedCategoriesSet.joinToString(", ")
                        Text(
                            text = displayCategories,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategoriesSet.isEmpty()) GrayLight else Color(0xFF1E293B),
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 2
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih Kategori",
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                ) {
                    allCategoryOptions.forEach { catOption ->
                        val isChecked = selectedCategoriesSet.contains(catOption)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = catOption,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isChecked) GreenPrimary else Color(0xFF1E293B)
                                    )
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val newSet = if (checked) {
                                                selectedCategoriesSet + catOption
                                            } else {
                                                if (selectedCategoriesSet.size > 1) selectedCategoriesSet - catOption else selectedCategoriesSet
                                            }
                                            selectedCategoriesSet = newSet
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GreenPrimary,
                                            checkmarkColor = Color.White
                                        ),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            onClick = {
                                val newSet = if (isChecked) {
                                    if (selectedCategoriesSet.size > 1) selectedCategoriesSet - catOption else selectedCategoriesSet
                                } else {
                                    selectedCategoriesSet + catOption
                                }
                                selectedCategoriesSet = newSet
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tambah Kategori Baru...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GreenPrimary
                                )
                            }
                        },
                        onClick = {
                            isCategoryDropdownExpanded = false
                            newCategoryInput = ""
                            showNewCategoryDialog = true
                        }
                    )
                }
            }

            // Dialog for Adding New Category
            if (showNewCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showNewCategoryDialog = false },
                    title = {
                        Text(
                            text = "Buat Kategori Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Masukkan nama kategori baru untuk mengelompokkan jadwal Anda:",
                                fontSize = 13.sp,
                                color = GrayLight
                            )
                            OutlinedTextField(
                                value = newCategoryInput,
                                onValueChange = { newCategoryInput = it },
                                placeholder = { Text("Contoh: Sore, Olahraga, Belajar") },
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
                                    selectedCategoriesSet = selectedCategoriesSet + cat
                                    showNewCategoryDialog = false
                                }
                            }
                        ) {
                            Text("SIMPAN", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 14.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNewCategoryDialog = false }) {
                            Text("BATAL", color = GrayLight, fontSize = 14.sp)
                        }
                    }
                )
            }

            // WAKTU AREA
            Text(
                text = "WAKTU",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayLight,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable { openTimePicker() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Jam Pelaksanaan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayLight
                        )
                        Text(
                            text = timeOfDay,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Tactile3DIconButton(
                        onClick = { openTimePicker() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Pilih Jam",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        size = 42.dp,
                        type = TactileButtonType.PRIMARY
                    )
                }
            }

            // Frequency & Schedule Selection
            Text(
                text = "FREKUENSI",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayLight,
                letterSpacing = 1.sp
            )

            // Frequency Buttons (3D Tactile style matching app)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("HARIAN", "Harian"),
                    Pair("MINGGUAN", "Mingguan"),
                    Pair("BULANAN", "Bulanan"),
                    Pair("TAHUNAN", "Tahunan")
                ).forEach { (fKey, fLabel) ->
                    val isSelected = frequencyType == fKey
                    Tactile3DChip(
                        selected = isSelected,
                        text = fLabel,
                        onClick = {
                            frequencyType = fKey
                            if (fKey == "HARIAN") {
                                selectedDays = setOf(1, 2, 3, 4, 5, 6, 7)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = 42.dp,
                        fontSize = 13.sp,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Interactive Selector based on Frequency Type
            if (frequencyType == "MINGGUAN" || frequencyType == "HARIAN") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PILIH HARI AKTIF SEMINGGU:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayLight
                    )

                    val daysMap = listOf(
                        Pair(1, "Sen"),
                        Pair(2, "Sel"),
                        Pair(3, "Rab"),
                        Pair(4, "Kam"),
                        Pair(5, "Jum"),
                        Pair(6, "Sab"),
                        Pair(7, "Min")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        daysMap.forEach { (dayInt, dayName) ->
                            val isDaySelected = selectedDays.contains(dayInt)
                            Tactile3DChip(
                                selected = isDaySelected,
                                text = dayName,
                                onClick = {
                                    val newDays = if (isDaySelected) {
                                        if (selectedDays.size > 1) selectedDays - dayInt else selectedDays
                                    } else {
                                        selectedDays + dayInt
                                    }
                                    selectedDays = newDays
                                    frequencyType = if (newDays.size == 7) "HARIAN" else "MINGGUAN"
                                },
                                modifier = Modifier.weight(1f),
                                height = 38.dp,
                                fontSize = 12.sp,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            } else if (frequencyType == "BULANAN") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "JADWAL HABIT BULANAN:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayLight
                    )

                    // 3D DatePicker Button
                    Tactile3DButton(
                        text = "PILIH DARI KALENDER",
                        onClick = {
                            openDatePicker { day, _, _ ->
                                selectedMonthlyDates = selectedMonthlyDates + day
                            }
                        },
                        height = 42.dp,
                        type = TactileButtonType.PRIMARY,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    Text(
                        text = "Pilih Tanggal Pelaksanaan (Bisa Pilih >1):",
                        fontSize = 11.sp,
                        color = GrayLight
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..31).chunked(7).forEach { rowDates ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowDates.forEach { dateNum ->
                                    val isSelected = selectedMonthlyDates.contains(dateNum)
                                    Tactile3DChip(
                                        selected = isSelected,
                                        text = "$dateNum",
                                        onClick = {
                                            selectedMonthlyDates = if (isSelected) {
                                                if (selectedMonthlyDates.size > 1) selectedMonthlyDates - dateNum else selectedMonthlyDates
                                            } else {
                                                selectedMonthlyDates + dateNum
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        height = 36.dp,
                                        fontSize = 12.sp,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                                repeat(7 - rowDates.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Text(
                        text = "Terjadwal pada tanggal: ${selectedMonthlyDates.sorted().joinToString(", ")} setiap bulan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            } else if (frequencyType == "TAHUNAN") {
                val maxDaysForYearlyMonth = selectedYearlyMonths.maxOfOrNull { m ->
                    when (m) {
                        2 -> 29
                        4, 6, 9, 11 -> 30
                        else -> 31
                    }
                } ?: 31

                val validYearlyDates = selectedYearlyDates.filter { it <= maxDaysForYearlyMonth }.toSet()
                if (validYearlyDates.isEmpty()) {
                    selectedYearlyDates = setOf(1)
                } else if (validYearlyDates.size != selectedYearlyDates.size) {
                    selectedYearlyDates = validYearlyDates
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "JADWAL HABIT TAHUNAN:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayLight
                    )

                    // 3D DatePicker Button
                    Tactile3DButton(
                        text = "TAMBAH TANGGAL DARI KALENDER",
                        onClick = {
                            openDatePicker { day, month, _ ->
                                selectedYearlyMonths = selectedYearlyMonths + month
                                val maxD = when (month) {
                                    2 -> 29
                                    4, 6, 9, 11 -> 30
                                    else -> 31
                                }
                                val validDay = if (day > maxD) maxD else day
                                selectedYearlyDates = selectedYearlyDates + validDay
                            }
                        },
                        height = 42.dp,
                        type = TactileButtonType.PRIMARY,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    Text(text = "Pilih Bulan Pelaksanaan (Bisa Pilih >1):", fontSize = 11.sp, color = GrayLight)
                    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        monthNames.take(6).forEachIndexed { index, mName ->
                            val mInt = index + 1
                            val isSelected = selectedYearlyMonths.contains(mInt)
                            Tactile3DChip(
                                selected = isSelected,
                                text = mName,
                                onClick = {
                                    selectedYearlyMonths = if (isSelected) {
                                        if (selectedYearlyMonths.size > 1) selectedYearlyMonths - mInt else selectedYearlyMonths
                                    } else {
                                        selectedYearlyMonths + mInt
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                height = 36.dp,
                                fontSize = 11.sp,
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        monthNames.drop(6).forEachIndexed { index, mName ->
                            val mInt = index + 7
                            val isSelected = selectedYearlyMonths.contains(mInt)
                            Tactile3DChip(
                                selected = isSelected,
                                text = mName,
                                onClick = {
                                    selectedYearlyMonths = if (isSelected) {
                                        if (selectedYearlyMonths.size > 1) selectedYearlyMonths - mInt else selectedYearlyMonths
                                    } else {
                                        selectedYearlyMonths + mInt
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                height = 36.dp,
                                fontSize = 11.sp,
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    Text(text = "Pilih Tanggal Pelaksanaan (Bisa Pilih >1):", fontSize = 11.sp, color = GrayLight)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..maxDaysForYearlyMonth).chunked(7).forEach { rowDates ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowDates.forEach { dNum ->
                                    val isSelected = selectedYearlyDates.contains(dNum)
                                    Tactile3DChip(
                                        selected = isSelected,
                                        text = "$dNum",
                                        onClick = {
                                            selectedYearlyDates = if (isSelected) {
                                                if (selectedYearlyDates.size > 1) selectedYearlyDates - dNum else selectedYearlyDates
                                            } else {
                                                selectedYearlyDates + dNum
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        height = 36.dp,
                                        fontSize = 11.sp,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                                repeat(7 - rowDates.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    val formattedMonths = selectedYearlyMonths.sorted().map { monthNames[it - 1] }.joinToString(", ")
                    Text(
                        text = "Terjadwal tahunan: Tanggal ${selectedYearlyDates.sorted().joinToString(", ")} pada bulan: $formattedMonths",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }

            // Emoji Icon Selection
            Text(
                text = "PILIH IKON HABIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayLight,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                iconsList.forEach { (name, emoji) ->
                    val isSelected = selectedIcon == name
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) GreenPrimary.copy(alpha = 0.2f) else Color.White
                            )
                            .border(
                                2.dp,
                                if (isSelected) GreenPrimary else Color(0xFFE2E8F0),
                                CircleShape
                            )
                            .clickable { selectedIcon = name },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }

            // Notifikasi Pengingat Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Notifikasi Pengingat",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Switch(
                            checked = showInNotification,
                            onCheckedChange = { checked ->
                                showInNotification = checked
                                reminderEnabled = checked
                                if (checked && Build.VERSION.SDK_INT >= 33) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GreenPrimary,
                                checkedBorderColor = GreenPrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = GrayLight.copy(alpha = 0.5f),
                                uncheckedBorderColor = GrayLight.copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (showInNotification) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = Color(0xFFF1F5F9)
                        )

                        // Waktu Pengingat
                        Text(
                            text = "Waktu Pengingat Notifikasi:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        val timingOptions = listOf(
                            0 to "Tepat waktu",
                            5 to "5 mnt sebelum",
                            10 to "10 mnt sebelum",
                            15 to "15 mnt sebelum",
                            30 to "30 mnt sebelum",
                            60 to "1 jam sebelum"
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timingOptions.forEach { (mins, label) ->
                                val isSelected = reminderMinutesBefore == mins
                                Tactile3DChip(
                                    selected = isSelected,
                                    text = label,
                                    onClick = { reminderMinutesBefore = mins },
                                    height = 36.dp,
                                    fontSize = 12.sp,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        // Event Karakter Maskot
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = Color(0xFFF1F5F9)
                        )

                        Text(
                            text = "Gunakan Event Karakter Maskot:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        val selectedEventTitle = mascotEvents.find { it.event.id == linkedMascotEventId }?.event?.title ?: "Bawaan (Pengingat Jadwal)"

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .clickable { isEventDropdownExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedEventTitle,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E293B)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Pilih Event",
                                        tint = Color(0xFF1E293B)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isEventDropdownExpanded,
                                onDismissRequest = { isEventDropdownExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Bawaan (Pengingat Jadwal)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                    onClick = {
                                        linkedMascotEventId = null
                                        isEventDropdownExpanded = false
                                    }
                                )
                                mascotEvents.forEach { mascotEvent ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = mascotEvent.event.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            linkedMascotEventId = mascotEvent.event.id
                                            isEventDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Google Calendar Sync Checkbox & Permission Integration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = syncGoogleCalendar,
                        onCheckedChange = { checked ->
                            syncGoogleCalendar = checked
                            if (checked) {
                                checkAndRequestCalendarPermission()
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GreenPrimary,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.clickable {
                            val newChecked = !syncGoogleCalendar
                            syncGoogleCalendar = newChecked
                            if (newChecked) {
                                checkAndRequestCalendarPermission()
                            }
                        }
                    ) {
                        Text(
                            text = "Tambahkan ke Google Calendar",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Sinkronkan pengingat jadwal langsung ke Google Calendar ponsel",
                            fontSize = 12.sp,
                            color = GrayLight
                        )
                    }
                }
            }

            // Schedule Assignment (Multi-Jadwal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Terapkan Pada Jadwal",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Pilih jadwal khusus atau berlaku di semua jadwal",
                                    fontSize = 12.sp,
                                    color = GrayLight
                                )
                            }
                        }
                    }

                    // Schedule options list / chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Option: Semua Jadwal
                        val isAllSelected = selectedScheduleId == null
                        Tactile3DChip(
                            selected = isAllSelected,
                            text = "Semua Jadwal",
                            onClick = { selectedScheduleId = null },
                            modifier = Modifier.weight(1f),
                            height = 42.dp,
                            fontSize = 12.5.sp,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Options from available schedules
                        schedules.forEach { schedule ->
                            val isSelected = selectedScheduleId == schedule.id
                            val schedColor = remember(schedule.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(schedule.colorHex))
                                } catch (_: Exception) {
                                    BlueAccent
                                }
                            }
                            Tactile3DChip(
                                selected = isSelected,
                                text = schedule.name,
                                onClick = { selectedScheduleId = schedule.id },
                                modifier = Modifier.weight(1f),
                                height = 42.dp,
                                fontSize = 12.5.sp,
                                shape = RoundedCornerShape(8.dp),
                                customSelectedBg = schedColor,
                                customSelectedShadow = schedColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button with enlarged bold text
            Tactile3DButton(
                text = if (isEditing) "SIMPAN PERUBAHAN" else "TAMBAH JADWAL BARU",
                onClick = {
                    if (title.isNotBlank()) {
                        val daysString = when (frequencyType) {
                            "HARIAN", "MINGGUAN" -> selectedDays.sorted().joinToString(",")
                            "BULANAN" -> selectedMonthlyDates.sorted().joinToString(",")
                            "TAHUNAN" -> "M:" + selectedYearlyMonths.sorted().joinToString(",") + ";D:" + selectedYearlyDates.sorted().joinToString(",")
                            else -> selectedDays.sorted().joinToString(",")
                        }

                        val finalCategoryString = if (selectedCategoriesSet.isEmpty()) "Umum" else selectedCategoriesSet.joinToString(", ")

                        val habitToSave = HabitEntity(
                            id = initialHabit?.id ?: 0,
                            title = title.trim(),
                            description = description.trim(),
                            targetDaysPerWeek = if (frequencyType == "MINGGUAN" || frequencyType == "HARIAN") selectedDays.size else 7,
                            timeCategory = finalCategoryString,
                            timeOfDay = timeOfDay,
                            reminderEnabled = reminderEnabled,
                            reminderMinutesBefore = reminderMinutesBefore,
                            showInNotification = showInNotification,
                            showOnScreenOverlay = false,
                            linkedMascotEventId = linkedMascotEventId,
                            iconName = selectedIcon,
                            isGoogleCalendarSynced = syncGoogleCalendar,
                            questionTrigger = null,
                            activeCondition = "ALL",
                            frequencyType = frequencyType,
                            selectedDaysOfWeek = daysString,
                            scheduleId = selectedScheduleId
                        )
                        onSaveHabit(habitToSave, syncGoogleCalendar)
                    }
                },
                type = TactileButtonType.PRIMARY,
                enabled = title.isNotBlank()
            )
        }
    }
}
