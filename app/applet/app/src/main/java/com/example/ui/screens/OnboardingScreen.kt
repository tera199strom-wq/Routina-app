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
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary

@Composable
fun OnboardingScreen(
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }

    val steps = listOf(
        OnboardingPageData(
            title = "Kelola Jadwal Harianmu",
            description = "Atur kebiasaan positif setiap pagi, siang, dan malam dengan pengingat notifikasi otomatis.",
            badgeText = "FASE 1 • JADWAL HARIAN",
            badgeType = BadgeType.IN_PROGRESS,
            emoji = "⏰"
        ),
        OnboardingPageData(
            title = "Karakter Maskot Interaktif",
            description = "Dapatkan dorongan semangat dari karakter maskot melayang favoritmu yang memberikan pesan motivasi ceria.",
            badgeText = "FASE 2 • MASKOT MELAYANG",
            badgeType = BadgeType.IN_PROGRESS,
            emoji = "🐾"
        ),
        OnboardingPageData(
            title = "Pantau Analisis Bulanan",
            description = "Lihat grafik perkembangan bulanan dan pertahankan streak kebiasaanmu agar tetap termotivasi setiap hari.",
            badgeText = "FASE 3 • STATISTIK & STREAK",
            badgeType = BadgeType.STREAK,
            emoji = "🔥"
        ),
        OnboardingPageData(
            title = "Efek Suara & Catatan Kustom",
            description = "Sesuaikan pesan harian dengan rekaman audio kustom, musik pilihan, serta tata letak posisi gambar yang unik.",
            badgeText = "FASE 4 • AUDIO & TEMA",
            badgeType = BadgeType.STREAK,
            emoji = "🎵"
        ),
        OnboardingPageData(
            title = "Sinkronisasi Awan & Kalender",
            description = "Akses data habit dari berbagai perangkat secara aman dan terhubung langsung dengan Google Calendar.",
            badgeText = "FASE 5 • CLOUD & CALENDAR",
            badgeType = BadgeType.COMPLETED,
            emoji = "☁️"
        ),
        OnboardingPageData(
            title = "Evaluasi & Komunitas Positif",
            description = "Ikuti kuis kebiasaan personal untuk menemukan rutinitas terbaik yang sesuai dengan gaya hidupmu.",
            badgeText = "FASE 6 • HABIT PERSONAL",
            badgeType = BadgeType.COMPLETED,
            emoji = "🎯"
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Top Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_user_app_icon),
                        contentDescription = "Routina Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "routina habit",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenPrimary
                    )
                }

                // Step Counter Badge
                Text(
                    text = "${currentStep + 1} / ${steps.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayLight,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Graphic & Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StatusBadge(
                        text = steps[currentStep].badgeText,
                        type = steps[currentStep].badgeType
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = steps[currentStep].emoji,
                        fontSize = 56.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding_hero),
                        contentDescription = "Hero Art",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = steps[currentStep].title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = steps[currentStep].description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = GrayLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Indicators (6 dots)
                Row(
                    modifier = Modifier.padding(top = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (index == currentStep) 22.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentStep) GreenPrimary else MaterialTheme.colorScheme.outline)
                                .clickable { currentStep = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions
            Column(modifier = Modifier.fillMaxWidth()) {
                Tactile3DButton(
                    text = if (currentStep < steps.size - 1) "LANJUTKAN" else "MULAI SEKARANG",
                    onClick = {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            onNavigateToAuth()
                        }
                    },
                    type = TactileButtonType.PRIMARY
                )

                if (currentStep < steps.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Tactile3DButton(
                        text = "LEWATI / DAFTAR AKUN",
                        onClick = onNavigateToAuth,
                        type = TactileButtonType.SECONDARY
                    )
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
    val emoji: String
)
