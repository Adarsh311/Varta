package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.NewsArticle

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
    val savedAt: Long = System.currentTimeMillis()
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
    }
}
