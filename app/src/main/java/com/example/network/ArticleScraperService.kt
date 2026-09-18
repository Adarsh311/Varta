package com.example.network

import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.model.ArticleMedia
import com.example.model.NewsArticle
import com.example.model.ScrapedArticle
import com.example.util.DateTimeFormatterHelper
import com.example.util.NewsImageHelper
import com.example.util.TextDivisionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ArticleScraperService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
) {
    private val memoryCache = ConcurrentHashMap<String, ScrapedArticle>()
    private val userAgent =
        "Mozilla/5.0 (Linux; Android 14; Mobile; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    suspend fun scrapeArticle(article: NewsArticle): ScrapedArticle = withContext(Dispatchers.IO) {
        memoryCache[article.id]?.let { return@withContext it }

        try {
            // 1. Decode Google News URL to real publisher destination
            val decodedUrl = GoogleNewsUrlDecoder.decodeUrl(client, article.link, userAgent)
            Log.d("ArticleScraperService", "Target URL: $decodedUrl (from ${article.link})")

            val scraped = doScrapePublisher(article, decodedUrl)
            memoryCache[article.id] = scraped
            return@withContext scraped
        } catch (e: Exception) {
            Log.e("ArticleScraperService", "Failed to scrape ${article.link}: ${e.message}", e)
            val fallback = buildEditorialFallback(article, article.link)
            memoryCache[article.id] = fallback
            return@withContext fallback
        }
    }

    suspend fun fetchRealHeroImage(article: NewsArticle): String? = withContext(Dispatchers.IO) {
        memoryCache[article.id]?.heroImageUrl?.let { cached ->
            if (NewsImageHelper.isValidEditorialImage(cached) && !NewsImageHelper.isFallbackStockImage(cached)) {
                return@withContext cached
            }
        }

        try {
            val targetUrl = GoogleNewsUrlDecoder.decodeUrl(client, article.link, userAgent)
            val req = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", userAgent)
                .header("Referer", "https://www.google.com/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val html = response.body?.string().orEmpty()
                if (html.isBlank() || isWafErrorPage(html)) return@withContext null

                val doc = Jsoup.parse(html, response.request.url.toString())
                val candidate = extractHeroImageUrl(doc, targetUrl)

                if (candidate != null && NewsImageHelper.isValidEditorialImage(candidate)) {
                    return@withContext candidate
                }
            }
        } catch (_: Exception) {}
        null
    }

    private fun doScrapePublisher(article: NewsArticle, targetUrl: String): ScrapedArticle {
        var finalUrl = targetUrl
        var doc: Document? = null

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-IN,en-US;q=0.9,en;q=0.8,hi;q=0.7")
                .header("Referer", "https://www.google.com/")
                .header("Upgrade-Insecure-Requests", "1")
                .build()

            val response = client.newCall(request).execute()
            finalUrl = response.request.url.toString()
            val isSuccess = response.isSuccessful
            val html = response.body?.string().orEmpty()
            response.close()

            if (isSuccess && html.isNotBlank() && !isWafErrorPage(html)) {
                doc = Jsoup.parse(html, finalUrl)
            } else {
                doc = tryFallbackFetch(targetUrl)
            }
        } catch (e: Exception) {
            Log.w("ArticleScraperService", "Network fetch error for $targetUrl: ${e.message}")
            doc = tryFallbackFetch(targetUrl)
        }

        if (doc == null) {
            return buildEditorialFallback(article, finalUrl)
        }

        return extractArticleData(doc, article, finalUrl)
    }

    private fun tryFallbackFetch(targetUrl: String): Document? {
        return try {
            val fallbackUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            val req = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", fallbackUa)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    if (body.isNotBlank() && !isWafErrorPage(body)) {
                        return Jsoup.parse(body, resp.request.url.toString())
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun isWafErrorPage(html: String): Boolean {
        val lower = html.lowercase()
        return lower.contains("reference #") ||
               lower.contains("errors.edgesuite.net") ||
               (lower.contains("access denied") && (lower.contains("edgesuite") || lower.contains("server"))) ||
               (lower.contains("cloudflare") && (lower.contains("ray id:") || lower.contains("attention required"))) ||
               (lower.contains("just a moment...") && lower.contains("cloudflare")) ||
               lower.contains("403 forbidden") ||
               lower.contains("checking your browser before accessing") ||
               lower.contains("please enable javascript and cookies") ||
               lower.contains("perimeterx") ||
               lower.contains("security service to protect itself from online attacks")
    }

    private fun extractArticleData(doc: Document, article: NewsArticle, finalUrl: String): ScrapedArticle {
        // 1. Extract Hero Image using full publisher meta, JSON-LD, and lead figure analysis
        var heroImage: String? = extractHeroImageUrl(doc, finalUrl)

        // 2. Author Extraction
        var author: String? = doc.select("meta[name=author]").first()?.attr("content")
            ?: doc.select("meta[property=article:author]").first()?.attr("content")
        if (author.isNullOrBlank()) {
            author = doc.select(".author, .byline, [rel=author], .author-name, .story-author, .article-author").first()?.text()
        }
        if (author != null && (author.length > 50 || author.contains("http") || author.contains("@"))) {
            author = null
        }

        // 3. Published Date
        val rawDate = doc.select("meta[property=article:published_time]").first()?.attr("content")
            ?: doc.select("meta[name=publish-date]").first()?.attr("content")
            ?: doc.select("time").first()?.text()
            ?: article.pubDate.ifBlank { article.relativeTime }
        val publishedTime = DateTimeFormatterHelper.formatPublishedDate(rawDate, article.timestamp)

        // Clean out noise elements from document
        doc.select("script, style, noscript, iframe, nav, header, footer, .ad, .advertisement, .ad-box, .banner, .social-share, .comments, .related, .sidebar, .menu, .popup, .newsletter, .widget").remove()

        // 4. Target primary article container
        val candidateContainers = listOf(
            "[itemprop=articleBody]",
            ".articlebodycontent",
            "div[class*=\"articlebodycontent\"]",
            "div[class*=\"content-body\"]",
            "div[id*=\"content-body\"]",
            "div[class*=\"story-element-text\"]",
            ".article-body",
            ".story-body",
            ".story-content",
            ".story_details",
            ".article__content",
            ".article__body",
            ".entry-content",
            ".main-content",
            ".arttext",
            "div.Normal",
            "div._3YYSt",
            "div[data-articlebody]",
            "div[class*=\"article_content\"]",
            "div.story-section",
            "#ins_storybody",
            ".ins_storybody",
            "div[class*=\"StoryDetail_articleBody\"]",
            "div[class*=\"sp-cn\"]",
            "div[class*=\"story-detail\"]",
            "#pcl-full-content",
            ".story-details",
            "div[class*=\"ev-meter-content\"]",
            ".storyDetails",
            "article",
            "#article-body",
            "#story-body",
            ".content-article",
            ".story-text"
        )

        var container: Element? = null
        for (selector in candidateContainers) {
            val found = doc.select(selector).first()
            if (found != null && found.text().length > 150) {
                container = found
                break
            }
        }

        val rawParagraphs = mutableListOf<String>()
        val inlineImages = mutableListOf<ArticleMedia>()
        val keyHighlights = mutableListOf<String>()

        // Check for bullet highlights in container or document
        val highlightElements = container?.select(".highlights li, .key-points li, .bullet-points li, .summary-points li, ul li, ol li")
            ?: doc.select(".highlights li, .key-points li, .bullet-points li")
        if (highlightElements != null) {
            for (li in highlightElements) {
                val point = cleanParagraphText(li.text())
                if (point.length in 35..180 && !point.contains("Also Read", ignoreCase = true)) {
                    keyHighlights.add(point)
                    if (keyHighlights.size >= 3) break
                }
            }
        }

        if (container != null) {
            // Replace <br> with newlines so paragraph boundaries are preserved
            container.select("br").append("\n")

            val pElements = container.select("p, div.Normal, div._3YYSt, div.story-element-text")
            for (p in pElements) {
                val text = cleanParagraphText(p.text())
                if (isQualityParagraph(text)) {
                    rawParagraphs.add(text)
                }
            }

            // Extract inline images from container
            val imgElements = container.select("img")
            for (img in imgElements) {
                val src = img.absUrl("src").ifBlank { img.attr("data-src") }
                val alt = img.attr("alt").ifBlank { img.attr("title") }
                if (NewsImageHelper.isValidEditorialImage(src) && src != heroImage) {
                    inlineImages.add(ArticleMedia(url = src, caption = alt.ifBlank { null }))
                }
            }
        }

        // If container was not found or yielded too few paragraphs, search whole body
        if (rawParagraphs.size < 2) {
            doc.body().select("br").append("\n")
            val allPs = doc.body().select("p, div.Normal, div[class*=\"article_content\"] > div")
            for (p in allPs) {
                val text = cleanParagraphText(p.text())
                if (isQualityParagraph(text) && text.length > 40) {
                    rawParagraphs.add(text)
                }
            }
        }

        // Divide long paragraphs cleanly into comfortable reading paragraphs
        val formattedParagraphs = TextDivisionHelper.divideIntoReadingParagraphs(rawParagraphs)

        // If no paragraphs found or fewer than 2 paragraphs (strict paywall / SPA / scrape block),
        // fall back to our high quality structured editorial presentation
        if (formattedParagraphs.size < 2) {
            val fallback = buildEditorialFallback(article, finalUrl)
            return fallback.copy(
                heroImageUrl = heroImage ?: article.imageUrl?.takeIf { NewsImageHelper.isValidEditorialImage(it) },
                author = author,
                finalUrl = finalUrl
            )
        }

        // If key highlights are missing, automatically derive 2-3 crisp highlights from lead paragraphs
        if (keyHighlights.isEmpty() && formattedParagraphs.isNotEmpty()) {
            val autoHighlights = formattedParagraphs.take(3)
                .map { it.split(". ").first().trim() }
                .filter { it.length in 35..140 }
            keyHighlights.addAll(autoHighlights.take(3))
        }

        // If hero image still null, check first image found in article
        if (heroImage == null && inlineImages.isNotEmpty()) {
            heroImage = inlineImages.first().url
            inlineImages.removeAt(0)
        }

        // If still no hero image or if image was generic/crest/logo, use authentic source image or fallback photo
        if (heroImage.isNullOrBlank() || !NewsImageHelper.isValidEditorialImage(heroImage)) {
            heroImage = article.imageUrl?.takeIf { NewsImageHelper.isValidEditorialImage(it) && !NewsImageHelper.isFallbackStockImage(it) }
                ?: NewsImageHelper.getValidHeroImage(article)
        }

        return ScrapedArticle(
            id = article.id,
            title = article.title,
            source = article.source,
            originalLink = article.link,
            finalUrl = finalUrl,
            heroImageUrl = heroImage,
            author = author,
            publishedDate = publishedTime,
            paragraphs = formattedParagraphs,
            inlineImages = inlineImages.take(3),
            keyHighlights = keyHighlights.take(3),
            isScrapedFromWeb = true
        )
    }

    private fun cleanParagraphText(raw: String): String {
        return raw.trim()
            .replace("\\s+".toRegex(), " ")
            .replace("Also Read:.*".toRegex(), "")
            .replace("CLICK HERE TO DOWNLOAD.*".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("Follow us on.*".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun isQualityParagraph(text: String): Boolean {
        if (text.length < 35) return false
        val lower = text.lowercase()
        val bannedSubstrings = listOf(
            "copyright", "all rights reserved", "subscribe now", "terms of use",
            "privacy policy", "cookie policy", "sign up for", "click here",
            "advertisement", "newsletter", "disclaimer:", "read also", "share this:",
            "reference #", "errors.edgesuite", "edgesuite.net", "access denied",
            "cloudflare", "ray id:", "please enable cookies", "verify you are a human",
            "turn on javascript", "web application firewall", "security check",
            "403 forbidden", "incident id:", "perimeterx", "akamai"
        )
        for (banned in bannedSubstrings) {
            if (lower.contains(banned)) {
                return false
            }
        }
        return true
    }

    private fun resolveUrl(url: String, baseUrl: String): String {
        return try {
            val trimmed = url.trim()
            if (trimmed.startsWith("//")) {
                "https:$trimmed"
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                URI(baseUrl).resolve(trimmed).toString()
            }
        } catch (_: Exception) {
            url
        }
    }

    private fun extractHeroImageUrl(doc: Document, baseUrl: String): String? {
        // 1. Check OpenGraph, Twitter, schema, and common publisher image meta tags
        val metaSelectors = listOf(
            "meta[property=\"og:image\"]",
            "meta[name=\"og:image\"]",
            "meta[property=\"og:image:url\"]",
            "meta[property=\"og:image:secure_url\"]",
            "meta[name=\"twitter:image\"]",
            "meta[property=\"twitter:image\"]",
            "meta[name=\"twitter:image:src\"]",
            "meta[name=\"image\"]",
            "meta[itemprop=\"image\"]",
            "link[rel=\"image_src\"]"
        )
        for (selector in metaSelectors) {
            val elem = doc.select(selector).firstOrNull() ?: continue
            val raw = elem.attr("content").ifBlank { elem.attr("href") }
            if (raw.isNotBlank() && NewsImageHelper.isValidEditorialImage(raw)) {
                return resolveUrl(raw, baseUrl)
            }
        }

        // 2. Check JSON-LD metadata
        val ldJsonElements = doc.select("script[type=\"application/ld+json\"]")
        for (elem in ldJsonElements) {
            try {
                val raw = elem.data()
                if (raw.contains("\"image\"")) {
                    val json = JSONObject(raw)
                    val found = extractImageFromJsonLd(json, baseUrl)
                    if (found != null) return found
                }
            } catch (_: Exception) {}
        }

        // 3. Check Lead article figure / hero element
        val leadImgSelectors = listOf(
            "figure.featured-image img",
            "figure[class*=\"lead\"] img",
            "figure[class*=\"hero\"] img",
            "div[class*=\"featured-image\"] img",
            "div[class*=\"story-image\"] img",
            "div[class*=\"article-image\"] img",
            "div[class*=\"lead-image\"] img",
            "div[class*=\"hero-image\"] img",
            "article figure img",
            ".article-lead-image img",
            "#lead-image img"
        )
        for (selector in leadImgSelectors) {
            val img = doc.select(selector).firstOrNull() ?: continue
            val raw = img.absUrl("src").ifBlank {
                img.attr("data-src").ifBlank { img.attr("src") }
            }
            if (raw.isNotBlank() && NewsImageHelper.isValidEditorialImage(raw)) {
                return resolveUrl(raw, baseUrl)
            }
        }

        return null
    }

    private fun extractImageFromJsonLd(json: JSONObject, baseUrl: String): String? {
        if (json.has("image")) {
            val imgObj = json.get("image")
            if (imgObj is String && NewsImageHelper.isValidEditorialImage(imgObj)) {
                return resolveUrl(imgObj, baseUrl)
            } else if (imgObj is JSONObject && imgObj.has("url")) {
                val u = imgObj.getString("url")
                if (NewsImageHelper.isValidEditorialImage(u)) {
                    return resolveUrl(u, baseUrl)
                }
            } else if (imgObj is org.json.JSONArray && imgObj.length() > 0) {
                val first = imgObj.opt(0)
                if (first is String && NewsImageHelper.isValidEditorialImage(first)) {
                    return resolveUrl(first, baseUrl)
                } else if (first is JSONObject && first.has("url")) {
                    val u = first.getString("url")
                    if (NewsImageHelper.isValidEditorialImage(u)) {
                        return resolveUrl(u, baseUrl)
                    }
                }
            }
        }
        if (json.has("@graph")) {
            val graph = json.optJSONArray("@graph")
            if (graph != null) {
                for (i in 0 until graph.length()) {
                    val node = graph.optJSONObject(i) ?: continue
                    val res = extractImageFromJsonLd(node, baseUrl)
                    if (res != null) return res
                }
            }
        }
        return null
    }

    private fun buildEditorialFallback(article: NewsArticle, finalUrl: String): ScrapedArticle {
        val cleanDesc = TextDivisionHelper.cleanDescriptionSnippet(article.description, article.title, article.source)

        val rawParagraphs = mutableListOf<String>()

        // 1. Lead Paragraph: Headline expansion & publisher attribution
        val leadSentence = if (cleanDesc.isNotBlank()) {
            "${article.title}. According to reports by ${article.source}, $cleanDesc"
        } else {
            "In an unfolding dispatch reported by ${article.source}, significant developments have emerged regarding ${article.title}."
        }
        rawParagraphs.add(leadSentence)

        // 2. Background and Category Context
        val categoryContext = when (article.category.lowercase()) {
            "india" -> "This development highlights critical governance, judicial, and civic developments currently shaping the national landscape across India."
            "world" -> "This international event marks a significant diplomatic and geopolitical development with regional and global ramifications."
            "business" -> "Financial analysts and market observers are actively evaluating the economic, industrial, and fiscal implications of this announcement."
            "technology" -> "Technology specialists and industry analysts are closely tracking this development and its wider implications for consumer and enterprise sectors."
            "sports" -> "Sports analysts and tournament commentators are reviewing the performance benchmarks and competitive outcomes linked to this match update."
            "entertainment" -> "Entertainment correspondents and industry commentators are documenting audience reception and cultural buzz around this release."
            "science" -> "Researchers, scientific observers, and institutional teams are monitoring the experimental findings and technical milestones of this initiative."
            "health" -> "Healthcare authorities, medical practitioners, and policy bodies are monitoring the health outcomes and advisory protocols in connection with this alert."
            else -> "Correspondents from ${article.source} and syndicated wire networks are actively compiling firsthand statements and verification data on this developing situation."
        }
        rawParagraphs.add(categoryContext)

        // 3. Ongoing development & Live Portal Attribution
        val closingParagraph = "Live dispatches, official press releases, and multimedia coverage continue to develop. Readers can review the full unedited report, official statements, and live documentation directly through the ${article.source} publication portal."
        rawParagraphs.add(closingParagraph)

        val formattedParagraphs = TextDivisionHelper.divideIntoReadingParagraphs(rawParagraphs)
        val hero = article.imageUrl?.takeIf { NewsImageHelper.isValidEditorialImage(it) && !NewsImageHelper.isFallbackStockImage(it) }
            ?: NewsImageHelper.getValidHeroImage(article)
        val formattedDate = DateTimeFormatterHelper.formatPublishedDate(article.pubDate, article.timestamp)

        val highlights = listOf(
            article.title,
            "Reporting and ongoing investigation provided by ${article.source}",
            "Continuing coverage and official releases tracked across national networks"
        )

        return ScrapedArticle(
            id = article.id,
            title = article.title,
            source = article.source,
            originalLink = article.link,
            finalUrl = finalUrl,
            heroImageUrl = hero,
            author = null,
            publishedDate = formattedDate,
            paragraphs = formattedParagraphs,
            inlineImages = emptyList(),
            keyHighlights = highlights,
            isScrapedFromWeb = false
        )
    }
}
