package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.components.SnackbarManager
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    mainViewModel: com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBackClick: () -> Unit = {},
    onPremiumClick: () -> Unit = {}
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isPremium by mainViewModel.isPremium.collectAsState()
    
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val packageName = context.packageName

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }



    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = onSurface
                )
            }
            Text(
                text = stringResource(id = R.string.settings_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onSurface
            )

        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Group 1: App Preferences
            SettingsGroup(title = stringResource(id = R.string.group_app_preferences), isDark = isDark) {
                SettingsRow(
                    icon = Icons.Rounded.DarkMode,
                    text = stringResource(id = R.string.label_theme),
                    trailingText = when (themeMode) {
                        "light" -> stringResource(id = R.string.theme_light)
                        "dark" -> stringResource(id = R.string.theme_dark)
                        else -> stringResource(id = R.string.theme_system)
                    },
                    isDark = isDark,
                    onClick = { showThemeDialog = true }
                )

                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    text = stringResource(id = R.string.label_language),
                    trailingText = appLanguage,
                    isDark = isDark,
                    onClick = { showLanguageDialog = true }
                )

            }

            // Group 2: Study Reminders
            SettingsGroup(title = stringResource(id = R.string.group_study_reminders), isDark = isDark) {
                val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
                val savedHour by viewModel.reminderHour.collectAsState()
                val savedMinute by viewModel.reminderMinute.collectAsState()
                var showTimePicker by remember { mutableStateOf(false) }

                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    text = stringResource(id = R.string.label_daily_practice),
                    subtitle = if (isReminderEnabled) {
                        stringResource(id = R.string.reminder_time_prefix) + " " + String.format("%02d:%02d", savedHour, savedMinute)
                    } else null,
                    isDark = isDark,
                    onClick = {
                        if (isReminderEnabled) {
                            viewModel.disableReminder()
                        } else {
                            showTimePicker = true
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showTimePicker = true
                                } else {
                                    viewModel.disableReminder()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionStart
                            )
                        )
                    }
                )

                if (showTimePicker) {
                    ReminderTimePickerDialog(
                        initialHour = savedHour,
                        initialMinute = savedMinute,
                        onConfirm = { hour, minute ->
                            viewModel.enableReminder(hour, minute)
                            showTimePicker = false
                        },
                        onDismiss = { showTimePicker = false },
                        isDark = isDark
                    )
                }
            }



            // Group 3: Subscription & Account
            SettingsGroup(title = stringResource(id = R.string.group_subscription), isDark = isDark) {
                val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()
                
                SettingsRow(
                    icon = Icons.Rounded.CloudUpload,
                    text = stringResource(id = R.string.label_cloud_backup),
                    subtitle = stringResource(id = R.string.desc_cloud_backup),
                    isDark = isDark,
                    onClick = {
                        if (isPremium) {
                            val nextState = !isCloudSyncEnabled
                            viewModel.toggleCloudSync(nextState)
                            if (nextState) {
                                mainViewModel.triggerCloudSync()
                            }
                        } else {
                            onPremiumClick()
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = isCloudSyncEnabled,
                            onCheckedChange = { enabled ->
                                if (isPremium) {
                                    viewModel.toggleCloudSync(enabled)
                                    if (enabled) {
                                        mainViewModel.triggerCloudSync()
                                    }
                                } else {
                                    onPremiumClick()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionStart
                            )
                        )
                    }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                
                SettingsRow(
                    icon = Icons.Rounded.WorkspacePremium,
                    text = stringResource(id = R.string.label_manage_subscription),
                    iconTint = YellowAccent,
                    isDark = isDark,
                    onClick = { 
                        try {
                            uriHandler.openUri("https://play.google.com/store/account/subscriptions?package=$packageName")
                        } catch (e: Exception) {
                            SnackbarManager.showError(messageResId = R.string.err_open_play_store)
                        }
                    }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Restore,
                    text = stringResource(id = R.string.label_restore_purchases),
                    isDark = isDark,
                    onClick = { 
                        SnackbarManager.showInfo(messageResId = R.string.info_restoring_purchases)
                        // Logic for billing library would go here
                    }
                )
            }


            // Group 4: Support & About
            SettingsGroup(title = stringResource(id = R.string.group_support), isDark = isDark) {
                SettingsRow(
                    icon = Icons.Rounded.Email,
                    text = stringResource(id = R.string.label_contact_support),
                    isDark = isDark,
                    onClick = { 
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ishara.m.official@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Support: Leben in Deutschland App")
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        } catch (e: Exception) {
                            SnackbarManager.showError(messageResId = R.string.err_no_email_app)
                        }
                    }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Policy,
                    text = stringResource(id = R.string.label_privacy_policy),
                    isDark = isDark,
                    onClick = { 
                        uriHandler.openUri("https://ishara-madu.github.io/German-Citizenship-Test-Android-App/privacy-policy.html")
                    }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    text = stringResource(id = R.string.label_app_version),
                    trailingText = "v1.0.0",
                    isDark = isDark,
                    onClick = { 
                        SnackbarManager.showInfo(messageResId = R.string.info_app_version_msg)
                    }
                )
            }

            // Banner Ad
            if (!isPremium) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    com.pixeleye.einbuergerungstest.lebenindeutschland.ads.BannerAdView()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentMode = themeMode,
                onModeSelected = { mode ->
                    viewModel.setThemeMode(mode)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false },
                isDark = isDark
            )
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = appLanguage,
                onLanguageSelected = { language ->
                    viewModel.setAppLanguage(language)
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false },
                isDark = isDark
            )
        }
    }
}



private fun getDividerColor(isDark: Boolean): Color {
    return if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
}

@Composable
private fun SettingsGroup(
    title: String,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardBg = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val titleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = cardBg
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    text: String,
    isDark: Boolean,
    subtitle: String? = null,
    iconTint: Color? = null,
    trailingText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val onSurface = if (isDark) Color.White else Color(0xFF1E293B)
    val onSurfaceVariant = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val defaultIconTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint ?: defaultIconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        if (trailingText != null) {
            Text(
                text = trailingText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = onSurfaceVariant
            )
        } else if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Navigate",
                tint = onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
@Composable
private fun ThemeSelectionDialog(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.theme_select_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ThemeOptionRow("system", stringResource(id = R.string.theme_system), currentMode, onModeSelected, isDark)
                ThemeOptionRow("light", stringResource(id = R.string.theme_light), currentMode, onModeSelected, isDark)
                ThemeOptionRow("dark", stringResource(id = R.string.theme_dark), currentMode, onModeSelected, isDark)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = PrimaryActionStart)
            }
        },
        containerColor = if (isDark) GamifiedSurfaceDark else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}


@Composable
private fun ThemeOptionRow(
    mode: String,
    label: String,
    currentMode: String,
    onModeSelected: (String) -> Unit,
    isDark: Boolean
) {
    val selected = mode == currentMode
    val onSurface = if (isDark) Color.White else Color(0xFF1E293B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onModeSelected(mode) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = { onModeSelected(mode) },
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryActionStart)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = onSurface, fontSize = 16.sp)
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val languages = listOf("German", "English", "Turkish", "Arabic", "Persian")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.language_select_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                languages.forEach { language ->
                    LanguageOptionRow(language, currentLanguage, onLanguageSelected, isDark)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = PrimaryActionStart)
            }
        },
        containerColor = if (isDark) GamifiedSurfaceDark else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}


@Composable
private fun LanguageOptionRow(
    language: String,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    isDark: Boolean
) {
    val selected = language == currentLanguage
    val onSurface = if (isDark) Color.White else Color(0xFF1E293B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageSelected(language) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = { onLanguageSelected(language) },
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryActionStart)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = language, color = onSurface, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.reminder_time_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.reminder_time_desc),
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimeInput(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = PrimaryActionStart.copy(alpha = 0.15f),
                        timeSelectorSelectedContentColor = PrimaryActionStart,
                        timeSelectorUnselectedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
            ) {
                Text(stringResource(id = R.string.btn_confirm), color = PrimaryActionStart, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
            }
        },
        containerColor = if (isDark) GamifiedSurfaceDark else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
