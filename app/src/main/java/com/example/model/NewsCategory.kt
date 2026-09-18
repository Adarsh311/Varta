package com.example.model

import java.net.URLEncoder

enum class NewsCategory(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val feedUrls: List<String>
) {
    INDIA(
        id = "india",
        title = "India",
        hindiTitle = "भारत",
        feedUrls = listOf(
            "https://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms",
            "https://www.thehindu.com/news/national/feeder/default.rss",
            "https://feeds.feedburner.com/ndtvnews-india-news",
            "https://news.google.com/rss/headlines/section/geo/India?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    TOP_STORIES(
        id = "top_stories",
        title = "Top Stories",
        hindiTitle = "प्रमुख समाचार",
        feedUrls = listOf(
            "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
            "https://www.thehindu.com/feeder/default.rss",
            "https://feeds.feedburner.com/ndtvnews-top-stories",
            "https://news.google.com/rss?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    BUSINESS(
        id = "business",
        title = "Business",
        hindiTitle = "व्यापार",
        feedUrls = listOf(
            "https://timesofindia.indiatimes.com/rssfeeds/1898055.cms",
            "https://www.thehindu.com/business/feeder/default.rss",
            "https://feeds.feedburner.com/ndtvprofit-latest",
            "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    TECHNOLOGY(
        id = "technology",
        title = "Technology",
        hindiTitle = "प्रौद्योगिकी",
        feedUrls = listOf(
            "https://timesofindia.indiatimes.com/rssfeeds/66949542.cms",
            "https://feeds.feedburner.com/gadgets360-latest",
            "https://www.thehindu.com/sci-tech/technology/feeder/default.rss",
            "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    SPORTS(
        id = "sports",
        title = "Sports",
        hindiTitle = "खेल",
        feedUrls = listOf(
            "https://timesofindia.indiatimes.com/rssfeeds/4719148.cms",
            "https://www.thehindu.com/sport/feeder/default.rss",
            "https://feeds.feedburner.com/ndtvsports-latest",
            "https://news.google.com/rss/headlines/section/topic/SPORTS?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    ENTERTAINMENT(
        id = "entertainment",
        title = "Entertainment",
        hindiTitle = "मनोरंजन",
        feedUrls = listOf(
            "https://feeds.feedburner.com/ndtvmovies-latest",
            "https://timesofindia.indiatimes.com/rssfeeds/1081479906.cms",
            "https://www.thehindu.com/entertainment/feeder/default.rss",
            "https://news.google.com/rss/headlines/section/topic/ENTERTAINMENT?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    SCIENCE(
        id = "science",
        title = "Science",
        hindiTitle = "विज्ञान",
        feedUrls = listOf(
            "https://www.thehindu.com/sci-tech/science/feeder/default.rss",
            "https://www.thehindu.com/sci-tech/energy-and-environment/feeder/default.rss",
            "https://news.google.com/rss/headlines/section/topic/SCIENCE?hl=en-IN&gl=IN&ceid=IN:en"
        )
    ),
    HEALTH(
        id = "health",
        title = "Health",
        hindiTitle = "स्वास्थ्य",
        feedUrls = listOf(
            "https://timesofindia.indiatimes.com/rssfeeds/3908999.cms",
            "https://www.thehindu.com/sci-tech/health/feeder/default.rss",
            "https://news.google.com/rss/headlines/section/topic/HEALTH?hl=en-IN&gl=IN&ceid=IN:en"
        )
    );

    val feedUrl: String get() = feedUrls.first()

    companion object {
        fun searchUrl(query: String): String {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            return "https://news.google.com/rss/search?q=$encodedQuery&hl=en-IN&gl=IN&ceid=IN:en"
        }
    }
}
