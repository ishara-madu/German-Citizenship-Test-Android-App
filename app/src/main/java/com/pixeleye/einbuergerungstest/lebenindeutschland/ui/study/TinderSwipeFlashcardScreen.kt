package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.study

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.quiz.QuizViewModel
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ads.findActivity


data class SwipeFlashcard(
    val id: Int,
    val question: String,
    val answer: String,
    val imageResName: String? = null
)


@Composable
fun TinderSwipeFlashcardScreen(
    onBackClick: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel(),
    mainViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPremium by mainViewModel.isPremium.collectAsState()
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight
    val onSurface = MaterialTheme.colorScheme.onSurface
    val context = androidx.compose.ui.platform.LocalContext.current

    // Local list to manage swiping animations
    val swipeCards = remember { mutableStateListOf<SwipeFlashcard>() }
    
    // Sync local cards with ViewModel questions once they are loaded
    LaunchedEffect(uiState.questions) {
        if (uiState.questions.isNotEmpty() && swipeCards.isEmpty() && !uiState.isLoading) {
            swipeCards.addAll(uiState.questions.map { 
                SwipeFlashcard(it.id, it.questionText, it.correctAnswer, it.imageResName)
            })
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadQuickReview(10)
    }

    // Wrap back click to show interstitial when session is complete
    val handleBackClick: () -> Unit = {
        if (!isPremium && swipeCards.isEmpty() && !uiState.isLoading) {
            val activity = context.findActivity()
            if (activity != null) {
                com.pixeleye.einbuergerungstest.lebenindeutschland.ads.AdManager.showInterstitial(activity) {
                    onBackClick()
                }
            } else {
                onBackClick()
            }
        } else {
            onBackClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Top Navigation Header
        StudyTopBar(handleBackClick, onSurface)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryActionStart)
            } else if (swipeCards.isNotEmpty()) {
                // Render top 3 cards in reverse order for correct stacking
                val count = swipeCards.size
                val visibleCount = minOf(count, 3)
                
                for (i in (visibleCount - 1) downTo 0) {
                    val card = swipeCards[i]
                    key(card.id) {
                        SwipeableCard(
                            flashcard = card,
                            isDark = isDark,
                            stackIndex = i,
                            onSwiped = { 
                                swipeCards.removeAt(0)
                            }
                        )
                    }
                }
            } else if (!uiState.isLoading) {
                EmptySessionState(onSurface)
            }
        }

        // Bottom Controls
        StudyBottomControls(
            isDark = isDark,
            onSwipeLeft = { if (swipeCards.isNotEmpty()) swipeCards.removeAt(0) },
            onSwipeRight = { if (swipeCards.isNotEmpty()) swipeCards.removeAt(0) }
        )
    }
}

@Composable
fun SwipeableCard(
    flashcard: SwipeFlashcard,
    isDark: Boolean,
    stackIndex: Int,
    onSwiped: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val threshold = screenWidth / 3.5f
    
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    
    // Dynamic Scale for background cards
    val baseScale = 1f - (stackIndex * 0.05f)
    val baseOffsetY = (stackIndex * 20).dp
    
    // Track if threshold haptic was already fired
    var hasFiredHapticThreshold by remember { mutableStateOf(false) }

    // 3D Flip State
    var isFlipped by remember { mutableStateOf(false) }
    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessLow
        ),
        label = "flip"
    )

    val cardModifier = if (stackIndex == 0) {
        Modifier
            .padding(32.dp)
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                // Dynamic Tilt: Proportional to X offset
                rotationZ = (offsetX.value / 15f).coerceIn(-15f, 15f)
                rotationY = flipRotation
                cameraDistance = 15f * density
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isFlipped = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            
                            // Trigger haptic when crossing threshold
                            val currentX = abs(offsetX.value)
                            if (currentX > threshold && !hasFiredHapticThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                hasFiredHapticThreshold = true
                            } else if (currentX <= threshold) {
                                hasFiredHapticThreshold = false
                            }
                        }
                    },
                    onDragEnd = {
                        if (abs(offsetX.value) > threshold) {
                            // Fling off screen
                            coroutineScope.launch {
                                val targetX = if (offsetX.value > 0) screenWidth * 1.5f else -screenWidth * 1.5f
                                // Precise fling animation
                                offsetX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                )
                                onSwiped()
                            }
                        } else {
                            // Snap back with spring
                            coroutineScope.launch {
                                launch { offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)) }
                                launch { offsetY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)) }
                            }
                        }
                    }
                )
            }
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFlipped = !isFlipped 
            }
    } else {
        // Background Card Animation (Scale up as top card moves)
        val dragProgress = (abs(offsetX.value) / screenWidth).coerceIn(0f, 1f)
        val animatedScale = baseScale + (0.05f * dragProgress)
        val animatedOffset = baseOffsetY - (20.dp * dragProgress)
        
        Modifier
            .padding(32.dp)
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .offset(y = animatedOffset)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = (0.4f + (0.6f * dragProgress)).coerceIn(0f, 1f)
            }
    }

    Card(
        modifier = cardModifier.shadow(
            elevation = if (stackIndex == 0) 12.dp else 2.dp,
            shape = RoundedCornerShape(32.dp),
            spotColor = Color.Black.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Front / Back Content
            if (flipRotation <= 90f || flipRotation >= 270f) {
                CardContent(
                    title = stringResource(id = R.string.flashcard_question), 
                    text = flashcard.question, 
                    imageResName = flashcard.imageResName,
                    isDark = isDark
                )
            } else {
                Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                    CardContent(
                        title = stringResource(id = R.string.flashcard_answer), 
                        text = flashcard.answer, 
                        isDark = isDark, 
                        titleColor = AnswerCorrectGreen
                    )

                }
            }

            // Proportional Overlay Tints
            if (stackIndex == 0) {
                val swipeAlpha = (abs(offsetX.value) / threshold).coerceIn(0f, 0.6f)
                
                if (offsetX.value > 0) {
                    SwipeOverlay(AnswerCorrectGreen, Icons.Rounded.Check, swipeAlpha, Alignment.TopStart)
                } else if (offsetX.value < 0) {
                    SwipeOverlay(AnswerWrongRed, Icons.Rounded.Close, swipeAlpha, Alignment.TopEnd)
                }
            }
        }
    }
}

@Composable
private fun SwipeOverlay(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, alpha: Float, alignment: Alignment) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = alpha))
            .padding(24.dp),
        contentAlignment = alignment
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = alpha),
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun StudyTopBar(onBackClick: () -> Unit, onSurface: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = onSurface)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(stringResource(id = R.string.quick_review), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = onSurface)
            Text(stringResource(id = R.string.swipe_categorized_study), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@Composable
private fun StudyBottomControls(isDark: Boolean, onSwipeLeft: () -> Unit, onSwipeRight: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(AnswerWrongRed, Icons.Rounded.Close, onSwipeLeft)
        Spacer(modifier = Modifier.width(48.dp))
        ActionButton(AnswerCorrectGreen, Icons.Rounded.Check, onSwipeRight)
    }
}

@Composable
private fun ActionButton(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(68.dp).clip(CircleShape).clickable { onClick() },
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.padding(18.dp))
    }
}

@Composable
private fun EmptySessionState(onSurface: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = CircleShape, color = AnswerCorrectGreen.copy(alpha = 0.1f), modifier = Modifier.size(100.dp)) {
            Icon(Icons.Rounded.Check, contentDescription = "Done", tint = AnswerCorrectGreen, modifier = Modifier.padding(24.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(id = R.string.daily_review_done), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = onSurface)
        Text(stringResource(id = R.string.mastered_questions), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
fun CardContent(
    title: String, 
    text: String, 
    isDark: Boolean, 
    imageResName: String? = null,
    titleColor: Color? = null
) {
    val onSurface = if (isDark) Color.White else Color(0xFF1E293B)
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title.uppercase(), 
            fontSize = 12.sp, 
            fontWeight = FontWeight.ExtraBold, 
            letterSpacing = 2.sp, 
            color = titleColor ?: (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
        )
        
        Spacer(modifier = Modifier.height(16.dp))

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
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Text(
            text = text, 
            fontSize = 22.sp, 
            fontWeight = FontWeight.Bold, 
            color = onSurface, 
            textAlign = TextAlign.Center, 
            lineHeight = 32.sp
        )


    }
}
