package com.pixeleye.einbuergerungstest.lebenindeutschland.ui
import android.graphics.Typeface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.res.ResourcesCompat
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                3 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.profile_learner
                4 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_student
                5 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_researcher
                6 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_expert
                7 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_advanced
                8 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_scholar
                9 -> com.pixeleye.einbuergerungstest.lebenindeutschland.R.string.level_master
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
        observeAuthChanges()
        observeQuestionCount()
        observeSubjectMastery()
        observeBookmarkedQuestions()
        observeMistakeQuestions()
    }

    private fun observeAuthChanges() {
        viewModelScope.launch {
            authService.userFlow.collect { user ->
                if (user != null) {
                    syncWithCloud()
                }
            }
        }
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
                // Restore Selected State
                (cloudData["selected_state"] as? String)?.let { preferenceManager.setSelectedState(it) }
                
                // Restore Streak
                (cloudData["streak"] as? Long)?.toInt()?.let { cloudStreak ->
                    if (cloudStreak > preferenceManager.getCurrentStreak()) {
                        // For now, we trust the higher number
                        // In a real app, we might compare "last_updated" timestamps
                        preferenceManager.setStreak(cloudStreak)
                    }
                }

                // Restore Mastered Questions
                (cloudData["mastered_questions"] as? List<*>)?.forEach { id ->
                    (id as? String)?.toIntOrNull()?.let { preferenceManager.addMasteredQuestion(it) }
                }

                // Restore Answered Question IDs
                (cloudData["answered_question_ids"] as? List<*>)?.forEach { id ->
                    (id as? String)?.toIntOrNull()?.let { preferenceManager.incrementTotalAnswered(it) }
                }

                // Restore Bookmarks
                (cloudData["bookmarks"] as? List<*>)?.forEach { id ->
                    val qId = (id as? Long)?.toInt() ?: (id as? String)?.toIntOrNull()
                    qId?.let { repository.updateBookmarkStatus(it, true) }
                }

                // Restore Mistakes
                (cloudData["mistakes"] as? List<*>)?.forEach { id ->
                    val qId = (id as? Long)?.toInt() ?: (id as? String)?.toIntOrNull()
                    qId?.let { repository.updateMistakeStatus(it, true) }
                }

                // Restore Exam Stats for the selected state
                val restoreState = (cloudData["selected_state"] as? String)?.takeIf { it.isNotEmpty() } ?: "General"
                (cloudData["exams_completed"] as? Long)?.toInt()?.let { cloudExams ->
                    val localExams = preferenceManager.getExamsCompleted(restoreState)
                    if (cloudExams > localExams) {
                        val cloudScore = (cloudData["total_score_sum"] as? Long)?.toInt() ?: 0
                        preferenceManager.setExamStats(restoreState, cloudExams, cloudScore)
                    }
                }
            }
            
            // 2. Push current local to cloud as initial backup
            pushProgressToCloud()
        }
    }
    private suspend fun pushProgressToCloud() {
        val user = authService.getCurrentUser() ?: return
        
        // Fetch Bookmarks and Mistakes from local DB
        val bookmarks = repository.getBookmarkedQuestionsOnce().map { it.id }
        val mistakes = repository.getMistakeQuestionsOnce().map { it.id }

        // Read exam stats from the selected state (where they are actually stored)
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


    fun openPdfFolder() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        val downloadFolder = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val uri = android.net.Uri.parse(downloadFolder.path)
        intent.setDataAndType(uri, "resource/folder")
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: Just open the default file manager or downloads app
            val fallbackIntent = android.content.Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS)
            fallbackIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                // Last resort: Log or show toast (not possible here directly without context access safely)
            }
        }
    }

    fun exportMistakesToPdf(onResult: (String?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val mistakes = _mistakeQuestions.value
            if (mistakes.isEmpty()) {
                onResult(null)
                return@launch
            }

            // Load Custom Fonts
            val poppinsBold = try { ResourcesCompat.getFont(context, R.font.poppins_bold) } catch (e: Exception) { Typeface.DEFAULT_BOLD }
            val poppinsRegular = try { ResourcesCompat.getFont(context, R.font.poppins_regular) } catch (e: Exception) { Typeface.DEFAULT }

            val pdfDocument = PdfDocument()
            
            val titlePaint = Paint().apply {
                typeface = poppinsBold
                textSize = 22f
                color = AndroidColor.BLACK
            }
            val questionPaint = Paint().apply {
                typeface = poppinsBold
                textSize = 16f
                color = AndroidColor.BLACK
            }
            val answerPaint = Paint().apply {
                typeface = poppinsRegular
                textSize = 14f
                color = AndroidColor.BLACK
            }
            val bulletPaint = Paint().apply {
                typeface = poppinsRegular
                textSize = 14f
                color = AndroidColor.BLACK
            }

            var pageNumber = 1
            val pageWidth = 595
            val pageHeight = 842
            val margin = 50f
            val contentWidth = pageWidth - (margin * 2)

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var y = margin + 20f

            canvas.drawText("Mistakes Review - ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())}", margin, y, titlePaint)
            y += 50f

            mistakes.forEachIndexed { index, question ->
                val imageResName = question.imageResName
                val bitmap = if (!imageResName.isNullOrEmpty()) {
                    val resId = context.resources.getIdentifier(imageResName, "drawable", context.packageName)
                    if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
                } else null

                val imageHeight = if (bitmap != null) {
                    val scale = contentWidth / bitmap.width.toFloat()
                    (bitmap.height * scale).coerceAtMost(200f)
                } else 0f

                // Check if we need a new page (considering image + question + answer approx)
                if (y + imageHeight + 100f > pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin + 20f
                }

               

                // Draw Question (Numbered)
                val questionLabel = "${index + 1}. "
                val labelWidth = questionPaint.measureText(questionLabel)
                canvas.drawText(questionLabel, margin, y, questionPaint)
                
                val qWords = question.questionText.split(" ")
                var qLine = ""
                var firstLine = true
                qWords.forEach { word ->
                    val testLine = if (qLine.isEmpty()) word else "$qLine $word"
                    val currentMargin = if (firstLine) margin + labelWidth else margin + labelWidth
                    if (questionPaint.measureText(testLine) > (pageWidth - currentMargin - margin)) {
                        canvas.drawText(qLine, currentMargin, y, questionPaint)
                        y += questionPaint.textSize + 8f
                        qLine = word
                        firstLine = false
                        
                        if (y > pageHeight - margin) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            y = margin + 20f
                            firstLine = true // Reset margin for new page line
                        }
                    } else {
                        qLine = testLine
                    }
                }
                canvas.drawText(qLine, if (firstLine) margin + labelWidth else margin + labelWidth, y, questionPaint)
                y += 30f

                 // Draw Image if exists
                if (bitmap != null) {
                    val scale = contentWidth / bitmap.width.toFloat()
                    val targetWidth = contentWidth
                    val targetHeight = (bitmap.height * scale).coerceAtMost(200f)
                    val destRect = android.graphics.RectF(margin, y, margin + targetWidth, y + targetHeight)
                    canvas.drawBitmap(bitmap, null, destRect, null)
                    y += targetHeight + 20f
                    bitmap.recycle() // Free memory
                }

                // Draw Answer (Bulleted)
                val bullet = "\u2022 "
                val bulletWidth = answerPaint.measureText(bullet)
                val answerIndent = margin + 20f
                canvas.drawText(bullet, answerIndent, y, bulletPaint)
                
                val aWords = question.correctAnswer.split(" ")
                var aLine = ""
                aWords.forEach { word ->
                    val testLine = if (aLine.isEmpty()) word else "$aLine $word"
                    if (answerPaint.measureText(testLine) > (pageWidth - answerIndent - bulletWidth - margin)) {
                        canvas.drawText(aLine, answerIndent + bulletWidth, y, answerPaint)
                        y += answerPaint.textSize + 6f
                        aLine = word
                        
                        if (y > pageHeight - margin) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            y = margin + 20f
                        }
                    } else {
                        aLine = testLine
                    }
                }
                canvas.drawText(aLine, answerIndent + bulletWidth, y, answerPaint)
                y += 40f // Space between items
            }

            pdfDocument.finishPage(page)

            val fileName = "German_Citizenship_Mistakes_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

            try {
                pdfDocument.writeTo(FileOutputStream(file))
                onResult(file.absolutePath)
            } catch (e: Exception) {
                onResult(null)
            } finally {
                pdfDocument.close()
            }
        }
    }



    fun addExamResult(score: Int) {
        preferenceManager.addExamResult(score)
        viewModelScope.launch { pushProgressToCloud() }
    }
}

