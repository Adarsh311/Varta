package com.example.network

import androidx.core.text.HtmlCompat
import com.example.model.NewsArticle
import com.example.util.NewsImageHelper
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

object RssFeedParser {
    private val dateFormats = listOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        },
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("d MMM yyyy HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )

    private val imgPattern = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)

    fun parse(inputStream: InputStream, categoryName: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var inItem = false
        var currentTag = ""

        var currentTitle = StringBuilder()
        var currentLink = StringBuilder()
        var currentGuid = StringBuilder()
        var currentPubDate = StringBuilder()
        var currentDescription = StringBuilder()
        var currentSource = StringBuilder()
        var currentImageUrl: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name.lowercase(Locale.ROOT)
                    currentTag = tagName
                    if (tagName == "item") {
                        inItem = true
                        currentTitle = StringBuilder()
                        currentLink = StringBuilder()
                        currentGuid = StringBuilder()
                        currentPubDate = StringBuilder()
                        currentDescription = StringBuilder()
                        currentSource = StringBuilder()
                        currentImageUrl = null
                    } else if (inItem) {
                        if (tagName == "media:content" || tagName == "content" || tagName == "media:thumbnail" ||
                            tagName == "thumbnail" || tagName == "enclosure" || tagName.endsWith(":content") ||
                            tagName.endsWith(":thumbnail")
                        ) {
                            val count = parser.attributeCount
                            var foundUrl: String? = null
                            for (i in 0 until count) {
                                val attrName = parser.getAttributeName(i).lowercase(Locale.ROOT)
                                val attrVal = parser.getAttributeValue(i) ?: continue
                                if (attrName == "url" || attrName.endsWith(":url") || attrName == "src" || attrName == "href") {
                                    if (attrVal.isNotBlank() && NewsImageHelper.isValidEditorialImage(attrVal)) {
                                        foundUrl = attrVal
                                        break
                                    }
                                }
                            }
                            if (currentImageUrl == null && foundUrl != null) {
                                currentImageUrl = foundUrl
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inItem) {
                        val text = parser.text ?: ""
                        when (currentTag) {
                            "title" -> currentTitle.append(text)
                            "link" -> currentLink.append(text)
                            "guid" -> currentGuid.append(text)
                            "pubdate" -> currentPubDate.append(text)
                            "description" -> currentDescription.append(text)
                            "source" -> currentSource.append(text)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name.lowercase(Locale.ROOT)
                    if (tagName == currentTag) {
                        currentTag = ""
                    }
                    if (tagName == "item" && inItem) {
                        inItem = false
                        val rawTitle = currentTitle.toString().trim()
                        val rawLink = currentLink.toString().trim()
                        val rawGuid = currentGuid.toString().trim()
                        val rawPubDate = currentPubDate.toString().trim()
                        val rawDesc = currentDescription.toString().trim()
                        val explicitSource = currentSource.toString().trim()

                        val (cleanedTitle, extractedSource) = extractTitleAndSource(rawTitle, explicitSource)
                        val finalSource = when {
                            rawLink.contains("thehindu.com", ignoreCase = true) -> "The Hindu"
                            rawLink.contains("timesofindia", ignoreCase = true) || rawLink.contains("toiimg", ignoreCase = true) -> "The Times of India"
                            rawLink.contains("ndtv.com", ignoreCase = true) -> "NDTV"
                            rawLink.contains("gadgets360", ignoreCase = true) -> "Gadgets360"
                            rawLink.contains("indianexpress.com", ignoreCase = true) -> "The Indian Express"
                            rawLink.contains("hindustantimes.com", ignoreCase = true) -> "Hindustan Times"
                            rawLink.contains("scroll.in", ignoreCase = true) -> "Scroll.in"
                            rawLink.contains("theprint.in", ignoreCase = true) -> "ThePrint"
                            rawLink.contains("thewire.in", ignoreCase = true) -> "The Wire"
                            rawLink.contains("livelaw.in", ignoreCase = true) -> "LiveLaw"
                            explicitSource.isNotBlank() -> explicitSource
                            extractedSource.isNotBlank() -> extractedSource
                            else -> "Google News"
                        }

                        // Try extracting from description html
                        var finalImage = currentImageUrl
                        if (finalImage == null && rawDesc.isNotBlank()) {
                            val extracted = extractImageFromHtml(rawDesc)
                            if (NewsImageHelper.isValidEditorialImage(extracted)) {
                                finalImage = extracted
                            }
                        }

                        val cleanDescription = com.example.util.TextDivisionHelper.cleanDescriptionSnippet(rawDesc, cleanedTitle, finalSource)
                        val timestamp = parseDateToTimestamp(rawPubDate)
                        val id = if (rawGuid.isNotBlank()) rawGuid else rawLink.ifBlank { cleanedTitle }

                        if (cleanedTitle.isNotBlank()) {
                            val candidate = NewsArticle(
                                id = id,
                                title = cleanedTitle,
                                link = rawLink,
                                pubDate = rawPubDate,
                                timestamp = timestamp,
                                source = finalSource,
                                description = cleanDescription,
                                category = categoryName,
                                imageUrl = finalImage
                            )
                            articles.add(candidate)
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return articles
    }

    private fun extractImageFromHtml(html: String): String? {
        return try {
            val matcher = imgPattern.matcher(html)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractTitleAndSource(rawTitle: String, explicitSource: String): Pair<String, String> {
        val lastDashIndex = rawTitle.lastIndexOf(" - ")
        return if (lastDashIndex != -1 && lastDashIndex > 5) {
            val titlePart = rawTitle.substring(0, lastDashIndex).trim()
            val sourcePart = rawTitle.substring(lastDashIndex + 3).trim()
            Pair(titlePart, sourcePart)
        } else {
            Pair(rawTitle, explicitSource)
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

    private fun parseDateToTimestamp(pubDateStr: String): Long {
        if (pubDateStr.isBlank()) return System.currentTimeMillis()
        for (format in dateFormats) {
            try {
                val date = format.parse(pubDateStr.trim())
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
                // Try next format
            }
        }
        return System.currentTimeMillis()
    }
}
