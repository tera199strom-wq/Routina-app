package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import com.example.R
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.rememberUiScale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.HelpOutline
import com.example.data.local.QuestionRouteEntity
import com.example.data.local.ScheduleEntity
import com.example.data.local.MascotEventEntity
import com.example.data.local.MascotEventWithMessages
import com.example.data.local.MascotMessageEntity
import com.example.service.MascotFloatingService
import com.example.ui.components.MascotDialogueOverlay
import com.example.ui.components.LinkifiedText
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GoldenAccent
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.NavText
import com.example.ui.theme.OrangeAccent
import com.example.util.SoundHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotSettingsScreen(
    mascotEvents: List<MascotEventWithMessages>,
    schedules: List<ScheduleEntity> = emptyList(),
    questionRoutes: List<QuestionRouteEntity> = emptyList(),
    mascotName: String = "Karakter",
    onUpdateMascotName: (String) -> Unit = {},
    onAddEvent: (eventType: String, title: String, description: String) -> Unit,
    onToggleEvent: (event: MascotEventEntity) -> Unit,
    onUpdateEventSettings: (event: MascotEventEntity, showInNotification: Boolean, showOnScreenOverlay: Boolean) -> Unit = { _, _, _ -> },
    onDeleteEvent: (event: MascotEventEntity) -> Unit,
    onSaveQuestionRoute: (parentEventId: Long, answer: String, nextEventId: Long?, targetScheduleId: Long?) -> Unit = { _, _, _, _ -> },
    onDeleteQuestionRoute: (parentEventId: Long, answer: String) -> Unit = { _, _ -> },
    onAddMessage: (
        eventId: Long,
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
        soundName: String?,
        orderIndex: Int
    ) -> Unit,
    onUpdateMessage: (message: MascotMessageEntity) -> Unit,
    onDeleteMessage: (message: MascotMessageEntity) -> Unit,
    onMoveMessageUp: (message: MascotMessageEntity) -> Unit = {},
    onMoveMessageDown: (message: MascotMessageEntity) -> Unit = {},
    onReorderMessages: (eventId: Long, messages: List<MascotMessageEntity>) -> Unit = { _, _ -> },
    onNavigateToHome: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State for floating service status & overlay permission
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isFloatingActive by remember { mutableStateOf(MascotFloatingService.isRunning) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                isFloatingActive = MascotFloatingService.isRunning
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
        if (hasOverlayPermission) {
            Toast.makeText(context, "Izin diberikan! Sekarang kamu bisa mengaktifkan karakter melayang.", Toast.LENGTH_LONG).show()
        }
    }

    // State for interactive full preview dialog
    var previewMessages by remember { mutableStateOf<List<MascotMessageEntity>?>(null) }

    // State for Add Event Sheet
    var showAddEventSheet by remember { mutableStateOf(false) }

    // State for Add/Edit Message Dialog
    var editingMessageEventId by remember { mutableStateOf<Long?>(null) }
    var messageToEdit by remember { mutableStateOf<MascotMessageEntity?>(null) }
    var showMessageEditor by remember { mutableStateOf(false) }

    fun deleteLocalImageFileIfExists(imageUri: String?) {
        if (imageUri != null && imageUri.startsWith("file://")) {
            try {
                val path = Uri.parse(imageUri).path
                if (path != null) {
                    val file = File(path)
                    if (file.exists() && file.name.startsWith("avatar_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = BlueAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Karakter",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventSheet = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_mascot_event_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tambah Event", fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            MascotBottomNavBar(
                currentTab = "MASCOT",
                onNavigateToHome = onNavigateToHome,
                onNavigateToStats = onNavigateToStats,
                onNavigateToMascot = { /* Already here */ },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Event Karakter",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${mascotEvents.size} Event",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent
                    )
                }
            }

            if (mascotEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada event karakter.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Klik tombol '+ Tambah Event' di bawah untuk membuat sapaan interaktif!",
                                fontSize = 13.sp,
                                color = GrayLight,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // List of Events
            items(mascotEvents, key = { it.event.id }) { eventWithMessages ->
                MascotEventCard(
                    eventWithMessages = eventWithMessages,
                    schedules = schedules,
                    allEvents = mascotEvents,
                    questionRoutes = questionRoutes.filter { it.parentEventId == eventWithMessages.event.id },
                    onSaveQuestionRoute = onSaveQuestionRoute,
                    onDeleteQuestionRoute = onDeleteQuestionRoute,
                    onToggleEnabled = { onToggleEvent(eventWithMessages.event) },
                    onUpdateEventSettings = onUpdateEventSettings,
                    onDeleteEvent = {
                        eventWithMessages.messages.forEach { msg ->
                            deleteLocalImageFileIfExists(msg.imageUri)
                        }
                        onDeleteEvent(eventWithMessages.event)
                    },
                    onTestDialogue = {
                        if (eventWithMessages.messages.isNotEmpty()) {
                            previewMessages = eventWithMessages.messages
                        } else {
                            Toast.makeText(context, "Tambahkan minimal 1 pesan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAddMessageClick = {
                        editingMessageEventId = eventWithMessages.event.id
                        messageToEdit = null
                        showMessageEditor = true
                    },
                    onEditMessageClick = { msg ->
                        editingMessageEventId = eventWithMessages.event.id
                        messageToEdit = msg
                        showMessageEditor = true
                    },
                    onDeleteMessageClick = { msg ->
                        deleteLocalImageFileIfExists(msg.imageUri)
                        onDeleteMessage(msg)
                    },
                    onMoveUp = { msg -> onMoveMessageUp(msg) },
                    onMoveDown = { msg -> onMoveMessageDown(msg) },
                    onReorderMessages = { eventId, msgs -> onReorderMessages(eventId, msgs) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Modal Sheet: Add Event
    if (showAddEventSheet) {
        AddEventBottomSheet(
            onDismiss = { showAddEventSheet = false },
            onSave = { type, title, desc ->
                onAddEvent(type, title, desc)
                showAddEventSheet = false
                Toast.makeText(context, "Event '$title' berhasil dibuat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Message Editor (Text, Custom Gallery Image, Interactive Touch-Gesture Positioning, Sound)
    if (showMessageEditor && editingMessageEventId != null) {
        MessageEditorDialog(
            eventId = editingMessageEventId!!,
            initialMessage = messageToEdit,
            onDismiss = { showMessageEditor = false },
            onSave = { text, characterName, uri, rot, scale, ox, oy, box, boy, soundUri, soundName ->
                if (messageToEdit != null) {
                    if (messageToEdit!!.imageUri != uri) {
                        deleteLocalImageFileIfExists(messageToEdit!!.imageUri)
                    }
                    onUpdateMessage(
                        messageToEdit!!.copy(
                            text = text,
                            characterName = characterName,
                            imageUri = uri,
                            rotation = rot,
                            scale = scale,
                            offsetX = ox,
                            offsetY = oy,
                            bubbleOffsetX = box,
                            bubbleOffsetY = boy,
                            soundUri = soundUri,
                            soundName = soundName
                        )
                    )
                    Toast.makeText(context, "Pesan berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                } else {
                    val nextOrder = mascotEvents.find { it.event.id == editingMessageEventId }?.messages?.size ?: 0
                    onAddMessage(
                        editingMessageEventId!!,
                        text,
                        characterName,
                        uri,
                        rot,
                        scale,
                        ox,
                        oy,
                        box,
                        boy,
                        soundUri,
                        soundName,
                        nextOrder
                    )
                    Toast.makeText(context, "Pesan baru berhasil ditambahkan.", Toast.LENGTH_SHORT).show()
                }
                showMessageEditor = false
            }
        )
    }

    // Full Interactive Dialogue Simulator Overlay
    if (previewMessages != null) {
        MascotDialogueOverlay(
            messages = previewMessages!!,
            characterName = mascotName,
            onDismiss = { previewMessages = null }
        )
    }
}

@Composable
fun FloatingMascotPermissionCard(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, OrangeAccent.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Karakter di Luar Aplikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Izin tampil di atas aplikasi lain",
                        fontSize = 11.sp,
                        color = GrayLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangeAccent.copy(alpha = 0.1f))
                    .border(1.dp, OrangeAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Izin Tampil di Atas Layar Diperlukan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                    Text(
                        text = "Aktifkan izin agar karakter dapat melayang di layar HP saat kamu membuka aplikasi lain. Card ini otomatis hilang setelah kamu memberi izin.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Buka Pengaturan Izin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MascotEventCard(
    eventWithMessages: MascotEventWithMessages,
    schedules: List<ScheduleEntity> = emptyList(),
    allEvents: List<MascotEventWithMessages> = emptyList(),
    questionRoutes: List<QuestionRouteEntity> = emptyList(),
    onSaveQuestionRoute: (parentEventId: Long, answer: String, nextEventId: Long?, targetScheduleId: Long?) -> Unit = { _, _, _, _ -> },
    onDeleteQuestionRoute: (parentEventId: Long, answer: String) -> Unit = { _, _ -> },
    onToggleEnabled: () -> Unit,
    onUpdateEventSettings: (event: MascotEventEntity, showInNotification: Boolean, showOnScreenOverlay: Boolean) -> Unit = { _, _, _ -> },
    onDeleteEvent: () -> Unit,
    onTestDialogue: () -> Unit,
    onAddMessageClick: () -> Unit,
    onEditMessageClick: (MascotMessageEntity) -> Unit,
    onDeleteMessageClick: (MascotMessageEntity) -> Unit,
    onMoveUp: (MascotMessageEntity) -> Unit,
    onMoveDown: (MascotMessageEntity) -> Unit,
    onReorderMessages: (eventId: Long, messages: List<MascotMessageEntity>) -> Unit = { _, _ -> }
) {
    val event = eventWithMessages.event
    var localMessages by remember(eventWithMessages.messages) {
        mutableStateOf(eventWithMessages.messages.sortedBy { it.orderIndex })
    }
    LaunchedEffect(eventWithMessages.messages) {
        localMessages = eventWithMessages.messages.sortedBy { it.orderIndex }
    }
    val messages = localMessages

    var draggingMessageId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scale = rememberUiScale()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var routeConfigAnswer by remember { mutableStateOf<String?>(null) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Hapus Event Karakter?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text("Apakah kamu yakin ingin menghapus event '${event.title}' beserta semua dialog pesannya?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteEvent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (routeConfigAnswer != null) {
        val answer = routeConfigAnswer!!
        val currentRoute = questionRoutes.find {
            it.answerCondition.equals(answer, ignoreCase = true) ||
            (answer == "YA" && it.answerCondition.equals("YES", ignoreCase = true)) ||
            (answer == "TIDAK" && it.answerCondition.equals("NO", ignoreCase = true))
        }
        RouteConfigDialog(
            parentEventTitle = event.title,
            answer = answer,
            currentRoute = currentRoute,
            schedules = schedules,
            otherQuestions = allEvents.map { it.event }.filter { it.id != event.id && it.eventType == "SCHEDULE_QUESTION" },
            onDismiss = { routeConfigAnswer = null },
            onSave = { nextEvtId, targetSchedId ->
                onSaveQuestionRoute(event.id, answer, nextEvtId, targetSchedId)
                routeConfigAnswer = null
            },
            onDelete = {
                onDeleteQuestionRoute(event.id, answer)
                routeConfigAnswer = null
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.5.dp,
            if (event.isEnabled) GreenPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Event Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val icon = when (event.eventType) {
                        "APP_OPEN" -> Icons.Default.SmartToy
                        "LEAVE_STREAK" -> Icons.Default.Whatshot
                        "SCHEDULE_QUESTION" -> Icons.Default.AltRoute
                        else -> Icons.Default.AutoAwesome
                    }
                    val iconColor = when (event.eventType) {
                        "APP_OPEN" -> BlueAccent
                        "LEAVE_STREAK" -> OrangeAccent
                        "SCHEDULE_QUESTION" -> GreenPrimary
                        else -> GoldenAccent
                    }

                    Box(
                        modifier = Modifier
                            .size((34 * scale).dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size((20 * scale).dp)
                        )
                    }

                    Spacer(modifier = Modifier.width((10 * scale).dp))

                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = event.isEnabled,
                        onCheckedChange = { onToggleEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenPrimary,
                            checkedBorderColor = GreenPrimary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = GrayLight.copy(alpha = 0.5f),
                            uncheckedBorderColor = GrayLight.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.size((34 * scale).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Event",
                            tint = Color(0xFFE53935).copy(alpha = 0.85f),
                            modifier = Modifier.size((20 * scale).dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Test Dialogue & Add Message)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestDialogue,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Uji Coba Dialog (${messages.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onAddMessageClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BlueAccent),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tambah Pesan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent
                    )
                }
            }

            // Branching Routes Section for SCHEDULE_QUESTION
            if (event.eventType == "SCHEDULE_QUESTION") {
                Spacer(modifier = Modifier.height(12.dp))
                BranchingRoutesSection(
                    questionRoutes = questionRoutes,
                    schedules = schedules,
                    allEvents = allEvents.map { it.event },
                    onEditRoute = { answer -> routeConfigAnswer = answer }
                )
            }

            // Message Items List with Reorder strip handle & sound preview
            if (localMessages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localMessages.forEachIndexed { index, msg ->
                        val isItemDragging = (draggingMessageId == msg.messageId)
                        key(msg.messageId) {
                            MessageItemRow(
                                index = index,
                                totalCount = localMessages.size,
                                message = msg,
                                isDragging = isItemDragging,
                                dragOffsetY = if (isItemDragging) dragOffsetY else 0f,
                                onRowHeightMeasured = { h ->
                                    if (h > 0f) rowHeightPx = h
                                },
                                onDragStart = {
                                    draggingMessageId = msg.messageId
                                    dragOffsetY = 0f
                                },
                                onDragDelta = { deltaY ->
                                    dragOffsetY += deltaY
                                    val step = if (rowHeightPx > 0f) rowHeightPx else with(density) { 76.dp.toPx() }
                                    val threshold = step * 0.45f

                                    val curIdx = localMessages.indexOfFirst { it.messageId == msg.messageId }
                                    if (curIdx != -1) {
                                        if (dragOffsetY > threshold && curIdx < localMessages.size - 1) {
                                            val list = localMessages.toMutableList()
                                            val item = list.removeAt(curIdx)
                                            list.add(curIdx + 1, item)
                                            localMessages = list
                                            dragOffsetY -= step
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (_: Exception) {}
                                        } else if (dragOffsetY < -threshold && curIdx > 0) {
                                            val list = localMessages.toMutableList()
                                            val item = list.removeAt(curIdx)
                                            list.add(curIdx - 1, item)
                                            localMessages = list
                                            dragOffsetY += step
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val wasDragging = (draggingMessageId != null)
                                    draggingMessageId = null
                                    dragOffsetY = 0f
                                    if (wasDragging) {
                                        onReorderMessages(event.id, localMessages)
                                    }
                                },
                                onEdit = { onEditMessageClick(msg) },
                                onDelete = { onDeleteMessageClick(msg) },
                                onMoveUp = {
                                    val curIdx = localMessages.indexOfFirst { it.messageId == msg.messageId }
                                    if (curIdx > 0) {
                                        val list = localMessages.toMutableList()
                                        val item = list.removeAt(curIdx)
                                        list.add(curIdx - 1, item)
                                        localMessages = list
                                        onReorderMessages(event.id, list)
                                    }
                                },
                                onMoveDown = {
                                    val curIdx = localMessages.indexOfFirst { it.messageId == msg.messageId }
                                    if (curIdx in 0 until localMessages.size - 1) {
                                        val list = localMessages.toMutableList()
                                        val item = list.removeAt(curIdx)
                                        list.add(curIdx + 1, item)
                                        localMessages = list
                                        onReorderMessages(event.id, list)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BranchingRoutesSection(
    questionRoutes: List<QuestionRouteEntity>,
    schedules: List<ScheduleEntity>,
    allEvents: List<MascotEventEntity>,
    onEditRoute: (answer: String) -> Unit
) {
    val yesRoute = questionRoutes.find { it.answerCondition.equals("YA", ignoreCase = true) || it.answerCondition.equals("YES", ignoreCase = true) }
    val noRoute = questionRoutes.find { it.answerCondition.equals("TIDAK", ignoreCase = true) || it.answerCondition.equals("NO", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccountTree,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Alur Cabang Jawaban Pertanyaan",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Branch YA
        BranchRouteItem(
            answerLabel = "Jawaban Ya",
            answerColor = GreenPrimary,
            route = yesRoute,
            schedules = schedules,
            allEvents = allEvents,
            onConfigure = { onEditRoute("YA") }
        )

        // Branch TIDAK
        BranchRouteItem(
            answerLabel = "Jawaban Tidak",
            answerColor = Color(0xFFE53935),
            route = noRoute,
            schedules = schedules,
            allEvents = allEvents,
            onConfigure = { onEditRoute("TIDAK") }
        )
    }
}

@Composable
fun BranchRouteItem(
    answerLabel: String,
    answerColor: Color,
    route: QuestionRouteEntity?,
    schedules: List<ScheduleEntity>,
    allEvents: List<MascotEventEntity>,
    onConfigure: () -> Unit
) {
    val targetSchedule = route?.targetScheduleId?.let { id -> schedules.find { it.id == id } }
    val nextEvent = route?.nextEventId?.let { id -> allEvents.find { it.id == id } }

    val destinationText = when {
        targetSchedule != null -> "Jadwal: ${targetSchedule.name}"
        nextEvent != null -> "Lanjut ke: ${nextEvent.title}"
        else -> "Belum ditentukan (Jadwal Default)"
    }

    val isSet = targetSchedule != null || nextEvent != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isSet) answerColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onConfigure)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = answerLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = answerColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSet) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = GrayLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = destinationText,
                    fontSize = 12.sp,
                    fontWeight = if (isSet) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSet) MaterialTheme.colorScheme.onSurface else GrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onConfigure, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Atur Cabang",
                tint = answerColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RouteConfigDialog(
    parentEventTitle: String,
    answer: String,
    currentRoute: QuestionRouteEntity?,
    schedules: List<ScheduleEntity>,
    otherQuestions: List<MascotEventEntity>,
    onDismiss: () -> Unit,
    onSave: (nextEventId: Long?, targetScheduleId: Long?) -> Unit,
    onDelete: () -> Unit
) {
    // Mode: "SCHEDULE" or "QUESTION"
    var selectedMode by remember {
        mutableStateOf(if (currentRoute?.nextEventId != null) "QUESTION" else "SCHEDULE")
    }
    var selectedScheduleId by remember {
        mutableStateOf(currentRoute?.targetScheduleId ?: schedules.firstOrNull()?.id)
    }
    var selectedNextEventId by remember {
        mutableStateOf(currentRoute?.nextEventId ?: otherQuestions.firstOrNull()?.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Atur Cabang: Jawaban $answer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Untuk: $parentEventTitle",
                    fontSize = 12.sp,
                    color = GrayLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ketika pengguna menjawab $answer, apa yang terjadi?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Mode selector tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedMode = "SCHEDULE" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == "SCHEDULE") GreenPrimary else GrayLight.copy(alpha = 0.2f),
                            contentColor = if (selectedMode == "SCHEDULE") Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("Tentukan Jadwal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedMode = "QUESTION" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == "QUESTION") BlueAccent else GrayLight.copy(alpha = 0.2f),
                            contentColor = if (selectedMode == "QUESTION") Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("Lanjut Tanya", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (selectedMode == "SCHEDULE") {
                    Text(
                        text = "Pilih Jadwal Aktif:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    if (schedules.isEmpty()) {
                        Text(
                            text = "Belum ada jadwal lain. Buat jadwal di Pengaturan Jadwal.",
                            fontSize = 12.sp,
                            color = GrayLight
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            schedules.forEach { schedule ->
                                val isSelected = selectedScheduleId == schedule.id
                                val scheduleColor = try {
                                    Color(android.graphics.Color.parseColor(schedule.colorHex))
                                } catch (e: Exception) {
                                    GreenPrimary
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) scheduleColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                        .border(
                                            2.dp,
                                            if (isSelected) scheduleColor else MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedScheduleId = schedule.id }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(scheduleColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = schedule.name + if (schedule.isDefault) " (Default)" else "",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) scheduleColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Pilih Pertanyaan Lanjutan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    if (otherQuestions.isEmpty()) {
                        Text(
                            text = "Belum ada event pertanyaan lain. Buat event dengan tipe 'Pertanyaan Penentu Jadwal' terlebih dahulu!",
                            fontSize = 12.sp,
                            color = GrayLight
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            otherQuestions.forEach { qEvent ->
                                val isSelected = selectedNextEventId == qEvent.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) BlueAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                        .border(
                                            2.dp,
                                            if (isSelected) BlueAccent else MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedNextEventId = qEvent.id }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = if (isSelected) BlueAccent else GrayLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = qEvent.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) BlueAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode == "SCHEDULE") {
                        onSave(null, selectedScheduleId)
                    } else {
                        onSave(selectedNextEventId, null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = (selectedMode == "SCHEDULE" && selectedScheduleId != null) ||
                        (selectedMode == "QUESTION" && selectedNextEventId != null)
            ) {
                Text("Simpan Alur", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (currentRoute != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                    ) {
                        Text("Hapus Cabang")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
            }
        }
    )
}

@Composable
fun MessageItemRow(
    index: Int,
    totalCount: Int,
    message: MascotMessageEntity,
    isDragging: Boolean,
    dragOffsetY: Float,
    onRowHeightMeasured: (Float) -> Unit,
    onDragStart: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val scale = rememberUiScale()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        label = "dragElevation"
    )
    val scaleCard by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1.0f,
        label = "dragScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 1f)
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .scale(scaleCard)
            .shadow(elevation, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
            .border(
                if (isDragging) 2.dp else 1.5.dp,
                if (isDragging) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(14.dp)
            )
            .onGloballyPositioned { coordinates ->
                val h = coordinates.size.height.toFloat() + with(density) { 8.dp.toPx() }
                onRowHeightMeasured(h)
            }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character thumbnail
            Box(
                modifier = Modifier
                    .size((44 * scale).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (!message.imageUri.isNullOrBlank()) {
                    if (message.imageUri.startsWith("res:")) {
                        val resName = message.imageUri.removePrefix("res:")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_mascot_1),
                            contentDescription = null,
                            modifier = Modifier
                                .size((36 * scale).dp)
                                .rotate(message.rotation)
                                .scale(message.scale),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(message.imageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size((36 * scale).dp)
                                .rotate(message.rotation)
                                .scale(message.scale),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_1),
                        contentDescription = null,
                        modifier = Modifier
                            .size((36 * scale).dp)
                            .rotate(message.rotation)
                            .scale(message.scale),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width((10 * scale).dp))

            // Message text & Sound badge
            Column(modifier = Modifier.weight(1f)) {
                // Audio Sound Indicator (Tanpa background color)
                val soundDisplayName = SoundHelper.getSoundDisplayName(message.soundUri, message.soundName)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            SoundHelper.playSound(context, message.soundUri)
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Uji Suara",
                        tint = BlueAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = soundDisplayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                LinkifiedText(
                    text = message.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    linkColor = BlueAccent,
                    maxLines = 2
                )
            }

            // Actions: Edit, Delete, Drag Handle (Strip) on far right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((2 * scale).dp)
            ) {
                // Edit Button
                IconButton(onClick = onEdit, modifier = Modifier.size((28 * scale).dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Pesan",
                        tint = BlueAccent,
                        modifier = Modifier.size((16 * scale).dp)
                    )
                }

                // Delete Button (Tong Sampah)
                IconButton(onClick = onDelete, modifier = Modifier.size((28 * scale).dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Pesan",
                        tint = Color(0xFFE53935).copy(alpha = 0.85f),
                        modifier = Modifier.size((16 * scale).dp)
                    )
                }

                // Drag Handle (Reorder Grip) on the far right with responsive drag & drop + tap dropdown
                var showReorderMenu by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size((38 * scale).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDragging) GreenPrimary.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        )
                        .border(
                            1.dp,
                            if (isDragging) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                        .pointerInput(message.messageId) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                var dragStarted = false
                                var accumY = 0f

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break

                                    val dy = change.positionChange().y
                                    accumY += dy

                                    if (!dragStarted && (kotlin.math.abs(accumY) > 6f || kotlin.math.abs(dy) > 2f)) {
                                        dragStarted = true
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (_: Exception) {}
                                        onDragStart()
                                    }

                                    if (dragStarted) {
                                        change.consume()
                                        onDragDelta(dy)
                                    }
                                }

                                if (dragStarted) {
                                    onDragEnd()
                                } else {
                                    // Tapped on the handle without dragging: open quick menu
                                    showReorderMenu = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Tarik untuk Ubah Urutan (Drag Handle)",
                        tint = if (isDragging) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size((22 * scale).dp)
                    )

                    DropdownMenu(
                        expanded = showReorderMenu,
                        onDismissRequest = { showReorderMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("⬆️ Geser ke Atas", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            },
                            onClick = {
                                showReorderMenu = false
                                onMoveUp()
                            },
                            enabled = index > 0
                        )
                        DropdownMenuItem(
                            text = {
                                Text("⬇️ Geser ke Bawah", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            },
                            onClick = {
                                showReorderMenu = false
                                onMoveDown()
                            },
                            enabled = index < totalCount - 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventBottomSheet(
    onDismiss: () -> Unit,
    onSave: (eventType: String, title: String, description: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedType by remember { mutableStateOf("APP_OPEN") }
    var title by remember { mutableStateOf("Saat Membuka Aplikasi") }
    var description by remember { mutableStateOf("Menyapa pengguna dengan semangat saat membuka aplikasi.") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Tambah Trigger Event Karakter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Pilih Jenis Trigger Event:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Preset Trigger 1: App Open
            TriggerTypeCard(
                title = "1. Pengguna saat membuka aplikasi",
                desc = "Dialog muncul di beranda saat pengguna baru login atau buka app",
                icon = Icons.Default.SmartToy,
                isSelected = selectedType == "APP_OPEN",
                onClick = {
                    selectedType = "APP_OPEN"
                    title = "Saat Membuka Aplikasi"
                    description = "Menyapa pengguna dengan semangat saat membuka aplikasi."
                }
            )

            // Preset Trigger 2: Leave/Broken Streak
            TriggerTypeCard(
                title = "2. Saat meninggalkan streak",
                desc = "Peringatan ramah dan dorongan agar streak kebiasaan tidak hilang",
                icon = Icons.Default.Whatshot,
                isSelected = selectedType == "LEAVE_STREAK",
                onClick = {
                    selectedType = "LEAVE_STREAK"
                    title = "Saat Meninggalkan Streak"
                    description = "Dorongan semangat saat belum checklist kebiasaan hari ini."
                }
            )

            // Preset Trigger 3: Schedule Reminder (Pengingat Jadwal)
            TriggerTypeCard(
                title = "3. Pengingat Jadwal",
                desc = "Pengingat jadwal kebiasaan harian otomatis dari maskot",
                icon = Icons.Default.Notifications,
                isSelected = selectedType == "SCHEDULE_REMINDER",
                onClick = {
                    selectedType = "SCHEDULE_REMINDER"
                    title = "Pengingat Jadwal"
                    description = "Pengingat jadwal kebiasaan harian dari karakter maskot."
                }
            )

            // Preset Trigger 4: Schedule Question
            TriggerTypeCard(
                title = "4. Pertanyaan Penentu Jadwal (Bercabang)",
                desc = "Maskot menanyakan kondisi hari ini untuk menentukan Jadwal aktif secara otomatis",
                icon = Icons.Default.AltRoute,
                isSelected = selectedType == "SCHEDULE_QUESTION",
                onClick = {
                    selectedType = "SCHEDULE_QUESTION"
                    title = "Pertanyaan Jadwal: Kerja Hari Ini?"
                    description = "Pertanyaan maskot dengan opsi YA / TIDAK untuk menentukan jadwal aktif."
                }
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nama Event") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(selectedType, title, description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(
                    text = "Buat Event",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TriggerTypeCard(
    title: String,
    desc: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            2.dp,
            if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) GreenPrimary else GrayLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = GrayLight
                )
            }
        }
    }
}

@Composable
fun MascotBottomNavBar(
    currentTab: String,
    onNavigateToHome: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToMascot: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = rememberUiScale()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🏠 Home Tab
            IconButton(
                onClick = onNavigateToHome,
                modifier = Modifier.size((46 * scale).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Beranda",
                    tint = if (currentTab == "HOME") GreenPrimary else NavText,
                    modifier = Modifier.size((24 * scale).dp)
                )
            }

            // 📅 Stats Tab
            IconButton(
                onClick = onNavigateToStats,
                modifier = Modifier.size((46 * scale).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Statistik",
                    tint = if (currentTab == "STATS") GreenPrimary else NavText,
                    modifier = Modifier.size((24 * scale).dp)
                )
            }

            // 🐾 Mascot Pet Tab (Karakter)
            IconButton(
                onClick = onNavigateToMascot,
                modifier = Modifier.size((46 * scale).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Karakter",
                    tint = if (currentTab == "MASCOT") GreenPrimary else NavText,
                    modifier = Modifier.size((24 * scale).dp)
                )
            }

            // 👤 Profile Tab
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier.size((46 * scale).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = if (currentTab == "PROFILE") GreenPrimary else NavText,
                    modifier = Modifier.size((24 * scale).dp)
                )
            }
        }
    }
}

@Composable
fun NavActiveItem(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit = {}
) {
    val scale = rememberUiScale()

    IconButton(
        onClick = onClick,
        modifier = Modifier.size((48 * scale).dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = GreenPrimary,
            modifier = Modifier.size((26 * scale).dp)
        )
    }
}
