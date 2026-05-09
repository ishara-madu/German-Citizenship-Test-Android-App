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
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    
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
                var isReminderEnabled by remember { mutableStateOf(true) }
                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    text = stringResource(id = R.string.label_daily_practice),
                    isDark = isDark,
                    onClick = { isReminderEnabled = !isReminderEnabled },
                    trailingContent = {
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionStart
                            )
                        )
                    }
                )
            }


            // Group 3: Subscription & Account
            SettingsGroup(title = stringResource(id = R.string.group_subscription), isDark = isDark) {
                SettingsRow(
                    icon = Icons.Rounded.WorkspacePremium,
                    text = stringResource(id = R.string.label_manage_subscription),
                    iconTint = PrimaryActionStart,
                    isDark = isDark,
                    onClick = { /* Manage Sub */ }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Restore,
                    text = stringResource(id = R.string.label_restore_purchases),
                    isDark = isDark,
                    onClick = { /* Restore Purchases */ }
                )
            }


            // Group 4: Support & About
            SettingsGroup(title = stringResource(id = R.string.group_support), isDark = isDark) {
                SettingsRow(
                    icon = Icons.Rounded.Email,
                    text = stringResource(id = R.string.label_contact_support),
                    isDark = isDark,
                    onClick = { /* Contact Support */ }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Policy,
                    text = stringResource(id = R.string.label_privacy_policy),
                    isDark = isDark,
                    onClick = { /* Privacy Policy */ }
                )
                HorizontalDivider(color = getDividerColor(isDark), thickness = 1.dp)
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    text = stringResource(id = R.string.label_app_version),
                    trailingText = "v1.0.0",
                    isDark = isDark,
                    onClick = { /* App Version Info */ }
                )
            }


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
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = onSurface,
            modifier = Modifier.weight(1f)
        )
        
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

