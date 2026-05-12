package com.pixeleye.einbuergerungstest.lebenindeutschland

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager
import com.pixeleye.einbuergerungstest.lebenindeutschland.navigation.AppNavHost
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.components.GlobalSnackbarOverlay
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
            
            val navController = rememberNavController()
            
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
                LocalContext provides localeContext
            ) {
                BürgertestTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .systemBarsPadding()
                        ) {
                            AppNavHost(navController = navController)
                            GlobalSnackbarOverlay()
                        }
                    }
                }
            }
        }
    }
}