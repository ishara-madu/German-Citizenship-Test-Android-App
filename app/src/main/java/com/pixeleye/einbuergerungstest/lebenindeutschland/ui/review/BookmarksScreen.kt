package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.pixeleye.einbuergerungstest.lebenindeutschland.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    questions: List<QuestionEntity>,
    onBackClick: () -> Unit = {},
    onToggleBookmark: (Int) -> Unit = {},
    isPremium: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) GamifiedBackgroundDark else GamifiedBackgroundLight

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.bookmarks),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(padding)
        ) {
            if (questions.isEmpty()) {
                EmptyBookmarksView(isDark)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(questions) { question ->
                        BookmarkCard(
                            question = question,
                            isDark = isDark,
                            onBookmarkClick = { onToggleBookmark(question.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (!isPremium) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                com.pixeleye.einbuergerungstest.lebenindeutschland.ads.BannerAdView()
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBookmarksView(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.BookmarkBorder,
            contentDescription = null,
            tint = PrimaryActionStart.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.no_bookmarks),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.no_bookmarks_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

    }
}

@Composable
fun BookmarkCard(
    question: QuestionEntity,
    isDark: Boolean,
    onBookmarkClick: () -> Unit
) {
    val cardColor = if (isDark) GamifiedSurfaceDark else GamifiedSurfaceLight
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            if (question.imageResName != null) {
                val context = LocalContext.current
                val imageResId = context.resources.getIdentifier(
                    question.imageResName, "drawable", context.packageName
                )
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "Question Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = stringResource(id = R.string.remove_bookmark),
                        tint = PrimaryActionStart
                    )

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Correct Answer Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AnswerCorrectGreen.copy(alpha = 0.1f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = AnswerCorrectGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.correct_answer_upper),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AnswerCorrectGreen,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = question.correctAnswer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                }
            }
        }
    }
}
