package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.NewsArticle
import com.example.model.ScrapedArticle
import org.json.JSONArray

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val link: String,
    val pubDate: String,
    val timestamp: Long,
    val source: String,
    val description: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val imageUrl: String? = null,
    val savedAt: Long = System.currentTimeMillis(),
    val paragraphsJson: String? = null,
    val keyHighlightsJson: String? = null,
    val author: String? = null,
    val finalUrl: String? = null
) {
    fun toNewsArticle(): NewsArticle {
        return NewsArticle(
            id = id,
            title = title,
            link = link,
            pubDate = pubDate,
            timestamp = timestamp,
            source = source,
            description = description,
            category = category,
            isBookmarked = isBookmarked,
            imageUrl = imageUrl
        )
    }

    fun getParagraphsList(): List<String> {
        if (paragraphsJson.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(paragraphsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val str = jsonArray.optString(i)
                if (str.isNotBlank()) list.add(str)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getHighlightsList(): List<String> {
        if (keyHighlightsJson.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(keyHighlightsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val str = jsonArray.optString(i)
                if (str.isNotBlank()) list.add(str)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromNewsArticle(article: NewsArticle, bookmarked: Boolean = article.isBookmarked): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                title = article.title,
                link = article.link,
                pubDate = article.pubDate,
                timestamp = article.timestamp,
                source = article.source,
                description = article.description,
                category = article.category,
                isBookmarked = bookmarked,
                imageUrl = article.imageUrl,
                savedAt = System.currentTimeMillis()
            )
        }

        fun fromScrapedArticle(article: NewsArticle, scraped: ScrapedArticle, bookmarked: Boolean = true): ArticleEntity {
            val paragraphsArray = JSONArray().apply {
                scraped.paragraphs.forEach { put(it) }
            }
            val highlightsArray = JSONArray().apply {
                scraped.keyHighlights.forEach { put(it) }
            }
            return ArticleEntity(
                id = article.id,
                title = article.title,
                link = article.link,
                pubDate = article.pubDate,
                timestamp = article.timestamp,
                source = article.source,
                description = article.description,
                category = article.category,
                isBookmarked = bookmarked,
                imageUrl = scraped.heroImageUrl ?: article.imageUrl,
                savedAt = System.currentTimeMillis(),
                paragraphsJson = paragraphsArray.toString(),
                keyHighlightsJson = highlightsArray.toString(),
                author = scraped.author,
                finalUrl = scraped.finalUrl
            )
        }
    }
}

