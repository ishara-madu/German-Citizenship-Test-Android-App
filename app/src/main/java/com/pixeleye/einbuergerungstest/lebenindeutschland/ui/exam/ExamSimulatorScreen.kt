package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.exam

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz.QuizUiState
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R


@Composable
fun ExamSimulatorScreen(
    quizUiState: QuizUiState,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit,
    onCloseClick: () -> Unit,
    onBookmarkToggle: (Int) -> Unit,
    onTranslateClick: () -> Unit = {},
    onExplainClick: () -> Unit = {},
    onDismissExplanation: () -> Unit = {}
) {



    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight

    if (quizUiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryActionStart)
        }
        return
    }

    if (quizUiState.isQuizFinished) {
        QuizResultScreen(quizUiState, onCloseClick)
        return
    }

    val currentQuestion = quizUiState.questions.getOrNull(quizUiState.currentQuestionIndex) ?: return
    
    if (quizUiState.isTranslating) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                if (quizUiState.isDownloadingModel) {
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
                        progress = { quizUiState.downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = PrimaryActionStart,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "${(quizUiState.downloadProgress * 100).toInt()}%",
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
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ExamHeaderRow(
                current = quizUiState.currentQuestionIndex + 1,
                total = quizUiState.questions.size,
                isBookmarked = currentQuestion.isBookmarked,
                onBookmarkToggle = { onBookmarkToggle(currentQuestion.id) },
                onCloseClick = onCloseClick,
                isExam = quizUiState.isExam,
                isTranslated = quizUiState.isTranslated,
                onTranslateClick = onTranslateClick
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            ExamQuestionCard(
                question = currentQuestion,
                selectedAnswer = quizUiState.selectedAnswer,
                isAnswerChecked = quizUiState.isAnswerChecked,
                isTranslated = quizUiState.isTranslated,
                translatedQuestion = quizUiState.translatedQuestion,
                translatedOptions = quizUiState.translatedOptions,
                onAnswerSelected = onAnswerSelected
            )

            // AI Explanation Sheet
            if (quizUiState.explanationText != null || quizUiState.isExplanationLoading) {
                com.pixeleye.einbuergerungstest.lebenindeutschland.ui.flashcard.ExplanationBottomSheet(
                    explanation = quizUiState.explanationText,
                    isLoading = quizUiState.isExplanationLoading,
                    onDismiss = onDismissExplanation
                )

            }


            Spacer(modifier = Modifier.height(24.dp))
        }

        ExamBottomNavigationBar(
            isNextEnabled = quizUiState.isAnswerChecked,
            isExam = quizUiState.isExam,
            onNextClicked = onNextClicked,
            onExplainClicked = onExplainClick
        )


    }
}

@Composable
fun ExamHeaderRow(
    current: Int,
    total: Int,
    isBookmarked: Boolean,
    isExam: Boolean,
    isTranslated: Boolean,
    onBookmarkToggle: () -> Unit,
    onCloseClick: () -> Unit,
    onTranslateClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Quit Exam",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Q $current / $total",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onBookmarkToggle) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) YellowAccent else MaterialTheme.colorScheme.onSurfaceVariant

                )
            }
            
            if (!isExam) {
                IconButton(onClick = onTranslateClick) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = "Translate",
                        tint = if (isTranslated) PrimaryActionStart else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun ExamQuestionCard(
    question: QuestionEntity,
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (question.imageResName != null) {
                val context = LocalContext.current
                val imageResId = context.resources.getIdentifier(
                    question.imageResName, "drawable", context.packageName
                )
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "Question Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Text(
                text = if (isTranslated && translatedQuestion != null) translatedQuestion else question.questionText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                ),


                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                options.forEach { option ->
                    val isCorrectOption = option == question.correctAnswer
                    val isSelectedOption = option == selectedAnswer
                    val displayText = if (isTranslated) translatedOptions[option] ?: option else option
                    
                    ExamAnswerOption(
                        text = displayText,
                        isSelected = isSelectedOption,
                        isCorrect = if (isAnswerChecked) isCorrectOption else null,
                        isEnabled = !isAnswerChecked,
                        onClick = { onAnswerSelected(option) }
                    )

                }
            }
        }
    }
}

@Composable
fun ExamAnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed && isEnabled) 0.98f else 1f, label = "scale")

    val containerColor by animateColorAsState(
        targetValue = when {
            isCorrect == true -> AnswerCorrectGreen.copy(alpha = 0.15f)
            isCorrect == false && isSelected -> AnswerWrongRed.copy(alpha = 0.15f)
            isSelected -> PrimaryActionStart.copy(alpha = 0.1f)
            else -> if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
        }, label = "bgColor"
    )

    val contentColor = when {
        isCorrect == true -> AnswerCorrectGreen
        isCorrect == false && isSelected -> AnswerWrongRed
        isSelected -> PrimaryActionStart
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isCorrect == true -> AnswerCorrectGreen
        isCorrect == false && isSelected -> AnswerWrongRed
        isSelected -> PrimaryActionStart
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
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

            Box(modifier = Modifier.size(24.dp)) {
                if (isCorrect == true) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Correct",
                        tint = AnswerCorrectGreen,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isCorrect == false && isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Incorrect",
                        tint = AnswerWrongRed,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ExamBottomNavigationBar(
    isNextEnabled: Boolean,
    isExam: Boolean,
    onNextClicked: () -> Unit,
    onExplainClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExam) Arrangement.End else Arrangement.SpaceBetween
        ) {
            if (!isExam) {
                OutlinedButton(
                    onClick = onExplainClicked,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PrimaryActionStart.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryActionStart),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_explain_ai), fontWeight = FontWeight.Bold)

                }
            }


            Button(

                onClick = onNextClicked,
                enabled = isNextEnabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryActionStart,
                    disabledContainerColor = PrimaryActionStart.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_next),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.NavigateNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun QuizResultScreen(quizUiState: QuizUiState, onCloseClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight
    val mainViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val isPremium by mainViewModel.isPremium.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().background(bgColor).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.quiz_finished),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.your_score, quizUiState.score, quizUiState.questions.size),
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryActionStart
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (!isPremium) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                com.pixeleye.einbuergerungstest.lebenindeutschland.ads.BannerAdView()
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(
            onClick = onCloseClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(id = R.string.back_to_dashboard), fontWeight = FontWeight.Bold)
        }

    }
}
