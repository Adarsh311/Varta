package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeFormatterHelper {

    private val supportedInputFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd MMM yyyy", Locale.US)
    )

    private val outputDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    private val outputDateTimeFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)

    /**
     * Formats any raw date string (e.g., ISO 8601 "2026-09-14T00:20:00+05:30" or RFC 822)
     * into an elegant, readable editorial format such as "Sep 14, 2026 • 12:20 AM"
     */
    fun formatPublishedDate(rawDate: String?, timestamp: Long): String {
        val parsedDate = parseDate(rawDate) ?: if (timestamp > 0L) Date(timestamp) else null

        if (parsedDate != null) {
            return try {
                outputDateTimeFormat.format(parsedDate)
            } catch (_: Exception) {
                outputDateFormat.format(parsedDate)
            }
        }

        // Fallback cleanup if parsing failed: strip technical timezone offsets like +05:30 or T separators
        if (!rawDate.isNullOrBlank()) {
            val cleaned = rawDate
                .replace("T", " ")
                .replace(Regex("[+-]\\d{2}:?\\d{2}$"), "")
                .replace("Z", "")
                .trim()
            if (cleaned.isNotBlank()) return cleaned
        }

        return "Today"
    }

    private fun parseDate(rawDate: String?): Date? {
        if (rawDate.isNullOrBlank()) return null
        val trimmed = rawDate.trim()
        for (format in supportedInputFormats) {
            try {
                val date = format.parse(trimmed)
                if (date != null) return date
            } catch (_: Exception) {
                // Continue trying next format
            }
        }
        return null
    }
}
