package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NewsArticle
import com.example.model.ReadingDensity
import com.example.ui.components.CompactStoryCard
import com.example.ui.components.MagazineStoryCard
import com.example.ui.theme.VartaSaffron

@Composable
fun SavedScreen(
    savedArticles: List<NewsArticle>,
    readingDensity: ReadingDensity,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ARCHIVED DISPATCHES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = VartaSaffron
                )
                Text(
                    text = "${savedArticles.size} Saved Stories",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (savedArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved dispatches",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the bookmark icon on any live story to read it later offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(savedArticles, key = { it.id }) { article ->
                    if (readingDensity == ReadingDensity.MAGAZINE) {
                        MagazineStoryCard(
                            article = article,
                            onClick = { onArticleClick(article) },
                            onBookmarkToggle = { onBookmarkToggle(article) },
                            onShare = { onShare(article) }
                        )
                    } else {
                        CompactStoryCard(
                            article = article,
                            onClick = { onArticleClick(article) },
                            onBookmarkToggle = { onBookmarkToggle(article) }
                        )
                    }
                }
            }
        }
    }
}
