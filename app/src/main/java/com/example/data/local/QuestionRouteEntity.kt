package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_routes")
data class QuestionRouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentEventId: Long,
    val answerCondition: String, // "YES" or "NO"
    val nextEventId: Long? = null, // Sub-question if branching further
    val targetScheduleId: Long? = null, // Resolved ScheduleEntity.id if direct result
    val createdAt: Long = System.currentTimeMillis()
)
