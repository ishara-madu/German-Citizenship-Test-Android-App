package com.pixeleye.einbuergerungstest.lebenindeutschland.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.pixeleye.einbuergerungstest.lebenindeutschland.R


sealed class Screen(val route: String, @StringRes val titleRes: Int? = null, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", R.string.nav_home, Icons.Outlined.Home)
    object Learning : Screen("learning", R.string.nav_learn, Icons.Outlined.School)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Outlined.AccountCircle)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Outlined.Settings)
    object MainContainer : Screen("main_container")

    
    object ExamSimulator : Screen("exam_simulator")
    object StateSelection : Screen("state_selection")
    object PremiumPaywall : Screen("premium_paywall")
    object TinderFlashcard : Screen("tinder_flashcard")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login?isLinkingMode={isLinkingMode}") {
        fun createRoute(isLinkingMode: Boolean) = "login?isLinkingMode=$isLinkingMode"
    }
    object SignUp : Screen("sign_up")
    object ReviewMistakes : Screen("review_mistakes")
    object Bookmarks : Screen("bookmarks")


    companion object {
        val bottomNavItems get() = listOf(
            Dashboard,
            Learning,
            Settings
        )

    }
}
