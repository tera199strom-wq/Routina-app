package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MascotEventWithMessages(
    @Embedded val event: MascotEventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "eventId"
    )
    val messages: List<MascotMessageEntity>
)

@Dao
interface MascotDao {

    @Transaction
    @Query("SELECT * FROM mascot_events ORDER BY createdAtMs ASC")
    fun getAllEventsWithMessages(): Flow<List<MascotEventWithMessages>>

    @Transaction
    @Query("SELECT * FROM mascot_events ORDER BY createdAtMs ASC")
    suspend fun getAllEventsWithMessagesList(): List<MascotEventWithMessages>

    @Transaction
    @Query("SELECT * FROM mascot_events WHERE eventType = :eventType AND isEnabled = 1 LIMIT 1")
    fun getEventWithMessagesByType(eventType: String): Flow<MascotEventWithMessages?>

    @Transaction
    @Query("SELECT * FROM mascot_events WHERE eventType = :eventType AND isEnabled = 1")
    fun getAllEventsWithMessagesByType(eventType: String): Flow<List<MascotEventWithMessages>>

    @Transaction
    @Query("SELECT * FROM mascot_events WHERE id = :id LIMIT 1")
    fun getEventWithMessagesById(id: Long): Flow<MascotEventWithMessages?>

    @Transaction
    @Query("SELECT * FROM mascot_events WHERE id = :id LIMIT 1")
    suspend fun getEventWithMessagesByIdSync(id: Long): MascotEventWithMessages?

    @Query("SELECT * FROM mascot_events ORDER BY createdAtMs ASC")
    fun getAllEvents(): Flow<List<MascotEventEntity>>

    @Query("SELECT * FROM mascot_messages WHERE eventId = :eventId ORDER BY orderIndex ASC")
    fun getMessagesForEvent(eventId: Long): Flow<List<MascotMessageEntity>>

    @Query("SELECT * FROM mascot_messages WHERE eventId = :eventId ORDER BY orderIndex ASC")
    suspend fun getMessagesForEventOnce(eventId: Long): List<MascotMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MascotEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<MascotEventEntity>)

    @Update
    suspend fun updateEvent(event: MascotEventEntity)

    @Delete
    suspend fun deleteEvent(event: MascotEventEntity)

    @Query("DELETE FROM mascot_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MascotMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MascotMessageEntity>)

    @Update
    suspend fun updateMessage(message: MascotMessageEntity)

    @Update
    suspend fun updateMessages(messages: List<MascotMessageEntity>)

    @Delete
    suspend fun deleteMessage(message: MascotMessageEntity)

    @Query("DELETE FROM mascot_messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("SELECT COUNT(*) FROM mascot_events")
    suspend fun getEventCount(): Int
}
