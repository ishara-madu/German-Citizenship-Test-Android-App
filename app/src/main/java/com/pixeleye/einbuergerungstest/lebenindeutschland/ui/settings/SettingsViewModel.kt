package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings

import androidx.lifecycle.ViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val themeMode: StateFlow<String> = preferenceManager.themeMode
    val appLanguage: StateFlow<String> = preferenceManager.appLanguage

    fun setThemeMode(mode: String) {
        preferenceManager.setThemeMode(mode)
    }

    fun setAppLanguage(language: String) {
        preferenceManager.setAppLanguage(language)
    }
}
