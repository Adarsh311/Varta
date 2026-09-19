package com.example.model

enum class AppLanguage(
    val code: String,
    val title: String,
    val nativeName: String,
    val flagBadge: String
) {
    ENGLISH("en", "English", "English", "EN"),
    HINDI("hi", "Hindi", "हिन्दी", "हि");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
