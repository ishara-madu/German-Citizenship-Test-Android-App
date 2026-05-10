package com.pixeleye.einbuergerungstest.lebenindeutschland.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.dashboard.MainDashboardScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings.SettingsScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.flashcard.LearningFlashcardScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz.QuizViewModel

@Composable
fun MainContainerScreen(
    rootNavController: NavHostController,
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    quizViewModel: QuizViewModel
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            AnimatedBottomNavBar(
                items = Screen.bottomNavItems,
                currentRoute = currentDestination?.route,
                onItemClick = { screen ->
                    nestedNavController.navigate(screen.route) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier // Removed padding(innerPadding) to allow bar to float
        ) {
            composable(Screen.Dashboard.route) {
                MainDashboardScreen(
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel,
                    onStartExamClick = { 
                        quizViewModel.loadMockExam()
                        rootNavController.navigate(Screen.ExamSimulator.route) 
                    },
                    onStartTrainingClick = {
                        quizViewModel.loadAllQuestions()
                        rootNavController.navigate(Screen.ExamSimulator.route)
                    },
                    onStateSelectionClick = { rootNavController.navigate(Screen.StateSelection.route) },
                    onProgressClick = { rootNavController.navigate(Screen.Profile.route) },
                    onQuickReviewClick = { rootNavController.navigate(Screen.TinderFlashcard.route) },
                    onProfileClick = { rootNavController.navigate(Screen.Profile.route) },
                    onReviewMistakesClick = { rootNavController.navigate(Screen.ReviewMistakes.route) },
                    onBookmarksClick = { rootNavController.navigate(Screen.Bookmarks.route) },
                    onPremiumClick = { rootNavController.navigate(Screen.PremiumPaywall.route) }
                )
            }

            composable(Screen.Learning.route) {
                LearningFlashcardScreen(
                    onBackClick = { 
                        // If we are in the nested nav, we might just want to go to Dashboard
                        nestedNavController.navigate(Screen.Dashboard.route) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) { inclusive = false }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { 
                        nestedNavController.navigate(Screen.Dashboard.route) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) { inclusive = false }
                        }
                    },
                    onPremiumClick = { rootNavController.navigate(Screen.PremiumPaywall.route) }
                )
            }
        }
    }
}
