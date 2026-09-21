package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.rememberUiScale
import com.example.data.local.HabitEntity
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BlueShadow
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.GoldenAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GrayText
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenShadow
import com.example.ui.theme.NavText
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeShadow
import com.example.ui.theme.RedAccent

// Badge Pills
enum class BadgeType {
    COMPLETED,   // Green
    IN_PROGRESS, // Blue
    FAILED,      // Red
    STREAK,      // Orange
    PREMIUM      // Golden
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (type) {
        BadgeType.COMPLETED -> Pair(GreenPrimary.copy(alpha = 0.12f), GreenPrimary)
        BadgeType.IN_PROGRESS -> Pair(BlueAccent.copy(alpha = 0.12f), BlueAccent)
        BadgeType.FAILED -> Pair(RedAccent.copy(alpha = 0.12f), RedAccent)
        BadgeType.STREAK -> Pair(OrangeAccent.copy(alpha = 0.12f), OrangeAccent)
        BadgeType.PREMIUM -> Pair(GoldenAccent.copy(alpha = 0.15f), Color(0xFFB8920F))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

// Streak Flame Icon with dynamic progressive coloring
@Composable
fun StreakFlameIcon(
    progress: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 22.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "flame_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 1. Base Flame: Full Gray when inactive or behind partial fill
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Api Streak",
            tint = Color(0xFFB0B0B0),
            modifier = Modifier.fillMaxSize()
        )

        // 2. Active Fiery Layer: Clipped vertically from bottom up based on animatedProgress
        if (animatedProgress > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val clipHeight = this.size.height * animatedProgress
                        val topY = this.size.height - clipHeight
                        clipRect(
                            left = 0f,
                            top = topY,
                            right = this.size.width,
                            bottom = this.size.height
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFD600), // Puncak api: Kuning Emas
                                        Color(0xFFFF9600), // Tengah: Oranye Menyala
                                        Color(0xFFFF3D00)  // Dasar: Merah Membara
                                    )
                                ),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                )
            }
        }
    }
}

// Streak Header Counter (Clean without background pill/circle per user request)
@Composable
fun StreakCounterBadge(
    streakCount: Int,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "badge_progress"
    )

    val isPartiallyOrFullyActive = animatedProgress > 0f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        StreakFlameIcon(
            progress = animatedProgress,
            size = 22.dp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streakCount",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isPartiallyOrFullyActive) OrangeAccent else Color(0xFF9E9E9E)
        )
    }
}

// 3D Progress Bar
@Composable
fun TactileProgressBar(
    progress: Float, // 0.0 to 1.0
    barColor: Color = GreenPrimary,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(7.dp))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = barColor
        )
    }
}

// Habit Item Card Component
@Composable
fun HabitItemCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val borderColor = if (isDark) MaterialTheme.colorScheme.outline else BorderColor

    val scale = rememberUiScale()

    val (iconBg, iconShadow) = when (habit.timeCategory) {
        "Pagi" -> Pair(GreenPrimary, GreenShadow)
        "Siang" -> Pair(OrangeAccent, OrangeShadow)
        else -> Pair(BlueAccent, BlueShadow)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.2.dp, if (isCompleted) GreenPrimary else if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFEAECEF), RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left Emoji Icon Box with 3D tactile shadow
            Box(
                modifier = Modifier
                    .size((48 * scale).dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg)
                    .border(2.dp, iconShadow, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getHabitIconEmoji(habit.iconName),
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width((14 * scale).dp))

            // Habit Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (habit.description.isNotBlank()) "${habit.description} • ${habit.timeOfDay}" else "${habit.timeCategory} • ${habit.timeOfDay}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width((10 * scale).dp))

            // Right Checkbox Circle (Interactive 3D Tactile ✓)
            Box(
                modifier = Modifier
                    .size((40 * scale).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCompleted) GreenPrimary else Color.Transparent)
                    .border(
                        2.5.dp,
                        if (isCompleted) GreenShadow else BorderColor,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onToggleComplete)
                    .testTag("habit_checkbox_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selesai",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun getHabitIconEmoji(iconName: String): String {
    return when (iconName) {
        "water" -> "💧"
        "fitness" -> "🏃‍♂️"
        "book" -> "📚"
        "meditate" -> "🧘"
        "code" -> "💻"
        "sleep" -> "🌙"
        "target" -> "🎯"
        else -> "⭐"
    }
}
