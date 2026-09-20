package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.rememberUiScale
import com.example.data.local.ScheduleEntity
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    schedules: List<ScheduleEntity>,
    onAddSchedule: (name: String, iconName: String, colorHex: String, isDefault: Boolean) -> Unit,
    onUpdateSchedule: (ScheduleEntity) -> Unit,
    onDeleteSchedule: (ScheduleEntity) -> Unit,
    onSetDefaultSchedule: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var scheduleToDelete by remember { mutableStateOf<ScheduleEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kelola Multi-Jadwal",
                        fontWeight = FontWeight.Bold,
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
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Bantuan / Note Multi-Jadwal",
                            tint = BlueAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSchedule = null
                    showAddEditDialog = true
                },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Jadwal Baru"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = {
                        Text(
                            text = "Tentang Multi-Jadwal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Setiap jadwal memiliki daftar kebiasaan khusus (misal: Hari Kerja, Libur, Shift). Maskot dapat menanyakan kondisimu di Beranda untuk memilih jadwal secara otomatis!",
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Mengerti", fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                )
            }

            Text(
                text = "DAFTAR JADWAL KAMU (${schedules.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = GrayLight,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = GrayLight
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Jadwal Kustom",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tekan tombol tambah untuk membuat jadwal baru.",
                            fontSize = 13.sp,
                            color = GrayLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleItemCard(
                            schedule = schedule,
                            onEditClick = {
                                editingSchedule = schedule
                                showAddEditDialog = true
                            },
                            onSetDefaultClick = {
                                onSetDefaultSchedule(schedule.id)
                            },
                            onDeleteClick = {
                                scheduleToDelete = schedule
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Schedule Dialog
    if (showAddEditDialog) {
        ScheduleFormDialog(
            initialSchedule = editingSchedule,
            onDismiss = {
                showAddEditDialog = false
                editingSchedule = null
            },
            onSave = { name, icon, colorHex, isDefault ->
                if (editingSchedule != null) {
                    onUpdateSchedule(
                        editingSchedule!!.copy(
                            name = name,
                            iconName = icon,
                            colorHex = colorHex,
                            isDefault = isDefault
                        )
                    )
                } else {
                    onAddSchedule(name, icon, colorHex, isDefault)
                }
                showAddEditDialog = false
                editingSchedule = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = {
                Text(
                    text = "Hapus Jadwal?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Jadwal \"${scheduleToDelete?.name}\" akan dihapus. Habit yang terkait dengan jadwal ini akan tetap tersimpan dan otomatis berlaku di semua jadwal.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scheduleToDelete?.let { onDeleteSchedule(it) }
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("Batal", color = GrayLight)
                }
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    schedule: ScheduleEntity,
    onEditClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scale = rememberUiScale()
    val scheduleColor = remember(schedule.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(schedule.colorHex))
        } catch (_: Exception) {
            GreenPrimary
        }
    }

    val iconVector = getScheduleIconVector(schedule.iconName)
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFBFC)),
        border = BorderStroke(
            if (schedule.isDefault) 2.dp else 1.dp,
            if (schedule.isDefault) GreenPrimary else if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size((48 * scale).dp)
                        .clip(CircleShape)
                        .background(scheduleColor.copy(alpha = 0.15f))
                        .border(1.5.dp, scheduleColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = scheduleColor,
                        modifier = Modifier.size((24 * scale).dp)
                    )
                }

                Spacer(modifier = Modifier.width((14 * scale).dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schedule.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (schedule.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GreenPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEFAULT ⭐",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                    Text(
                        text = if (schedule.isDefault) "Jadwal utama bawaan" else "Jadwal opsional",
                        fontSize = 12.sp,
                        color = GrayLight
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!schedule.isDefault) {
                    IconButton(
                        onClick = onSetDefaultClick,
                        modifier = Modifier.size((36 * scale).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Jadikan Default",
                            tint = GrayLight,
                            modifier = Modifier.size((20 * scale).dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size((36 * scale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Jadwal",
                        tint = BlueAccent,
                        modifier = Modifier.size((20 * scale).dp)
                    )
                }

                if (!schedule.isDefault) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size((36 * scale).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Jadwal",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size((20 * scale).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleFormDialog(
    initialSchedule: ScheduleEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, colorHex: String, isDefault: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialSchedule?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(initialSchedule?.iconName ?: "target") }
    var selectedColorHex by remember { mutableStateOf(initialSchedule?.colorHex ?: "#58CC02") }
    var isDefault by remember { mutableStateOf(initialSchedule?.isDefault ?: false) }

    val iconOptions = listOf(
        "target" to Icons.Default.TrackChanges,
        "work" to Icons.Default.Work,
        "sleep" to Icons.Default.NightsStay,
        "fitness" to Icons.Default.FitnessCenter,
        "book" to Icons.Default.MenuBook,
        "meditate" to Icons.Default.SelfImprovement,
        "water" to Icons.Default.WaterDrop,
        "code" to Icons.Default.Code
    )

    val colorOptions = listOf(
        "#58CC02", // Duolingo Green
        "#1CB0F6", // Blue Accent
        "#FF9600", // Orange
        "#FF4B4B", // Red
        "#CE82FF", // Purple
        "#2B70C9", // Dark Blue
        "#00CD9C"  // Teal
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSchedule != null) "Edit Jadwal" else "Tambah Jadwal Baru",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Jadwal") },
                    placeholder = { Text("Contoh: Jadwal Shift Malam") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "PILIH IKON JADWAL:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.take(4).forEach { (iconKey, vector) ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.5.dp,
                                    if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.drop(4).forEach { (iconKey, vector) ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.5.dp,
                                    if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "PILIH WARNA TEMA:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            GreenPrimary
                        }
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Jadikan Jadwal Utama",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Aktif otomatis jika tidak dijawab",
                            fontSize = 11.sp,
                            color = GrayLight
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedIcon, selectedColorHex, isDefault)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = GrayLight)
            }
        }
    )
}

fun getScheduleIconVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "target" -> Icons.Default.TrackChanges
        "work" -> Icons.Default.Work
        "sleep" -> Icons.Default.NightsStay
        "fitness" -> Icons.Default.FitnessCenter
        "book" -> Icons.Default.MenuBook
        "meditate" -> Icons.Default.SelfImprovement
        "water" -> Icons.Default.WaterDrop
        "code" -> Icons.Default.Code
        else -> Icons.Default.TrackChanges
    }
}
