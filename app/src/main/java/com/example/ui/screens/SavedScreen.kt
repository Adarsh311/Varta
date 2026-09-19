package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.ReadingDensity
import com.example.ui.components.CompactStoryCard
import com.example.ui.components.MagazineStoryCard
import com.example.ui.theme.VartaSaffron
import com.example.util.VartaStrings

@Composable
fun SavedScreen(
    savedArticles: List<NewsArticle>,
    readingDensity: ReadingDensity,
    language: AppLanguage = AppLanguage.ENGLISH,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredArticles = savedArticles.filter { article ->
        val matchesCategory = selectedCategoryFilter == null || article.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
            article.title.contains(searchQuery, ignoreCase = true) ||
            article.source.contains(searchQuery, ignoreCase = true) ||
            article.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalReadMinutes = savedArticles.size * 3

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = VartaStrings.clearArchivedTitle(language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = VartaStrings.clearArchivedDescription(language),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll?.invoke()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Text(VartaStrings.clear(language), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(VartaStrings.cancel(language))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = VartaStrings.archivedDispatches(language),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = VartaSaffron,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (savedArticles.isNotEmpty()) "${savedArticles.size} Stories • ~$totalReadMinutes min read" else VartaStrings.noSavedStories(language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            if (savedArticles.isNotEmpty() && onClearAll != null) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear all bookmarks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (savedArticles.isNotEmpty()) {
            // In-Saved Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Filter saved dispatches...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = VartaSaffron,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VartaSaffron,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isAllSelected = selectedCategoryFilter == null
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isAllSelected) VartaSaffron else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    contentColor = if (isAllSelected) Color(0xFF0B0B0C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedCategoryFilter = null }
                ) {
                    Text(
                        text = "All (${savedArticles.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                NewsCategory.entries.forEach { cat ->
                    val count = savedArticles.count { it.category.equals(cat.name, ignoreCase = true) }
                    if (count > 0) {
                        val isSelected = selectedCategoryFilter.equals(cat.name, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) VartaSaffron else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            contentColor = if (isSelected) Color(0xFF0B0B0C) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedCategoryFilter = if (isSelected) null else cat.name
                                }
                        ) {
                            Text(
                                text = "${cat.getLocalizedTitle(language)} ($count)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
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
                        text = VartaStrings.noSavedStories(language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = VartaStrings.noSavedStoriesDescription(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (filteredArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No dispatches match your filter",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedCategoryFilter = null
                    }) {
                        Text("Reset Filters", color = VartaSaffron, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredArticles, key = { it.id }) { article ->
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
