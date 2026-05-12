package com.pixeleye.einbuergerungstest.lebenindeutschland.data.repository

import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionDao
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) {
    fun getAllQuestions(): Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    suspend fun getAllQuestionsOnce(): List<QuestionEntity> = questionDao.getAllQuestionsOnce()
    fun getQuestionCount(): Flow<Int> = questionDao.getQuestionCount()

    
    fun getAllCategories(): Flow<List<String>> = questionDao.getAllCategories()
    
    suspend fun getQuestionsByCategory(category: String): List<QuestionEntity> = 
        questionDao.getQuestionsByCategory(category)

    suspend fun getQuestionsByState(state: String): List<QuestionEntity> = 
        questionDao.getQuestionsByState(state)

    suspend fun getGeneralQuestionsOnce(): List<QuestionEntity> = 
        questionDao.getGeneralQuestionsOnce()


    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>> = 
        questionDao.getBookmarkedQuestions()

    suspend fun getBookmarkedQuestionsOnce(): List<QuestionEntity> = 
        questionDao.getBookmarkedQuestionsOnce()

    fun getMistakeQuestions(): Flow<List<QuestionEntity>> = 
        questionDao.getMistakeQuestions()

    suspend fun getMistakeQuestionsOnce(): List<QuestionEntity> = 
        questionDao.getMistakeQuestionsOnce()

    suspend fun updateBookmarkStatus(questionId: Int, isBookmarked: Boolean) {
        questionDao.updateBookmarkStatus(questionId, isBookmarked)
    }

    suspend fun updateMistakeStatus(questionId: Int, isMistake: Boolean) {
        questionDao.updateMistakeStatus(questionId, isMistake)
    }

    suspend fun insertQuestions(questions: List<QuestionEntity>) {
        questionDao.insertAll(questions)
    }

    suspend fun insertIgnoreExisting(questions: List<QuestionEntity>) {
        questionDao.insertIgnoreExisting(questions)
    }
}
