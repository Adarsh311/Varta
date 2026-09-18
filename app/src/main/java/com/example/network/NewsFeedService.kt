package com.example.network

import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.model.NewsArticle
import com.example.util.NewsImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class NewsFeedService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
) {
    private val userAgent =
        "Mozilla/5.0 (Linux; Android 14; Mobile; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    private val jsonDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    )

    suspend fun fetchCategoryFeed(category: com.example.model.NewsCategory): Result<List<NewsArticle>> =
        withContext(Dispatchers.IO) {
            val collectedArticles = mutableListOf<NewsArticle>()
            val seenKeys = mutableSetOf<String>()

            for (url in category.feedUrls) {
                try {
                    val directResult = tryDirectRssXml(url, category.title)
                    if (directResult.isSuccess) {
                        val items = directResult.getOrNull() ?: emptyList()
                        for (item in items) {
                            val key = normalizeKey(item.title)
                            if (key.length >= 10 && seenKeys.add(key)) {
                                collectedArticles.add(item)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("NewsFeedService", "Failed to fetch publisher feed: $url", e)
                }

                val realImageCount = collectedArticles.count {
                    !it.imageUrl.isNullOrBlank() && !NewsImageHelper.isFallbackStockImage(it.imageUrl)
                }
                if (realImageCount >= 30) {
                    break
                }
            }

            if (collectedArticles.isNotEmpty()) {
                val sorted = collectedArticles.sortedByDescending { it.timestamp }
                val uniqueImageFeed = NewsImageHelper.ensureUniqueImagesAcrossFeed(sorted)
                Log.d("NewsFeedService", "Fetched ${uniqueImageFeed.size} multi-source articles for ${category.title}")
                return@withContext Result.success(uniqueImageFeed)
            }

            // Fallback to legacy single feed query
            fetchFeed(category.feedUrl, category.title)
        }

    private fun normalizeKey(title: String): String {
        return title.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")
            .take(50)
    }

    suspend fun fetchFeed(url: String, categoryName: String): Result<List<NewsArticle>> =
        withContext(Dispatchers.IO) {
            // 1. Direct Google News RSS XML fetch
            val directResult = tryDirectRssXml(url, categoryName)
            if (directResult.isSuccess && directResult.getOrNull()?.isNotEmpty() == true) {
                val uniqueImages = NewsImageHelper.ensureUniqueImagesAcrossFeed(directResult.getOrNull()!!)
                Log.d("NewsFeedService", "Fetched ${uniqueImages.size} items directly from RSS")
                return@withContext Result.success(uniqueImages)
            }

            // 2. Secondary fallback via rss2json proxy
            Log.d("NewsFeedService", "Querying RSS via live proxy")
            val proxyResult = tryRss2JsonProxy(url, categoryName)
            if (proxyResult.isSuccess && proxyResult.getOrNull()?.isNotEmpty() == true) {
                val uniqueImages = NewsImageHelper.ensureUniqueImagesAcrossFeed(proxyResult.getOrNull()!!)
                return@withContext Result.success(uniqueImages)
            }

            val error = proxyResult.exceptionOrNull()
                ?: directResult.exceptionOrNull()
                ?: Exception("Could not fetch live headlines. Please check network connection.")
            Result.failure(error)
        }

    private fun tryDirectRssXml(url: String, categoryName: String): Result<List<NewsArticle>> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "application/rss+xml, application/xml, text/xml; q=0.9, */*; q=0.8")
                .header("Accept-Language", "en-IN,en;q=0.9,hi;q=0.8")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
                val bytes = response.body?.bytes() ?: return Result.failure(Exception("Empty XML body"))
                val articles = RssFeedParser.parse(ByteArrayInputStream(bytes), categoryName)
                if (articles.isEmpty()) {
                    Result.failure(Exception("No items parsed from XML"))
                } else {
                    Result.success(articles)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tryRss2JsonProxy(rawRssUrl: String, categoryName: String): Result<List<NewsArticle>> {
        return try {
            val encodedUrl = URLEncoder.encode(rawRssUrl, "UTF-8")
            val proxyEndpoint = "https://api.rss2json.com/v1/api.json?rss_url=$encodedUrl"
            val request = Request.Builder()
                .url(proxyEndpoint)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("Proxy HTTP ${response.code}: ${response.message}"))
                }
                val jsonString = response.body?.string() ?: return Result.failure(Exception("Empty JSON body"))
                val rootJson = JSONObject(jsonString)
                val status = rootJson.optString("status", "")
                if (!status.equals("ok", ignoreCase = true)) {
                    val message = rootJson.optString("message", "Failed to retrieve feed from Google News")
                    return Result.failure(Exception(message))
                }

                val itemsArray = rootJson.optJSONArray("items") ?: return Result.failure(Exception("No items in feed"))
                val articles = mutableListOf<NewsArticle>()

                for (i in 0 until itemsArray.length()) {
                    val item = itemsArray.getJSONObject(i)
                    val rawTitle = item.optString("title", "").trim()
                    val link = item.optString("link", "").trim()
                    val guid = item.optString("guid", "").trim()
                    val pubDateStr = item.optString("pubDate", "").trim()
                    val rawDesc = item.optString("description", "").trim()
                    val author = item.optString("author", "").trim()

                    var thumbnail = item.optString("thumbnail", "").trim().ifBlank { null }
                    val enclosureObj = item.optJSONObject("enclosure")
                    if (thumbnail == null && enclosureObj != null) {
                        thumbnail = enclosureObj.optString("link", "").ifBlank { null }
                    }

                    if (rawTitle.isNotBlank()) {
                        val (title, extractedSource) = extractTitleAndSource(rawTitle, author)
                        val finalSource = when {
                            author.isNotBlank() -> author
                            extractedSource.isNotBlank() -> extractedSource
                            else -> "Google News"
                        }
                        val cleanDesc = com.example.util.TextDivisionHelper.cleanDescriptionSnippet(rawDesc, title, finalSource)
                        val timestamp = parseJsonDateToTimestamp(pubDateStr)
                        val id = if (guid.isNotBlank()) guid else link.ifBlank { title }

                        val rawImg = if (NewsImageHelper.isValidEditorialImage(thumbnail)) thumbnail else null
                        val candidate = NewsArticle(
                            id = id,
                            title = title,
                            link = link,
                            pubDate = pubDateStr,
                            timestamp = timestamp,
                            source = finalSource,
                            description = cleanDesc,
                            category = categoryName,
                            imageUrl = rawImg
                        )

                        val resolvedImage = if (!rawImg.isNullOrBlank() && !NewsImageHelper.isFallbackStockImage(rawImg)) {
                            rawImg
                        } else {
                            NewsImageHelper.getValidHeroImage(candidate)
                        }
                        articles.add(candidate.copy(imageUrl = resolvedImage))
                    }
                }

                if (articles.isEmpty()) {
                    Result.failure(Exception("No articles found in Google News feed"))
                } else {
                    Result.success(articles)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractTitleAndSource(rawTitle: String, fallbackAuthor: String): Pair<String, String> {
        val lastDashIndex = rawTitle.lastIndexOf(" - ")
        return if (lastDashIndex != -1 && lastDashIndex > 5) {
            val titlePart = rawTitle.substring(0, lastDashIndex).trim()
            val sourcePart = rawTitle.substring(lastDashIndex + 3).trim()
            Pair(titlePart, sourcePart)
        } else {
            Pair(rawTitle, fallbackAuthor)
        }
    }

    private fun cleanHtmlSnippet(rawHtml: String, title: String): String {
        if (rawHtml.isBlank()) return ""
        return try {
            val text = HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
            val sanitized = text.replace(Regex("\\s+"), " ")
                .replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&")
                .trim()
            if (sanitized.equals(title, ignoreCase = true)) {
                ""
            } else {
                sanitized
            }
        } catch (_: Exception) {
            rawHtml.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
        }
    }

    private fun parseJsonDateToTimestamp(pubDateStr: String): Long {
        if (pubDateStr.isBlank()) return System.currentTimeMillis()
        for (format in jsonDateFormats) {
            try {
                val date = format.parse(pubDateStr.trim())
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}
