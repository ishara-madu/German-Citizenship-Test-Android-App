package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*
import kotlinx.coroutines.launch


data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isStateSelection: Boolean = false
)


@Composable
fun OnboardingScreen(
    onFinish: (String) -> Unit = {}
) {

    val pages = listOf(
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_1),
            description = stringResource(id = R.string.onboarding_desc_1),
            icon = Icons.Rounded.Flag,
            color = PrimaryActionStart
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_2),
            description = stringResource(id = R.string.onboarding_desc_2),
            icon = Icons.Rounded.AutoAwesome,
            color = YellowAccent
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_3),
            description = stringResource(id = R.string.onboarding_desc_3),
            icon = Icons.Rounded.TrendingUp,
            color = AnswerCorrectGreen
        ),
        OnboardingPage(
            title = stringResource(id = R.string.onboarding_title_4),
            description = stringResource(id = R.string.onboarding_desc_4),
            icon = Icons.Rounded.Map,
            color = PrimaryActionStart,
            isStateSelection = true
        )
    )


    var selectedState by remember { mutableStateOf<String?>(null) }


    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = remember { derivedStateOf { pagerState.currentPage == pages.size - 1 } }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot Indicator
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(targetValue = if (isSelected) 32.dp else 12.dp, label = "dot")
                        
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) PrimaryActionStart else Color.LightGray.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                // Action Button
                AnimatedContent(
                    targetState = isLastPage.value,
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                    label = "button"
                ) { lastPage ->
                    if (lastPage) {
                        Button(
                            onClick = { selectedState?.let { onFinish(it) } },
                            enabled = selectedState != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),

                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionStart)
                        ) {
                            Text(
                                text = stringResource(id = R.string.btn_get_started),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                    } else {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = stringResource(id = R.string.btn_next),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !pages[pagerState.currentPage].isStateSelection,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { pageIndex ->
            OnboardingPageContent(
                page = pages[pageIndex],
                selectedState = selectedState,
                onStateSelected = { selectedState = it }
            )
        }
    }
}


@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    selectedState: String? = null,
    onStateSelected: (String) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            color = page.color.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.color,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        if (page.isStateSelection) {
            Spacer(modifier = Modifier.height(32.dp))
            StateSelectionGrid(
                selectedState = selectedState,
                onStateSelected = onStateSelected
            )
        }
    }
}

@Composable
fun StateSelectionGrid(
    selectedState: String?,
    onStateSelected: (String) -> Unit
) {
    val states = listOf(
        "Baden-Württemberg", "Bavaria", "Berlin", "Brandenburg",
        "Bremen", "Hamburg", "Hessen", "Lower Saxony",
        "Mecklenburg-Vorpommern", "North Rhine-Westphalia", "Rhineland-Palatinate", "Saarland",
        "Saxony", "Saxony-Anhalt", "Schleswig-Holstein", "Thuringia"
    )
    
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        items(states.size) { index ->
            val state = states[index]
            val isSelected = state == selectedState
            
            Surface(
                onClick = { onStateSelected(state) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PrimaryActionStart else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = if (isSelected) PrimaryActionStart else Color.Transparent
                ),
                modifier = Modifier.height(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = state,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

