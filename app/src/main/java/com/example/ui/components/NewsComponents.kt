package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.NavigationTab
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.ReadingDensity
import com.example.ui.theme.VartaSaffron
import com.example.util.NewsImageHelper

@Composable
fun VartaMasthead(
    currentTab: NavigationTab,
    isDarkMode: Boolean,
    density: ReadingDensity,
    savedCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    onToggleDensity: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Masthead Title
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(NavigationTab.FEED) }
                    .testTag("masthead_logo")
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "V Ā R T A",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "वार्ता",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = VartaSaffron,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                // Masthead hairline underline
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .width(42.dp)
                        .height(2.dp)
                        .background(VartaSaffron)
                )
                Text(
                    text = "THE NEWS, DISTILLED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Top action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Feed",
                        tint = if (isRefreshing) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {
                        if (currentTab == NavigationTab.SEARCH) {
                            onTabSelected(NavigationTab.FEED)
                        } else {
                            onTabSelected(NavigationTab.SEARCH)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (currentTab == NavigationTab.SEARCH) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {
                        if (currentTab == NavigationTab.SAVED) {
                            onTabSelected(NavigationTab.FEED)
                        } else {
                            onTabSelected(NavigationTab.SAVED)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("saved_tab_button")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.SAVED) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Saved Articles",
                            tint = if (currentTab == NavigationTab.SAVED) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        if (savedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VartaSaffron)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onToggleDensity,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("density_toggle_button")
                ) {
                    Icon(
                        imageVector = if (density == ReadingDensity.MAGAZINE) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.GridView,
                        contentDescription = "Toggle Density",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelectorBar(
    categories: List<NewsCategory>,
    selectedCategory: NewsCategory,
    onSelectCategory: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) VartaSaffron else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) Color(0xFF0B0B0C) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectCategory(category) }
                    .testTag("category_chip_${category.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NewStoriesBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = VartaSaffron,
        contentColor = Color(0xFF0B0B0C),
        shadowElevation = 4.dp,
        modifier = modifier
            .clickable { onClick() }
            .testTag("new_stories_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New stories available — tap to refresh",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun LeadStoryCard(
    article: NewsArticle,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = NewsImageHelper.getValidHeroImage(article)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("lead_story_card")
    ) {
        // Hero Image on the Lead Story
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .testTag("lead_story_image")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Source & Time row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = VartaSaffron.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = article.source.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = VartaSaffron,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•   ${article.relativeTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Pulled Serif Headline
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (article.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
fun MagazineStoryCard(
    article: NewsArticle,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = NewsImageHelper.getValidHeroImage(article)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("magazine_story_card_${article.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Text Details Column (Left side)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = VartaSaffron
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${article.relativeTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 21.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (article.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Thumbnail Image (Right side)
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action icons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBookmarkToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            thickness = 1.dp
        )
    }
}

@Composable
fun CompactStoryCard(
    article: NewsArticle,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = NewsImageHelper.getValidHeroImage(article)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("compact_story_card_${article.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Image
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = VartaSaffron
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "•  ${article.relativeTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onBookmarkToggle,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}
