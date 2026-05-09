package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Neumorphic soft shadow/floating effect for Bento Grid cards.
 */
fun Modifier.antigravityCardStyle(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp
): Modifier = composed {
    val isDark = LocalIsDarkTheme.current

    // Neumorphic floating shadow colors
    // Light mode uses soft dark shadows and subtle light highlights.
    // Dark mode uses subtle black shadows and very faint light highlights.
    val darkShadowColor = if (isDark) Color(0x66000000) else Color(0x1A000000)
    val lightShadowColor = if (isDark) Color(0x0CFFFFFF) else Color(0x66FFFFFF)

    this.drawBehind {
        val shadowRadius = elevation.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        drawIntoCanvas { canvas ->
            // Bottom-Right Dark Shadow
            val paintDark = Paint()
            val frameworkPaintDark = paintDark.asFrameworkPaint()
            frameworkPaintDark.color = android.graphics.Color.TRANSPARENT
            frameworkPaintDark.setShadowLayer(
                shadowRadius,
                shadowRadius / 2,
                shadowRadius / 2,
                darkShadowColor.toArgb()
            )
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadiusPx,
                radiusY = cornerRadiusPx,
                paint = paintDark
            )

            // Top-Left Light Shadow (Neumorphism Highlight)
            val paintLight = Paint()
            val frameworkPaintLight = paintLight.asFrameworkPaint()
            frameworkPaintLight.color = android.graphics.Color.TRANSPARENT
            frameworkPaintLight.setShadowLayer(
                shadowRadius,
                -shadowRadius / 2,
                -shadowRadius / 2,
                lightShadowColor.toArgb()
            )
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadiusPx,
                radiusY = cornerRadiusPx,
                paint = paintLight
            )
        }
    }
}
