package com.example.util

import com.example.model.AppLanguage

object VartaStrings {
    fun dispatches(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "समाचार"
        AppLanguage.ENGLISH -> "DISPATCHES"
    }

    fun saved(language: AppLanguage, count: Int = 0): String {
        val base = when (language) {
            AppLanguage.HINDI -> "सहेजे गए"
            AppLanguage.ENGLISH -> "SAVED"
        }
        return if (count > 0) "$base ($count)" else base
    }

    fun searchIndex(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "खोजें"
        AppLanguage.ENGLISH -> "INDEX / SEARCH"
    }

    fun tagline(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "राष्ट्रीय एवं अंतर्राष्ट्रीय समाचारों का दैनिक संकलन"
        AppLanguage.ENGLISH -> "DAILY CHRONICLE OF NATIONAL & INTERNATIONAL DISPATCHES"
    }

    fun editionBadge(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "स्वतंत्र प्रेस संस्करण"
        AppLanguage.ENGLISH -> "FREE PRESS EDITION"
    }

    fun freshStoriesAvailable(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "ताज़ा समाचार उपलब्ध हैं • देखने के लिए टैप करें"
        AppLanguage.ENGLISH -> "Fresh Wire Dispatches Available • Tap to View"
    }

    fun compilingFeed(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "ताज़ा समाचार संकलित किए जा रहे हैं..."
        AppLanguage.ENGLISH -> "Compiling fresh dispatches..."
    }

    fun unableToFetch(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "लाइव समाचार लोड करने में असमर्थ"
        AppLanguage.ENGLISH -> "Unable to fetch live feed"
    }

    fun unableToFetchFeed(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "लाइव समाचार लोड करने में असमर्थ"
        AppLanguage.ENGLISH -> "Unable to fetch live feed"
    }

    fun checkNetwork(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "कृपया अपना इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।"
        AppLanguage.ENGLISH -> "Please check your network connection and try again."
    }

    fun retry(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "पुनः प्रयास करें"
        AppLanguage.ENGLISH -> "Retry Wire Connection"
    }

    fun retryFeed(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "पुनः प्रयास करें"
        AppLanguage.ENGLISH -> "Retry Wire Connection"
    }

    fun wireChronicles(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "राष्ट्रीय एवं वैश्विक समाचार"
        AppLanguage.ENGLISH -> "THE WIRE CHRONICLES"
    }

    fun sectionB(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "खंड बी • प्रमुख समाचार"
        AppLanguage.ENGLISH -> "SECTION B • GENERAL DISPATCHES"
    }

    fun searchPlaceholder(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "समाचार एवं विषय खोजें..."
        AppLanguage.ENGLISH -> "Search live Indian & Global wire..."
    }

    fun recentSearches(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "हाल की खोजें"
        AppLanguage.ENGLISH -> "RECENT SEARCH TOPICS"
    }

    fun trendingInIndia(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "भारत में चर्चित विषय"
        AppLanguage.ENGLISH -> "TRENDING TOPICS"
    }

    fun noResultsFound(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "कोई समाचार नहीं मिला"
        AppLanguage.ENGLISH -> "No dispatches found"
    }

    fun archivedDispatches(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "सहेजे गए समाचार"
        AppLanguage.ENGLISH -> "ARCHIVED DISPATCHES"
    }

    fun noArchived(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "अभी कोई सहेजा गया समाचार नहीं है"
        AppLanguage.ENGLISH -> "No archived dispatches yet"
    }

    fun noSavedStories(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "कोई सहेजा गया समाचार नहीं"
        AppLanguage.ENGLISH -> "No Saved Dispatches"
    }

    fun noSavedStoriesDescription(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "समाचारों को बाद में पढ़ने के लिए बुकमार्क आइकन पर टैप करें"
        AppLanguage.ENGLISH -> "Tap the bookmark icon on any article to save it for offline reading."
    }

    fun clearAll(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "सभी हटाएं"
        AppLanguage.ENGLISH -> "CLEAR ALL"
    }

    fun clearArchivedTitle(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "सहेजे गए समाचार हटाएं?"
        AppLanguage.ENGLISH -> "Clear Saved Dispatches?"
    }

    fun clearArchivedDescription(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "क्या आप अपने सहेजे गए सभी समाचार हटाना चाहते हैं?"
        AppLanguage.ENGLISH -> "Are you sure you want to remove all saved dispatches from your archive?"
    }

    fun clear(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "हटाएं"
        AppLanguage.ENGLISH -> "Clear"
    }

    fun cancel(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "रद्द करें"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun originalSource(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "मूल स्रोत"
        AppLanguage.ENGLISH -> "ORIGINAL SOURCE"
    }

    fun readTime(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "पढ़ने का समय"
        AppLanguage.ENGLISH -> "READ TIME"
    }

    fun published(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "प्रकाशित"
        AppLanguage.ENGLISH -> "PUBLISHED"
    }

    fun keyHighlights(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "प्रमुख बिंदु"
        AppLanguage.ENGLISH -> "KEY HIGHLIGHTS"
    }

    fun fullDispatch(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "विस्तृत समाचार"
        AppLanguage.ENGLISH -> "FULL DISPATCH"
    }

    fun readOriginalAt(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "मूल रिपोर्ट पढ़ें"
        AppLanguage.ENGLISH -> "READ ORIGINAL AT"
    }

    fun share(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "साझा करें"
        AppLanguage.ENGLISH -> "SHARE"
    }

    fun save(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "सहेजें"
        AppLanguage.ENGLISH -> "SAVE"
    }

    fun savedItem(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "सहेजा गया"
        AppLanguage.ENGLISH -> "SAVED"
    }

    fun preferencesTitle(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "सेटिंग्स एवं प्राथमिकताएं"
        AppLanguage.ENGLISH -> "PRESSROOM PREFERENCES"
    }

    fun languageSection(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "भाषा चयन (EDITION LANGUAGE)"
        AppLanguage.ENGLISH -> "EDITION LANGUAGE"
    }

    fun themeSection(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "थीम चयन (EDITION THEME)"
        AppLanguage.ENGLISH -> "EDITION DISPLAY THEME"
    }

    fun alertsSection(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> "ब्रेकिंग न्यूज़ अलर्ट"
        AppLanguage.ENGLISH -> "BREAKING DISPATCH ALERTS"
    }
}
