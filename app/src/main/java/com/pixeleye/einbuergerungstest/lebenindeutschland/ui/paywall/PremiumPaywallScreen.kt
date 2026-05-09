package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.paywall

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*

@Composable
fun PremiumPaywallScreen(
    onCloseClick: () -> Unit = {},
    onSubscribe: (planId: String) -> Unit = {},
    onRestore: () -> Unit = {},
    onTermsPrivacyClick: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight
    val surfaceColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    var selectedPlan by remember { mutableStateOf("yearly") }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // Top Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryActionStart.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Close Button
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 32.dp)
                .align(Alignment.TopStart)
                .background(surfaceColor.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = onSurface)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 100.dp), // Bottom padding for fixed button
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Icon / Illustration
            Surface(
                shape = CircleShape,
                color = PrimaryActionStart.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryActionStart,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title & Subtitle
            Text(
                text = stringResource(id = R.string.paywall_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurface
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Benefits List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BenefitItem(icon = Icons.Filled.AutoAwesome, text = stringResource(id = R.string.benefit_ai), isDark = isDark)
                BenefitItem(icon = Icons.Rounded.History, text = stringResource(id = R.string.benefit_mock), isDark = isDark)
                BenefitItem(icon = Icons.Rounded.Insights, text = stringResource(id = R.string.benefit_stats), isDark = isDark)
                BenefitItem(icon = Icons.Rounded.Block, text = stringResource(id = R.string.benefit_ads), isDark = isDark)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Subscription Plans
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SubscriptionPlanCard(
                    id = "monthly",
                    title = stringResource(id = R.string.plan_monthly),
                    price = "€4.99 / mo",
                    isSelected = selectedPlan == "monthly",
                    isDark = isDark,
                    onClick = { selectedPlan = "monthly" }
                )
                
                SubscriptionPlanCard(
                    id = "yearly",
                    title = stringResource(id = R.string.plan_yearly),
                    price = "€29.99 / yr",
                    tagText = stringResource(id = R.string.best_value),
                    isSelected = selectedPlan == "yearly",
                    isDark = isDark,
                    onClick = { selectedPlan = "yearly" }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Fixed Bottom Section (Button + Links)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, bgColor, bgColor, bgColor)
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onSubscribe(selectedPlan) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionStart)
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_continue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = stringResource(id = R.string.restore_purchases),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onRestore() }.padding(8.dp)
                    )
                    
                    Text(
                        text = stringResource(id = R.string.terms_privacy),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onTermsPrivacyClick() }.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitItem(icon: ImageVector, text: String, isDark: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryActionStart,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SubscriptionPlanCard(
    id: String,
    title: String,
    price: String,
    tagText: String? = null,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "scale")

    val baseCardBg = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryActionStart.copy(alpha = 0.1f) else baseCardBg,
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryActionStart else Color.Transparent,
        label = "borderColor"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            border = BorderStroke(width = 2.dp, color = borderColor)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radio button representation
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryActionStart else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = price,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (tagText != null) {
            Surface(
                shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                color = YellowAccent,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = tagText.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
