package com.pixeleye.einbuergerungstest.lebenindeutschland

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager
import com.pixeleye.einbuergerungstest.lebenindeutschland.navigation.MainNavigationScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings.SettingsViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.BürgertestTheme
import com.pixeleye.einbuergerungstest.lebenindeutschland.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result - no action needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Record that the user opened the app today
        preferenceManager.recordAppOpenedToday()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Load Interstitial Ad
        com.pixeleye.einbuergerungstest.lebenindeutschland.ads.AdManager.loadInterstitial(this)

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