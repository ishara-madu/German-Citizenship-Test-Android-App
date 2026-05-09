package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.paywall

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*

@Composable
fun PremiumPaywallScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else Color(0xFFFAFAFA)
    val onBgColor = if (isDark) Color.White else Color(0xFF1E293B)
    val surfaceColor = if (isDark) GamifiedSurfaceDark else Color.White
    val onSurfaceVariant = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var selectedOptionIndex by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = onBgColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Area
        Surface(
            shape = CircleShape,
            color = YellowAccent.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = "Premium",
                tint = YellowAccent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Unlock Premium",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = onBgColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pass your Citizenship Test with confidence.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Benefits List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BenefitRow("Ad-free experience", onBgColor)
            BenefitRow("AI 'Explain to Me' for complex questions", onBgColor)
            BenefitRow("Translation Mode Unlock", onBgColor)
            BenefitRow("Custom PDF Export for weak areas", onBgColor)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Pricing Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PricingCard(
                modifier = Modifier.weight(1f),
                title = "Monthly",
                price = "€0.99",
                subtitle = "/ month",
                isSelected = selectedOptionIndex == 0,
                isBestValue = false,
                surfaceColor = surfaceColor,
                onBgColor = onBgColor,
                onSurfaceVariant = onSurfaceVariant,
                onClick = { selectedOptionIndex = 0 }
            )

            PricingCard(
                modifier = Modifier.weight(1f),
                title = "Lifetime",
                price = "€3.99",
                subtitle = "One-time",
                isSelected = selectedOptionIndex == 1,
                isBestValue = true,
                surfaceColor = surfaceColor,
                onBgColor = onBgColor,
                onSurfaceVariant = onSurfaceVariant,
                onClick = { selectedOptionIndex = 1 }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Bottom Action Area
        Button(
            onClick = { /* Upgrade action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryActionStart
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Secure",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upgrade Now",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* Restore Purchases */ }) {
            Text(
                text = "Restore Purchases",
                color = onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BenefitRow(text: String, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = "Included",
            tint = PrimaryActionStart,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun PricingCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    subtitle: String,
    isSelected: Boolean,
    isBestValue: Boolean,
    surfaceColor: Color,
    onBgColor: Color,
    onSurfaceVariant: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryActionStart else Color.Transparent
    val cardBg = if (isSelected) PrimaryActionStart.copy(alpha = 0.05f) else surfaceColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) borderColor else onSurfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isBestValue) {
                Surface(
                    color = YellowAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.offset(y = (-8).dp)
                ) {
                    Text(
                        text = "Best Value",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryActionStart else onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = price,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onBgColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
