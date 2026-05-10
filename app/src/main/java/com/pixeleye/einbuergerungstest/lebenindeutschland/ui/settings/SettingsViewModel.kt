package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager
import com.pixeleye.einbuergerungstest.lebenindeutschland.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<String> = preferenceManager.themeMode
    val appLanguage: StateFlow<String> = preferenceManager.appLanguage

    private val _isReminderEnabled = MutableStateFlow(preferenceManager.isReminderEnabled())
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(preferenceManager.getReminderHour())
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(preferenceManager.getReminderMinute())
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    private val _isCloudSyncEnabled = MutableStateFlow(preferenceManager.isCloudSyncEnabled())
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    fun toggleCloudSync(enabled: Boolean) {
        preferenceManager.setCloudSyncEnabled(enabled)
        _isCloudSyncEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        preferenceManager.setThemeMode(mode)
    }

    fun setAppLanguage(language: String) {
        preferenceManager.setAppLanguage(language)
    }

    fun enableReminder(hour: Int, minute: Int) {
        preferenceManager.setReminderEnabled(true)
        preferenceManager.setReminderTime(hour, minute)
        _isReminderEnabled.value = true
        _reminderHour.value = hour
        _reminderMinute.value = minute
        ReminderScheduler.schedule(context, hour, minute)
    }

    fun disableReminder() {
        preferenceManager.setReminderEnabled(false)
        _isReminderEnabled.value = false
        ReminderScheduler.cancel(context)
    }
}
