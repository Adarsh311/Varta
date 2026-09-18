package com.example.ui

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
    val selectedArticle: NewsArticle? = null,
    val scrapedArticle: ScrapedArticle? = null,
    val isScrapingArticle: Boolean = false,
    val readingDensity: ReadingDensity = ReadingDensity.MAGAZINE,
    val isDarkMode: Boolean = true,
    val lastRefreshedAt: Long = System.currentTimeMillis()
)

class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState())
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
            val result = repository.getFeed(category)
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
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchErrorMessage = null) }
            val result = repository.searchNews(query)
            result.onSuccess { results ->
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false,
                        searchErrorMessage = if (results.isEmpty()) "No stories found matching \"$query\"" else null
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
            val fullArticle = repository.getFullArticle(article)
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
        openArticle(article)
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
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
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

    private fun startSilentAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(6 * 60 * 1000L)
                val currentCategory = _uiState.value.selectedCategory
                val result = repository.getFeed(currentCategory)
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

class NewsViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
