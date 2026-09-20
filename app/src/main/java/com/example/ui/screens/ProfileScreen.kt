package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.GoldenAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.NavText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

import com.example.util.HabitStatsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    habits: List<HabitEntity>,
    completedTodayCount: Int,
    totalLogsCount: Int,
    allLogs: List<HabitLogEntity> = emptyList(),
    isLoggedIn: Boolean = false,
    lastSyncTimeMs: Long = 0L,
    isSyncing: Boolean = false,
    onSyncNow: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToMascot: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Menghitung streak saat ini berdasarkan tanggal kalender log yang sebenarnya
    val streakCount = remember(allLogs) { HabitStatsHelper.calculateCurrentStreak(allLogs) }
    val todayPercent = if (habits.isNotEmpty()) (completedTodayCount * 100 / habits.size) else 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil Saya",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Tombol Settings di pojok kanan atas halaman Profil
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("profile_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = GreenPrimary,
                            modifier = Modifier.size(28.dp)
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
                currentTab = "PROFILE",
                onNavigateToHome = onNavigateToHome,
                onNavigateToStats = onNavigateToStats,
                onNavigateToMascot = onNavigateToMascot,
                onNavigateToProfile = { /* Already here */ }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Main User Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.15f))
                            .border(3.dp, GreenPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (userName.isNotBlank()) userName else "Pengguna Routina",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (userEmail.isNotBlank()) userEmail else "Belum masuk ke akun Google",
                        fontSize = 13.sp,
                        color = GrayLight
                    )
                }
            }

            // Summary Activity Statistics Cards (Labels above numbers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Runtunan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Runtunan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayLight,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$streakCount",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = GreenPrimary,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Hari",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                // Card 2: Selesai Hari Ini
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hari Ini",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayLight,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$completedTodayCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkBlue,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Card 3: Aktivitas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aktivitas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayLight,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalLogsCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldenAccent,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Level & XP Progress Card
            val currentLevel = (totalLogsCount / 10) + 1
            val currentXp = totalLogsCount % 10
            val xpMax = 10
            val xpProgress = currentXp / xpMax.toFloat()
            val levelTitle = when {
                currentLevel == 1 -> "Pemula Rutinitas"
                currentLevel == 2 -> "Penjelajah Kebiasaan"
                currentLevel in 3..4 -> "Pejuang Konsisten"
                currentLevel in 5..7 -> "Ahli Rutina"
                else -> "Master Legendaris"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldenAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = GoldenAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Level $currentLevel • $levelTitle",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$currentXp / $xpMax XP menuju Level ${currentLevel + 1}",
                                    fontSize = 11.5.sp,
                                    color = GrayLight
                                )
                            }
                        }
                        Text(
                            text = "${(xpProgress * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldenAccent
                        )
                    }

                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldenAccent,
                        trackColor = GoldenAccent.copy(alpha = 0.15f)
                    )
                }
            }

            // Statistics Section in Profile Screen
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Statistik",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Today Progress Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hari Ini",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "$todayPercent%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )
                        }

                        // Linear Progress Indicator
                        LinearProgressIndicator(
                            progress = { todayPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = GreenPrimary,
                            trackColor = GreenPrimary.copy(alpha = 0.15f)
                        )

                        Text(
                            text = "$completedTodayCount dari ${habits.size} habit telah diselesaikan hari ini.",
                            fontSize = 12.sp,
                            color = GrayLight
                        )
                    }
                }

                // Grafik Perkembangan Harian (Line Chart)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Konsistensi",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "7 Hari Terakhir",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayLight
                            )
                        }

                        if (totalLogsCount == 0 || habits.isEmpty()) {
                            // Empty State saat belum ada data
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = GrayLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "Belum Ada Data",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Selesaikan beberapa kebiasaan untuk melihat grafik.",
                                        fontSize = 11.5.sp,
                                        color = GrayLight,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Calculate chart values based on real completion data for the last 7 days
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val calendar = Calendar.getInstance()
                            
                            val last7DaysValues = mutableListOf<Int>()
                            val last7DaysLabels = mutableListOf<String>()
                            val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
                            
                            // Go back 6 days and then up to today (total 7 days)
                            calendar.add(Calendar.DAY_OF_YEAR, -6)
                            for (i in 0 until 7) {
                                val currentDate = calendar.time
                                val dateString = dateFormat.format(currentDate)
                                
                                // Generate label
                                val label = dayFormat.format(currentDate).replaceFirstChar { it.uppercase() }.take(3)
                                last7DaysLabels.add(label)
                                
                                // Calculate completion percentage for this specific day
                                val logsForThisDay = allLogs.filter { it.dateString == dateString }
                                val percentForThisDay = if (habits.isNotEmpty()) {
                                    (logsForThisDay.size * 100 / habits.size).coerceIn(0, 100)
                                } else 0
                                last7DaysValues.add(percentForThisDay)
                                
                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                            }
                            
                            val days = last7DaysLabels
                            val chartValues = last7DaysValues

                            val modelProducer = remember { ChartEntryModelProducer() }
                            
                            LaunchedEffect(chartValues) {
                                modelProducer.setEntries(chartValues.mapIndexed { index, y ->
                                    FloatEntry(x = index.toFloat(), y = y.toFloat())
                                })
                            }
                            
                            val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                                days.getOrNull(value.toInt()) ?: ""
                            }

                            Chart(
                                chart = lineChart(
                                    lines = listOf(
                                        lineSpec(
                                            lineColor = GreenPrimary,
                                            lineThickness = 3.dp,
                                            lineBackgroundShader = com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient(
                                                arrayOf(GreenPrimary.copy(alpha = 0.35f), GreenPrimary.copy(alpha = 0.02f))
                                            )
                                        )
                                    )
                                ),
                                chartModelProducer = modelProducer,
                                startAxis = rememberStartAxis(
                                    valueFormatter = { value, _ -> "${value.toInt()}%" },
                                    itemPlacer = com.patrykandpatrick.vico.core.axis.AxisItemPlacer.Vertical.default(maxItemCount = 5)
                                ),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = bottomAxisFormatter
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

