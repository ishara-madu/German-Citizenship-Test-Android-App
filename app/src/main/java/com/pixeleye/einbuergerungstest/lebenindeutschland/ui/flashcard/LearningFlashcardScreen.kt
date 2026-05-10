package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.flashcard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark

import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.AutoAwesome


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz.QuizUiState
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz.QuizViewModel
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*


@Composable
fun LearningFlashcardScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
    mainViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPremium by mainViewModel.isPremium.collectAsState()
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight

    LaunchedEffect(Unit) {
        viewModel.loadAllQuestions()
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryActionStart)
        }
        return
    }

    val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
    
    if (uiState.isTranslating) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                if (uiState.isDownloadingModel) {
                    Text(
                        stringResource(id = R.string.downloading_model),
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryActionStart,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(id = R.string.download_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = { uiState.downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = PrimaryActionStart,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "${(uiState.downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryActionStart,
                        fontWeight = FontWeight.Bold
                    )

                } else {
                    CircularProgressIndicator(color = PrimaryActionStart)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.translating), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        return
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TopNavigationAndProgress(
            current = uiState.currentQuestionIndex + 1,
            total = uiState.questions.size,
            isBookmarked = currentQuestion?.isBookmarked ?: false,
            isTranslated = uiState.isTranslated,
            onBookmarkToggle = { currentQuestion?.id?.let { viewModel.toggleBookmark(it) } },
            onTranslateToggle = { viewModel.toggleTranslation() },
            onBackClick = onBackClick
        )


        Spacer(modifier = Modifier.height(32.dp))
        
        if (uiState.isQuizFinished) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = R.string.great_job),
                        style = MaterialTheme.typography.displaySmall,
                        color = PrimaryActionStart
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.reviewed_all_questions),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBackClick) {
                        Text(stringResource(id = R.string.back_to_dashboard))
                    }
                }

            }
        } else if (currentQuestion != null) {
            FlashcardHeroArea(
                modifier = Modifier.weight(1f),
                question = currentQuestion.questionText,
                imageResName = currentQuestion.imageResName,
                options = listOf(currentQuestion.optionA, currentQuestion.optionB, currentQuestion.optionC, currentQuestion.optionD),
                correctAnswer = currentQuestion.correctAnswer,
                selectedAnswer = uiState.selectedAnswer,
                isAnswerChecked = uiState.isAnswerChecked,
                isTranslated = uiState.isTranslated,
                translatedQuestion = uiState.translatedQuestion,
                translatedOptions = uiState.translatedOptions,
                onAnswerSelected = { viewModel.selectAnswer(it) }
            )

            // AI Explanation Sheet
            if (uiState.explanationText != null || uiState.isExplanationLoading) {
                ExplanationBottomSheet(
                    explanation = uiState.explanationText,
                    isLoading = uiState.isExplanationLoading,
                    onDismiss = { viewModel.dismissExplanation() }
                )

            }



        } else if (!uiState.isLoading && uiState.questions.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_questions_found), textAlign = TextAlign.Center)
            }
        }

        if (!isPremium) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                com.pixeleye.einbuergerungstest.lebenindeutschland.ads.BannerAdView()
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        BottomUtilityBar(
            onNextClick = { viewModel.nextQuestion() },
            onExplainClick = { viewModel.getAiExplanation() },
            isNextEnabled = uiState.isAnswerChecked,
            current = uiState.currentQuestionIndex + 1,
            total = uiState.questions.size
        )


        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TopNavigationAndProgress(

    current: Int, 
    total: Int, 
    isBookmarked: Boolean,
    isTranslated: Boolean,
    onBookmarkToggle: () -> Unit,
    onTranslateToggle: () -> Unit,
    onBackClick: () -> Unit = {}
) {


    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Progress Bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        ) {
            val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = PrimaryActionStart,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }


        // Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onTranslateToggle) {
                Icon(
                    imageVector = Icons.Filled.Translate,
                    contentDescription = "Translate",
                    tint = if (isTranslated) PrimaryActionStart else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onBookmarkToggle) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) YellowAccent else MaterialTheme.colorScheme.onSurface
                )
            }
        }


    }
}

@Composable
fun FlashcardHeroArea(
    modifier: Modifier = Modifier,
    question: String,
    imageResName: String?,
    options: List<String>,
    correctAnswer: String,
    selectedAnswer: String?,
    isAnswerChecked: Boolean,
    isTranslated: Boolean,
    translatedQuestion: String?,
    translatedOptions: Map<String, String>,
    onAnswerSelected: (String) -> Unit
) {


    val isDark = LocalIsDarkTheme.current
    val cardBg = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Real Question Image
            if (imageResName != null) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val imageResId = context.resources.getIdentifier(
                    imageResName, "drawable", context.packageName
                )
                if (imageResId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = imageResId),
                        contentDescription = "Question Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }


            // Question Text
            Text(
                text = if (isTranslated && translatedQuestion != null) translatedQuestion else question,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                ),


                modifier = Modifier.weight(1f)
            )


            // Answers
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { option ->
                    val isCorrect = option == correctAnswer
                    val isSelected = option == selectedAnswer
                    val displayText = if (isTranslated) translatedOptions[option] ?: option else option
                    
                    val state = when {
                        !isAnswerChecked -> AnswerState.DEFAULT
                        isCorrect -> AnswerState.CORRECT
                        isSelected -> AnswerState.WRONG
                        else -> AnswerState.DEFAULT
                    }

                    AnswerOptionButton(
                        text = displayText,
                        state = state,
                        isEnabled = !isAnswerChecked,
                        onClick = { onAnswerSelected(option) }
                    )
                }

            }
        }
    }
}

enum class AnswerState {
    DEFAULT, CORRECT, WRONG
}

@Composable
fun AnswerOptionButton(
    text: String,
    state: AnswerState,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")

    // Determine colors based on state
    val containerColor = when (state) {
        AnswerState.DEFAULT -> if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
        AnswerState.CORRECT -> AnswerCorrectGreen
        AnswerState.WRONG -> AnswerWrongRed.copy(alpha = 0.15f)
    }

    val contentColor = when (state) {
        AnswerState.DEFAULT -> MaterialTheme.colorScheme.onSurface
        AnswerState.CORRECT -> Color.White
        AnswerState.WRONG -> AnswerWrongRed
    }

    val borderColor = when (state) {
        AnswerState.DEFAULT -> Color.Transparent
        AnswerState.CORRECT -> AnswerCorrectGreen
        AnswerState.WRONG -> AnswerWrongRed
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = isEnabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )

            )

            Spacer(modifier = Modifier.width(12.dp))

            // Icons for correct/wrong - Always present in layout but invisible if DEFAULT to prevent shift
            Box(modifier = Modifier.size(24.dp)) {
                when (state) {
                    AnswerState.CORRECT -> {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Correct",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AnswerState.WRONG -> {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Wrong",
                            tint = AnswerWrongRed,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AnswerState.DEFAULT -> { /* Empty box keeps space */ }
                }
            }
        }
    }
}

@Composable
fun BottomUtilityBar(onNextClick: () -> Unit, onExplainClick: () -> Unit, isNextEnabled: Boolean, current: Int, total: Int) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$current / $total",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

        // Explain (AI) Button
        OutlinedButton(
            onClick = onExplainClick,

            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PrimaryActionStart.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryActionStart
            )
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.btn_explain_ai),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }


        // Next Button (Added to replace dummy Translate for functional learning)
        Button(
            onClick = onNextClick,
            enabled = isNextEnabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryActionStart
            )
        ) {
            Text(
                text = stringResource(id = R.string.btn_next),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplanationBottomSheet(
    explanation: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    // Resolve strings outside ModalBottomSheet to preserve localized context
    val aiExplanationTitle = stringResource(id = R.string.ai_explanation)
    val loadingAiText = stringResource(id = R.string.loading_ai)
    val noExplanationText = stringResource(id = R.string.no_explanation)
    val gotItText = stringResource(id = R.string.got_it)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (LocalIsDarkTheme.current) GamifiedSurfaceDark else Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PrimaryActionStart.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = PrimaryActionStart,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = aiExplanationTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                CircularProgressIndicator(color = PrimaryActionStart)
                Spacer(modifier = Modifier.height(16.dp))
                Text(loadingAiText, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    text = explanation ?: noExplanationText,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionStart)
            ) {
                Text(gotItText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
