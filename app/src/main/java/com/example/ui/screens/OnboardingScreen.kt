package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.theme.GreenPrimary

@Composable
fun OnboardingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf(
        OnboardingPageData(
            title = "Kelola Jadwal Harianmu",
            description = "Atur kebiasaan positif setiap pagi, siang, dan malam dengan pengingat notifikasi otomatis.",
            badgeText = "LANGKAH 1 • JADWAL HARIAN",
            badgeType = BadgeType.IN_PROGRESS,
            emoji = "⏰",
            imageRes = R.drawable.img_onboarding_mood
        ),
        OnboardingPageData(
            title = "Pantau Analisis Bulanan",
            description = "Lihat grafik perkembangan bulanan dan pertahankan streak kebiasaanmu agar tetap termotivasi.",
            badgeText = "LANGKAH 2 • STATISTIK & STREAK",
            badgeType = BadgeType.STREAK,
            emoji = "🔥",
            imageRes = R.drawable.img_mascot_2
        ),
        OnboardingPageData(
            title = "Maskot & Pesan Kustom",
            description = "Ditemani karakter interaktif di layar HP yang menyemangatimu dengan pesan dan suara kustom.",
            badgeText = "LANGKAH 3 • MASKOT VIRTUAL",
            badgeType = BadgeType.COMPLETED,
            emoji = "🐾",
            imageRes = R.drawable.img_mascot_3
        ),
        OnboardingPageData(
            title = "Kuis Personalisasi Habit",
            description = "Rekomendasi target kebiasaan yang disesuaikan dengan kebutuhan dan rutinitas harianmu.",
            badgeText = "LANGKAH 4 • PERSONALISASI",
            badgeType = BadgeType.IN_PROGRESS,
            emoji = "🎯",
            imageRes = R.drawable.img_mascot_4
        ),
        OnboardingPageData(
            title = "Sinkronisasi Cloud & Kalender",
            description = "Akses data habit dari berbagai perangkat dan hubungkan jadwalmu dengan Google Calendar.",
            badgeText = "LANGKAH 5 • CLOUD & SYNC",
            badgeType = BadgeType.COMPLETED,
            emoji = "☁️",
            imageRes = R.drawable.img_onboarding_hero
        ),
        OnboardingPageData(
            title = "Siap Mulai Kebiasaan Baru?",
            description = "Bangun disiplin setiap hari dan capai target harianmu bersama Routina.",
            badgeText = "MULAI SEKARANG",
            badgeType = BadgeType.STREAK,
            emoji = "🚀",
            imageRes = R.drawable.img_mascot_1
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentStep == 0) {
            // Halaman Pertama: Gambar Poster Full-Screen
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding_mood),
                    contentDescription = "Onboarding Poster",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Tombol "Lewati" di Pojok Kanan Atas -> Langsung ke Langkah Terakhir
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { currentStep = steps.size - 1 }
                ) {
                    Text(
                        text = "Lewati",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                // Tombol Navigasi & Indikator di Bagian Bawah
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Indikator Titik
                    Row(
                        modifier = Modifier.padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        steps.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (index == currentStep) 24.dp else 10.dp, 10.dp)
                                    .clip(CircleShape)
                                    .background(if (index == currentStep) GreenPrimary else Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }

                    // Hanya 1 Tombol Utama yang Jelas di Halaman Intro
                    Tactile3DButton(
                        text = "LANJUTKAN",
                        onClick = { currentStep++ },
                        type = TactileButtonType.PRIMARY
                    )
                }
            }
        } else {
            // Halaman Onboarding Berikutnya (Steps 1..5)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: Logo + Brand & Tombol Lewati (jika belum di halaman akhir)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user_app_icon),
                            contentDescription = "Routina Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "routina habit",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = GreenPrimary
                        )
                    }

                    if (currentStep < steps.size - 1) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { currentStep = steps.size - 1 }
                        ) {
                            Text(
                                text = "Lewati",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Graphic & Illustration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = steps[currentStep].imageRes),
                        contentDescription = "Onboarding Visual",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBadge(
                        text = steps[currentStep].badgeText,
                        type = steps[currentStep].badgeType
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = steps[currentStep].title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = steps[currentStep].description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Indicators
                    Row(
                        modifier = Modifier.padding(top = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        steps.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (index == currentStep) 24.dp else 10.dp, 10.dp)
                                    .clip(CircleShape)
                                    .background(if (index == currentStep) GreenPrimary else MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Area: 1 Tombol untuk langkah 1..4, dan 2 tombol jelas di langkah terakhir
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentStep < steps.size - 1) {
                        Tactile3DButton(
                            text = "LANJUTKAN",
                            onClick = { currentStep++ },
                            type = TactileButtonType.PRIMARY
                        )
                    } else {
                        // Halaman Akhir: Opsi Masuk/Daftar atau Lewati
                        Tactile3DButton(
                            text = "MASUK ATAU DAFTAR",
                            onClick = onNavigateToRegister,
                            type = TactileButtonType.PRIMARY
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Tactile3DButton(
                            text = "LEWATI",
                            onClick = onSkip,
                            type = TactileButtonType.SECONDARY
                        )
                    }
                }
            }
        }
    }
}

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val badgeText: String,
    val badgeType: BadgeType,
    val emoji: String,
    val imageRes: Int
)
