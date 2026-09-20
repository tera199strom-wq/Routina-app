package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onBackClick: () -> Unit,
    onAcceptClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Syarat & Ketentuan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.5.dp, GreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ketentuan Penggunaan & Privasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "Terakhir diperbarui: September 2026",
                                fontSize = 12.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                Text(
                    text = "Selamat datang di Routina. Dengan mengakses, mendaftar, atau menggunakan aplikasi Routina, Anda menyetujui syarat dan ketentuan penggunaan layanan berikut:",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )

                // Section 1: Privasi & Keamanan Data
                TermsSectionCard(
                    icon = Icons.Default.Lock,
                    title = "1. Privasi & Keamanan Data",
                    description = "Data kebiasaan, riwayat jadwal, catatan, dan progres harian Anda disimpan dengan aman pada basis data lokal perangkat Anda serta terenkripsi saat sinkronisasi cloud akun. Kami tidak pernah membagikan atau menjual data pribadi Anda kepada pihak ketiga."
                )

                // Section 2: Integrasi Google Calendar & Izin Akses
                TermsSectionCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "2. Integrasi Kalender & Izin Akses",
                    description = "Routina menyediakan fitur integrasi ke Google Calendar dan kalender sistem ponsel Anda. Izin akses kalender (Read & Write Calendar) digunakan secara eksklusif untuk mengekspor jadwal kebiasaan pengguna, menambahkan alarm pengingat, dan mensinkronkan agenda tanpa mengakses informasi kalender pribadi lain di luar jadwal yang Anda pilih."
                )

                // Section 3: Notifikasi & Alarm Pengingat
                TermsSectionCard(
                    icon = Icons.Default.Notifications,
                    title = "3. Pengingat & Notifikasi Sistem",
                    description = "Untuk memastikan kebiasaan Anda berjalan konsisten, Routina membutuhkan izin pengingat notifikasi (Exact Alarm & Post Notifications). Anda dapat mengaktifkan, menonaktifkan, atau mengatur waktu pengingat kapan saja melalui pengaturan jadwal."
                )

                // Section 4: Akun & Tanggung Jawab Pengguna
                TermsSectionCard(
                    icon = Icons.Default.CheckCircle,
                    title = "4. Akun & Tanggung Jawab Pengguna",
                    description = "Pengguna bertanggung jawab menjaga kerahasiaan informasi akun dan kata sandi masing-masing. Setiap aktivitas yang dilakukan menggunakan akun terdaftar merupakan tanggung jawab penuh pemilik akun."
                )

                // Section 5: Pembaruan Ketentuan
                TermsSectionCard(
                    icon = Icons.Default.Shield,
                    title = "5. Perubahan & Pembaruan Layanan",
                    description = "Routina berhak melakukan peningkatan fitur dan pembaruan ketentuan sewaktu-waktu demi kenyamanan dan keamanan pengguna yang lebih optimal."
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom Action Button
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Tactile3DButton(
                        text = "SAYA MENGERTI & SETUJU",
                        onClick = {
                            onAcceptClick?.invoke() ?: onBackClick()
                        },
                        type = TactileButtonType.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsSectionCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}
