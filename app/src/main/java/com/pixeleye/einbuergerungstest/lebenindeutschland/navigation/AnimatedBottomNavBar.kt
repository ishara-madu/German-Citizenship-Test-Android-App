package com.pixeleye.einbuergerungstest.lebenindeutschland.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*

@Composable
fun AnimatedBottomNavBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val barWidth = maxWidth - 40.dp // accounting for horizontal padding
        val itemWidth = barWidth / items.size
        val selectedIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0
        
        val isDark = LocalIsDarkTheme.current
        val bgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)

        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(80.dp),
            shape = CircleShape,
            color = bgColor,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                // The Row of items
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        
                        NavBarItem(
                            screen = screen,
                            isSelected = isSelected,
                            width = itemWidth,
                            onClick = { onItemClick(screen) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    screen: Screen,
    isSelected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isDark = LocalIsDarkTheme.current
    
    val selectedContentColor = if (isDark) PrimaryActionEnd else PrimaryActionStart
    val unselectedContentColor = PillNavIconInactive

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        screen.icon?.let { icon ->
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = screen.titleRes?.let { stringResource(id = it) },
                    tint = if (isSelected) selectedContentColor else unselectedContentColor,
                    modifier = Modifier.scale(scale)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.material3.Text(
            text = screen.titleRes?.let { stringResource(id = it) } ?: "",
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) selectedContentColor else unselectedContentColor
        )
    }
}
