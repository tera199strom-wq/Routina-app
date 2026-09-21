package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.HabitEntity
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isLoggedIn: Boolean = false,
    userName: String = "",
    userEmail: String = "",
    isDarkMode: Boolean = false,
    isSyncing: Boolean = false,
    lastSyncTimeMs: Long = 0L,
    habits: List<HabitEntity> = emptyList(),
    onSyncNow: () -> Unit = {},
    onSendFeedback: (String) -> Unit = {},
    onSendTestimonial: (Int, String) -> Unit = { _, _ -> },
    onAddToGoogleCalendar: (HabitEntity) -> Unit = {},
    onImportDeviceCalendar: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToGoogleAuth: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Dialog state for setting options
    var showCsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showTestimonyDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    var feedbackText by remember { mutableStateOf("") }
    var testimonyRating by remember { mutableStateOf(5) }
    var testimonyText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan Routina",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali ke Profil"
                        )
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Info Header Card (Clean)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLoggedIn && userName.isNotBlank()) userName else "Pengguna Routina",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoggedIn && userEmail.isNotBlank()) userEmail else "Belum masuk ke akun Google",
                            fontSize = 13.sp,
                            color = GrayLight
                        )
                    }
                }
            }

            // Section 1: Tampilan & Preferensi
            Column {
                Text(
                    text = "TAMPILAN & PREFERENSI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Gelap Toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Mode Gelap (Eye Comfort)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
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
            }

            // Section 2: Integrasi Kalender
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "INTEGRASI KALENDER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                SettingsMenuItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Ekspor ke Google Calendar",
                    onClick = {
                        val topHabit = habits.firstOrNull()
                        if (topHabit != null) {
                            onAddToGoogleCalendar(topHabit)
                        } else {
                            Toast.makeText(context, "Belum ada habit yang dibuat. Buat habit baru terlebih dahulu!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // Section 3: Bantuan, Legal & Informasi
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "INFORMASI & DUKUNGAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                // 1. Syarat & Ketentuan (Terms & Service)
                SettingsMenuItem(
                    icon = Icons.Default.Description,
                    title = "Syarat & Ketentuan (Terms & Service)",
                    onClick = onNavigateToTerms
                )

                // 2. Tentang Aplikasi (About App)
                SettingsMenuItem(
                    icon = Icons.Default.Info,
                    title = "Tentang Aplikasi",
                    onClick = onNavigateToAbout
                )

                // 3. Bantuan & Layanan Pelanggan (Customer Service)
                SettingsMenuItem(
                    icon = Icons.Default.SupportAgent,
                    title = "Bantuan & Layanan Pelanggan",
                    onClick = { showCsDialog = true }
                )

                // 4. Saran & Kritik (Feedback) - Wajib Login agar tahu siapa pengirimnya
                SettingsMenuItem(
                    icon = Icons.Default.Feedback,
                    title = "Saran & Kritik",
                    onClick = {
                        if (isLoggedIn) {
                            showFeedbackDialog = true
                        } else {
                            showLoginRequiredDialog = true
                        }
                    }
                )

                // 5. Testimoni & Ulasan - Wajib Login agar tersimpan atas nama pengguna
                SettingsMenuItem(
                    icon = Icons.Default.Star,
                    title = "Beri Testimoni & Ulasan",
                    onClick = {
                        if (isLoggedIn) {
                            showTestimonyDialog = true
                        } else {
                            showLoginRequiredDialog = true
                        }
                    }
                )
            }

            // Section 4: Akun & Login / Logout
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SESI AKUN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrayLight,
                    letterSpacing = 1.sp
                )

                if (isLoggedIn) {
                    Tactile3DButton(
                        text = "KELUAR DARI AKUN (LOG OUT)",
                        onClick = { showLogoutConfirmDialog = true },
                        type = TactileButtonType.DANGER,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Tactile3DButton(
                        text = "MASUK / DAFTAR AKUN",
                        onClick = onNavigateToGoogleAuth,
                        type = TactileButtonType.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Routina Habit v1.0.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayLight
                )
                Text(
                    text = "Bangun kebiasaan setiap hari",
                    fontSize = 11.sp,
                    color = GrayLight.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    // Modern Gradient Pop-up Dialog Helper (Adapts to Dark Navy & Light)
    val isDark = isSystemInDarkTheme() || isDarkMode || MaterialTheme.colorScheme.background == DarkBackground
    val popupGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                DarkSurface,
                DarkSurfaceVariant
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFF1F5F9)
            )
        )
    }
    val popupBorderColor = if (isDark) DarkBorder else Color(0xFFE2E8F0)
    val popupTextColor = if (isDark) Color.White else Color(0xFF1E293B)
    val popupSubtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)

    // 1. Customer Service Dialog
    if (showCsDialog) {
        Dialog(onDismissRequest = { showCsDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(popupGradient)
                    .border(1.dp, popupBorderColor, RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bantuan Pelanggan",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = popupTextColor
                        )
                        IconButton(
                            onClick = { showCsDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = GrayLight)
                        }
                    }

                    Text(
                        text = "Mengalami kendala atau butuh panduan? Hubungi tim support Routina melalui kanal resmi berikut:",
                        fontSize = 13.sp,
                        color = popupSubtextColor
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) DarkSurfaceVariant else Color.White)
                            .border(1.dp, if (isDark) DarkBorder else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            .clickable {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:qizzytech@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Pertanyaan Bantuan Routina App")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Email CS: qizzytech@gmail.com", Toast.LENGTH_LONG).show()
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = "Email Dukungan:", fontSize = 11.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                            Text(text = "qizzytech@gmail.com", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showCsDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // 4. Feedback Dialog
    if (showFeedbackDialog) {
        Dialog(onDismissRequest = { showFeedbackDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(popupGradient)
                    .border(1.dp, popupBorderColor, RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kirim Saran & Kritik",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = popupTextColor
                        )
                        IconButton(
                            onClick = { showFeedbackDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = GrayLight)
                        }
                    }

                    Text(
                        text = "Kami sangat menghargai masukan Anda untuk membuat Routina semakin bermanfaat:",
                        fontSize = 13.sp,
                        color = popupSubtextColor
                    )

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("Tulis ide, saran fitur, atau kritik kamu di sini...", color = Color(0xFF94A3B8)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showFeedbackDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    onSendFeedback(feedbackText.trim())
                                    Toast.makeText(context, "Terima kasih, kritik & saran telah dikirim ke Supabase!", Toast.LENGTH_SHORT).show()
                                    feedbackText = ""
                                    showFeedbackDialog = false
                                } else {
                                    Toast.makeText(context, "Tuliskan saran terlebih dahulu ya!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Kirim", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 4b. Dialog Testimoni Pengguna
    if (showTestimonyDialog) {
        Dialog(onDismissRequest = { showTestimonyDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(popupGradient)
                    .border(1.dp, popupBorderColor, RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Beri Testimoni Pengguna",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = popupTextColor
                        )
                        IconButton(
                            onClick = { showTestimonyDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = GrayLight)
                        }
                    }

                    Text(
                        text = "Bagikan pengalamanmu menggunakan Routina untuk membangun kebiasaan produktif:",
                        fontSize = 13.sp,
                        color = popupSubtextColor
                    )

                    // Bintang Rating Interaktif (1 - 5)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { testimonyRating = star },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star Bintang",
                                    tint = if (star <= testimonyRating) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = testimonyText,
                        onValueChange = { testimonyText = it },
                        label = { Text("Ulasan / Testimoni") },
                        placeholder = { Text("Tulis pengalaman berhargamu dengan Routina di sini...", color = Color(0xFF94A3B8)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showTestimonyDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (testimonyText.isNotBlank()) {
                                    onSendTestimonial(testimonyRating, testimonyText.trim())
                                    Toast.makeText(context, "Terima kasih! Testimoni Anda telah dikirim ke database.", Toast.LENGTH_SHORT).show()
                                    testimonyText = ""
                                    testimonyRating = 5
                                    showTestimonyDialog = false
                                } else {
                                    Toast.makeText(context, "Tuliskan sedikit testimoni pengalamanmu ya!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text("Kirim Testimoni", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 5. Dialog Harus Login Terlebih Dahulu Sebelum Kirim Kritik/Saran
    if (showLoginRequiredDialog) {
        Dialog(onDismissRequest = { showLoginRequiredDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(popupGradient)
                    .border(1.dp, popupBorderColor, RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Masuk Terlebih Dahulu",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = popupTextColor
                    )
                    Text(
                        text = "Untuk mengirimkan saran & kritik, silakan masuk ke akun Anda terlebih dahulu agar tim pengembang dapat mengetahui pengirim dan menindaklanjuti masukan Anda.",
                        fontSize = 13.sp,
                        color = popupSubtextColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showLoginRequiredDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                        }
                        Button(
                            onClick = {
                                showLoginRequiredDialog = false
                                onNavigateToGoogleAuth()
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Masuk / Daftar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 6. Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        Dialog(onDismissRequest = { showLogoutConfirmDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(popupGradient)
                    .border(1.dp, popupBorderColor, RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Keluar dari Akun?",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = popupTextColor
                    )
                    Text(
                        text = "Anda akan keluar dari sesi akun ini. Semua data kebiasaan dan jadwal Anda tetap aman tersimpan di cloud Supabase dan perangkat ini.",
                        fontSize = 13.sp,
                        color = popupSubtextColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showLogoutConfirmDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                        }
                        Button(
                            onClick = {
                                showLogoutConfirmDialog = false
                                onLogout()
                                Toast.makeText(context, "Berhasil keluar dari akun.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Ya, Keluar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrayLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
