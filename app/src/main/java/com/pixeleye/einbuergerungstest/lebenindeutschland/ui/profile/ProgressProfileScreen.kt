package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*

import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow



@Composable
fun ProgressProfileScreen(
    onBackClick: () -> Unit = {},
    onBindAccountClick: () -> Unit = {},
    onReviewCategoryClick: (String) -> Unit = {},
    onStateEditClick: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),

    mainViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel = hiltViewModel()
) {
    val isDark = LocalIsDarkTheme.current
    val streakCount = mainViewModel.getCurrentStreak()
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight
    val isGuestAccount by authViewModel.isAnonymous.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val user by authViewModel.currentUser.collectAsState()
 
    val userName = if (isGuestAccount || user == null) stringResource(id = R.string.profile_guest) else user?.displayName?.ifBlank { user?.email?.substringBefore("@") } ?: stringResource(id = R.string.profile_learner)
    val userEmail = if (!isGuestAccount) user?.email ?: "" else ""
    val profileImageUrl = user?.photoUrl?.toString()



    var showEditDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showStateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }


    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentName = userName,
            currentAvatar = profileImageUrl,
            isGuest = isGuestAccount,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName, newAvatar ->
                if (newName != userName || newAvatar != profileImageUrl) {
                    authViewModel.updateProfile(newName, newAvatar)
                }
                showEditDialog = false
            },
            onResetPassword = {
                authViewModel.sendPasswordResetEmail(userEmail)
                showEditDialog = false
            }
        )
    }

    LaunchedEffect(Unit) {
        authViewModel.resetState()
    }

    LaunchedEffect(authState) {
        if (authState is com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthState.ActionSuccess) {
            // Optional: show a toast or snackbar
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (authState is com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthState.ActionSuccess) {
            Text(
                text = (authState as com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthState.ActionSuccess).message,
                color = Color(0xFF2E7D32), // Success Green
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (authState is com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthState.Error) {
            Text(
                text = (authState as com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        val overallProgress by mainViewModel.overallProgress.collectAsState()
        
        val level = ((overallProgress * 9).toInt() + 1).coerceIn(1, 10)
        val levelTitleResId by mainViewModel.currentLevelTitle.collectAsState()
        val levelName = stringResource(id = levelTitleResId)

        val selectedState = mainViewModel.getSelectedState() ?: "Bayern" // Default is Bavaria/Bayern


        ProfileHeader(
            onBackClick = onBackClick,
            userName = userName,
            profileImageUrl = profileImageUrl,
            userEmail = userEmail,
            level = level,
            levelTitle = levelName,
            selectedState = selectedState,
            onStateEditClick = { showStateDialog = true },
            onEditClick = { if (!isGuestAccount) showEditDialog = true }
        )



        if (isGuestAccount) {
            Spacer(modifier = Modifier.height(24.dp))
            GuestAccountCard(onBindAccountClick)
        }
        
        if (showStateDialog) {
            StateSelectionDialog(
                currentState = selectedState,
                onDismiss = { showStateDialog = false },
                onStateSelected = { newState ->
                    mainViewModel.setSelectedState(newState)
                    showStateDialog = false
                }
            )
        }

        
        Spacer(modifier = Modifier.height(32.dp))
        
        val totalAnswered = mainViewModel.getTotalAnswered()
        val totalQuestions by mainViewModel.totalQuestionCount.collectAsState()
        val examsCompleted = mainViewModel.getExamsCompleted()
        val avgScore = mainViewModel.getAverageScore()
        val subjectMastery by mainViewModel.subjectMastery.collectAsState()
        val weakestCategory by mainViewModel.weakestCategory.collectAsState()
        val mistakeCategories by mainViewModel.mistakeCategories.collectAsState()


        QuickStatsGrid(
            isDark = isDark,
            streakCount = streakCount,
            totalAnswered = totalAnswered,
            totalQuestions = totalQuestions,
            examsCompleted = examsCompleted,
            avgScore = avgScore
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        SubjectMasterySection(isDark, subjectMastery)
        
        // Needs Review Section - ONLY shown if there are active mistakes
        if (mistakeCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(id = R.string.needs_review),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                mistakeCategories.forEach { category ->
                    FocusAreaCard(
                        isDark = isDark, 
                        category = category,
                        onReviewClick = { onReviewCategoryClick(category.name) }
                    )
                }
            }
        }



        
        if (!isGuestAccount) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_logout),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )

            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun GuestAccountCard(onBindAccountClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFF7ED) // Soft Amber/Orange tint
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFD97706)
                )
                Text(
                    text = stringResource(id = R.string.guest_warning_title),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFF92400E)
                )
            }
            Text(
                text = stringResource(id = R.string.guest_warning_desc),
                color = Color(0xFFB45309),
                lineHeight = 20.sp
            )

            Button(
                onClick = onBindAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text(
                    text = stringResource(id = R.string.btn_bind_account),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

            }
        }
    }
}

@Composable
fun ProfileHeader(
    onBackClick: () -> Unit,
    userName: String,
    profileImageUrl: String?,
    userEmail: String,
    level: Int,
    levelTitle: String,
    selectedState: String,
    onStateEditClick: () -> Unit,
    onEditClick: () -> Unit
) {


    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val displayLarge = MaterialTheme.typography.displayLarge
    val labelLarge = MaterialTheme.typography.labelLarge

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = onSurface
                )
            }

            if (userEmail.isNotBlank()) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Profile",
                        tint = onSurface
                    )
                }
            }
        }


        Surface(
            shape = CircleShape,
            color = surfaceVariant,
            modifier = Modifier.size(100.dp)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val avatarResId = if (profileImageUrl != null && profileImageUrl.startsWith("avatar_")) {
                context.resources.getIdentifier(profileImageUrl, "drawable", context.packageName)
            } else 0

            if (avatarResId != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = avatarResId),
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else if (profileImageUrl != null) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    tint = onSurfaceVariant
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = onSurface,
            style = displayLarge
        )

        if (userEmail.isNotBlank()) {
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = onSurfaceVariant,
                style = labelLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PrimaryActionStart.copy(alpha = 0.1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Level",
                    tint = YellowAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(id = R.string.level_format, level, levelTitle),

                    color = PrimaryActionStart,
                    fontWeight = FontWeight.Bold,
                    style = labelLarge
                )

            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State/Region Row
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = "State",
                    tint = PrimaryActionStart,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = selectedState,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    style = labelLarge
                )
                
                VerticalDivider(modifier = Modifier.height(16.dp).padding(horizontal = 4.dp))

                Text(
                    text = stringResource(id = R.string.btn_edit),
                    color = PrimaryActionStart,
                    fontWeight = FontWeight.Bold,
                    style = labelLarge,
                    modifier = Modifier.clickable { onStateEditClick() }
                )

            }
        }
    }
}


@Composable
fun QuickStatsGrid(
    isDark: Boolean, 
    streakCount: Int,
    totalAnswered: Int,
    totalQuestions: Int,
    examsCompleted: Int,
    avgScore: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                isDark = isDark,
                value = if (streakCount == 1) stringResource(id = R.string.stat_one_day) else stringResource(id = R.string.stat_days, streakCount),
                label = stringResource(id = R.string.stat_current_streak),
                icon = Icons.Rounded.Whatshot,
                iconColor = Color(0xFFFF5722) // Deep Orange
            )
            StatCard(
                modifier = Modifier.weight(1f),
                isDark = isDark,
                value = "$totalAnswered/$totalQuestions",
                label = stringResource(id = R.string.stat_total_answered),
                icon = Icons.Rounded.DoneAll,
                iconColor = Color(0xFF2196F3) // Blue
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                isDark = isDark,
                value = "$examsCompleted",
                label = stringResource(id = R.string.stat_mock_exams),
                icon = Icons.Rounded.History,
                iconColor = Color(0xFF9C27B0) // Purple
            )
            StatCard(
                modifier = Modifier.weight(1f),
                isDark = isDark,
                value = "$avgScore%",
                label = stringResource(id = R.string.stat_avg_score),
                icon = Icons.Rounded.Star,
                iconColor = Color(0xFFFFC107) // Amber
            )

        }

    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    val cardBg = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val textColor = if (isDark) Color.White else Color.Black
    val textVariantColor = if (isDark) Color.LightGray else Color.DarkGray

    Surface(
        modifier = modifier.shadow(
            elevation = 6.dp, 
            shape = RoundedCornerShape(24.dp),
            spotColor = Color.Black.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(24.dp),
        color = cardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textVariantColor
            )
        }
    }
}

@Composable
fun SubjectMasterySection(
    isDark: Boolean,
    masteryList: List<com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel.CategoryMastery>
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val titleLarge = MaterialTheme.typography.titleLarge

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.subject_mastery),
            fontWeight = FontWeight.ExtraBold,
            color = onSurface,
            style = titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        val cardBg = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = cardBg
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (masteryList.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.no_category_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else {
                    masteryList.forEach { category ->
                        MasteryProgressBar(
                            title = getLocalizedCategoryName(category.name),
                            progress = category.progress,
                            progressText = "${(category.progress * 100).toInt()}%",
                            color = getCategoryColor(category.name)
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Politics & Democracy" -> Color(0xFF3B82F6) // Blue
        "History & Culture" -> Color(0xFFF59E0B) // Amber
        "Society & Culture" -> Color(0xFF10B981) // Emerald
        "State Specific" -> Color(0xFF8B5CF6) // Violet
        else -> Color(0xFF6B7280) // Gray
    }
}

@Composable
fun getLocalizedCategoryName(category: String): String {
    return when (category) {
        "Politics & Democracy" -> stringResource(id = R.string.politics_democracy)
        "History & Culture" -> stringResource(id = R.string.history_responsibility)
        "Society & Culture" -> stringResource(id = R.string.people_society)
        "State Specific" -> stringResource(id = R.string.state_specific)
        else -> category
    }
}



@Composable
fun MasteryProgressBar(title: String, progress: Float, progressText: String, color: Color) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val labelLarge = MaterialTheme.typography.labelLarge

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                style = labelLarge
            )
            Text(
                text = progressText,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                style = labelLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun FocusAreaCard(
    isDark: Boolean,
    category: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel.CategoryMastery,
    onReviewClick: () -> Unit
) {

    val cardBg = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val titleLarge = MaterialTheme.typography.titleLarge
    val titleMedium = MaterialTheme.typography.titleMedium
    val labelMedium = MaterialTheme.typography.labelMedium
    val labelSmall = MaterialTheme.typography.labelSmall
    val bodyMedium = MaterialTheme.typography.bodyMedium
    val labelLarge = MaterialTheme.typography.labelLarge

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = AnswerWrongRed.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AnswerWrongRed.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AnswerWrongRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Focus Area",
                        tint = AnswerWrongRed,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(id = R.string.needs_review).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = AnswerWrongRed,
                        style = labelSmall,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = getLocalizedCategoryName(category.name),
                        fontWeight = FontWeight.ExtraBold,
                        color = onSurface,
                        style = titleLarge
                    )

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val adviceText = if (category.mistakesCount > 0) {
                stringResource(id = R.string.advice_missed, category.mistakesCount)
            } else {
                stringResource(id = R.string.advice_mastery, (category.progress * 100).toInt())
            }


            Text(
                text = adviceText,
                color = onSurfaceVariant,
                lineHeight = 20.sp,
                style = bodyMedium
            )



            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onReviewClick,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AnswerWrongRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AnswerWrongRed.copy(alpha = 0.3f))
            ) {
                Text(
                    text = stringResource(id = R.string.btn_review_now),
                    fontWeight = FontWeight.Bold,
                    style = labelLarge
                )

            }
        }
    }
}
@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatar: String?,
    isGuest: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
    onResetPassword: () -> Unit
) {
    var name by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentName) }
    var selectedAvatar by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentAvatar) }
    
    val avatars = listOf(
        "avatar_1", "avatar_2", "avatar_3", "avatar_4",
        "avatar_5", "avatar_6", "avatar_7", "avatar_8"
    )

    // Resolve strings outside AlertDialog to preserve localized context
    val editProfileText = stringResource(id = R.string.edit_profile)
    val fullNameText = stringResource(id = R.string.full_name)
    val selectAvatarText = stringResource(id = R.string.select_avatar)
    val accountSecurityText = stringResource(id = R.string.account_security)
    val sendPasswordResetText = stringResource(id = R.string.send_password_reset)
    val passwordResetDescText = stringResource(id = R.string.password_reset_desc)
    val saveChangesText = stringResource(id = R.string.save_changes)
    val cancelText = stringResource(id = R.string.cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(editProfileText, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(fullNameText) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Avatar Picker
                Text(selectAvatarText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(avatars.size) { index ->
                        val avatarName = avatars[index]
                        val isSelected = selectedAvatar == avatarName
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val resId = context.resources.getIdentifier(avatarName, "drawable", context.packageName)
                        
                        Surface(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .clickable { selectedAvatar = avatarName }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                ),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }

                // Password Reset (if not guest)
                if (!isGuest) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(accountSecurityText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    OutlinedButton(
                        onClick = onResetPassword,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.LockReset, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(sendPasswordResetText)
                    }
                    Text(
                        passwordResetDescText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, selectedAvatar) }) {
                Text(saveChangesText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText)
            }
        }
    )
}

@Composable
fun StateSelectionDialog(
    currentState: String,
    onDismiss: () -> Unit,
    onStateSelected: (String) -> Unit
) {
    val states = listOf(
        "Baden-Württemberg", "Bavaria", "Berlin", "Brandenburg",
        "Bremen", "Hamburg", "Hessen", "Lower Saxony",
        "Mecklenburg-Vorpommern", "North Rhine-Westphalia", "Rhineland-Palatinate", "Saarland",
        "Saxony", "Saxony-Anhalt", "Schleswig-Holstein", "Thuringia"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Your Region") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    "Selecting your region filters the exam to include your local questions.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(states.size) { index ->
                        val state = states[index]
                        val isSelected = state == currentState
                        
                        Surface(
                            onClick = { onStateSelected(state) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryActionStart.copy(alpha = 0.1f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PrimaryActionStart else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryActionStart else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = PrimaryActionStart,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
