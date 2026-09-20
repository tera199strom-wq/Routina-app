package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mascot_events")
data class MascotEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String, // "APP_OPEN", "LEAVE_STREAK", "SCHEDULE_REMINDER", "SCHEDULE_QUESTION", "CUSTOM"
    val title: String,
    val description: String = "",
    val isEnabled: Boolean = true,
    val showInNotification: Boolean = true,
    val showOnScreenOverlay: Boolean = true,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "mascot_messages",
    foreignKeys = [
        ForeignKey(
            entity = MascotEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["eventId"])]
)
data class MascotMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val messageId: Long = 0,
    val eventId: Long,
    val orderIndex: Int = 0,
    val text: String,
    val characterName: String = "Karakter", // Custom character name per message
    val imageUri: String? = null, // null = Default Blue Cat (R.drawable.img_app_logo)
    val soundUri: String? = null, // null or Uri/preset (e.g. "preset_meow", "preset_bell", etc.)
    val soundName: String? = null, // Display name of sound
    val offsetX: Float = 0f, // in DP
    val offsetY: Float = 0f, // in DP
    val rotation: Float = 0f, // degrees (-180f .. 180f)
    val scale: Float = 1.0f, // 0.5f .. 2.0f
    val bubbleOffsetX: Float = 0f, // in DP
    val bubbleOffsetY: Float = 0f // in DP
)
