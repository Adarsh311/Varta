package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    results: List<NewsArticle>,
    isSearching: Boolean,
    errorMessage: String?,
    readingDensity: ReadingDensity,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search Google News India...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
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
                        text = "Searching live Google News India...",
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
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (results.isEmpty() && query.isBlank()) {
            // Suggestion chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "EXPLORE TOPICS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = VartaSaffron
                )
                Spacer(modifier = Modifier.height(12.dp))
                val sampleTopics = listOf(
                    "ISRO Gaganyaan", "RBI Monetary Policy", "AI in India",
                    "Sensex Nifty", "Renewable Energy", "Cricket World Test Championship"
                )
                sampleTopics.forEach { topic ->
                    Text(
                        text = topic,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("suggested_topic_$topic")
                            .clickable {
                                onQueryChange(topic)
                                onSearch(topic)
                            }
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
