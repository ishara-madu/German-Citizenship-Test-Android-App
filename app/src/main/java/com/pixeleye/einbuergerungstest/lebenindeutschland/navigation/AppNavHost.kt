package com.pixeleye.einbuergerungstest.lebenindeutschland.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.dashboard.MainDashboardScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.exam.ExamSimulatorScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.flashcard.LearningFlashcardScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.paywall.PremiumPaywallScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.profile.ProgressProfileScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings.SettingsScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.states.StateSelectionScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.study.TinderSwipeFlashcardScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.onboarding.OnboardingScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.LoginScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.SignUpScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthState
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.review.BookmarksScreen
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.review.ReviewMistakesScreen

import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz.QuizViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    // Shared ViewModels
    val quizViewModel: QuizViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val startDestination = remember {
        if (mainViewModel.isOnboardingCompleted()) Screen.Dashboard.route else Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = { state ->
                    mainViewModel.setSelectedState(state)
                    mainViewModel.setOnboardingCompleted()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Screen.Dashboard.route) {
            MainDashboardScreen(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onStartExamClick = { 
                    quizViewModel.loadMockExam()
                    navController.navigate(Screen.ExamSimulator.route) 
                },
                onStartTrainingClick = {
                    quizViewModel.loadAllQuestions()
                    navController.navigate(Screen.ExamSimulator.route)
                },
                onStateSelectionClick = { navController.navigate(Screen.StateSelection.route) },
                onProgressClick = { navController.navigate(Screen.Profile.route) },
                onQuickReviewClick = { navController.navigate(Screen.TinderFlashcard.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onReviewMistakesClick = { navController.navigate(Screen.ReviewMistakes.route) },
                onBookmarksClick = { navController.navigate(Screen.Bookmarks.route) },
                onPremiumClick = { navController.navigate(Screen.PremiumPaywall.route) }
            )
        }

        
        composable(Screen.Learning.route) {
            LearningFlashcardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.route) {
            ProgressProfileScreen(
                onBackClick = { navController.popBackStack() },
                onBindAccountClick = { 
                    navController.navigate(Screen.Login.createRoute(isLinkingMode = true))
                },
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onReviewCategoryClick = { category ->
                    quizViewModel.loadCategoryMistakes(category)
                    navController.navigate(Screen.ExamSimulator.route)
                },

                onStateEditClick = {
                    navController.navigate(Screen.StateSelection.route)
                }
            )


        }
        
        composable(Screen.ReviewMistakes.route) {
            val mistakes by mainViewModel.mistakeQuestions.collectAsState()
            ReviewMistakesScreen(
                questions = mistakes,
                onBackClick = { navController.popBackStack() },
                onRemoveMistake = { questionId: Int -> mainViewModel.toggleMistake(questionId) },
                onExportPdfClick = { callback ->
                    mainViewModel.exportMistakesToPdf(callback)
                },
                onViewDownloadsClick = { mainViewModel.openPdfFolder() }
            )
        }


        composable(Screen.Bookmarks.route) {
            val bookmarks by mainViewModel.bookmarkedQuestions.collectAsState()
            BookmarksScreen(
                questions = bookmarks,
                onBackClick = { navController.popBackStack() },
                onToggleBookmark = { questionId: Int -> mainViewModel.toggleBookmark(questionId) }
            )
        }


        composable(Screen.Settings.route) {

            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ExamSimulator.route) {
            val uiState by quizViewModel.uiState.collectAsState()
            ExamSimulatorScreen(
                quizUiState = uiState,
                onAnswerSelected = { quizViewModel.selectAnswer(it) },
                onNextClicked = { quizViewModel.nextQuestion() },
                onCloseClick = { navController.popBackStack() },
                onBookmarkToggle = { quizViewModel.toggleBookmark(it) },
                onTranslateClick = { quizViewModel.toggleTranslation() },
                onExplainClick = { quizViewModel.getAiExplanation() },
                onDismissExplanation = { quizViewModel.dismissExplanation() }
            )

        }
        
        composable(Screen.StateSelection.route) {
            StateSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onStateConfirmed = { state ->
                    quizViewModel.loadStateSpecificQuestions(state)
                    navController.navigate(Screen.ExamSimulator.route)
                }
            )
        }
        
        composable(Screen.PremiumPaywall.route) {
            PremiumPaywallScreen(
                onCloseClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TinderFlashcard.route) {
            TinderSwipeFlashcardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Login.route,
            arguments = listOf(
                navArgument("isLinkingMode") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val isLinkingMode = backStackEntry.arguments?.getBoolean("isLinkingMode") ?: false
            val authState by authViewModel.authState.collectAsState()
            
            val isAnonymous by authViewModel.isAnonymous.collectAsState()
            
            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    if (!isAnonymous) {
                        if (isLinkingMode) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            }

            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { email, pass -> authViewModel.signInWithEmail(email, pass) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { email -> authViewModel.sendPasswordResetEmail(email) },
                onResetState = { authViewModel.resetState() },
                authState = authState
            )

        }

        composable(Screen.SignUp.route) {
            val authState by authViewModel.authState.collectAsState()
            
            val isAnonymous by authViewModel.isAnonymous.collectAsState()
            
            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    if (!isAnonymous) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }

            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { email, pass, name, avatar -> 
                    authViewModel.signUpWithEmail(email, pass, name, avatar) 
                },
                onLoginClick = { navController.popBackStack() },
                onResetState = { authViewModel.resetState() },
                authState = authState
            )

        }
    }
}

