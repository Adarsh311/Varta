package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppLanguage
import com.example.model.NavigationTab
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.ReadingDensity
import com.example.ui.theme.VartaPressRed
import com.example.ui.theme.VartaSaffron
import com.example.util.NewsImageHelper
import com.example.util.VartaStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VartaMasthead(
    currentTab: NavigationTab,
    isDarkMode: Boolean,
    density: ReadingDensity,
    savedCount: Int,
    isRefreshing: Boolean,
    notificationsEnabled: Boolean = true,
    language: AppLanguage = AppLanguage.ENGLISH,
    onRefresh: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    onToggleDensity: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateString = rememberTodayDateString()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Safe inset spacer for Android Status Bar
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        // --- 1. Top Broadsheet Ears Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Ear: Volume & Edition tag
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "VOL. CXVII • NO. 42",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "EST. 2024 • BHARAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Center: Hindi Scripture / Motto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "❖ सत्यमेव जयते ❖",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = VartaSaffron,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Ear: Dateline & Price tag
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = dateString.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = VartaStrings.editionBadge(language),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = VartaPressRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top Thin Rule
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- 2. Grand Newspaper Masthead Title ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTabSelected(NavigationTab.FEED) }
                .testTag("masthead_logo")
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "THE VĀRTA DISPATCH",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = VartaSaffron.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VartaSaffron.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "वार्ता",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = VartaSaffron,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = VartaStrings.tagline(language),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 8.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3. Broadsheet Double Rules Framing the Masthead ---
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onBackground,
                thickness = 2.dp
            )
            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )
        }

        // --- 4. Editorial Edition Navigation & Quick Controls Toolbar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Editorial Section Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditorialNavButton(
                    title = VartaStrings.dispatches(language),
                    isSelected = currentTab == NavigationTab.FEED,
                    onClick = { onTabSelected(NavigationTab.FEED) }
                )

                EditorialNavButton(
                    title = VartaStrings.saved(language, savedCount),
                    isSelected = currentTab == NavigationTab.SAVED,
                    onClick = {
                        if (currentTab == NavigationTab.SAVED) {
                            onTabSelected(NavigationTab.FEED)
                        } else {
                            onTabSelected(NavigationTab.SAVED)
                        }
                    },
                    modifier = Modifier.testTag("saved_tab_button")
                )

                EditorialNavButton(
                    title = VartaStrings.searchIndex(language),
                    isSelected = currentTab == NavigationTab.SEARCH,
                    onClick = {
                        if (currentTab == NavigationTab.SEARCH) {
                            onTabSelected(NavigationTab.FEED)
                        } else {
                            onTabSelected(NavigationTab.SEARCH)
                        }
                    },
                    modifier = Modifier.testTag("search_toggle_button")
                )
            }

            // Quick Pressroom Utility Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Language Switch Chip: Instant toggle between English and Hindi
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        VartaSaffron.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onToggleLanguage() }
                        .testTag("language_toggle_button")
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "हिन्दी" else "EN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = VartaSaffron
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onOpenNotificationSettings,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("notification_settings_button")
                ) {
                    Icon(
                        imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Notification Alerts Settings",
                        tint = if (notificationsEnabled) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Press Wire",
                        tint = if (isRefreshing) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onToggleDensity,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("density_toggle_button")
                ) {
                    Icon(
                        imageVector = if (density == ReadingDensity.MAGAZINE) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.GridView,
                        contentDescription = "Toggle Broadsheet / Wire Density",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Day / Midnight Edition",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Bottom Double Rule
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )
            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun EditorialNavButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CategorySelectorBar(
    categories: List<NewsCategory>,
    selectedCategory: NewsCategory,
    onSelectCategory: (NewsCategory) -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = if (isSelected) VartaSaffron else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                contentColor = if (isSelected) Color(0xFF131211) else MaterialTheme.colorScheme.onSurface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.8.dp,
                    color = if (isSelected) VartaSaffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .clickable { onSelectCategory(category) }
                    .testTag("category_chip_${category.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Text(
                            text = "❖ ",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF131211)
                        )
                    }
                    Text(
                        text = category.getLocalizedTitle(language).uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
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
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = VartaPressRed,
        contentColor = Color.White,
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
        modifier = modifier
            .clickable { onClick() }
            .testTag("new_stories_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "★",
                fontSize = 12.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = VartaStrings.freshStoriesAvailable(language),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = Color.White
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("lead_story_card")
    ) {
        // Kicker Bar / Category Stamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "◆ FRONT PAGE DISPATCH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    ),
                    color = VartaPressRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•   ${article.relativeTime}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
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
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grand Broadsheet Serif Headline
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Newspaper Photo Frame with Caption
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column {
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

                // Photo Caption & Credit Line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WIRE DISPATCH • ${article.source.uppercase(Locale.getDefault())}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRESS PHOTOGRAPHY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }

        if (article.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            // Broadsheet Dateline + Drop Cap / Lead Description
            val cleanDesc = article.description.trim()
            val annotatedLead = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    append("${article.source.uppercase(Locale.getDefault())} — ")
                }
                append(cleanDesc)
            }

            Text(
                text = annotatedLead,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Broadsheet Section Double Hairline Divider
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(2.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
        }
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("magazine_story_card_${article.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Text Details Column (Left side - newspaper column feel)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.source.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = VartaSaffron
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${article.relativeTime}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
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
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Thumbnail Image in Newspaper Frame (Right side)
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
                        .aspectRatio(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action icons row with classic newspaper styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONTINUE READING ➔",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row {
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 0.8.dp
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .testTag("compact_story_card_${article.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image in Newspaper Wire Frame
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .border(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
                        .aspectRatio(1f)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.source.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = VartaSaffron
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${article.relativeTime}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onBookmarkToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            thickness = 0.6.dp
        )
    }
}

@Composable
private fun rememberTodayDateString(): String {
    val formatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date())
}
