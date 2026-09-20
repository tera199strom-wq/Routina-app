package com.example.data.repository

import com.example.data.local.MascotDao
import com.example.data.local.MascotEventEntity
import com.example.data.local.MascotEventWithMessages
import com.example.data.local.MascotMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MascotRepository(private val mascotDao: MascotDao) {

    val allEventsWithMessages: Flow<List<MascotEventWithMessages>> =
        mascotDao.getAllEventsWithMessages().map { list ->
            list.map { it.copy(messages = it.messages.sortedBy { m -> m.orderIndex }) }
        }

    fun getEventWithMessagesByType(eventType: String): Flow<MascotEventWithMessages?> =
        mascotDao.getEventWithMessagesByType(eventType).map { eventWithMsg ->
            eventWithMsg?.copy(messages = eventWithMsg.messages.sortedBy { m -> m.orderIndex })
        }

    fun getAllEventsWithMessagesByType(eventType: String): Flow<List<MascotEventWithMessages>> =
        mascotDao.getAllEventsWithMessagesByType(eventType).map { list ->
            list.map { it.copy(messages = it.messages.sortedBy { m -> m.orderIndex }) }
        }

    fun getEventWithMessagesById(id: Long): Flow<MascotEventWithMessages?> =
        mascotDao.getEventWithMessagesById(id).map { eventWithMsg ->
            eventWithMsg?.copy(messages = eventWithMsg.messages.sortedBy { m -> m.orderIndex })
        }

    suspend fun getEventWithMessagesByIdSync(id: Long): MascotEventWithMessages? =
        mascotDao.getEventWithMessagesByIdSync(id)?.let { eventWithMsg ->
            eventWithMsg.copy(messages = eventWithMsg.messages.sortedBy { m -> m.orderIndex })
        }

    suspend fun addEvent(event: MascotEventEntity): Long =
        mascotDao.insertEvent(event)

    suspend fun updateEvent(event: MascotEventEntity) =
        mascotDao.updateEvent(event)

    suspend fun deleteEvent(event: MascotEventEntity) =
        mascotDao.deleteEvent(event)

    suspend fun addMessage(message: MascotMessageEntity): Long =
        mascotDao.insertMessage(message)

    suspend fun updateMessage(message: MascotMessageEntity) =
        mascotDao.updateMessage(message)

    suspend fun updateMessagesOrder(messages: List<MascotMessageEntity>) =
        mascotDao.updateMessages(messages)

    suspend fun deleteMessage(message: MascotMessageEntity) =
        mascotDao.deleteMessage(message)

    suspend fun seedInitialMascotEventsIfEmpty() {
        val count = mascotDao.getEventCount()
        if (count == 0) {
            // Seed 1: Saat Membuka Aplikasi
            val openAppEventId = mascotDao.insertEvent(
                MascotEventEntity(
                    eventType = "APP_OPEN",
                    title = "Saat Membuka Aplikasi",
                    description = "Karakter menyapa hangat dengan pesan motivasi setiap kali kamu membuka aplikasi.",
                    isEnabled = true
                )
            )

            mascotDao.insertMessage(
                MascotMessageEntity(
                    eventId = openAppEventId,
                    orderIndex = 0,
                    text = "Meow! Halo, selamat datang kembali di Routina! 🐾",
                    imageUri = "res:img_mascot_1",
                    soundUri = "preset_meow",
                    soundName = "Meow Kucing 🐾",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0f,
                    scale = 1.0f
                )
            )

            mascotDao.insertMessage(
                MascotMessageEntity(
                    eventId = openAppEventId,
                    orderIndex = 1,
                    text = "Jangan biarkan streak kebiasaanmu terputus hari ini ya! 🔥",
                    imageUri = "res:img_mascot_2",
                    soundUri = "preset_pop",
                    soundName = "Pop Balon 🎈",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0f,
                    scale = 1.0f
                )
            )

            mascotDao.insertMessage(
                MascotMessageEntity(
                    eventId = openAppEventId,
                    orderIndex = 2,
                    text = "Ayo selesaikan target harianmu sekarang. Aku selalu mendukungmu! ✨",
                    imageUri = "res:img_mascot_3",
                    soundUri = "preset_bell",
                    soundName = "Lonceng Kemenangan 🔔",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0f,
                    scale = 1.0f
                )
            )

            mascotDao.insertMessage(
                MascotMessageEntity(
                    eventId = openAppEventId,
                    orderIndex = 3,
                    text = "Kamu pasti bisa mencapai semua resolusimu hari ini! 🌟",
                    imageUri = "res:img_mascot_4",
                    soundUri = "preset_chime",
                    soundName = "Chime Notifikasi ✨",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0f,
                    scale = 1.0f
                )
            )

            // Seed 2: Saat Meninggalkan Streak
            val streakRiskEventId = mascotDao.insertEvent(
                MascotEventEntity(
                    eventType = "LEAVE_STREAK",
                    title = "Saat Meninggalkan Streak",
                    description = "Peringatan ramah dan dorongan semangat saat streak kebiasaan berisiko hilang.",
                    isEnabled = true
                )
            )

            mascotDao.insertMessage(
                MascotMessageEntity(
                    eventId = streakRiskEventId,
                    orderIndex = 0,
                    text = "Aww tidak! Streak kebiasaanmu berisiko hilang jika belum ada aktivitas! 😿",
                    imageUri = "res:img_mascot_1",
                    soundUri = "preset_meow",
                    soundName = "Meow Kucing 🐾",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = -8f,
                    scale = 1.05f
                )
            )

            mascotDao.insertMessage(
                MascotMessageEntity(
                    eventId = streakRiskEventId,
                    orderIndex = 1,
                    text = "Yuk luangkan waktu sebentar untuk checklist 1 kebiasaanmu hari ini! 💪",
                    imageUri = "res:img_mascot_1",
                    soundUri = "preset_chime",
                    soundName = "Chime Notifikasi ✨",
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 5f,
                    scale = 1.1f
                )
            )
        }
    }
}
