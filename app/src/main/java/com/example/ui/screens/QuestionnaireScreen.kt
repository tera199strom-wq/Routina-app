package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BadgeType
import com.example.ui.components.StatusBadge
import com.example.ui.components.Tactile3DButton
import com.example.ui.components.TactileButtonType
import com.example.ui.components.TactileProgressBar
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenShadow

data class QuestionnaireOption(
    val id: String,
    val iconRes: Int,
    val iconColor: Color,
    val label: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestionnaireScreen(
    onQuestionnaireCompleted: (sourceInfo: String, usageGoal: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedSource by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("") }

    val sourceOptions = remember {
        listOf(
            QuestionnaireOption("facebook", R.drawable.ic_ob_facebook, Color(0xFF1877F2), "Facebook"),
            QuestionnaireOption("tiktok", R.drawable.ic_ob_tiktok, Color(0xFF000000), "TikTok"),
            QuestionnaireOption("instagram", R.drawable.ic_ob_instagram, Color(0xFFE1306C), "Instagram"),
            QuestionnaireOption("google_play", R.drawable.ic_ob_googleplay, Color(0xFF0086F8), "Google Play"),
            QuestionnaireOption("teman", R.drawable.ic_ob_teman, Color(0xFF58CC02), "Teman"),
            QuestionnaireOption("iklan", R.drawable.ic_ob_iklan, Color(0xFFFF9600), "Iklan"),
            QuestionnaireOption("buku", R.drawable.ic_ob_buku, Color(0xFF8549BA), "Buku")
        )
    }

    val goalOptions = remember {
        listOf(
            QuestionnaireOption("kebiasaan_sehat", R.drawable.ic_ob_kebiasaan_sehat, Color(0xFF58CC02), "Kebiasaan Sehat"),
            QuestionnaireOption("produktivitas", R.drawable.ic_ob_produktivitas, Color(0xFFFF9600), "Meningkatkan Produktivitas"),
            QuestionnaireOption("kesehatan_mental", R.drawable.ic_ob_kesehatan_mental, Color(0xFF1CB0F6), "Menjaga Kesehatan Mental"),
            QuestionnaireOption("belajar_kerja", R.drawable.ic_ob_belajar_kerja, Color(0xFF8549BA), "Belajar dan Kerja"),
            QuestionnaireOption("olahraga_kebugaran", R.drawable.ic_ob_olahraga_kebugaran, Color(0xFFFF4B4B), "Olahraga dan Kebugaran")
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = if (currentStep == 1) "PERTANYAAN 1 DARI 2" else "PERTANYAAN 2 DARI 2",
                    type = BadgeType.COMPLETED
                )

                Text(
                    text = "Langkah $currentStep/2",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            TactileProgressBar(progress = if (currentStep == 1) 0.5f else 1.0f)
            Spacer(modifier = Modifier.height(16.dp))

            // Body Question & Options
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() with fadeOut() },
                    label = "questionnaire_step"
                ) { step ->
                    if (step == 1) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Darimana Kamu Dapat Info App Ini?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(sourceOptions, key = { it.id }) { item ->
                                    val isSelected = selectedSource == item.label
                                    Tactile3DOptionCard(
                                        option = item,
                                        isSelected = isSelected,
                                        onSelect = { selectedSource = item.label }
                                    )
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Apa Tujuan Utama Kamu?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(goalOptions, key = { it.id }) { item ->
                                    val isSelected = selectedGoal == item.label
                                    Tactile3DOptionCard(
                                        option = item,
                                        isSelected = isSelected,
                                        onSelect = { selectedGoal = item.label }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                if (currentStep == 1) {
                    Tactile3DButton(
                        text = "LANJUTKAN",
                        onClick = {
                            if (selectedSource.isNotBlank()) {
                                currentStep = 2
                            }
                        },
                        type = TactileButtonType.PRIMARY,
                        enabled = selectedSource.isNotBlank()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Tactile3DButton(
                                text = "KEMBALI",
                                onClick = { currentStep = 1 },
                                type = TactileButtonType.SECONDARY
                            )
                        }

                        Box(modifier = Modifier.weight(2f)) {
                            Tactile3DButton(
                                text = "MULAI HABIT",
                                onClick = {
                                    if (selectedGoal.isNotBlank()) {
                                        onQuestionnaireCompleted(selectedSource, selectedGoal)
                                    }
                                },
                                type = TactileButtonType.PRIMARY,
                                enabled = selectedGoal.isNotBlank()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tactile3DOptionCard(
    option: QuestionnaireOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowDepth = 4.dp
    val cardHeight = 58.dp
    val shape = RoundedCornerShape(16.dp)

    val currentPressOffset by animateDpAsState(
        targetValue = if (isPressed) shadowDepth else 0.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "option_tactile_press"
    )

    val currentScale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "option_tactile_scale"
    )

    // Solid Opaque colors so background shadow never bleeds through the top
    val frontBgColor = if (isSelected) Color(0xFFF2FBF0) else Color.White
    val borderColor = if (isSelected) GreenPrimary else Color(0xFFE2E8F0)
    val shadowColor = if (isSelected) GreenShadow else Color(0xFFCBD5E1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight + shadowDepth)
            .scale(currentScale)
    ) {
        // Bottom 3D Shadow Base (Darker bottom edge giving 3D physical depth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .offset(y = shadowDepth)
                .background(shadowColor, shape = shape)
        )

        // Front Face (Solid opaque card face that moves down on press)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .offset(y = currentPressOffset)
                .background(frontBgColor, shape = shape)
                .border(2.dp, borderColor, shape = shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onSelect
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GreenPrimary.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = option.iconRes),
                            contentDescription = option.label,
                            tint = option.iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = option.label,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terpilih",
                        tint = Color(0xFF1CB0F6),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

