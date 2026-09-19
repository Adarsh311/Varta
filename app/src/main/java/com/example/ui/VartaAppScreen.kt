package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.NavigationTab
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.ui.components.NotificationSettingsDialog
import com.example.ui.components.VartaMasthead
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.FullArticleScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SearchScreen
import kotlinx.coroutines.launch

@Composable
fun VartaAppScreen(
    viewModel: NewsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val articles by viewModel.combinedArticles.collectAsStateWithLifecycle()
    val searchResults by viewModel.combinedSearchResults.collectAsStateWithLifecycle()
    val savedArticles by viewModel.bookmarkedArticles.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val activeArticle = uiState.selectedArticle

    // System Back Handler: return to Discourse Feed when in Search or Saved tab
    BackHandler(enabled = uiState.currentTab != NavigationTab.FEED && activeArticle == null) {
        viewModel.selectTab(NavigationTab.FEED)
    }

    val onShareArticle: (NewsArticle) -> Unit = { article ->
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${article.title}\n\nSource: ${article.source}\nLink: ${article.link}\n— Shared via Vārta")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Article")
        context.startActivity(shareIntent)
    }

    val onBookmarkToggleWithFeedback: (NewsArticle) -> Unit = { article ->
        val willBeBookmarked = !article.isBookmarked
        viewModel.toggleBookmark(article)
        scope.launch {
            val message = if (willBeBookmarked) {
                if (uiState.language == com.example.model.AppLanguage.HINDI) "आर्काइव में सहेजा गया" else "Saved to Archived Dispatches"
            } else {
                if (uiState.language == com.example.model.AppLanguage.HINDI) "आर्काइव से हटाया गया" else "Removed from Archived Dispatches"
            }
            snackbarHostState.showSnackbar(message = message, withDismissAction = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Persistent Feed and Navigation Layer (Never unmounted, preserving scroll and image cache)
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                VartaMasthead(
                    currentTab = uiState.currentTab,
                    isDarkMode = uiState.isDarkMode,
                    density = uiState.readingDensity,
                    savedCount = savedArticles.size,
                    isRefreshing = uiState.isRefreshing,
                    notificationsEnabled = uiState.isImportantNotificationsEnabled,
                    language = uiState.language,
                    onRefresh = { viewModel.refreshCurrentFeed() },
                    onTabSelected = { tab -> viewModel.selectTab(tab) },
                    onToggleDensity = { viewModel.toggleReadingDensity() },
                    onToggleTheme = { viewModel.toggleTheme() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onOpenNotificationSettings = { viewModel.showNotificationSettings(true) },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    NavigationTab.FEED -> {
                        FeedScreen(
                            categories = NewsCategory.entries,
                            selectedCategory = uiState.selectedCategory,
                            onSelectCategory = { cat -> viewModel.selectCategory(cat) },
                            articles = articles,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            hasNewStories = uiState.hasNewStoriesAvailable,
                            onApplyNewStories = { viewModel.applyPendingNewStories() },
                            readingDensity = uiState.readingDensity,
                            language = uiState.language,
                            onArticleClick = { article -> viewModel.openArticle(article) },
                            onBookmarkToggle = onBookmarkToggleWithFeedback,
                            onShare = onShareArticle,
                            onRetry = { viewModel.refreshCurrentFeed() }
                        )
                    }
                    NavigationTab.SEARCH -> {
                        SearchScreen(
                            query = uiState.searchQuery,
                            onQueryChange = { q -> viewModel.onSearchQueryChanged(q) },
                            onSearch = { q -> viewModel.executeSearch(q) },
                            results = searchResults,
                            isSearching = uiState.isSearching,
                            errorMessage = uiState.searchErrorMessage,
                            readingDensity = uiState.readingDensity,
                            language = uiState.language,
                            recentSearches = uiState.recentSearches,
                            onRemoveRecentSearch = { q -> viewModel.removeRecentSearch(q) },
                            onClearRecentSearches = { viewModel.clearRecentSearches() },
                            onArticleClick = { article -> viewModel.openArticle(article) },
                            onBookmarkToggle = onBookmarkToggleWithFeedback,
                            onShare = onShareArticle
                        )
                    }
                    NavigationTab.SAVED -> {
                        SavedScreen(
                            savedArticles = savedArticles,
                            readingDensity = uiState.readingDensity,
                            language = uiState.language,
                            onArticleClick = { article -> viewModel.openArticle(article) },
                            onBookmarkToggle = onBookmarkToggleWithFeedback,
                            onShare = onShareArticle,
                            onClearAll = { viewModel.clearAllSaved() }
                        )
                    }
                }
            }
        }

        if (uiState.showNotificationSettingsDialog) {
            NotificationSettingsDialog(
                isEnabled = uiState.isImportantNotificationsEnabled,
                isDarkMode = uiState.isDarkMode,
                language = uiState.language,
                onToggle = { enabled ->
                    viewModel.setImportantNotificationsEnabled(context, enabled)
                },
                onToggleTheme = {
                    viewModel.toggleTheme()
                },
                onToggleLanguage = {
                    viewModel.toggleLanguage()
                },
                onSendTest = {
                    viewModel.triggerTestImportantNotification(context)
                },
                onDismiss = {
                    viewModel.showNotificationSettings(false)
                }
            )
        }

        // Butter-smooth slide & fade transition for the Full Article screen
        AnimatedVisibility(
            visible = activeArticle != null,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            if (activeArticle != null) {
                FullArticleScreen(
                    article = activeArticle,
                    scrapedArticle = uiState.scrapedArticle,
                    isLoading = uiState.isScrapingArticle,
                    language = uiState.language,
                    onBack = { viewModel.closeArticle() },
                    onBookmarkToggle = onBookmarkToggleWithFeedback,
                    onShare = onShareArticle,
                    onRetry = { viewModel.retryScrapeArticle() }
                )
            }
        }
    }
}
