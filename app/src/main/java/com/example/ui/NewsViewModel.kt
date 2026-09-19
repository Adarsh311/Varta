package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.NewsRepository
import com.example.model.NavigationTab
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.ReadingDensity
import com.example.model.ScrapedArticle
import com.example.util.NewsImageHelper
import com.example.util.NewsNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NewsUiState(
    val language: com.example.model.AppLanguage = com.example.model.AppLanguage.ENGLISH,
    val currentTab: NavigationTab = NavigationTab.FEED,
    val selectedCategory: NewsCategory = NewsCategory.INDIA,
    val articles: List<NewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hasNewStoriesAvailable: Boolean = false,
    val pendingNewStories: List<NewsArticle> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<NewsArticle> = emptyList(),
    val isSearching: Boolean = false,
    val searchErrorMessage: String? = null,
    val recentSearches: List<String> = listOf("ISRO Gaganyaan", "RBI Repo Rate", "Semiconductor India", "Sensex Nifty"),
    val savedCategoryFilter: String? = null,
    val savedSearchQuery: String = "",
    val selectedArticle: NewsArticle? = null,
    val scrapedArticle: ScrapedArticle? = null,
    val isScrapingArticle: Boolean = false,
    val readingDensity: ReadingDensity = ReadingDensity.MAGAZINE,
    val isDarkMode: Boolean = true,
    val isImportantNotificationsEnabled: Boolean = true,
    val showNotificationSettingsDialog: Boolean = false,
    val lastRefreshedAt: Long = System.currentTimeMillis()
)

class NewsViewModel(
    private val repository: NewsRepository,
    private val context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences("varta_settings_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        NewsUiState(
            language = com.example.model.AppLanguage.fromCode(prefs.getString("app_language", "en")),
            isDarkMode = prefs.getBoolean("is_dark_mode", true)
        )
    )
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    val bookmarkedArticles: StateFlow<List<NewsArticle>> = repository.bookmarkedArticles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val bookmarkedIds: StateFlow<Set<String>> = repository.bookmarkedIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val combinedArticles: StateFlow<List<NewsArticle>> = combine(_uiState, bookmarkedIds) { state, savedIds ->
        state.articles.map { article ->
            article.copy(isBookmarked = savedIds.contains(article.id))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val combinedSearchResults: StateFlow<List<NewsArticle>> = combine(_uiState, bookmarkedIds) { state, savedIds ->
        state.searchResults.map { article ->
            article.copy(isBookmarked = savedIds.contains(article.id))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var autoRefreshJob: Job? = null
    private var searchDebounceJob: Job? = null

    init {
        loadCategoryFeed(_uiState.value.selectedCategory)
        startSilentAutoRefresh()
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectCategory(category: NewsCategory) {
        if (_uiState.value.selectedCategory == category && _uiState.value.articles.isNotEmpty()) return
        _uiState.update {
            it.copy(
                selectedCategory = category,
                hasNewStoriesAvailable = false,
                pendingNewStories = emptyList()
            )
        }
        loadCategoryFeed(category)
    }

    fun refreshCurrentFeed() {
        if (_uiState.value.currentTab == NavigationTab.FEED) {
            loadCategoryFeed(_uiState.value.selectedCategory, isManualRefresh = true)
        } else if (_uiState.value.currentTab == NavigationTab.SEARCH && _uiState.value.searchQuery.isNotBlank()) {
            executeSearch(_uiState.value.searchQuery)
        }
    }

    private fun loadCategoryFeed(category: NewsCategory, isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isManualRefresh && it.articles.isEmpty(),
                    isRefreshing = isManualRefresh || it.articles.isNotEmpty(),
                    errorMessage = null
                )
            }
            val currentLang = _uiState.value.language
            val result = repository.getFeed(category, currentLang)
            result.onSuccess { freshArticles ->
                _uiState.update {
                    it.copy(
                        articles = freshArticles,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                        hasNewStoriesAvailable = false,
                        pendingNewStories = emptyList(),
                        lastRefreshedAt = System.currentTimeMillis()
                    )
                }
                if (category == NewsCategory.INDIA || category == NewsCategory.TOP_STORIES) {
                    freshArticles.firstOrNull()?.let { top ->
                        prefs.edit().putString("last_notified_article_id", top.id).apply()
                    }
                }
                resolveMissingImagesInBackground(freshArticles)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = if (it.articles.isEmpty()) {
                            error.localizedMessage ?: "Unable to fetch live news. Please tap to retry."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchDebounceJob?.cancel()
        if (query.trim().length >= 2) {
            searchDebounceJob = viewModelScope.launch {
                delay(600)
                executeSearch(query.trim())
            }
        } else if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false, searchErrorMessage = null) }
        }
    }

    fun executeSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        addRecentSearch(trimmed)
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchErrorMessage = null) }
            val currentLang = _uiState.value.language
            val result = repository.searchNews(trimmed, currentLang)
            result.onSuccess { results ->
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false,
                        searchErrorMessage = if (results.isEmpty()) "No stories found matching \"$trimmed\"" else null
                    )
                }
                resolveMissingImagesInBackground(results)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        searchErrorMessage = err.localizedMessage ?: "Search failed. Check connection."
                    )
                }
            }
        }
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        _uiState.update { state ->
            val updated = (listOf(trimmed) + state.recentSearches.filterNot { it.equals(trimmed, ignoreCase = true) }).take(8)
            state.copy(recentSearches = updated)
        }
    }

    fun removeRecentSearch(query: String) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches.filterNot { it.equals(query, ignoreCase = true) })
        }
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    fun setSavedCategoryFilter(category: String?) {
        _uiState.update { it.copy(savedCategoryFilter = category) }
    }

    fun setSavedSearchQuery(query: String) {
        _uiState.update { it.copy(savedSearchQuery = query) }
    }

    fun clearAllSaved() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
        }
    }

    fun applyPendingNewStories() {
        val pending = _uiState.value.pendingNewStories
        if (pending.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    articles = pending,
                    hasNewStoriesAvailable = false,
                    pendingNewStories = emptyList(),
                    lastRefreshedAt = System.currentTimeMillis()
                )
            }
        }
    }

    fun toggleBookmark(article: NewsArticle) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    fun openArticle(article: NewsArticle) {
        _uiState.update {
            it.copy(
                selectedArticle = article,
                isScrapingArticle = true,
                scrapedArticle = null
            )
        }
        viewModelScope.launch {
            val fullArticle = repository.getFullArticle(article, _uiState.value.language)
            _uiState.update { current ->
                if (current.selectedArticle?.id == article.id) {
                    val resolvedHero = fullArticle.heroImageUrl
                    val hasRealSourceImage = !resolvedHero.isNullOrBlank() &&
                        !NewsImageHelper.isFallbackStockImage(resolvedHero) &&
                        NewsImageHelper.isValidEditorialImage(resolvedHero)

                    val updatedSelected = if (hasRealSourceImage) {
                        current.selectedArticle.copy(imageUrl = resolvedHero)
                    } else {
                        current.selectedArticle
                    }

                    val updatedArticles = current.articles.map { item ->
                        if (item.id == article.id && hasRealSourceImage) {
                            item.copy(imageUrl = resolvedHero)
                        } else {
                            item
                        }
                    }

                    current.copy(
                        selectedArticle = updatedSelected,
                        articles = updatedArticles,
                        scrapedArticle = fullArticle,
                        isScrapingArticle = false
                    )
                } else {
                    current
                }
            }
        }
    }

    fun retryScrapeArticle() {
        val article = _uiState.value.selectedArticle ?: return
        viewModelScope.launch {
            repository.clearArticleCache(article.id)
            openArticle(article)
        }
    }

    fun closeArticle() {
        _uiState.update {
            it.copy(
                selectedArticle = null,
                scrapedArticle = null,
                isScrapingArticle = false
            )
        }
    }

    fun toggleReadingDensity() {
        _uiState.update {
            val next = if (it.readingDensity == ReadingDensity.MAGAZINE) ReadingDensity.COMPACT else ReadingDensity.MAGAZINE
            it.copy(readingDensity = next)
        }
    }

    fun toggleTheme() {
        _uiState.update { state ->
            val nextMode = !state.isDarkMode
            prefs.edit().putBoolean("is_dark_mode", nextMode).apply()
            state.copy(isDarkMode = nextMode)
        }
    }

    fun setLanguage(language: com.example.model.AppLanguage) {
        if (_uiState.value.language == language) return
        prefs.edit().putString("app_language", language.code).apply()
        _uiState.update { it.copy(language = language) }
        loadCategoryFeed(_uiState.value.selectedCategory, isManualRefresh = true)
    }

    fun toggleLanguage() {
        val nextLanguage = if (_uiState.value.language == com.example.model.AppLanguage.HINDI) {
            com.example.model.AppLanguage.ENGLISH
        } else {
            com.example.model.AppLanguage.HINDI
        }
        setLanguage(nextLanguage)
    }

    private var imageEnrichJob: Job? = null

    private fun resolveMissingImagesInBackground(articles: List<NewsArticle>) {
        imageEnrichJob?.cancel()
        imageEnrichJob = viewModelScope.launch(Dispatchers.IO) {
            val candidates = articles.filter {
                it.imageUrl.isNullOrBlank() || NewsImageHelper.isFallbackStockImage(it.imageUrl)
            }.take(15)

            for (article in candidates) {
                if (!isActive) break
                try {
                    val realUrl = repository.resolveRealHeroImage(article)
                    if (realUrl != null && NewsImageHelper.isValidEditorialImage(realUrl)) {
                        _uiState.update { current ->
                            val updatedArticles = current.articles.map {
                                if (it.id == article.id) it.copy(imageUrl = realUrl) else it
                            }
                            val updatedSearch = current.searchResults.map {
                                if (it.id == article.id) it.copy(imageUrl = realUrl) else it
                            }
                            val updatedSelected = if (current.selectedArticle?.id == article.id) {
                                current.selectedArticle.copy(imageUrl = realUrl)
                            } else {
                                current.selectedArticle
                            }
                            current.copy(
                                articles = updatedArticles,
                                searchResults = updatedSearch,
                                selectedArticle = updatedSelected
                            )
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun checkAndTriggerNewBreakingStoryNotification(context: Context) {
        viewModelScope.launch {
            val topArticle = _uiState.value.articles.firstOrNull() ?: return@launch
            NewsNotificationManager.notifyIfNewBreakingStory(context, topArticle)
        }
    }

    fun initNotificationState(context: Context) {
        NewsNotificationManager.createNotificationChannel(context)
        val enabled = NewsNotificationManager.isImportantNotificationsEnabled(context) &&
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        _uiState.update { it.copy(isImportantNotificationsEnabled = enabled) }
    }

    fun setImportantNotificationsEnabled(context: Context, enabled: Boolean) {
        NewsNotificationManager.setImportantNotificationsEnabled(context, enabled)
        _uiState.update { it.copy(isImportantNotificationsEnabled = enabled) }
    }

    fun showNotificationSettings(show: Boolean) {
        _uiState.update { it.copy(showNotificationSettingsDialog = show) }
    }

    fun triggerTestImportantNotification(context: Context) {
        viewModelScope.launch {
            val sampleArticle = _uiState.value.articles.firstOrNull() ?: NewsArticle(
                id = "test_lead_dispatch",
                title = "Chandrayaan & Gaganyaan: Historic Milestone Achieved in High-Altitude Flight Tests",
                description = "ISRO achieves critical propulsion landmark with flawless cryogenic restart simulation ahead of scheduled crewed space mission.",
                link = "https://news.google.com",
                source = "ISRO DISPATCH",
                pubDate = "Just now",
                timestamp = System.currentTimeMillis(),
                category = "science",
                imageUrl = "https://images.unsplash.com/photo-1517976487508-8f8303d6a457?auto=format&fit=crop&w=1200&q=80"
            )
            NewsNotificationManager.showImportantNewsNotification(context, sampleArticle)
        }
    }

    fun openArticleFromNotification(
        id: String,
        title: String,
        link: String,
        source: String,
        imageUrl: String?,
        desc: String,
        category: String
    ) {
        val article = NewsArticle(
            id = id,
            title = title,
            description = desc,
            link = link,
            source = source,
            pubDate = "Just now",
            timestamp = System.currentTimeMillis(),
            category = category,
            imageUrl = imageUrl
        )
        openArticle(article)
    }

    private fun startSilentAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(6 * 60 * 1000L)
                val currentCategory = _uiState.value.selectedCategory
                val result = repository.getFeed(currentCategory, _uiState.value.language)
                result.onSuccess { latestArticles ->
                    val currentIds = _uiState.value.articles.map { it.id }.toSet()
                    val hasNew = latestArticles.any { !currentIds.contains(it.id) }
                    if (hasNew && latestArticles.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                hasNewStoriesAvailable = true,
                                pendingNewStories = latestArticles
                            )
                        }
                    }
                }
            }
        }
    }
}

class NewsViewModelFactory(
    private val repository: NewsRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(repository, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
