package com.example.data.repository

import com.example.data.local.QuestionRouteDao
import com.example.data.local.QuestionRouteEntity
import kotlinx.coroutines.flow.Flow

class QuestionRepository(
    private val questionRouteDao: QuestionRouteDao
) {
    val allRoutes: Flow<List<QuestionRouteEntity>> = questionRouteDao.getAllRoutes()

    fun getRoutesForParent(parentEventId: Long): Flow<List<QuestionRouteEntity>> {
        return questionRouteDao.getRoutesForParent(parentEventId)
    }

    suspend fun getRoutesForParentSync(parentEventId: Long): List<QuestionRouteEntity> {
        return questionRouteDao.getRoutesForParentSync(parentEventId)
    }

    suspend fun getRouteForParentAndAnswer(parentEventId: Long, answer: String): QuestionRouteEntity? {
        return questionRouteDao.getRouteForParentAndAnswer(parentEventId, answer)
    }

    suspend fun saveRoute(
        parentEventId: Long,
        answer: String,
        nextEventId: Long?,
        targetScheduleId: Long?
    ): Long {
        val existing = questionRouteDao.getRouteForParentAndAnswer(parentEventId, answer)
        return if (existing != null) {
            val updated = existing.copy(
                nextEventId = nextEventId,
                targetScheduleId = targetScheduleId
            )
            questionRouteDao.updateRoute(updated)
            existing.id
        } else {
            questionRouteDao.insertRoute(
                QuestionRouteEntity(
                    parentEventId = parentEventId,
                    answerCondition = answer,
                    nextEventId = nextEventId,
                    targetScheduleId = targetScheduleId
                )
            )
        }
    }

    suspend fun deleteRoute(route: QuestionRouteEntity) {
        questionRouteDao.deleteRoute(route)
    }

    suspend fun deleteRoutesByParent(parentEventId: Long) {
        questionRouteDao.deleteRoutesByParent(parentEventId)
    }

    suspend fun deleteRouteByParentAndAnswer(parentEventId: Long, answer: String) {
        questionRouteDao.deleteRouteByParentAndAnswer(parentEventId, answer)
    }
}
