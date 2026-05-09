package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.repository.QuestionRepository
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AiExplanationService
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pixeleye.einbuergerungstest.lebenindeutschland.util.TranslationManager


data class QuizUiState(
    val isLoading: Boolean = false,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerChecked: Boolean = false,
    val isQuizFinished: Boolean = false,
    val isExam: Boolean = false,
    val isPreviewMode: Boolean = false,
    val isTranslated: Boolean = false,
    val translatedQuestion: String? = null,
    val translatedOptions: Map<String, String> = emptyMap(),
    val isTranslating: Boolean = false,
    val isDownloadingModel: Boolean = false,
    val downloadProgress: Float = 0f,
    val explanationText: String? = null,
    val isExplanationLoading: Boolean = false
)









@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val cloudSyncService: com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.CloudSyncService,
    private val authService: com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AuthService,
    private val preferenceManager: com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager,
    private val translationManager: TranslationManager
) : ViewModel() {



    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()


    /**
     * Pushes the complete user progress to cloud.
     * Includes all synced fields so that merge works correctly.
     */
    private suspend fun pushFullProgressToCloud() {
        val user = authService.getCurrentUser() ?: return

        val bookmarks = repository.getBookmarkedQuestionsOnce().map { it.id }
        val mistakes = repository.getMistakeQuestionsOnce().map { it.id }

        val currentState = preferenceManager.getSelectedState() ?: "General"

        val progress = mapOf(
            "streak" to preferenceManager.getCurrentStreak(),
            "answered_question_ids" to preferenceManager.getAnsweredQuestionIds().toList(),
            "mastered_questions" to preferenceManager.getMasteredQuestionIds().toList(),
            "exams_completed" to preferenceManager.getExamsCompleted(currentState),
            "total_score_sum" to preferenceManager.getTotalScoreSum(currentState),
            "selected_state" to currentState,
            "bookmarks" to bookmarks,
            "mistakes" to mistakes,
            "last_updated" to com.google.firebase.Timestamp.now()
        )
        cloudSyncService.saveUserProgress(user.uid, progress)
    }




    fun loadMockExam() {
        viewModelScope.launch {
            resetState()
            _uiState.update { it.copy(isLoading = true) }
            
            val selectedState = preferenceManager.getSelectedState() ?: "Bavaria"
            
            val generalQuestions = repository.getGeneralQuestionsOnce().shuffled().take(30)
            val stateQuestions = repository.getQuestionsByState(selectedState).shuffled().take(3)
            
            val examQuestions = (generalQuestions + stateQuestions).shuffled()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    questions = examQuestions,
                    isExam = true
                )
            }
        }
    }


    fun loadAllQuestions() {
        viewModelScope.launch {
            resetState()
            _uiState.update { it.copy(isLoading = true) }
            
            val selectedState = preferenceManager.getSelectedState() ?: "Bavaria"
            val generalQuestions = repository.getGeneralQuestionsOnce()
            val stateQuestions = repository.getQuestionsByState(selectedState)
            
            val allStudyQuestions = (generalQuestions + stateQuestions).shuffled()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    questions = allStudyQuestions
                )
            }
        }
    }

    fun loadQuickReview(count: Int = 10) {
        viewModelScope.launch {
            resetState()
            _uiState.update { it.copy(isLoading = true) }
            
            val selectedState = preferenceManager.getSelectedState() ?: "Bavaria"
            val generalQuestions = repository.getGeneralQuestionsOnce()
            val stateQuestions = repository.getQuestionsByState(selectedState)
            
            val allStudyQuestions = (generalQuestions + stateQuestions).shuffled()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    questions = allStudyQuestions.take(count)
                )
            }
        }
    }


    fun loadStateSpecificQuestions(state: String) {
        viewModelScope.launch {
            resetState()
            _uiState.update { it.copy(isLoading = true, isPreviewMode = true) }
            val stateQuestions = repository.getQuestionsByState(state)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    questions = stateQuestions.shuffled(),
                    isPreviewMode = true
                )
            }
        }
    }


    private fun resetState() {
        _uiState.value = QuizUiState()
    }

    fun loadCategoryMistakes(category: String) {
        viewModelScope.launch {
            resetState()
            _uiState.update { it.copy(isLoading = true) }
            val questions = repository.getQuestionsByCategory(category)
                .filter { it.isMistake }
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    questions = questions
                )
            }
        }
    }



    fun selectAnswer(answer: String) {
        _uiState.update { currentState ->
            if (currentState.isAnswerChecked) return@update currentState
            
            val currentQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex) 
                ?: return@update currentState
                
            val isCorrect = answer == currentQuestion.correctAnswer
            
            // Record statistics
            viewModelScope.launch {
                // If correct, always clear mistake status (so it disappears from Needs Review/Mistakes)
                if (isCorrect) {
                    repository.updateMistakeStatus(currentQuestion.id, false)
                }

                // ONLY update official stats if it's a REAL EXAM (isExam == true)
                // And NOT in preview mode
                if (currentState.isExam && !currentState.isPreviewMode) {
                    if (isCorrect) {
                        preferenceManager.addMasteredQuestion(currentQuestion.id)
                    } else {
                        repository.updateMistakeStatus(currentQuestion.id, true)
                    }

                    preferenceManager.incrementTotalAnswered(currentQuestion.id)
                    pushFullProgressToCloud()
                }
            }






            
            currentState.copy(
                selectedAnswer = answer,
                isAnswerChecked = true,
                score = if (isCorrect) currentState.score + 1 else currentState.score
            )
        }
    }

    fun nextQuestion() {
        _uiState.update { currentState ->
            if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
                currentState.copy(
                    currentQuestionIndex = currentState.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerChecked = false,
                    isTranslated = false, // Reset translation on next question
                    explanationText = null,
                    isExplanationLoading = false
                )
            } else {
                // Quiz Finished - Record exam result if it was an exam
                if (currentState.isExam) {
                    viewModelScope.launch {
                        val selectedState = preferenceManager.getSelectedState() ?: "General"
                        preferenceManager.addExamResult(currentState.score, selectedState)
                        pushFullProgressToCloud()
                    }
                }

                currentState.copy(isQuizFinished = true)
            }
        }
    }

    fun getAiExplanation() {
        val currentState = _uiState.value
        val currentQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return
        
        if (currentState.explanationText != null || currentState.isExplanationLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExplanationLoading = true) }
            
            val appLanguage = preferenceManager.getAppLanguage() ?: "English"
            val language = if (currentState.isTranslated) {
                if (appLanguage.lowercase() == "german" || appLanguage.lowercase() == "english") "English" 
                else appLanguage
            } else "German"
            
            // Use translated text if available, otherwise original
            val questionText = if (currentState.isTranslated && currentState.translatedQuestion != null) {
                currentState.translatedQuestion
            } else {
                currentQuestion.questionText
            }

            val originalOptions = listOf(currentQuestion.optionA, currentQuestion.optionB, currentQuestion.optionC, currentQuestion.optionD)
            val optionsList = if (currentState.isTranslated && currentState.translatedOptions.isNotEmpty()) {
                originalOptions.map { currentState.translatedOptions[it] ?: it }
            } else {
                originalOptions
            }

            val correctAnswerText = if (currentState.isTranslated) {
                currentState.translatedOptions[currentQuestion.correctAnswer] ?: currentQuestion.correctAnswer
            } else {
                currentQuestion.correctAnswer
            }

            try {
                val explanation = AiExplanationService.generateExplanation(
                    question = questionText,
                    options = optionsList,
                    correctAnswer = correctAnswerText,
                    language = language
                )
                
                _uiState.update { 
                    it.copy(
                        isExplanationLoading = false,
                        explanationText = explanation ?: "Could not load explanation. Please try again."
                    )
                }
            } catch (e: Exception) {

                _uiState.update { 
                    it.copy(
                        isExplanationLoading = false,
                        explanationText = "An error occurred while fetching explanation."
                    )
                }
            }
        }
    }


    fun dismissExplanation() {
        _uiState.update { it.copy(explanationText = null, isExplanationLoading = false) }
    }


    fun toggleTranslation() {

        val currentState = _uiState.value
        if (currentState.isTranslated) {
            // Already translated, just toggle off
            _uiState.update { it.copy(isTranslated = false) }
            return
        }

        // Need to translate
        viewModelScope.launch {
            val currentQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return@launch
            val appLanguage = preferenceManager.getAppLanguage() ?: "English"
            
            _uiState.update { it.copy(isTranslating = true) }
            
            // Check if model is already available locally
            val isAlreadyDownloaded = translationManager.isModelDownloaded(appLanguage)
            
            if (!isAlreadyDownloaded) {
                // Ensure model is downloaded
                _uiState.update { it.copy(isDownloadingModel = true, downloadProgress = 0f) }
                
                val progressJob = viewModelScope.launch {
                    var progress = 0f
                    while (progress < 0.9f) {
                        kotlinx.coroutines.delay(100)
                        progress += 0.01f
                        _uiState.update { it.copy(downloadProgress = progress) }
                    }
                }

                val downloaded = translationManager.downloadModelIfNeeded(appLanguage)
                progressJob.cancel()
                
                if (downloaded) {
                    _uiState.update { it.copy(downloadProgress = 1f) }
                    kotlinx.coroutines.delay(200) // Show 100% briefly
                }
                
                _uiState.update { it.copy(isDownloadingModel = false) }
                
                if (!downloaded) {
                    _uiState.update { it.copy(isTranslating = false) }
                    return@launch
                }
            }


            // Translate everything
            val tQuestion = translationManager.translate(currentQuestion.questionText, appLanguage)
            val tOptions = mapOf(
                currentQuestion.optionA to translationManager.translate(currentQuestion.optionA, appLanguage),
                currentQuestion.optionB to translationManager.translate(currentQuestion.optionB, appLanguage),
                currentQuestion.optionC to translationManager.translate(currentQuestion.optionC, appLanguage),
                currentQuestion.optionD to translationManager.translate(currentQuestion.optionD, appLanguage)
            )

            _uiState.update { 
                it.copy(
                    isTranslated = true,
                    isTranslating = false,
                    translatedQuestion = tQuestion,
                    translatedOptions = tOptions
                )
            }
        }
    }


    fun toggleBookmark(questionId: Int) {

        viewModelScope.launch {
            val isBookmarked = _uiState.value.questions.find { it.id == questionId }?.isBookmarked ?: false
            val newStatus = !isBookmarked
            
            repository.updateBookmarkStatus(questionId, newStatus)
            
            _uiState.update { currentState ->
                val updatedQuestions = currentState.questions.map {
                    if (it.id == questionId) it.copy(isBookmarked = newStatus) else it
                }
                currentState.copy(questions = updatedQuestions)
            }
            
            pushFullProgressToCloud()
        }
    }


    fun toggleMistakeStatus(questionId: Int, isMistake: Boolean) {
        viewModelScope.launch {
            repository.updateMistakeStatus(questionId, isMistake)
            pushFullProgressToCloud()
        }
    }
}
