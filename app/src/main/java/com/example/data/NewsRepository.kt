package com.example.data

import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import com.example.model.ScrapedArticle
import com.example.network.ArticleScraperService
import com.example.network.NewsFeedService
import com.example.network.TranslationService
import com.example.util.NewsImageHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository(
    private val feedService: NewsFeedService,
    private val articleDao: ArticleDao,
    private val scraperService: ArticleScraperService = ArticleScraperService(),
    private val translationService: TranslationService = TranslationService()
) {
    val bookmarkedArticles: Flow<List<NewsArticle>> = articleDao.getBookmarkedArticles()
        .map { list -> list.map { it.toNewsArticle() } }

    val bookmarkedIds: Flow<Set<String>> = articleDao.getBookmarkedIds()
        .map { it.toSet() }

    suspend fun getFeed(
        category: NewsCategory,
        language: com.example.model.AppLanguage = com.example.model.AppLanguage.ENGLISH
    ): Result<List<NewsArticle>> {
        return feedService.fetchCategoryFeed(category, language)
    }

    suspend fun searchNews(
        query: String,
        language: com.example.model.AppLanguage = com.example.model.AppLanguage.ENGLISH
    ): Result<List<NewsArticle>> {
        val searchUrl = NewsCategory.searchUrl(query, language)
        return feedService.fetchFeed(searchUrl, "Search: $query", language)
    }

    suspend fun getFullArticle(
        article: NewsArticle,
        language: com.example.model.AppLanguage = com.example.model.AppLanguage.ENGLISH
    ): ScrapedArticle {
        // 1. Check if we have offline cached paragraphs in Room database
        val localEntity = articleDao.getArticleById(article.id)
        if (localEntity != null) {
            val localParagraphs = localEntity.getParagraphsList()
            val isOldFallback = localParagraphs.size <= 3 && localParagraphs.any {
                it.contains("यह घटनाक्रम देश के राष्ट्रीय") ||
                it.contains("According to reports by") ||
                it.contains("पाठक पूरी रिपोर्ट") ||
                it.contains("इस विषय पर लगातार ताज़ा रिपोर्ट") ||
                it.contains("Readers can review the full unedited report")
            }
            if (localParagraphs.size >= 2 && !isOldFallback) {
                val highlights = localEntity.getHighlightsList()
                val cachedArticle = ScrapedArticle(
                    id = localEntity.id,
                    title = localEntity.title,
                    source = localEntity.source,
                    originalLink = localEntity.link,
                    finalUrl = localEntity.finalUrl ?: localEntity.link,
                    heroImageUrl = localEntity.imageUrl ?: article.imageUrl,
                    author = localEntity.author,
                    publishedDate = localEntity.pubDate,
                    paragraphs = localParagraphs,
                    inlineImages = emptyList(),
                    keyHighlights = highlights,
                    isScrapedFromWeb = true
                )
                return if (language == com.example.model.AppLanguage.HINDI) {
                    translationService.translateScrapedArticle(cachedArticle)
                } else {
                    cachedArticle
                }
            }
        }

        // 2. Fetch/Scrape from web
        val scraped = scraperService.scrapeArticle(article)

        // 3. If bookmarked or already in db, cache the scraped content for offline reading
        if (articleDao.isBookmarked(article.id)) {
            val updatedEntity = ArticleEntity.fromScrapedArticle(article, scraped, bookmarked = true)
            articleDao.insertOrUpdate(updatedEntity)
        }

        return if (language == com.example.model.AppLanguage.HINDI) {
            translationService.translateScrapedArticle(scraped)
        } else {
            scraped
        }
    }

    suspend fun clearArticleCache(articleId: String) {
        scraperService.clearCacheFor(articleId)
        val entity = articleDao.getArticleById(articleId)
        if (entity != null) {
            if (!entity.isBookmarked) {
                articleDao.deleteById(articleId)
            } else {
                articleDao.insertOrUpdate(entity.copy(paragraphsJson = null, keyHighlightsJson = null))
            }
        }
    }

    suspend fun clearAllBookmarks() {
        articleDao.deleteAllBookmarks()
    }

    suspend fun resolveRealHeroImage(article: NewsArticle): String? {
        val realUrl = scraperService.fetchRealHeroImage(article)
        if (realUrl != null && NewsImageHelper.isValidEditorialImage(realUrl)) {
            updateArticleImageIfBookmarked(article.id, realUrl)
            return realUrl
        }
        return null
    }

    suspend fun updateArticleImageIfBookmarked(articleId: String, imageUrl: String) {
        if (articleDao.isBookmarked(articleId)) {
            val list = articleDao.getBookmarkedArticlesSync()
            val existing = list.firstOrNull { it.id == articleId }
            if (existing != null) {
                articleDao.insertOrUpdate(existing.copy(imageUrl = imageUrl))
            }
        }
    }

    suspend fun toggleBookmark(article: NewsArticle) {
        val isCurrentlyBookmarked = articleDao.isBookmarked(article.id)
        if (isCurrentlyBookmarked) {
            articleDao.setBookmark(article.id, false)
            articleDao.deleteById(article.id)
        } else {
            val entity = ArticleEntity.fromNewsArticle(article, bookmarked = true)
            articleDao.insertOrUpdate(entity)
        }
    }

    suspend fun isBookmarked(id: String): Boolean {
        return articleDao.isBookmarked(id)
    }
}
