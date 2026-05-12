package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.states

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.MainViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.profile.getLocalizedStateName


@Composable
fun StateSelectionScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onStateConfirmed: (String) -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight

    // State to hold the currently selected state name
    var selectedState by remember { mutableStateOf(mainViewModel.getSelectedState()) }
    var searchQuery by remember { mutableStateOf("") }

    val germanStates = listOf(
        "Baden-Württemberg", "Bayern", "Berlin", "Brandenburg",
        "Bremen", "Hamburg", "Hessen", "Mecklenburg-Vorpommern",
        "Niedersachsen", "Nordrhein-Westfalen", "Rheinland-Pfalz", "Saarland",
        "Sachsen", "Sachsen-Anhalt", "Schleswig-Holstein", "Thüringen"
    )

    // Pre-resolve localized names in the Composable scope
    val localizedStateMap = germanStates.associateWith { getLocalizedStateName(it) }

    val filteredStates = remember(searchQuery, localizedStateMap) {
        if (searchQuery.isEmpty()) germanStates
        else germanStates.filter { state ->
            state.contains(searchQuery, ignoreCase = true) ||
            (localizedStateMap[state]?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            StateSelectionHeader(
                onBackClick = onBackClick,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            if (filteredStates.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.no_states_match), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {

                // Lazy Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredStates) { stateName ->
                        StateCard(
                            name = stateName,
                            isSelected = selectedState == stateName,
                            onClick = { selectedState = stateName }
                        )
                    }
                }
            }
        }

        StickyBottomActionBar(
            isSelected = selectedState != null,
            onStartPractice = { 
                selectedState?.let { 
                    onStateConfirmed(it) 
                } 
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }
}

@Composable
fun StateSelectionHeader(
    onBackClick: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.offset(x = (-12).dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.select_your_state_title),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.state_selection_desc),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )


        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.search_state)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },

            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryActionStart,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            singleLine = true
        )
    }
}

@Composable
fun StateCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")

    val defaultBgColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val containerColor = if (isSelected) PrimaryActionStart.copy(alpha = 0.1f) else defaultBgColor
    val borderColor = if (isSelected) PrimaryActionStart else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 12.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isSelected) PrimaryActionStart.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(width = if (isSelected) 2.dp else 0.dp, color = borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // State Image as Background
            val context = LocalContext.current
            val resNameMap = mapOf(
                "Bayern" to "state_bavaria",
                "Niedersachsen" to "state_lower_saxony",
                "Nordrhein-Westfalen" to "state_north_rhine_westphalia",
                "Rheinland-Pfalz" to "state_rhineland_palatinate",
                "Sachsen" to "state_saxony",
                "Thüringen" to "state_thuringia"
            )
            val resName = resNameMap[name] ?: ("state_" + name.lowercase()
                .replace("-", "_")
                .replace(" ", "_")
                .replace("ü", "u"))
            
            val imageResId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            
            if (imageResId != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = imageResId),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = if (isDark) 0.6f else 0.8f
                )
            } else {
                // Fallback to Icon Pattern
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Bottom Gradient for Text Readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 300f
                        )
                    )
            )

            // State Name Overlay
            Text(
                text = getLocalizedStateName(name),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )

            // Selection Checkmark
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryActionStart,
                    modifier = Modifier.size(28.dp),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StickyBottomActionBar(
    isSelected: Boolean,
    onStartPractice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight
    
    // Smooth gradient fade at the top of the sticky bar so the grid blends under it
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, bgColor, bgColor),
                    startY = 0f,
                    endY = 100f
                )
            )
            .padding(top = 32.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Button(
            onClick = onStartPractice,
            enabled = isSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryActionStart,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text(
                text = stringResource(id = R.string.btn_start_practice),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )

        }
    }
}
