package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenShadow
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TactileButtonType {
    PRIMARY,   // Green with 3D shadow
    SECONDARY, // Border with Green text and gray shadow
    DANGER,    // Red with dark red shadow
    GHOST      // Simple text
}

@Composable
fun Tactile3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: TactileButtonType = TactileButtonType.PRIMARY,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    icon: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowDepth = 4.dp
    
    // Quick guaranteed visual click state so quick taps show full 3D press
    var isClickAnimating by remember { mutableStateOf(false) }

    val isVisuallyPressed = (isPressed || isClickAnimating) && enabled

    val currentPressOffset by animateDpAsState(
        targetValue = if (isVisuallyPressed) shadowDepth else 0.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "tactile_press"
    )
    val currentScale by animateFloatAsState(
        targetValue = if (isVisuallyPressed) 0.98f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "tactile_scale"
    )

    val (bgColor, shadowColor, textColor, borderColor) = when (type) {
        TactileButtonType.PRIMARY -> Tuple4(
            GreenPrimary,
            GreenShadow,
            Color.White,
            Color.Transparent
        )
        TactileButtonType.SECONDARY -> Tuple4(
            Color.White,
            Color(0xFFCBD5E1),
            GreenPrimary,
            Color(0xFFE2E8F0)
        )
        TactileButtonType.DANGER -> Tuple4(
            RedAccent,
            Color(0xFFCC3C3C),
            Color.White,
            Color.Transparent
        )
        TactileButtonType.GHOST -> Tuple4(
            Color.Transparent,
            Color.Transparent,
            GreenPrimary,
            Color.Transparent
        )
    }

    val finalAlpha = if (enabled) 1f else 0.45f
    val cornerShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .height(height + shadowDepth)
            .fillMaxWidth()
            .scale(currentScale)
    ) {
        // 3D Bottom Shadow Base
        if (type != TactileButtonType.GHOST && enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .offset(y = shadowDepth)
                    .background(shadowColor, shape = cornerShape)
            )
        }

        // Top Face Layer (Presses Down)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = currentPressOffset)
                .background(
                    if (isVisuallyPressed && type != TactileButtonType.GHOST) 
                        bgColor.copy(alpha = finalAlpha * 0.92f) 
                    else 
                        bgColor.copy(alpha = finalAlpha), 
                    shape = cornerShape
                )
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(2.dp, borderColor, cornerShape)
                    } else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                        
                        coroutineScope.launch {
                            isClickAnimating = true
                            delay(100)
                            isClickAnimating = false
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = textColor.copy(alpha = finalAlpha),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun Tactile3DSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: TactileButtonType = TactileButtonType.PRIMARY,
    enabled: Boolean = true,
    height: Dp = 38.dp,
    fontSize: TextUnit = 12.5.sp,
    shape: Shape = RoundedCornerShape(8.dp),
    icon: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowDepth = 3.dp
    var isClickAnimating by remember { mutableStateOf(false) }
    val isVisuallyPressed = (isPressed || isClickAnimating) && enabled

    val currentPressOffset by animateDpAsState(
        targetValue = if (isVisuallyPressed) shadowDepth else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tactile_press_small"
    )

    val (bgColor, shadowColor, textColor, borderColor) = when (type) {
        TactileButtonType.PRIMARY -> Tuple4(GreenPrimary, GreenShadow, Color.White, Color.Transparent)
        TactileButtonType.SECONDARY -> Tuple4(Color.White, Color(0xFFCBD5E1), GreenPrimary, Color(0xFFE2E8F0))
        TactileButtonType.DANGER -> Tuple4(RedAccent, Color(0xFFCC3C3C), Color.White, Color.Transparent)
        TactileButtonType.GHOST -> Tuple4(Color.Transparent, Color.Transparent, GreenPrimary, Color.Transparent)
    }

    Box(
        modifier = modifier.height(height + shadowDepth)
    ) {
        // Shadow base
        if (enabled && type != TactileButtonType.GHOST) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .offset(y = shadowDepth)
                    .background(shadowColor, shape = shape)
            )
        }

        // Top Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = currentPressOffset)
                .background(bgColor, shape = shape)
                .then(
                    if (borderColor != Color.Transparent) Modifier.border(1.5.dp, borderColor, shape) else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                        coroutineScope.launch {
                            isClickAnimating = true
                            delay(90)
                            isClickAnimating = false
                            onClick()
                        }
                    }
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun Tactile3DChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    fontSize: TextUnit = 12.5.sp,
    shape: Shape = RoundedCornerShape(8.dp),
    customSelectedBg: Color? = null,
    customSelectedShadow: Color? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowDepth = 3.dp
    var isClickAnimating by remember { mutableStateOf(false) }
    val isVisuallyPressed = isPressed || isClickAnimating

    val currentPressOffset by animateDpAsState(
        targetValue = if (isVisuallyPressed) shadowDepth else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tactile_chip_press"
    )

    val bgColor = if (selected) (customSelectedBg ?: GreenPrimary) else Color.White
    val shadowColor = if (selected) (customSelectedShadow ?: GreenShadow) else Color(0xFFCBD5E1)
    val textColor = if (selected) Color.White else Color(0xFF1E293B)
    val borderColor = if (selected) Color.Transparent else Color(0xFFE2E8F0)

    Box(
        modifier = modifier.height(height + shadowDepth)
    ) {
        // Shadow base
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = shadowDepth)
                .background(shadowColor, shape = shape)
        )

        // Top Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = currentPressOffset)
                .background(bgColor, shape = shape)
                .then(
                    if (borderColor != Color.Transparent) Modifier.border(1.5.dp, borderColor, shape) else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                        coroutineScope.launch {
                            isClickAnimating = true
                            delay(90)
                            isClickAnimating = false
                            onClick()
                        }
                    }
                )
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun Tactile3DIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    type: TactileButtonType = TactileButtonType.PRIMARY,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowDepth = 3.dp
    var isClickAnimating by remember { mutableStateOf(false) }
    val isVisuallyPressed = (isPressed || isClickAnimating) && enabled

    val currentPressOffset by animateDpAsState(
        targetValue = if (isVisuallyPressed) shadowDepth else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tactile_icon_press"
    )

    val (bgColor, shadowColor, textColor, borderColor) = when (type) {
        TactileButtonType.PRIMARY -> Tuple4(GreenPrimary, GreenShadow, Color.White, Color.Transparent)
        TactileButtonType.SECONDARY -> Tuple4(Color.White, Color(0xFFCBD5E1), GreenPrimary, Color(0xFFE2E8F0))
        TactileButtonType.DANGER -> Tuple4(RedAccent, Color(0xFFCC3C3C), Color.White, Color.Transparent)
        TactileButtonType.GHOST -> Tuple4(Color.Transparent, Color.Transparent, GreenPrimary, Color.Transparent)
    }

    Box(
        modifier = modifier
            .size(width = size, height = size + shadowDepth)
    ) {
        // Shadow base
        if (enabled && type != TactileButtonType.GHOST) {
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(y = shadowDepth)
                    .background(shadowColor, shape = shape)
            )
        }

        // Top Layer
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = currentPressOffset)
                .background(bgColor, shape = shape)
                .then(
                    if (borderColor != Color.Transparent) Modifier.border(1.5.dp, borderColor, shape) else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                        coroutineScope.launch {
                            isClickAnimating = true
                            delay(90)
                            isClickAnimating = false
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
