package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*


import androidx.compose.material.icons.rounded.ViewCarousel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ads.findActivity

@Composable
fun MainDashboardScreen(
    modifier: Modifier = Modifier,
    onStartExamClick: () -> Unit = {},
    onStartTrainingClick: () -> Unit = {},
    onStateSelectionClick: () -> Unit = {},
    onProgressClick: () -> Unit = {},
    onQuickReviewClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReviewMistakesClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    mainViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel = androidx.hilt.navigation.compose.hiltViewModel(),

    authViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val isDark = LocalIsDarkTheme.current
    val streakCount = mainViewModel.getCurrentStreak()
    val authState by authViewModel.authState.collectAsState()
    val isAnonymous by authViewModel.isAnonymous.collectAsState()
    val user by authViewModel.currentUser.collectAsState()
    val overallProgress by mainViewModel.overallProgress.collectAsState()
    val levelTitleRes by mainViewModel.currentLevelTitle.collectAsState()
    val selectedState = mainViewModel.getSelectedState() ?: "Bavaria"
    val isPremium by mainViewModel.isPremium.collectAsState()


    val guestStr = stringResource(id = R.string.profile_guest)
    val learnerStr = stringResource(id = R.string.profile_learner)
    val userName = remember(user, isAnonymous, guestStr, learnerStr) {
        if (isAnonymous || user == null) guestStr else user?.displayName?.ifBlank { user?.email?.substringBefore("@") } ?: learnerStr
    }

    val profileImageUrl = remember(user) {
        user?.photoUrl?.toString()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top Header Row
            TopHeaderRow(isDark, streakCount, userName, profileImageUrl, isPremium, onProfileClick, onPremiumClick)

            // Center/Lower Focus: Hero Action Section
            val context = androidx.compose.ui.platform.LocalContext.current
            HeroActionCard(
                levelTitleRes = levelTitleRes,
                onClick = {
                    if (!isPremium) {
                        val activity = context.findActivity()
                        if (activity != null) {
                            com.pixeleye.einbuergerungstest.lebenindeutschland.ads.AdManager.showInterstitial(activity) {
                                onStartExamClick()
                            }
                        } else {
                            onStartExamClick()
                        }
                    } else {
                        onStartExamClick()
                    }
                }
            )

            // Secondary Actions: Bento Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StateSpecificCard(
                    modifier = Modifier.weight(1f), 
                    isDark = isDark,
                    stateName = selectedState ?: "Bavaria",
                    onClick = onStateSelectionClick
                )
                ProgressTrackerCard(
                    modifier = Modifier.weight(1f), 
                    isDark = isDark,
                    progress = overallProgress,
                    onClick = onProgressClick
                )
            }

            // Quick Review Swipe Action
            QuickReviewSwipeCard(onClick = onQuickReviewClick, isDark = isDark)

            // Bottom Bento Row: Focus Area & Bookmarks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                SmallActionCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(id = R.string.mistakes),
                    subtitle = stringResource(id = R.string.review_errors),
                    icon = Icons.Rounded.ErrorOutline,
                    color = RedAccent,
                    isDark = isDark,
                    onClick = {
                        if (!isPremium) {
                            val activity = context.findActivity()
                            if (activity != null) {
                                com.pixeleye.einbuergerungstest.lebenindeutschland.ads.AdManager.showInterstitial(activity) {
                                    onReviewMistakesClick()
                                }
                            } else {
                                onReviewMistakesClick()
                            }
                        } else {
                            onReviewMistakesClick()
                        }
                    }
                )
                SmallActionCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(id = R.string.bookmarks),
                    subtitle = stringResource(id = R.string.saved_items),
                    icon = Icons.Rounded.BookmarkBorder,
                    color = PrimaryActionStart,
                    isDark = isDark,
                    onClick = onBookmarksClick
                )

            }

            if (!isPremium) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    com.pixeleye.einbuergerungstest.lebenindeutschland.ads.BannerAdView()
                }
            }


            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
        }
    }
}




@Composable
fun QuickReviewSwipeCard(onClick: () -> Unit, isDark: Boolean) {
    val surfaceColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = YellowAccent.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ViewCarousel,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.padding(14.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.quick_review_swipe),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = stringResource(id = R.string.daily_flashcard_desc),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SmallActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val surfaceColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    
    Card(
        modifier = modifier
            .height(110.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )            }
        }
    }
}


@Composable
fun TopHeaderRow(
    isDark: Boolean,
    streakCount: Int,
    userName: String,
    profileImageUrl: String?,
    isPremium: Boolean,
    onProfileClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onProfileClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Avatar
            Surface(
                shape = CircleShape,
                color = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight,
                shadowElevation = 8.dp,
                modifier = Modifier.size(56.dp)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val avatarResId = if (profileImageUrl != null && profileImageUrl.startsWith("avatar_")) {
                    context.resources.getIdentifier(profileImageUrl, "drawable", context.packageName)
                } else 0

                if (avatarResId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = avatarResId),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                Text(
                    text = stringResource(id = R.string.greeting_morning),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium Upgrade Badge
            if (!isPremium) {
                Surface(
                    shape = CircleShape,
                    color = YellowAccent.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onPremiumClick() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = "Upgrade to Premium",
                        tint = YellowAccent,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Daily Streak Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = StreakFlame,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (streakCount == 1) stringResource(id = R.string.stat_one_day) else stringResource(id = R.string.stat_days, streakCount),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = StreakFlame,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HeroActionCard(levelTitleRes: Int, onClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current
    var isExpanded by remember { mutableStateOf(false) }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(PrimaryActionStart, PrimaryActionEnd)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = PrimaryActionStart.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section of the card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.thirty_three_questions),
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    // Animated Interactive Star Icon
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = if (isExpanded) 0.3f else 0.2f),
                        modifier = Modifier
                            .animateContentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isExpanded = !isExpanded }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(if (isExpanded) 12.dp else 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isExpanded) {
                                Text(
                                    text = stringResource(id = levelTitleRes),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Level",
                                tint = YellowAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Bottom section of the card
                Column {
                    Text(
                        text = stringResource(id = R.string.btn_start_exam),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.start_now),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PrimaryActionStart,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StateSpecificCard(
    modifier: Modifier = Modifier, 
    isDark: Boolean, 
    stateName: String,
    onClick: () -> Unit = {}
) {

    ErgonomicBentoCard(modifier = modifier.aspectRatio(0.85f).clickable { onClick() }, isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = RedAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Location",
                    tint = RedAccent,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column {
                Text(
                    text = stateName,
                    style = MaterialTheme.typography.labelLarge.copy(color = RedAccent)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.state_specific),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ProgressTrackerCard(
    modifier: Modifier = Modifier, 
    isDark: Boolean, 
    progress: Float,
    onClick: () -> Unit = {}
) {

    ErgonomicBentoCard(modifier = modifier.aspectRatio(0.85f).clickable { onClick() }, isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Chart
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryActionStart,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Column {
                Text(
                    text = stringResource(id = R.string.progress_title),
                    style = MaterialTheme.typography.labelLarge.copy(color = PrimaryActionStart)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.smart_tracker),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ErgonomicBentoCard(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val cardColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            spotColor = Color.Black.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        content()
    }
}


