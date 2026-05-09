package com.pixeleye.einbuergerungstest.lebenindeutschland.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.pixeleye.einbuergerungstest.lebenindeutschland.R


sealed class Screen(val route: String, @StringRes val titleRes: Int? = null, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", R.string.nav_home, Icons.Rounded.Home)
    object Learning : Screen("learning", R.string.nav_learn, Icons.Rounded.School)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Rounded.AccountCircle)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Rounded.Settings)

    
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
        val bottomNavItems = listOf(
            Dashboard,
            Learning,
            Settings
        )

    }
}
