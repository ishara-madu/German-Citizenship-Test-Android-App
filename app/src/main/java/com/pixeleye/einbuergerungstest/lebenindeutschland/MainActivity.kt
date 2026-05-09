package com.pixeleye.einbuergerungstest.lebenindeutschland

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.navigation.MainNavigationScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings.SettingsViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.BürgertestTheme
import com.pixeleye.einbuergerungstest.lebenindeutschland.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val appLanguage by settingsViewModel.appLanguage.collectAsState()
            
            // Handle Theme
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            // Handle Language (Locale)
            val context = LocalContext.current
            val localeContext = remember(appLanguage) {
                LocaleHelper.updateLocale(context, appLanguage)
            }

            // Wrap the app in the localized context and theme
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localeContext
            ) {
                BürgertestTheme(darkTheme = darkTheme) {
                    MainNavigationScreen()
                }
            }
        }
    }
}