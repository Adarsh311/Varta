package com.example.util

import androidx.core.text.HtmlCompat

object TextDivisionHelper {

    // Common abbreviations that shouldn't be split as sentence terminators
    private val abbreviations = listOf(
        "Mr.", "Mrs.", "Ms.", "Dr.", "Prof.", "Govt.", "U.S.", "U.K.", "U.N.",
        "Lt.", "Col.", "Gen.", "Rs.", "vs.", "e.g.", "i.e.", "No.", "St.", "PM.",
        "p.m.", "a.m.", "Ltd.", "Pvt.", "Inc.", "Corp.", "Bros.", "Dept."
    )

    /**
     * Divides long or monolithic raw paragraphs into well-proportioned,
     * comfortable reading paragraphs (typically 2 to 3 sentences, 200-350 chars each).
     */
    fun divideIntoReadingParagraphs(rawParagraphs: List<String>): List<String> {
        val result = mutableListOf<String>()

        for (raw in rawParagraphs) {
            val cleaned = cleanText(raw)
            if (cleaned.isBlank()) continue

            // First, split by hard line-breaks if present
            val subBlocks = cleaned.split(Regex("(\r?\n)+"))
                .map { cleanText(it) }
                .filter { it.isNotBlank() }

            for (block in subBlocks) {
                if (block.length <= 380) {
                    // Small enough to be a standalone paragraph
                    result.add(block)
                } else {
                    // Break long monolithic paragraph by sentence boundaries
                    val splitSentences = splitIntoSentences(block)
                    if (splitSentences.size <= 2) {
                        result.add(block)
                    } else {
                        // Group 2-3 sentences into natural reading paragraphs
                        val grouped = groupSentencesIntoParagraphs(splitSentences)
                        result.addAll(grouped)
                    }
                }
            }
        }

        // If after division we still have very few paragraphs (< 3) and the first is long,
        // force a cleaner split
        if (result.size == 1 && result[0].length > 250) {
            val s = splitIntoSentences(result[0])
            if (s.size >= 2) {
                return groupSentencesIntoParagraphs(s)
            }
        }

        return result
    }

    /**
     * Splits a text block into individual sentences while respecting common title abbreviations.
     */
    private fun splitIntoSentences(text: String): List<String> {
        // Protect abbreviations with a placeholder
        var masked = text
        abbreviations.forEachIndexed { index, abbr ->
            masked = masked.replace(abbr, "___ABBR_${index}___")
        }

        // Regex: Look for sentence-ending punctuation followed by whitespace and a capital letter or quote
        val regex = Regex("(?<=[.!?])\\s+(?=[A-Z0-9“\"'\\(\\[])")
        val tokens = masked.split(regex)

        val sentences = mutableListOf<String>()
        for (token in tokens) {
            var restored = token
            abbreviations.forEachIndexed { index, abbr ->
                restored = restored.replace("___ABBR_${index}___", abbr)
            }
            val trimmed = cleanText(restored)
            if (trimmed.isNotBlank()) {
                sentences.add(trimmed)
            }
        }
        return sentences
    }

    /**
     * Groups sentences into optimal editorial chunks (target 200 - 350 chars each).
     */
    private fun groupSentencesIntoParagraphs(sentences: List<String>): List<String> {
        val paragraphs = mutableListOf<String>()
        val currentChunk = StringBuilder()

        for (sentence in sentences) {
            if (currentChunk.isEmpty()) {
                currentChunk.append(sentence)
            } else if (currentChunk.length + sentence.length < 320) {
                currentChunk.append(" ").append(sentence)
            } else {
                paragraphs.add(currentChunk.toString())
                currentChunk.clear()
                currentChunk.append(sentence)
            }
        }

        if (currentChunk.isNotEmpty()) {
            paragraphs.add(currentChunk.toString())
        }

        return paragraphs
    }

    /**
     * Finds an impactful sentence to feature as an editorial Pull Quote midway through the story.
     */
    fun extractPullQuote(paragraphs: List<String>): String? {
        if (paragraphs.size < 3) return null

        // Scan from paragraph 1 onward for a punchy sentence (between 50 and 140 chars)
        for (i in 1 until paragraphs.size) {
            val p = paragraphs[i]
            // Prefer sentences with quotation marks or strong declarative statements
            if (p.contains("\"") || p.contains("“") || p.contains("”")) {
                val quoteMatch = Regex("[“\"][^”\"]{40,160}[”\"]").find(p)
                if (quoteMatch != null) {
                    return quoteMatch.value.trim('“', '”', '"', ' ')
                }
            }
            val sentences = splitIntoSentences(p)
            for (sentence in sentences) {
                if (sentence.length in 55..140 && !sentence.contains("http") && !sentence.contains("Copyright")) {
                    return sentence
                }
            }
        }

        // Fallback: take a representative sentence from the middle paragraph
        val mid = paragraphs[paragraphs.size / 2]
        val sentences = splitIntoSentences(mid)
        return sentences.firstOrNull { it.length in 45..150 }
    }

    /**
     * Calculates estimated reading time in minutes (~200 words/min).
     */
    fun calculateReadingTimeMinutes(paragraphs: List<String>): Int {
        val totalWords = paragraphs.sumOf { p ->
            p.split(Regex("\\s+")).count { it.isNotBlank() }
        }
        val minutes = (totalWords / 180).coerceAtLeast(1)
        return minutes
    }

    /**
     * Cleans messy Google News RSS description snippets that often duplicate the title
     * or repeat the publisher name multiple times.
     */
    fun cleanDescriptionSnippet(rawDesc: String, title: String, source: String): String {
        if (rawDesc.isBlank()) return ""

        // Strip HTML
        var text = try {
            HtmlCompat.fromHtml(rawDesc, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (_: Exception) {
            rawDesc.replace(Regex("<[^>]*>"), " ").trim()
        }

        text = text.replace(Regex("\\s+"), " ").trim()

        // 1. If description starts with title, strip it
        if (text.startsWith(title, ignoreCase = true)) {
            text = text.substring(title.length).trim()
        }
        text = text.trimStart('-', '—', ':', '|', '•', ' ')

        // 2. Strip repeated publisher name at start
        var changed = true
        while (changed) {
            changed = false
            val before = text
            if (source.isNotBlank() && text.startsWith(source, ignoreCase = true)) {
                text = text.substring(source.length).trim()
                changed = true
            }
            text = text.trimStart('-', '—', ':', '|', '•', ' ')
            if (text != before) changed = true
        }

        // 3. Remove consecutive repeated occurrences of source name
        if (source.isNotBlank()) {
            text = text.replace(Regex("(?i)\\b${Regex.escape(source)}\\b\\s*(?:\\b${Regex.escape(source)}\\b\\s*)+"), "")
        }

        text = text.replace(Regex("\\s+"), " ").trimStart('-', '—', ':', '|', '•', ' ').trim()

        // If the remaining snippet is too short or is identical to title, return a clean editorial summary
        if (text.length < 15 || text.equals(title, ignoreCase = true)) {
            return ""
        }

        return text
    }

    private fun cleanText(text: String): String {
        return text.trim()
            .replace(Regex("\\s+"), " ")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .trim()
    }
}
