package com.pixeleye.einbuergerungstest.lebenindeutschland.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager

import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionJsonParser
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AuthService
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.CloudSyncService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: QuestionRepository,
    private val cloudSyncService: CloudSyncService,
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    data class CategoryMastery(
        val name: String,
        val progress: Float,
        val totalQuestions: Int,
        val masteredQuestions: Int,
        val mistakesCount: Int
    )


    private val _totalQuestionCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val totalQuestionCount: kotlinx.coroutines.flow.StateFlow<Int> = _totalQuestionCount

    private val _selectedStateFlow = kotlinx.coroutines.flow.MutableStateFlow(preferenceManager.getSelectedState())
    val selectedStateFlow: kotlinx.coroutines.flow.StateFlow<String?> = _selectedStateFlow


    private val _subjectMastery = kotlinx.coroutines.flow.MutableStateFlow<List<CategoryMastery>>(emptyList())
    val subjectMastery: kotlinx.coroutines.flow.StateFlow<List<CategoryMastery>> = _subjectMastery

    private val _weakestCategory = kotlinx.coroutines.flow.MutableStateFlow<CategoryMastery?>(null)
    val weakestCategory: kotlinx.coroutines.flow.StateFlow<CategoryMastery?> = _weakestCategory

    private val _mistakeCategories = kotlinx.coroutines.flow.MutableStateFlow<List<CategoryMastery>>(emptyList())
    val mistakeCategories: kotlinx.coroutines.flow.StateFlow<List<CategoryMastery>> = _mistakeCategories

    private val _overallProgress = kotlinx.coroutines.flow.MutableStateFlow(0f)
    val overallProgress: kotlinx.coroutines.flow.StateFlow<Float> = _overallProgress

    val currentLevelTitle: kotlinx.coroutines.flow.StateFlow<Int> = _overallProgress
        .map { progress ->
            val level = ((progress * 9).toInt() + 1).coerceIn(1, 10)
            when(level) {
                1 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_beginner
                2 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_novice
                3 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_student
                4 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_researcher
                5 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_expert
                6 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_advanced
                7 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_scholar
                8 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_master
                else -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_citizen
            }
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_beginner)


    private val _bookmarkedQuestions = kotlinx.coroutines.flow.MutableStateFlow<List<QuestionEntity>>(emptyList())
    val bookmarkedQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _bookmarkedQuestions

    private val _mistakeQuestions = kotlinx.coroutines.flow.MutableStateFlow<List<QuestionEntity>>(emptyList())
    val mistakeQuestions: kotlinx.coroutines.flow.StateFlow<List<QuestionEntity>> = _mistakeQuestions








    init {
        initializeData()
        updateStreak()
        syncWithCloud()
        observeQuestionCount()
        observeSubjectMastery()
        observeBookmarkedQuestions()
        observeMistakeQuestions()
    }



    private fun observeSubjectMastery() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.getAllQuestions(),
                selectedStateFlow
            ) { allQuestions, selectedState ->
                Pair(allQuestions, selectedState)
            }.collect { (allQuestions, selectedState) ->
                val masteredIds = preferenceManager.getMasteredQuestionIds()
                
                // Filter questions: only null state or selected state
                val filteredQuestions = allQuestions.filter { 
                    it.stateSpecific == null || it.stateSpecific == "" || it.stateSpecific == selectedState 
                }

                val masteryList = filteredQuestions
                    .groupBy { it.category }
                    .map { (category, questions) ->
                        val total = questions.size
                        val mastered = questions.count { it.id.toString() in masteredIds }
                        val mistakes = questions.count { it.isMistake }
                        CategoryMastery(
                            name = category,
                            progress = if (total > 0) mastered.toFloat() / total else 0f,
                            totalQuestions = total,
                            masteredQuestions = mastered,
                            mistakesCount = mistakes
                        )
                    }
                    .sortedByDescending { it.progress }
                
                _subjectMastery.value = masteryList
                _totalQuestionCount.value = filteredQuestions.size
                
                // Identify weakest category
                _weakestCategory.value = masteryList
                    .filter { it.progress < 1.0f }
                    .minByOrNull { it.progress }
                
                // Identify categories that have active mistakes
                _mistakeCategories.value = masteryList
                    .filter { it.mistakesCount > 0 }
                    .sortedByDescending { it.mistakesCount }

                // Calculate overall progress
                val totalQ = masteryList.sumOf { it.totalQuestions }
                val masteredQ = masteryList.sumOf { it.masteredQuestions }
                _overallProgress.value = if (totalQ > 0) masteredQ.toFloat() / totalQ else 0f
            }
        }
    }






    private fun observeBookmarkedQuestions() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.getBookmarkedQuestions(),
                selectedStateFlow
            ) { questions, selectedState ->
                questions.filter { it.stateSpecific == null || it.stateSpecific == "" || it.stateSpecific == selectedState }
            }.collect { filtered ->
                _bookmarkedQuestions.value = filtered
            }
        }
    }

    private fun observeMistakeQuestions() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.getMistakeQuestions(),
                selectedStateFlow
            ) { questions, selectedState ->
                questions.filter { it.stateSpecific == null || it.stateSpecific == "" || it.stateSpecific == selectedState }
            }.collect { filtered ->
                _mistakeQuestions.value = filtered
            }
        }
    }



    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            val allQuestions = repository.getAllQuestionsOnce()
            val question = allQuestions.find { it.id == questionId }
                ?: return@launch
            repository.updateBookmarkStatus(questionId, !question.isBookmarked)
            pushProgressToCloud()
        }
    }

    fun toggleMistake(questionId: Int) {
        viewModelScope.launch {
            repository.updateMistakeStatus(questionId, false)
            pushProgressToCloud()
        }
    }


    private fun observeQuestionCount() {
        // Handled in observeSubjectMastery now to stay in sync
    }



    private fun syncWithCloud() {
        viewModelScope.launch {
            val user = authService.getCurrentUser() ?: return@launch
            
            // 1. Try to pull latest from cloud
            val cloudData = cloudSyncService.getUserProgress(user.uid)
            if (cloudData != null) {
                // If cloud has newer or existing data, update local
                (cloudData["selected_state"] as? String)?.let { preferenceManager.setSelectedState(it) }
                // Note: Streak sync logic could be more complex (comparing timestamps), 
                // but for now we trust the cloud as backup
            }
            
            // 2. Push current local to cloud as initial backup
            pushProgressToCloud()
        }
    }
    private suspend fun pushProgressToCloud() {
        val user = authService.getCurrentUser() ?: return
        val progress = mapOf(
            "streak" to preferenceManager.getCurrentStreak(),
            "total_answered" to preferenceManager.getTotalAnswered(),
            "mastered_questions" to preferenceManager.getMasteredQuestionIds().toList(),
            "exams_completed" to preferenceManager.getExamsCompleted(),
            "total_score_sum" to preferenceManager.getTotalScoreSum(),
            "selected_state" to (preferenceManager.getSelectedState() ?: ""),
            "last_updated" to com.google.firebase.Timestamp.now()
        )

        cloudSyncService.saveUserProgress(user.uid, progress)
    }

    private fun initializeData() {
        viewModelScope.launch {
            val count = repository.getQuestionCount().first()
            if (!preferenceManager.isDataInitialized() || count == 0) {
                val questions = QuestionJsonParser.parseQuestionsFromJson(context)
                if (questions.isNotEmpty()) {
                    repository.insertQuestions(questions)
                    preferenceManager.setDataInitialized(true)
                }
            }
        }
    }


    fun updateStreak() {
        preferenceManager.updateStreak()
        viewModelScope.launch { pushProgressToCloud() }
    }

    fun setSelectedState(state: String?) {
        preferenceManager.setSelectedState(state)
        _selectedStateFlow.value = state
        viewModelScope.launch { pushProgressToCloud() }
    }


    fun getCurrentStreak(): Int {
        return preferenceManager.getCurrentStreak()
    }

    fun getExamsCompleted(): Int {
        val state = selectedStateFlow.value ?: "General"
        return preferenceManager.getExamsCompleted(state)
    }

    fun getAverageScore(): Int {
        val state = selectedStateFlow.value ?: "General"
        return preferenceManager.getAverageScore(state)
    }

    fun getTotalAnswered(): Int {
        val masteredIds = preferenceManager.getMasteredQuestionIds()
        val allMastered = masteredIds.mapNotNull { it.toIntOrNull() }
        
        // We can't easily get the full filtered list synchronously here without re-calculating
        // or using the current state of _totalQuestionCount/subjectMastery.
        // Let's use the current mastery list if available.
        return _subjectMastery.value.sumOf { it.masteredQuestions }
    }


    fun isOnboardingCompleted(): Boolean {
        return preferenceManager.isOnboardingCompleted()
    }

    fun setOnboardingCompleted() {
        preferenceManager.setOnboardingCompleted(true)
    }

    fun getSelectedState() = preferenceManager.getSelectedState()

    fun incrementTotalAnswered(questionId: Int) {

        preferenceManager.incrementTotalAnswered(questionId)
        viewModelScope.launch { pushProgressToCloud() }
    }

    fun addMasteredQuestion(questionId: Int) {
        preferenceManager.addMasteredQuestion(questionId)
        viewModelScope.launch { pushProgressToCloud() }
    }


    fun addExamResult(score: Int) {
        preferenceManager.addExamResult(score)
        viewModelScope.launch { pushProgressToCloud() }
    }
}

