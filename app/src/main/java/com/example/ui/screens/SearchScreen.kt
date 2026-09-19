package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NewsArticle
import com.example.model.ReadingDensity
import com.example.ui.components.CompactStoryCard
import com.example.ui.components.MagazineStoryCard
import com.example.ui.theme.VartaSaffron

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    results: List<NewsArticle>,
    isSearching: Boolean,
    errorMessage: String?,
    readingDensity: ReadingDensity,
    recentSearches: List<String> = emptyList(),
    onRemoveRecentSearch: ((String) -> Unit)? = null,
    onClearRecentSearches: (() -> Unit)? = null,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchFocused = it.isFocused }
                    .testTag("search_input_field"),
                placeholder = @Composable {
                    if (!isSearchFocused && query.isEmpty()) {
                        Text(
                            text = "Search Indian & global dispatches...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Serif
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = VartaSaffron,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    onSearch(query)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VartaSaffron,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(4.dp)
            )
        }

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = VartaSaffron,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Searching live Google News India dispatches...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (results.isEmpty() && query.isBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Recent Searches Section
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = VartaSaffron,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RECENT SEARCHES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = VartaSaffron
                                )
                            }
                            if (onClearRecentSearches != null) {
                                TextButton(onClick = onClearRecentSearches) {
                                    Text("Clear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(recentSearches) { recent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable {
                                    onQueryChange(recent)
                                    onSearch(recent)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = recent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (onRemoveRecentSearch != null) {
                                IconButton(
                                    onClick = { onRemoveRecentSearch(recent) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove recent search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }

                // Trending Indian Topics Section
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = VartaSaffron,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TRENDING IN INDIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = VartaSaffron
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val sampleTopics = listOf(
                    "ISRO Gaganyaan Mission",
                    "Reserve Bank Monetary Policy",
                    "India Semiconductor Mission",
                    "Sensex & Nifty Live Market",
                    "Solar Energy Infrastructure",
                    "Cricket World Test Championship",
                    "AI Startup Ecosystem India",
                    "Vande Bharat Sleeper Trains"
                )

                items(sampleTopics) { topic ->
                    Text(
                        text = topic,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
                            .clickable {
                                onQueryChange(topic)
                                onSearch(topic)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("suggested_topic_$topic")
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "${results.size} stories found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                items(results, key = { it.id }) { article ->
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
