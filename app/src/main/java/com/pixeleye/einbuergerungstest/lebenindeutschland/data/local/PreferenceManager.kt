package com.pixeleye.einbuergerungstest.lebenindeutschland.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


@Singleton
class PreferenceManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("bürgertest_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(getAppLanguage())
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()



    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_DATA_INITIALIZED = "data_initialized"
        private const val KEY_SELECTED_STATE = "selected_state"
        private const val KEY_LAST_ACTIVITY_DATE = "last_activity_date"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_ANSWERED_QUESTION_IDS = "answered_question_ids"
        private const val KEY_MASTERED_QUESTION_IDS = "mastered_question_ids"
        private const val KEY_EXAMS_COMPLETED = "exams_completed"
        private const val KEY_TOTAL_SCORE_SUM = "total_score_sum"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEY_LAST_APP_OPEN_DATE = "last_app_open_date"
    }



    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isDataInitialized(): Boolean {
        return sharedPreferences.getBoolean(KEY_DATA_INITIALIZED, false)
    }

    fun setDataInitialized(initialized: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DATA_INITIALIZED, initialized).apply()
    }

    fun getSelectedState(): String? {
        return sharedPreferences.getString(KEY_SELECTED_STATE, null)
    }

    fun setSelectedState(state: String?) {
        sharedPreferences.edit().putString(KEY_SELECTED_STATE, state).apply()
    }

    fun getCurrentStreak(): Int {
        return sharedPreferences.getInt(KEY_CURRENT_STREAK, 0)
    }

    fun updateStreak() {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastActivity = sharedPreferences.getLong(KEY_LAST_ACTIVITY_DATE, 0)
        val currentStreak = sharedPreferences.getInt(KEY_CURRENT_STREAK, 0)

        if (lastActivity == today) {
            // Already updated today
            return
        }

        val yesterday = today - 24 * 60 * 60 * 1000

        val newStreak = when (lastActivity) {
            0L -> 1 // First time
            yesterday -> currentStreak + 1 // Consecutive day
            else -> 1 // Streak broken
        }

        sharedPreferences.edit()
            .putLong(KEY_LAST_ACTIVITY_DATE, today)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .apply()
    }

    fun setStreak(streak: Int) {
        sharedPreferences.edit()
            .putInt(KEY_CURRENT_STREAK, streak)
            .apply()
    }

    fun getAnsweredQuestionIds(): Set<String> {
        return sharedPreferences.getStringSet(KEY_ANSWERED_QUESTION_IDS, emptySet()) ?: emptySet()
    }

    fun getTotalAnswered(): Int {
        val set = sharedPreferences.getStringSet(KEY_ANSWERED_QUESTION_IDS, emptySet())
        return set?.size ?: 0
    }
    
    fun incrementTotalAnswered(questionId: Int) {
        val currentSet = sharedPreferences.getStringSet(KEY_ANSWERED_QUESTION_IDS, emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet()
        newSet.add(questionId.toString())
        sharedPreferences.edit().putStringSet(KEY_ANSWERED_QUESTION_IDS, newSet).apply()
    }

    fun getMasteredQuestionIds(): Set<String> {
        return sharedPreferences.getStringSet(KEY_MASTERED_QUESTION_IDS, emptySet()) ?: emptySet()
    }

    fun addMasteredQuestion(questionId: Int) {
        val currentSet = sharedPreferences.getStringSet(KEY_MASTERED_QUESTION_IDS, emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet()
        newSet.add(questionId.toString())
        sharedPreferences.edit().putStringSet(KEY_MASTERED_QUESTION_IDS, newSet).apply()
    }

    fun getExamsCompleted(state: String = "General"): Int = 
        sharedPreferences.getInt("${KEY_EXAMS_COMPLETED}_$state", 0)

    fun getTotalScoreSum(state: String = "General"): Int = 
        sharedPreferences.getInt("${KEY_TOTAL_SCORE_SUM}_$state", 0)

    fun addExamResult(score: Int, state: String = "General") {
        val exams = getExamsCompleted(state)
        val scoreSum = getTotalScoreSum(state)
        sharedPreferences.edit()
            .putInt("${KEY_EXAMS_COMPLETED}_$state", exams + 1)
            .putInt("${KEY_TOTAL_SCORE_SUM}_$state", scoreSum + score)
            .apply()
    }

    fun setExamStats(state: String, count: Int, scoreSum: Int) {
        sharedPreferences.edit()
            .putInt("${KEY_EXAMS_COMPLETED}_$state", count)
            .putInt("${KEY_TOTAL_SCORE_SUM}_$state", scoreSum)
            .apply()
    }

    fun getAverageScore(state: String = "General"): Int {
        val exams = getExamsCompleted(state)
        if (exams == 0) return 0
        return getTotalScoreSum(state) / exams
    }

    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, "system") ?: "system"
    }

    fun setThemeMode(mode: String) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun getAppLanguage(): String {
        return sharedPreferences.getString(KEY_APP_LANGUAGE, "German") ?: "German"
    }


    fun setAppLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_APP_LANGUAGE, language).apply()
        _appLanguage.value = language
    }

    // Daily Reminder Preferences
    fun isReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, false)
    }

    fun setReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    fun getReminderHour(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_HOUR, 20) // Default: 8 PM
    }

    fun getReminderMinute(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        sharedPreferences.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun recordAppOpenedToday() {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        sharedPreferences.edit().putLong(KEY_LAST_APP_OPEN_DATE, today).apply()
    }

    fun wasAppOpenedToday(): Boolean {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return sharedPreferences.getLong(KEY_LAST_APP_OPEN_DATE, 0L) == today
    }
}
