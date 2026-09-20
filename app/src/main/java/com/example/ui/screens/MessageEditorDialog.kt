package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
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
import com.example.ui.components.DuolingoSpeechBubble
import com.example.ui.components.MascotCharacterView
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.example.util.SoundHelper
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageEditorDialog(
    eventId: Long,
    initialMessage: MascotMessageEntity?,
    onDismiss: () -> Unit,
    onSave: (
        text: String,
        characterName: String,
        imageUri: String?,
        rotation: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        bubbleOffsetX: Float,
        bubbleOffsetY: Float,
        soundUri: String?,
        soundName: String?
    ) -> Unit
) {
    var text by remember { mutableStateOf(initialMessage?.text ?: "Ayo selesaikan targetmu hari ini!") }
    var characterName by remember { mutableStateOf(initialMessage?.characterName ?: "Karakter") }
    var selectedImageUri by remember { mutableStateOf<String?>(initialMessage?.imageUri) }
    var selectedSoundUri by remember { mutableStateOf<String?>(initialMessage?.soundUri ?: "preset_meow") }
    var selectedSoundName by remember { mutableStateOf<String?>(initialMessage?.soundName ?: "Meow Kucing") }
    var rotation by remember { mutableFloatStateOf(initialMessage?.rotation ?: 0f) }
    var scale by remember { mutableFloatStateOf(initialMessage?.scale ?: 1.0f) }
    var offsetX by remember { mutableFloatStateOf(initialMessage?.offsetX ?: 0f) }
    var offsetY by remember { mutableFloatStateOf(initialMessage?.offsetY ?: 0f) }
    var bubbleOffsetX by remember { mutableFloatStateOf(initialMessage?.bubbleOffsetX ?: 0f) }
    var bubbleOffsetY by remember { mutableFloatStateOf(initialMessage?.bubbleOffsetY ?: 0f) }

    // State to track if user is pressing, moving, or transforming elements on the canvas
    var isManipulatingElement by remember { mutableStateOf(false) }

    // Active selection on canvas: "CHARACTER" vs "BUBBLE"
    var selectedElement by remember { mutableStateOf("CHARACTER") }

    // Active focused edit tab: "BUBBLE", "CHARACTER", or "SOUND"
    var activeEditTab by remember { mutableStateOf("CHARACTER") }

    // Bottom Sheet for Editing
    var showContentEditSheet by remember { mutableStateOf(false) }
    var isRemovingBackground by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Helper function to compress and save image to internal app storage
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val imagesDir = File(context.filesDir, "mascot_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val maxDimension = 512
            var sampleSize = 1
            if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while (halfHeight / sampleSize >= maxDimension && halfWidth / sampleSize >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val originalBitmap = contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val scaleFactor = maxDimension.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                val newWidth = (originalBitmap.width * scaleFactor).toInt()
                val newHeight = (originalBitmap.height * scaleFactor).toInt()
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            val fileName = "avatar_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png"
            val destinationFile = File(imagesDir, fileName)

            FileOutputStream(destinationFile).use { output ->
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 85, output)
            }

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            Uri.fromFile(destinationFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Gallery Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = saveImageToInternalStorage(context, uri)
            selectedImageUri = savedUri ?: uri.toString()
        }
    }

    // Custom Audio Picker Launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = SoundHelper.saveAudioToInternalStorage(context, uri)
            if (result != null) {
                selectedSoundUri = result.first
                selectedSoundName = result.second
                SoundHelper.playSound(context, result.first)
                Toast.makeText(context, "Audio kustom dipilih: ${result.second}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memuat file audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2E3138)) // Abu-abu canvas studio
        ) {
            // Background Canvas: Abu-abu dengan pola dot-dot (dotted matrix grid)
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val dotRadius = 1.75.dp.toPx()
                val spacingPx = 22.dp.toPx()
                val dotColor = Color(0xFFB0B4BC).copy(alpha = 0.45f)

                val startX = (size.width % spacingPx) / 2f
                val startY = (size.height % spacingPx) / 2f

                var x = startX
                while (x <= size.width) {
                    var y = startY
                    while (y <= size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        y += spacingPx
                    }
                    x += spacingPx
                }
            }

            // Canvas Workspace Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. SPEECH BUBBLE LAYER (Rendered behind character)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 50.dp)
                        .offset {
                            IntOffset(
                                x = bubbleOffsetX.dp.roundToPx(),
                                y = bubbleOffsetY.dp.roundToPx()
                            )
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                isManipulatingElement = true
                                selectedElement = "BUBBLE"
                                try {
                                    do {
                                        val event = awaitPointerEvent()
                                        val panChange = event.calculatePan()
                                        if (panChange != Offset.Zero) {
                                            bubbleOffsetX += panChange.x / 1.5f
                                            bubbleOffsetY += panChange.y / 1.5f
                                            event.changes.forEach {
                                                if (it.positionChanged()) {
                                                    it.consume()
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                } finally {
                                    isManipulatingElement = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    DuolingoSpeechBubble(
                        text = text.ifBlank { "Semangat terus ya!" },
                        displayedText = text.ifBlank { "Semangat terus ya!" },
                        isTyping = false,
                        stepIndex = 0,
                        totalSteps = 1,
                        isMini = false,
                        onClick = { selectedElement = "BUBBLE" },
                        characterName = characterName.ifBlank { "Karakter" },
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )

                    // Selection Bounding Box for Speech Bubble (Clean border + corner handles, without text badge)
                    if (selectedElement == "BUBBLE") {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                                .border(
                                    2.5.dp,
                                    BlueAccent,
                                    RoundedCornerShape(24.dp)
                                )
                        ) {
                            // Corner Handles
                            CornerHandle(Modifier.align(Alignment.TopStart), color = BlueAccent)
                            CornerHandle(Modifier.align(Alignment.TopEnd), color = BlueAccent)
                            CornerHandle(Modifier.align(Alignment.BottomStart), color = BlueAccent)
                            CornerHandle(Modifier.align(Alignment.BottomEnd), color = BlueAccent)
                        }
                    }
                }

                // 2. CHARACTER LAYER (Rendered in front of speech bubble)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 100.dp)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                isManipulatingElement = true
                                selectedElement = "CHARACTER"
                                try {
                                    do {
                                        val event = awaitPointerEvent()
                                        val zoomChange = event.calculateZoom()
                                        val rotationChange = event.calculateRotation()
                                        val panChange = event.calculatePan()

                                        if (panChange != Offset.Zero || zoomChange != 1f || rotationChange != 0f) {
                                            offsetX += panChange.x / 1.5f
                                            offsetY += panChange.y / 1.5f
                                            scale = (scale * zoomChange).coerceIn(0.4f, 3.5f)
                                            rotation = (rotation + rotationChange).coerceIn(-180f, 180f)
                                            event.changes.forEach {
                                                if (it.positionChanged()) {
                                                    it.consume()
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                } finally {
                                    isManipulatingElement = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    MascotCharacterView(
                        imageUri = selectedImageUri,
                        rotation = rotation,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        sizeDp = 180
                    )

                    // Selection Bounding Box for Character (Rotates with character, clean border + corner handles without text badge)
                    if (selectedElement == "CHARACTER") {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = offsetX.dp.roundToPx(),
                                        y = offsetY.dp.roundToPx()
                                    )
                                }
                                .rotate(rotation)
                                .size((180 * scale).dp)
                                .border(
                                    2.5.dp,
                                    GreenPrimary,
                                    RoundedCornerShape(20.dp)
                                )
                        ) {
                            // Corner Handles
                            CornerHandle(Modifier.align(Alignment.TopStart))
                            CornerHandle(Modifier.align(Alignment.TopEnd))
                            CornerHandle(Modifier.align(Alignment.BottomStart))
                            CornerHandle(Modifier.align(Alignment.BottomEnd))
                        }
                    }
                }
            }

            // 🔝 TOP CONTROLS BAR: Edit (Sudut Kiri Atas), Reset (Tengah Atas), Simpan (Sudut Kanan Atas)
            AnimatedVisibility(
                visible = !isManipulatingElement,
                enter = fadeIn(animationSpec = tween(150)) + slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(150)
                ),
                exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(150)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SUDUT KIRI ATAS: Icon / Tombol Edit Terfokus
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Focused Edit Button (Opens focused editor for current selected element)
                        val isCharacter = selectedElement == "CHARACTER"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isCharacter) GreenPrimary.copy(alpha = 0.2f) else BlueAccent.copy(alpha = 0.2f))
                                .border(1.2.dp, if (isCharacter) GreenPrimary else BlueAccent, RoundedCornerShape(18.dp))
                                .clickable {
                                    activeEditTab = selectedElement
                                    showContentEditSheet = true
                                }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Konten",
                                    tint = if (isCharacter) GreenPrimary else BlueAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isCharacter) "Karakter" else "Teks",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // TENGAH ATAS: Tombol Reset Posisi
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                            .border(1.dp, Color(0xFF475569), RoundedCornerShape(18.dp))
                            .clickable {
                                offsetX = 0f
                                offsetY = 0f
                                scale = 1.0f
                                rotation = 0f
                                bubbleOffsetX = 0f
                                bubbleOffsetY = 0f
                                Toast.makeText(context, "Posisi & skala direset!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Posisi",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // SUDUT KANAN ATAS: Tombol Simpan
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(GreenPrimary)
                            .clickable {
                                if (text.isBlank()) {
                                    Toast.makeText(context, "Pesan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                onSave(
                                    text.trim(),
                                    characterName.trim().ifBlank { "Karakter" },
                                    selectedImageUri,
                                    rotation,
                                    scale,
                                    offsetX,
                                    offsetY,
                                    bubbleOffsetX,
                                    bubbleOffsetY,
                                    selectedSoundUri,
                                    selectedSoundName
                                )
                                Toast.makeText(context, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Simpan",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Simpan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 🔻 BOTTOM FLOATING HELPER TOOLBAR: Selector & Dedicated Focus Buttons
            AnimatedVisibility(
                visible = !isManipulatingElement,
                enter = fadeIn(animationSpec = tween(150)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(150)
                ),
                exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(150)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 36.dp, start = 12.dp, end = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Selector Switcher Pill with direct Edit tabs
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF202227).copy(alpha = 0.95f))
                            .border(1.5.dp, Color(0xFF454954), RoundedCornerShape(24.dp))
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Button Karakter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selectedElement == "CHARACTER") GreenPrimary else Color.Transparent)
                                .clickable {
                                    if (selectedElement == "CHARACTER") {
                                        activeEditTab = "CHARACTER"
                                        showContentEditSheet = true
                                    } else {
                                        selectedElement = "CHARACTER"
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Karakter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedElement == "CHARACTER") Color.White else Color(0xFF94A3B8),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Button Balon Pesan
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selectedElement == "BUBBLE") BlueAccent else Color.Transparent)
                                .clickable {
                                    if (selectedElement == "BUBBLE") {
                                        activeEditTab = "BUBBLE"
                                        showContentEditSheet = true
                                    } else {
                                        selectedElement = "BUBBLE"
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Balon Teks",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedElement == "BUBBLE") Color.White else Color(0xFF94A3B8),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Button Suara
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Transparent)
                                .clickable {
                                    activeEditTab = "SOUND"
                                    showContentEditSheet = true
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Suara",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    // 📋 FOCUSED CONTENT EDIT BOTTOM SHEET
    if (showContentEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showContentEditSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (activeEditTab) {
                            "CHARACTER" -> "Pengaturan Gambar Karakter"
                            "SOUND" -> "Pilih Efek Suara"
                            else -> "Edit Pesan & Karakter"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { showContentEditSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // -------------------------------------------------------------
                // 1. EDIT PESAN & KARAKTER (Di edit pesan ada 2: Pengaturan Pesan & Gambar Karakter)
                // -------------------------------------------------------------
                if (activeEditTab == "BUBBLE") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // A. Pengaturan Teks & Nama Karakter
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "1. Teks & Nama Karakter",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueAccent
                            )

                            OutlinedTextField(
                                value = characterName,
                                onValueChange = { characterName = it },
                                label = { Text("Nama Karakter di Pesan Ini") },
                                placeholder = { Text("Contoh: Karakter, Dino, Mimi, dsb.") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BlueAccent,
                                    cursorColor = BlueAccent
                                )
                            )

                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                label = { Text("Isi Pesan Balon Kata") },
                                placeholder = { Text("Contoh: Cek info ini https://google.com ya!") },
                                supportingText = {
                                    Text(
                                        "Tautan atau URL otomatis berwarna biru dan dapat diklik",
                                        fontSize = 11.sp,
                                        color = BlueAccent
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BlueAccent,
                                    cursorColor = BlueAccent
                                )
                            )

                            // Quick suggestion pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Semangat terus!",
                                    "Yuk checklist targetmu!",
                                    "Kunjungi https://google.com"
                                ).forEach { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .clickable { text = suggestion }
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // B. Pengaturan Karakter
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "2. Gambar Karakter",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )

                            MascotSelectionSection(
                                selectedImageUri = selectedImageUri,
                                onImageSelected = { selectedImageUri = it },
                                onGalleryClick = { galleryLauncher.launch("image/*") }
                            )
                        }

                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isRemovingBackground = true
                                    MascotImageHelper.removeBackground(
                                        context = context,
                                        imageUri = selectedImageUri!!,
                                        onSuccess = { newUri ->
                                            isRemovingBackground = false
                                            selectedImageUri = newUri
                                            Toast.makeText(context, "Berhasil menghapus latar belakang!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            isRemovingBackground = false
                                            Toast.makeText(context, "Tidak dapat mendeteksi latar. Untuk kartun/anime, gunakan gambar dengan latar belakang polos (misal putih).", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                enabled = !isRemovingBackground
                            ) {
                                if (isRemovingBackground) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Memproses...",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hapus Latar Belakang",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Mendukung foto dan gambar berlatar belakang polos.",
                                fontSize = 11.sp,
                                color = GrayLight,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showContentEditSheet = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                        ) {
                            Text(
                                text = "Terapkan Perubahan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 2. FOKUS: EDIT KARAKTER SAJA (Tanpa skala, tanpa rotasi, tanpa pilihan lain)
                // -------------------------------------------------------------
                if (activeEditTab == "CHARACTER") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Nama & Gambar Karakter:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = characterName,
                            onValueChange = { characterName = it },
                            label = { Text("Nama Karakter di Pesan Ini") },
                            placeholder = { Text("Contoh: Karakter, Dino, Mimi, dsb.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            )
                        )

                        Text(
                            text = "Pilih Gambar Karakter:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        MascotSelectionSection(
                            selectedImageUri = selectedImageUri,
                            onImageSelected = { selectedImageUri = it },
                            onGalleryClick = { galleryLauncher.launch("image/*") }
                        )

                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isRemovingBackground = true
                                    MascotImageHelper.removeBackground(
                                        context = context,
                                        imageUri = selectedImageUri!!,
                                        onSuccess = { newUri ->
                                            isRemovingBackground = false
                                            selectedImageUri = newUri
                                            Toast.makeText(context, "Berhasil menghapus latar belakang!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            isRemovingBackground = false
                                            Toast.makeText(context, "Tidak dapat mendeteksi latar. Untuk kartun/anime, gunakan gambar dengan latar belakang polos (misal putih).", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                enabled = !isRemovingBackground
                            ) {
                                if (isRemovingBackground) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Memproses...",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hapus Latar Belakang",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Mendukung foto dan gambar berlatar belakang polos.",
                                fontSize = 11.sp,
                                color = GrayLight,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showContentEditSheet = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text(
                                text = "Terapkan Karakter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3. FOKUS: PILIH EFEK SUARA
                // -------------------------------------------------------------
                if (activeEditTab == "SOUND") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Suara Saat Pesan Muncul:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // Preview Sound Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BlueAccent.copy(alpha = 0.12f))
                                    .clickable {
                                        SoundHelper.playSound(context, selectedSoundUri)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Uji Suara",
                                    tint = BlueAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Uji Suara",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueAccent
                                )
                            }
                        }

                        // Sound Presets Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "preset_meow" to "Meow",
                                "preset_chime" to "Chime",
                                "preset_pop" to "Pop",
                                "preset_bell" to "Lonceng"
                            ).forEach { (presetKey, label) ->
                                val isSelected = selectedSoundUri == presetKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) GreenPrimary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedSoundUri = presetKey
                                            selectedSoundName = label.substringAfter(" ")
                                            SoundHelper.playSound(context, presetKey)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Pick Custom Audio File from Phone Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedSoundUri?.startsWith("preset_") == false && selectedSoundUri != null)
                                        BlueAccent.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (selectedSoundUri?.startsWith("preset_") == false && selectedSoundUri != null)
                                        BlueAccent
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    audioPickerLauncher.launch("audio/*")
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = if (selectedSoundUri?.startsWith("preset_") == false && selectedSoundUri != null) BlueAccent else GrayLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (selectedSoundUri?.startsWith("preset_") == false && selectedSoundUri != null)
                                                "Audio Kustom: ${selectedSoundName ?: "Dipilih"}"
                                            else "Pilih File Audio Kustom dari HP",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedSoundUri?.startsWith("preset_") == false && selectedSoundUri != null) BlueAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Gunakan file MP3, WAV, atau rekaman audio",
                                            fontSize = 10.sp,
                                            color = GrayLight
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GrayLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Preset Tanpa Suara
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedSoundUri == "preset_none") Color(0xFFEF4444).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, if (selectedSoundUri == "preset_none") Color(0xFFEF4444) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedSoundUri = "preset_none"
                                    selectedSoundName = "Tanpa Suara"
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Matikan Efek Suara (Hening)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedSoundUri == "preset_none") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSoundUri == "preset_none") Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showContentEditSheet = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text(
                                text = "Terapkan Efek Suara",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CornerHandle(modifier: Modifier = Modifier, color: Color = GreenPrimary) {
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, Color.White, CircleShape)
    )
}



@Composable
fun MascotSelectionSection(
    selectedImageUri: String?,
    onImageSelected: (String?) -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultMascots = listOf(
        "res:img_mascot_1",
        "res:img_mascot_2",
        "res:img_mascot_3",
        "res:img_mascot_4"
    )
    val isGallerySelected = selectedImageUri != null && !selectedImageUri.startsWith("res:")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tombol Pilih Foto (Persegi Panjang Agak Bulat di Atas Gambar Karakter)
        Button(
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGallerySelected) BlueAccent else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isGallerySelected) Color.White else MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(
                1.5.dp,
                if (isGallerySelected) BlueAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGallerySelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(selectedImageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Foto Galeri Terpilih",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Pilih Foto dari Galeri",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Gambar Karakter (di bawah tombol, tanpa judul, bentuk card & agak gede biar kelihatan)
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
        ) {
            items(defaultMascots.size) { index ->
                val mascotUri = defaultMascots[index]
                val isSelected = selectedImageUri == mascotUri || (selectedImageUri == null && index == 0)

                Card(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onImageSelected(mascotUri) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val resName = mascotUri.removePrefix("res:")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_mascot_1),
                            contentDescription = "Pilih Karakter ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
