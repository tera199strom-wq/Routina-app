package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionRouteDao {

    @Query("SELECT * FROM question_routes ORDER BY id ASC")
    fun getAllRoutes(): Flow<List<QuestionRouteEntity>>

    @Query("SELECT * FROM question_routes ORDER BY id ASC")
    suspend fun getAllRoutesList(): List<QuestionRouteEntity>

    @Query("SELECT * FROM question_routes WHERE parentEventId = :parentEventId")
    fun getRoutesForParent(parentEventId: Long): Flow<List<QuestionRouteEntity>>

    @Query("SELECT * FROM question_routes WHERE parentEventId = :parentEventId")
    suspend fun getRoutesForParentSync(parentEventId: Long): List<QuestionRouteEntity>

    @Query("SELECT * FROM question_routes WHERE parentEventId = :parentEventId AND answerCondition = :answer LIMIT 1")
    suspend fun getRouteForParentAndAnswer(parentEventId: Long, answer: String): QuestionRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: QuestionRouteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoutes(routes: List<QuestionRouteEntity>)

    @Update
    suspend fun updateRoute(route: QuestionRouteEntity)

    @Delete
    suspend fun deleteRoute(route: QuestionRouteEntity)

    @Query("DELETE FROM question_routes WHERE parentEventId = :parentEventId")
    suspend fun deleteRoutesByParent(parentEventId: Long)

    @Query("DELETE FROM question_routes WHERE parentEventId = :parentEventId AND answerCondition = :answer")
    suspend fun deleteRouteByParentAndAnswer(parentEventId: Long, answer: String)
}
