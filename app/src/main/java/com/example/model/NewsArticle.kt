package com.example.model

data class NewsArticle(
    val id: String,
    val title: String,
    val link: String,
    val pubDate: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "Google News",
    val description: String = "",
    val category: String = "India",
    val isBookmarked: Boolean = false,
    val imageUrl: String? = null
) {
    val relativeTime: String
        get() {
            if (timestamp <= 0L) return pubDate
            val now = System.currentTimeMillis()
            val diffMs = now - timestamp
            if (diffMs < 0) return "Just now"
            val diffMinutes = diffMs / (1000 * 60)
            val diffHours = diffMinutes / 60
            val diffDays = diffHours / 24
            return when {
                diffMinutes < 1 -> "Just now"
                diffMinutes < 60 -> "${diffMinutes}m ago"
                diffHours < 24 -> "${diffHours}h ago"
                diffDays == 1L -> "Yesterday"
                diffDays < 7 -> "${diffDays}d ago"
                else -> {
                    val datePart = pubDate.split(" ").take(4).joinToString(" ")
                    if (datePart.isNotBlank()) datePart else "${diffDays}d ago"
                }
            }
        }
}
