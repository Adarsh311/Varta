package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.ReadingDensity
import com.example.ui.components.CategorySelectorBar
import com.example.ui.components.CompactStoryCard
import com.example.ui.components.LeadStoryCard
import com.example.ui.components.MagazineStoryCard
import com.example.ui.components.NewStoriesBanner
import com.example.ui.components.SkeletonEditorialFeed
import com.example.ui.theme.VartaSaffron
import com.example.util.VartaStrings

@Composable
fun FeedScreen(
    categories: List<NewsCategory>,
    selectedCategory: NewsCategory,
    onSelectCategory: (NewsCategory) -> Unit,
    articles: List<NewsArticle>,
    isLoading: Boolean,
    errorMessage: String?,
    hasNewStories: Boolean,
    onApplyNewStories: () -> Unit,
    readingDensity: ReadingDensity,
    language: AppLanguage = AppLanguage.ENGLISH,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedCategory) {
        listState.scrollToItem(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Selector Horizontal Tabs
            CategorySelectorBar(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory,
                language = language
            )

            if (isLoading && articles.isEmpty()) {
                SkeletonEditorialFeed()
            } else if (errorMessage != null && articles.isEmpty()) {
                // Error State with Retry
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = VartaSaffron,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = VartaStrings.unableToFetchFeed(language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VartaSaffron,
                                contentColor = Color(0xFF0B0B0C)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("retry_feed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(VartaStrings.retryFeed(language), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    if (articles.isNotEmpty()) {
                        // Lead Story (First Article)
                        item(key = "lead_${articles.first().id}") {
                            LeadStoryCard(
                                article = articles.first(),
                                onClick = { onArticleClick(articles.first()) },
                                onBookmarkToggle = { onBookmarkToggle(articles.first()) },
                                onShare = { onShare(articles.first()) }
                            )
                        }

                        val remainingStories = articles.drop(1)

                        // Remaining Stories Header
                        if (remainingStories.isNotEmpty()) {
                            item(key = "section_header_wire") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = VartaStrings.wireChronicles(language),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(
                                            text = VartaStrings.sectionB(language),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 8.5.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )
                                }
                            }

                            itemsIndexed(remainingStories, key = { _, item -> item.id }) { _, article ->
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
        }

        // Floating "New stories available" banner
        AnimatedVisibility(
            visible = hasNewStories,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp)
        ) {
            NewStoriesBanner(onClick = onApplyNewStories, language = language)
        }
    }
}
