package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.CompositionLocalProvider


import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDarkTheme = staticCompositionLocalOf<Boolean> { error("No dark theme provided") }

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryActionStart,
    secondary = StreakFlame,
    tertiary = RedAccent,
    background = GamifiedBackgroundDark,
    surface = GamifiedSurfaceDark,
    surfaceVariant = GamifiedSurfaceDark,
    onPrimary = GamifiedBackgroundLight,
    onSecondary = GamifiedBackgroundLight,
    onTertiary = GamifiedBackgroundLight,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = RedAccent
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryActionStart,
    secondary = StreakFlame,
    tertiary = RedAccent,
    background = GamifiedBackgroundLight,
    surface = GamifiedSurfaceLight,
    surfaceVariant = GamifiedSurfaceLight,
    onPrimary = GamifiedBackgroundLight,
    onSecondary = GamifiedBackgroundDark,
    onTertiary = GamifiedBackgroundLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = RedAccent
)

@Composable
fun BürgertestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides Typography.bodyLarge,
            LocalIsDarkTheme provides darkTheme
        ) {
            content()
        }
    }
}