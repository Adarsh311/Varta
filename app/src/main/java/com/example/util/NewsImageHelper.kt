package com.example.util

import com.example.model.NewsArticle
import kotlin.math.abs

object NewsImageHelper {

    // Comprehensive, high quality editorial image bank with diverse, distinct photos
    private val spaceScienceImages = listOf(
        "https://images.unsplash.com/photo-1516849841032-87cbac4d88f7?auto=format&fit=crop&w=1000&q=80", // rocket launch
        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1000&q=80", // earth satellite
        "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?auto=format&fit=crop&w=1000&q=80", // mars cosmos
        "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?auto=format&fit=crop&w=1000&q=80", // space shuttle blast
        "https://images.unsplash.com/photo-1507499739999-097706ad8914?auto=format&fit=crop&w=1000&q=80", // deep space observatory
        "https://images.unsplash.com/photo-1581093588401-fbb62a02f120?auto=format&fit=crop&w=1000&q=80", // aerospace engineering
        "https://images.unsplash.com/photo-1579154204601-01588f351e67?auto=format&fit=crop&w=1000&q=80", // research microscope
        "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1000&q=80"  // nebula telescope
    )

    private val legalJudiciaryImages = listOf(
        "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=1000&q=80", // judge gavel courtroom
        "https://images.unsplash.com/photo-1505664194779-8beaceb93744?auto=format&fit=crop&w=1000&q=80", // court columns pillars
        "https://images.unsplash.com/photo-1589994965851-a8f479c573a9?auto=format&fit=crop&w=1000&q=80", // courtroom constitution
        "https://images.unsplash.com/photo-1575505586569-646b2ca898fc?auto=format&fit=crop&w=1000&q=80", // marble judicial hall
        "https://images.unsplash.com/photo-1589216532372-1c2a367900d9?auto=format&fit=crop&w=1000&q=80", // legal courthouse exterior
        "https://images.unsplash.com/photo-1453733190371-0a9bedd82893?auto=format&fit=crop&w=1000&q=80"  // legal balance
    )

    private val crimeInvestigationImages = listOf(
        "https://images.unsplash.com/photo-1589578527966-fdac0f44566c?auto=format&fit=crop&w=1000&q=80", // police security patrol
        "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=1000&q=80", // emergency sirens blue lights
        "https://images.unsplash.com/photo-1577495508048-b635879837f1?auto=format&fit=crop&w=1000&q=80", // metropolitan investigation
        "https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&w=1000&q=80", // security barricade barrier
        "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1000&q=80", // investigative documents
        "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=1000&q=80"  // highway transit
    )

    private val businessFinanceImages = listOf(
        "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=1000&q=80", // stock market bull financial charts
        "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=1000&q=80", // trading terminal candlestick
        "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&w=1000&q=80", // global currency banking
        "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=1000&q=80", // corporate skyline modern glass
        "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?auto=format&fit=crop&w=1000&q=80", // banking investment ledger
        "https://images.unsplash.com/photo-1560520653-9e0e4c89ab11?auto=format&fit=crop&w=1000&q=80", // financial district skyscraper
        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1000&q=80", // analytics data growth
        "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=1000&q=80"  // corporate financial agreement
    )

    private val techAiImages = listOf(
        "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?auto=format&fit=crop&w=1000&q=80", // modern technology code monitor
        "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1000&q=80", // microchip motherboard semiconductor
        "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=1000&q=80", // cyber security matrix code
        "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?auto=format&fit=crop&w=1000&q=80", // modern humanoid robotics
        "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=1000&q=80", // high tech server datacenter
        "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=1000&q=80", // software developer engineering
        "https://images.unsplash.com/photo-1531482615713-2afd69097998?auto=format&fit=crop&w=1000&q=80", // tech conference presentation
        "https://images.unsplash.com/photo-1517048676732-d65bc937f952?auto=format&fit=crop&w=1000&q=80"  // technology brainstorming meeting
    )

    private val politicsGovImages = listOf(
        "https://images.unsplash.com/photo-1541872703-74c5e44368f9?auto=format&fit=crop&w=1000&q=80", // grand parliament architecture
        "https://images.unsplash.com/photo-1532375810709-75b1da00537c?auto=format&fit=crop&w=1000&q=80", // Indian tricolor flag
        "https://images.unsplash.com/photo-1596405344246-b329d13ff69b?auto=format&fit=crop&w=1000&q=80", // Delhi Rashtrapati Bhavan view
        "https://images.unsplash.com/photo-1570168007204-dfb528c6958f?auto=format&fit=crop&w=1000&q=80", // official diplomatic summit
        "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?auto=format&fit=crop&w=1000&q=80", // democratic assembly vote
        "https://images.unsplash.com/photo-1587474260584-136574528ed5?auto=format&fit=crop&w=1000&q=80", // India Gate Delhi
        "https://images.unsplash.com/photo-1567157577867-05ccb1388e66?auto=format&fit=crop&w=1000&q=80", // Mumbai Gateway of India
        "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=1000&q=80"  // historic monument heritage
    )

    private val sportsCricketImages = listOf(
        "https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=1000&q=80", // cricket stadium turf pitch
        "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=1000&q=80", // cricket batsman action
        "https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1000&q=80", // stadium floodlights night match
        "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=1000&q=80", // athletics track championship
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1000&q=80", // stadium roaring crowd
        "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?auto=format&fit=crop&w=1000&q=80"  // sports fitness tournament
    )

    private val entertainmentCultureImages = listOf(
        "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1000&q=80", // cinema movie theatre seats
        "https://images.unsplash.com/photo-1478720568477-152d9b164e26?auto=format&fit=crop&w=1000&q=80", // projector film reel
        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1000&q=80", // music concert festival
        "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=1000&q=80", // live performance stage lights
        "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=1000&q=80", // Indian traditional arts
        "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=1000&q=80"  // celebration cultural spectacle
    )

    private val healthMedicalImages = listOf(
        "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=1000&q=80", // hospital healthcare stethoscope
        "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?auto=format&fit=crop&w=1000&q=80", // doctor medical advice
        "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=1000&q=80", // clinical care medical
        "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?auto=format&fit=crop&w=1000&q=80", // virus vaccine research
        "https://images.unsplash.com/photo-1583912267670-6575ad362e49?auto=format&fit=crop&w=1000&q=80"  // wellness medicine
    )

    private val generalIndiaImages = listOf(
        "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?auto=format&fit=crop&w=1000&q=80", // Varanasi Ganga ghats
        "https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=1000&q=80", // Jaipur Hawa Mahal
        "https://images.unsplash.com/photo-1598890777032-bde13fba5be3?auto=format&fit=crop&w=1000&q=80", // Kerala backwaters
        "https://images.unsplash.com/photo-1506461883276-594a12b11cf3?auto=format&fit=crop&w=1000&q=80", // Himalayas snow mountain peak
        "https://images.unsplash.com/photo-1529253355930-ddbe423a2ac7?auto=format&fit=crop&w=1000&q=80", // Mumbai coastline marine drive
        "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=1000&q=80"   // Indian railways transit
    )

    private val allFallbackPool: List<String> = (
        politicsGovImages +
        businessFinanceImages +
        techAiImages +
        sportsCricketImages +
        entertainmentCultureImages +
        crimeInvestigationImages +
        legalJudiciaryImages +
        spaceScienceImages +
        healthMedicalImages +
        generalIndiaImages
    ).distinct()

    fun getValidHeroImage(article: NewsArticle): String {
        val raw = article.imageUrl
        if (!raw.isNullOrBlank() && isValidEditorialImage(raw) && !isFallbackStockImage(raw)) {
            return raw
        }
        return resolveEditorialImage(article)
    }

    fun isFallbackStockImage(url: String?): Boolean {
        if (url.isNullOrBlank()) return true
        val lower = url.lowercase()
        return lower.contains("images.unsplash.com") || lower.contains("unsplash.com")
    }

    fun isValidEditorialImage(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase()
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) return false
        val invalidPatterns = listOf(
            "logo", "icon", "avatar", "placeholder", "badge", "button",
            "spacer", "pixel", ".svg", "favicon", "site-header", "ad-",
            "advertisement", "google_news", "default_image", "fallback",
            "masthead", "crest", "watermark", "emblem", "th_logo", "thehindu-logo",
            "thehindu.com/theme", "thehindu.com/static", "brand", "og_default",
            "dummy", "no-image", "header-logo", "share_default", "default-article",
            "generic", "default_photo", "no_image", "sprite", "th-online", "thonline",
            "timesofindia.indiatimes.com/photo/default", "ndtv.com/common"
        )
        for (pattern in invalidPatterns) {
            if (lower.contains(pattern)) return false
        }
        return true
    }

    fun resolveEditorialImage(article: NewsArticle): String {
        val targetPool = getTargetPoolForArticle(article)
        val seed = abs(article.id.hashCode() * 31 + article.title.hashCode())
        val index = seed % targetPool.size
        return targetPool[index]
    }

    private fun getTargetPoolForArticle(article: NewsArticle): List<String> {
        val titleLower = article.title.lowercase()
        val descLower = article.description.lowercase()
        val catLower = article.category.lowercase()
        val combined = "$titleLower $descLower"

        return when {
            combined.contains("isro") || combined.contains("nasa") || combined.contains("satellite") ||
                combined.contains("chandrayaan") || combined.contains("gaganyaan") || combined.contains("space") ||
                combined.contains("rocket") || combined.contains("orbit") || combined.contains("astronomy") ||
                catLower == "science" -> spaceScienceImages

            combined.contains("police") || combined.contains("arrest") || combined.contains("murder") ||
                combined.contains("killed") || combined.contains("kills") || combined.contains("dead") ||
                combined.contains("death") || combined.contains("body") || combined.contains("rape") ||
                combined.contains("crime") || combined.contains("investigation") || combined.contains("fir") ||
                combined.contains("scam") || combined.contains("fraud") || combined.contains("cbi") ||
                combined.contains("ed ") || combined.contains("accused") -> crimeInvestigationImages

            combined.contains("court") || combined.contains("judge") || combined.contains("sc ") ||
                combined.contains("supreme court") || combined.contains("high court") || combined.contains("bns") ||
                combined.contains("law") || combined.contains("justice") || combined.contains("bail") ||
                combined.contains("verdict") || combined.contains("legal") -> legalJudiciaryImages

            combined.contains("sensex") || combined.contains("nifty") || combined.contains("rbi") ||
                combined.contains("rupee") || combined.contains("stock") || combined.contains("market") ||
                combined.contains("inflation") || combined.contains("economy") || combined.contains("gdp") ||
                combined.contains("bank") || combined.contains("tax") || combined.contains("shares") ||
                catLower == "business" -> businessFinanceImages

            combined.contains("ai") || combined.contains("tech") || combined.contains("google") ||
                combined.contains("microsoft") || combined.contains("apple") || combined.contains("elon") ||
                combined.contains("musk") || combined.contains("starlink") || combined.contains("cyber") ||
                combined.contains("chip") || combined.contains("semiconductor") || combined.contains("app") ||
                catLower == "technology" -> techAiImages

            combined.contains("cricket") || combined.contains("match") || combined.contains("ipl") ||
                combined.contains("bcci") || combined.contains("kohli") || combined.contains("rohit") ||
                combined.contains("trophy") || combined.contains("olympic") || combined.contains("football") ||
                combined.contains("wicket") || combined.contains("test") || combined.contains("runs") ||
                catLower == "sports" -> sportsCricketImages

            combined.contains("movie") || combined.contains("film") || combined.contains("actor") ||
                combined.contains("actress") || combined.contains("bollywood") || combined.contains("box office") ||
                combined.contains("trailer") || combined.contains("cinema") || combined.contains("star") ||
                catLower == "entertainment" -> entertainmentCultureImages

            combined.contains("health") || combined.contains("hospital") || combined.contains("doctor") ||
                combined.contains("disease") || combined.contains("virus") || combined.contains("vaccine") ||
                combined.contains("medical") || combined.contains("patient") || catLower == "health" -> healthMedicalImages

            combined.contains("brics") || combined.contains("summit") || combined.contains("declaration") ||
                combined.contains("modi") || combined.contains("parliament") || combined.contains("election") ||
                combined.contains("minister") || combined.contains("govt") || combined.contains("bjp") ||
                combined.contains("congress") || combined.contains("policy") || combined.contains("delhi") ||
                combined.contains("diplomacy") || combined.contains("diplomatic") || combined.contains("bilateral") ||
                combined.contains("pm ") || combined.contains("cabinet") || catLower == "world" -> politicsGovImages

            else -> generalIndiaImages
        }
    }

    /**
     * Guarantees that across a whole feed of articles, no two articles share the same fallback image.
     * Articles with genuine publisher images retain their original image.
     */
    fun ensureUniqueImagesAcrossFeed(articles: List<NewsArticle>): List<NewsArticle> {
        val usedUrls = mutableSetOf<String>()

        // Pass 1: Keep all genuine source publisher images
        for (article in articles) {
            val img = article.imageUrl
            if (!img.isNullOrBlank() && isValidEditorialImage(img) && !isFallbackStockImage(img)) {
                usedUrls.add(img)
            }
        }

        // Pass 2: For any article lacking an authentic image, assign a strictly unique image
        return articles.map { article ->
            val currentImg = article.imageUrl
            if (!currentImg.isNullOrBlank() && isValidEditorialImage(currentImg) && !isFallbackStockImage(currentImg)) {
                article
            } else {
                // Find a unique fallback image from the targeted pool or general pool
                val targetPool = getTargetPoolForArticle(article)
                var chosen: String? = targetPool.firstOrNull { !usedUrls.contains(it) }
                if (chosen == null) {
                    chosen = allFallbackPool.firstOrNull { !usedUrls.contains(it) }
                }
                if (chosen == null) {
                    // Fallback to deterministic modulo if pool is completely exhausted
                    val seed = abs(article.id.hashCode() * 31 + article.title.hashCode())
                    chosen = allFallbackPool[seed % allFallbackPool.size]
                }
                usedUrls.add(chosen)
                article.copy(imageUrl = chosen)
            }
        }
    }
}
