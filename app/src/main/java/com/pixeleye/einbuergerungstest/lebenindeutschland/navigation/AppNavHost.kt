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
import com.pixeleye.einbuergerungstest.lebenindeutschland.ads.findActivity

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    // Shared ViewModels
    val quizViewModel: QuizViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val startDestination = if (mainViewModel.isOnboardingCompleted()) Screen.MainContainer.route else Screen.Onboarding.route

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
                    navController.navigate(Screen.MainContainer.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Screen.MainContainer.route) {
            MainContainerScreen(
                rootNavController = navController,
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                quizViewModel = quizViewModel
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
            val isPremium by mainViewModel.isPremium.collectAsState()
            ReviewMistakesScreen(
                questions = mistakes,
                onBackClick = { navController.popBackStack() },
                onRemoveMistake = { mainViewModel.toggleMistake(it) },
                onExportPdfClick = { onComplete ->
                    mainViewModel.exportMistakesToPdf(onComplete)
                },
                onViewDownloadsClick = {
                    mainViewModel.openPdfFolder()
                },
                isPremium = isPremium,
                onPremiumClick = { navController.navigate(Screen.PremiumPaywall.route) }
            )
        }


        composable(Screen.Bookmarks.route) {
            val bookmarks by mainViewModel.bookmarkedQuestions.collectAsState()
            val isPremium by mainViewModel.isPremium.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            BookmarksScreen(
                questions = bookmarks,
                onBackClick = {
                    if (!isPremium) {
                        val activity = context.findActivity()
                        if (activity != null) {
                            com.pixeleye.einbuergerungstest.lebenindeutschland.ads.AdManager.showInterstitial(activity) {
                                navController.popBackStack()
                            }
                        } else {
                            navController.popBackStack()
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onToggleBookmark = { questionId: Int -> mainViewModel.toggleBookmark(questionId) },
                isPremium = isPremium
            )
        }


        
        composable(Screen.ExamSimulator.route) {
            val uiState by quizViewModel.uiState.collectAsState()
            val isPremium by mainViewModel.isPremium.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            ExamSimulatorScreen(
                quizUiState = uiState,
                onAnswerSelected = { quizViewModel.selectAnswer(it) },
                onNextClicked = { quizViewModel.nextQuestion() },
                onCloseClick = {
                    if (!isPremium) {
                        val activity = context.findActivity()
                        if (activity != null) {
                            com.pixeleye.einbuergerungstest.lebenindeutschland.ads.AdManager.showInterstitial(activity) {
                                navController.popBackStack()
                            }
                        } else {
                            navController.popBackStack()
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onBookmarkToggle = { quizViewModel.toggleBookmark(it) },
                onTranslateClick = {
                    if (isPremium || quizViewModel.canUseTranslation()) {
                        quizViewModel.toggleTranslation()
                    } else {
                        navController.navigate(Screen.PremiumPaywall.route)
                    }
                },
                onExplainClick = {
                    if (isPremium || quizViewModel.canUseAiExplanation()) {
                        quizViewModel.getAiExplanation()
                    } else {
                        navController.navigate(Screen.PremiumPaywall.route)
                    }
                },
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
                onCloseClick = { 
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.MainContainer.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
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
                            navController.navigate(Screen.MainContainer.route) {
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
                        navController.navigate(Screen.MainContainer.route) {
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

