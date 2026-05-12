package com.pixeleye.einbuergerungstest.lebenindeutschland.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsOnce(): List<QuestionEntity>


    @Query("SELECT * FROM questions WHERE stateSpecific IS NULL OR stateSpecific = ''")
    suspend fun getGeneralQuestionsOnce(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE stateSpecific = :stateName")
    suspend fun getQuestionsByState(stateName: String): List<QuestionEntity>


    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    suspend fun getBookmarkedQuestionsOnce(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE isMistake = 1")
    fun getMistakeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isMistake = 1")
    suspend fun getMistakeQuestionsOnce(): List<QuestionEntity>

    @Query("UPDATE questions SET isBookmarked = :isBookmarked WHERE id = :questionId")
    suspend fun updateBookmarkStatus(questionId: Int, isBookmarked: Boolean)

    @Query("UPDATE questions SET isMistake = :isMistake WHERE id = :questionId")
    suspend fun updateMistakeStatus(questionId: Int, isMistake: Boolean)

    @Query("SELECT * FROM questions WHERE category = :categoryName")
    suspend fun getQuestionsByCategory(categoryName: String): List<QuestionEntity>

    @Query("SELECT DISTINCT category FROM questions")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM questions")
    fun getQuestionCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoreExisting(questions: List<QuestionEntity>)
}
