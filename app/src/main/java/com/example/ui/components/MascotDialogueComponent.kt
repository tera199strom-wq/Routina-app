package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.MascotMessageEntity
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GreenPrimary
import com.example.util.SoundHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DuolingoSpeechBubble(
    text: String,
    displayedText: String,
    isTyping: Boolean,
    stepIndex: Int,
    totalSteps: Int,
    onClick: () -> Unit,
    characterName: String = "Karakter",
    modifier: Modifier = Modifier,
    isMini: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble Box: Pure White without borders, soft shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isMini) 3.dp else 8.dp,
                    shape = RoundedCornerShape(if (isMini) 12.dp else 20.dp),
                    spotColor = Color(0x33000000)
                )
                .clip(RoundedCornerShape(if (isMini) 12.dp else 20.dp))
                .background(Color.White)
                .padding(
                    horizontal = if (isMini) 10.dp else 18.dp,
                    vertical = if (isMini) 8.dp else 16.dp
                )
        ) {
            Column {
                // Header inside bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(if (isMini) 6.dp else 8.dp)
                                .clip(CircleShape)
                                .background(BlueAccent)
                        )
                        Spacer(modifier = Modifier.width(if (isMini) 4.dp else 6.dp))
                        Text(
                            text = characterName.ifBlank { "Karakter" },
                            fontSize = if (isMini) 8.sp else 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueAccent
                        )
                    }

                    // Step counter badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(if (isMini) 4.dp else 8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(
                                horizontal = if (isMini) 5.dp else 8.dp,
                                vertical = if (isMini) 1.dp else 3.dp
                            )
                    ) {
                        Text(
                            text = "${stepIndex + 1}/$totalSteps",
                            fontSize = if (isMini) 7.sp else 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isMini) 4.dp else 8.dp))

                // The Dialogue Message Text (Black on White) with Link Support (Blue & Clickable)
                LinkifiedText(
                    text = displayedText,
                    fontSize = if (isMini) 10.sp else 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    linkColor = BlueAccent,
                    lineHeight = if (isMini) 13.sp else 21.sp,
                    maxLines = if (isMini) 3 else Int.MAX_VALUE,
                    onNonLinkClick = onClick
                )

                Spacer(modifier = Modifier.height(if (isMini) 4.dp else 10.dp))

                // Action hint at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTyping) {
                        Text(
                            text = "klik skip...",
                            fontSize = if (isMini) 8.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(if (isMini) 6.dp else 10.dp))
                                .background(GreenPrimary)
                                .padding(
                                    horizontal = if (isMini) 6.dp else 10.dp,
                                    vertical = if (isMini) 2.dp else 4.dp
                                )
                        ) {
                            Text(
                                text = if (stepIndex + 1 < totalSteps) "Lanjut" else "Selesai",
                                fontSize = if (isMini) 8.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(if (isMini) 8.dp else 14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tail / Pointer of the speech bubble (Pure White)
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(if (isMini) 14.dp else 22.dp)
                .height(if (isMini) 6.dp else 10.dp)
                .offset(y = (-1).dp)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path = path, color = Color.White)
        }
    }
}

@Composable
fun MascotCharacterView(
    imageUri: String?,
    rotation: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier,
    sizeDp: Int = 160
) {
    val animRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "mascot_rotation"
    )
    val animScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "mascot_scale"
    )
    val animOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "mascot_offset_x"
    )
    val animOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "mascot_offset_y"
    )

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = animOffsetX.dp.roundToPx(),
                    y = animOffsetY.dp.roundToPx()
                )
            }
            .rotate(animRotation)
            .scale(animScale),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        if (!imageUri.isNullOrBlank()) {
            if (imageUri.startsWith("res:")) {
                val resName = imageUri.removePrefix("res:")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                Image(
                    painter = painterResource(id = if (resId != 0) resId else R.drawable.img_mascot_1),
                    contentDescription = "Mascot Character",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(sizeDp.dp)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Custom Mascot Character",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(sizeDp.dp)
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_1),
                contentDescription = "Mascot Character",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(sizeDp.dp)
            )
        }
    }
}

@Composable
fun MascotDialogueContent(
    messages: List<MascotMessageEntity>,
    onDismiss: () -> Unit,
    characterName: String = "Karakter",
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    val currentMessage = messages.getOrNull(currentStep) ?: run {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var displayedText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var typingJob by remember { mutableStateOf<Job?>(null) }

    // Play sound and trigger typewriter effect coroutine whenever step changes
    LaunchedEffect(currentStep, currentMessage.messageId) {
        SoundHelper.playSound(context, currentMessage.soundUri)
        displayedText = ""
        isTyping = true
        typingJob?.cancel()

        typingJob = coroutineScope.launch {
            val fullText = currentMessage.text
            for (i in 1..fullText.length) {
                displayedText = fullText.substring(0, i)
                delay(22) // Typewriter typing speed
            }
            isTyping = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            SoundHelper.stop()
        }
    }

    fun handleAdvance() {
        if (isTyping) {
            typingJob?.cancel()
            displayedText = currentMessage.text
            isTyping = false
        } else {
            if (currentStep + 1 < messages.size) {
                currentStep++
            } else {
                onDismiss()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { handleAdvance() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Top Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 20.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        val animBubbleOffsetX by animateFloatAsState(
            targetValue = currentMessage.bubbleOffsetX,
            animationSpec = spring(dampingRatio = 0.7f),
            label = "bubble_offset_x"
        )
        val animBubbleOffsetY by animateFloatAsState(
            targetValue = currentMessage.bubbleOffsetY,
            animationSpec = spring(dampingRatio = 0.7f),
            label = "bubble_offset_y"
        )

        // Canvas Workspace Area (Identical positioning & coordinate structure to MessageEditorDialog)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Speech Bubble layer behind
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
                    .offset {
                        IntOffset(
                            x = animBubbleOffsetX.dp.roundToPx(),
                            y = animBubbleOffsetY.dp.roundToPx()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                DuolingoSpeechBubble(
                    text = currentMessage.text,
                    displayedText = displayedText,
                    isTyping = isTyping,
                    stepIndex = currentStep,
                    totalSteps = messages.size,
                    onClick = { handleAdvance() },
                    characterName = currentMessage.characterName.ifBlank { characterName },
                    modifier = Modifier.fillMaxWidth(0.92f)
                )
            }

            // 2. Character layer rendered IN FRONT of message bubble
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                MascotCharacterView(
                    imageUri = currentMessage.imageUri,
                    rotation = currentMessage.rotation,
                    scale = currentMessage.scale,
                    offsetX = currentMessage.offsetX,
                    offsetY = currentMessage.offsetY,
                    sizeDp = 180
                )
            }
        }
    }
}

@Composable
fun MascotDialogueOverlay(
    messages: List<MascotMessageEntity>,
    onDismiss: () -> Unit,
    characterName: String = "Karakter",
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        MascotDialogueContent(
            messages = messages,
            onDismiss = onDismiss,
            characterName = characterName,
            modifier = modifier
        )
    }
}
