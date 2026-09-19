package com.example.network

import android.util.Log
import com.example.model.NewsArticle
import com.example.model.ScrapedArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class TranslationService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
) {
    private val cache = ConcurrentHashMap<String, String>()
    private val userAgent =
        "Mozilla/5.0 (Linux; Android 14; Mobile; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    suspend fun translateToHindi(text: String): String = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return@withContext text

        // If already contains Devanagari Hindi characters (\u0900..\u097F)
        if (trimmed.any { it in '\u0900'..'\u097F' }) {
            return@withContext trimmed
        }

        cache[trimmed]?.let { cached ->
            return@withContext cached
        }

        val translated = fetchTranslationGtx(trimmed)
            ?: fetchTranslationMyMemory(trimmed)
            ?: trimmed

        if (translated != trimmed) {
            cache[trimmed] = translated
        }
        translated
    }

    suspend fun translateListToHindi(list: List<String>): List<String> = withContext(Dispatchers.IO) {
        if (list.isEmpty()) return@withContext list
        val deferreds = list.map { text ->
            async { translateToHindi(text) }
        }
        deferreds.awaitAll()
    }

    suspend fun translateArticle(article: NewsArticle): NewsArticle = withContext(Dispatchers.IO) {
        if (article.title.any { it in '\u0900'..'\u097F' } && article.description.any { it in '\u0900'..'\u097F' }) {
            return@withContext article
        }
        val deferredTitle = async { translateToHindi(article.title) }
        val deferredDesc = async { translateToHindi(article.description) }

        article.copy(
            title = deferredTitle.await(),
            description = deferredDesc.await()
        )
    }

    suspend fun translateArticles(articles: List<NewsArticle>): List<NewsArticle> = withContext(Dispatchers.IO) {
        if (articles.isEmpty()) return@withContext articles
        val deferreds = articles.map { article ->
            async { translateArticle(article) }
        }
        deferreds.awaitAll()
    }

    suspend fun translateScrapedArticle(scraped: ScrapedArticle): ScrapedArticle = withContext(Dispatchers.IO) {
        val deferredTitle = async { translateToHindi(scraped.title) }
        val deferredParagraphs = async { translateListToHindi(scraped.paragraphs) }
        val deferredHighlights = async { translateListToHindi(scraped.keyHighlights) }

        scraped.copy(
            title = deferredTitle.await(),
            paragraphs = deferredParagraphs.await(),
            keyHighlights = deferredHighlights.await()
        )
    }

    private fun fetchTranslationGtx(text: String): String? {
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=hi&dt=t&q=$encoded"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val rootArray = JSONArray(body)
                val sentencesArray = rootArray.optJSONArray(0) ?: return null
                val sb = StringBuilder()
                for (i in 0 until sentencesArray.length()) {
                    val sentenceObj = sentencesArray.optJSONArray(i) ?: continue
                    val translatedChunk = sentenceObj.optString(0, "")
                    sb.append(translatedChunk)
                }
                val result = sb.toString().trim()
                if (result.isNotBlank()) result else null
            }
        } catch (e: Exception) {
            Log.w("TranslationService", "GTX error for text snippet: ${e.message}")
            null
        }
    }

    private fun fetchTranslationMyMemory(text: String): String? {
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://api.mymemory.translated.net/get?q=$encoded&langpair=en|hi"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val json = JSONObject(body)
                val responseData = json.optJSONObject("responseData") ?: return null
                val translated = responseData.optString("translatedText", "").trim()
                if (translated.isNotBlank() && !translated.contains("MYMEMORY WARNING", ignoreCase = true)) {
                    translated
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w("TranslationService", "MyMemory error: ${e.message}")
            null
        }
    }
}
