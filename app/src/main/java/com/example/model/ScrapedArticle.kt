package com.example.model

data class ScrapedArticle(
    val id: String,
    val title: String,
    val source: String,
    val originalLink: String,
    val finalUrl: String = originalLink,
    val heroImageUrl: String? = null,
    val author: String? = null,
    val publishedDate: String? = null,
    val paragraphs: List<String> = emptyList(),
    val inlineImages: List<ArticleMedia> = emptyList(),
    val keyHighlights: List<String> = emptyList(),
    val isScrapedFromWeb: Boolean = true
)

data class ArticleMedia(
    val url: String,
    val caption: String? = null
)
