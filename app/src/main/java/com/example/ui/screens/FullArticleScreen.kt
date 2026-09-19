package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppLanguage
import com.example.model.NewsArticle
import com.example.model.ScrapedArticle
import com.example.ui.theme.VartaSaffron
import com.example.util.ArticleSpeechHelper
import com.example.util.DateTimeFormatterHelper
import com.example.util.NewsImageHelper
import com.example.util.SpeechStatus
import com.example.util.TextDivisionHelper
import kotlinx.coroutines.launch

enum class ReaderFontSize(val label: String, val bodySize: TextUnit, val lineHeight: TextUnit, val leadSize: TextUnit) {
    COMPACT("A", 15.5.sp, 25.sp, 17.5.sp),
    STANDARD("Aa", 17.sp, 28.sp, 19.sp),
    COMFORT("A+", 19.sp, 31.sp, 21.sp)
}

enum class ReaderFontFamilyType(val label: String, val family: FontFamily) {
    SERIF("Serif", FontFamily.Serif),
    SANS("Sans", FontFamily.SansSerif)
}

enum class ReaderColorTheme(val label: String, val bg: Color, val text: Color, val surface: Color) {
    SYSTEM("Theme", Color.Unspecified, Color.Unspecified, Color.Unspecified),
    SEPIA("Sepia", Color(0xFFFBF0D9), Color(0xFF2C241B), Color(0xFFF3E4C6)),
    AMOLED("OLED", Color(0xFF000000), Color(0xFFEDE8DF), Color(0xFF141416))
}

@Composable
fun FullArticleScreen(
    article: NewsArticle,
    scrapedArticle: ScrapedArticle?,
    isLoading: Boolean,
    language: AppLanguage = AppLanguage.ENGLISH,
    onBack: () -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val speechHelper = remember { ArticleSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.shutdown()
        }
    }

    BackHandler {
        speechHelper.stop()
        onBack()
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var fontSizeMode by remember { mutableStateOf(ReaderFontSize.STANDARD) }
    var fontType by remember { mutableStateOf(ReaderFontFamilyType.SERIF) }
    var readerColorTheme by remember { mutableStateOf(ReaderColorTheme.SYSTEM) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val speechStatus by speechHelper.status.collectAsState()
    val currentSpeakingIndex by speechHelper.currentParagraphIndex.collectAsState()
    val speechRate by speechHelper.speechRate.collectAsState()

    val readProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
    }

    val showBackToTop by remember {
        derivedStateOf { scrollState.value > 900 }
    }

    val effectiveBg = if (readerColorTheme == ReaderColorTheme.SYSTEM) {
        MaterialTheme.colorScheme.background
    } else {
        readerColorTheme.bg
    }

    val effectiveText = if (readerColorTheme == ReaderColorTheme.SYSTEM) {
        MaterialTheme.colorScheme.onBackground
    } else {
        readerColorTheme.text
    }

    val effectiveSurface = if (readerColorTheme == ReaderColorTheme.SYSTEM) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        readerColorTheme.surface
    }

    val resolved = scrapedArticle ?: ScrapedArticle(
        id = article.id,
        title = article.title,
        source = article.source,
        originalLink = article.link,
        heroImageUrl = article.imageUrl?.takeIf { NewsImageHelper.isValidEditorialImage(it) }
            ?: NewsImageHelper.getValidHeroImage(article),
        paragraphs = emptyList(),
        isScrapedFromWeb = false
    )

    val heroImg = when {
        !article.imageUrl.isNullOrBlank() &&
            !NewsImageHelper.isFallbackStockImage(article.imageUrl) &&
            NewsImageHelper.isValidEditorialImage(article.imageUrl) -> article.imageUrl

        !scrapedArticle?.heroImageUrl.isNullOrBlank() &&
            !NewsImageHelper.isFallbackStockImage(scrapedArticle?.heroImageUrl) &&
            NewsImageHelper.isValidEditorialImage(scrapedArticle?.heroImageUrl) -> scrapedArticle!!.heroImageUrl!!

        !resolved.heroImageUrl.isNullOrBlank() &&
            !NewsImageHelper.isFallbackStockImage(resolved.heroImageUrl) &&
            NewsImageHelper.isValidEditorialImage(resolved.heroImageUrl) -> resolved.heroImageUrl!!

        !article.imageUrl.isNullOrBlank() -> article.imageUrl

        !scrapedArticle?.heroImageUrl.isNullOrBlank() &&
            NewsImageHelper.isValidEditorialImage(scrapedArticle?.heroImageUrl) -> scrapedArticle!!.heroImageUrl!!

        else -> NewsImageHelper.getValidHeroImage(article)
    }

    val paragraphs = if (resolved.paragraphs.isNotEmpty()) {
        TextDivisionHelper.divideIntoReadingParagraphs(resolved.paragraphs)
    } else emptyList()

    val readingMinutes = if (paragraphs.isNotEmpty()) {
        TextDivisionHelper.calculateReadingTimeMinutes(paragraphs)
    } else 2

    val formattedDate = DateTimeFormatterHelper.formatPublishedDate(
        resolved.publishedDate ?: article.pubDate,
        article.timestamp
    )
    val pullQuote = if (paragraphs.isNotEmpty()) TextDivisionHelper.extractPullQuote(paragraphs) else null

    val copyToClipboard: (String, String) -> Unit = { text, label ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        val msg = if (language == AppLanguage.HINDI) "$label कॉपी हो गया" else "$label copied to clipboard"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(effectiveBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_article_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Pinned Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        speechHelper.stop()
                        onBack()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("full_article_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (language == AppLanguage.HINDI) "पीछे जाएं" else "Back",
                        tint = effectiveText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Audio Reader Listen Action Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (speechStatus != SpeechStatus.IDLE) VartaSaffron else effectiveSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (speechStatus == SpeechStatus.PLAYING) {
                                    speechHelper.pause()
                                } else if (speechStatus == SpeechStatus.PAUSED) {
                                    speechHelper.resume()
                                } else {
                                    val toRead = if (paragraphs.isNotEmpty()) paragraphs else listOf(article.description.ifBlank { article.title })
                                    speechHelper.startListening(resolved.title, toRead)
                                }
                            }
                            .testTag("audio_listen_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (speechStatus == SpeechStatus.PLAYING) Icons.Default.VolumeUp else Icons.Default.Headphones,
                                contentDescription = "Listen to Dispatch",
                                tint = if (speechStatus != SpeechStatus.IDLE) Color(0xFF0B0B0C) else VartaSaffron,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (speechStatus == SpeechStatus.PLAYING) {
                                    if (language == AppLanguage.HINDI) "सुन रहे हैं" else "Listening"
                                } else {
                                    if (language == AppLanguage.HINDI) "सुनें" else "Listen"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (speechStatus != SpeechStatus.IDLE) Color(0xFF0B0B0C) else effectiveText
                            )
                        }
                    }

                    // Font Size Cycle Button ("Aa")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = effectiveSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                fontSizeMode = when (fontSizeMode) {
                                    ReaderFontSize.COMPACT -> ReaderFontSize.STANDARD
                                    ReaderFontSize.STANDARD -> ReaderFontSize.COMFORT
                                    ReaderFontSize.COMFORT -> ReaderFontSize.COMPACT
                                }
                            }
                            .testTag("reader_font_size_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fontSizeMode.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = VartaSaffron
                            )
                        }
                    }

                    // Theme & Typeface Selector Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showThemeMenu) VartaSaffron.copy(alpha = 0.2f) else effectiveSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showThemeMenu = !showThemeMenu }
                            .testTag("reader_theme_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fontType.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = VartaSaffron
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val targetUrl = scrapedArticle?.finalUrl ?: article.link
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(browserIntent)
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = effectiveText.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onBookmarkToggle(article) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("full_article_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark article",
                            tint = if (article.isBookmarked) VartaSaffron else effectiveText.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onShare(article) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("full_article_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share article",
                            tint = effectiveText.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expanded Theme & Font Controls Drawer
            androidx.compose.animation.AnimatedVisibility(visible = showThemeMenu) {
                Surface(
                    color = effectiveSurface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Typeface choices
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ReaderFontFamilyType.entries.forEach { f ->
                                    val isSelected = fontType == f
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) VartaSaffron else effectiveBg,
                                        contentColor = if (isSelected) Color(0xFF0B0B0C) else effectiveText,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { fontType = f }
                                    ) {
                                        Text(
                                            text = f.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Color palettes
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ReaderColorTheme.entries.forEach { c ->
                                    val isSelected = readerColorTheme == c
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) VartaSaffron else effectiveBg,
                                        contentColor = if (isSelected) Color(0xFF0B0B0C) else effectiveText,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { readerColorTheme = c }
                                    ) {
                                        Text(
                                            text = c.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reading Progress Line
            LinearProgressIndicator(
                progress = { readProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = VartaSaffron,
                trackColor = effectiveSurface.copy(alpha = 0.25f)
            )

            // Scrollable Article Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .testTag("scraped_article_scroll_content")
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Source badge & Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = VartaSaffron.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = article.source.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 10.5.sp
                                    ),
                                    color = VartaSaffron,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                color = effectiveText.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "$readingMinutes मिनट का पठन" else "$readingMinutes min read",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = effectiveText.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = effectiveText.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Article Title
                    Text(
                        text = resolved.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = fontType.family,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 34.sp,
                            fontSize = 24.sp
                        ),
                        color = effectiveText,
                        modifier = Modifier.testTag("full_article_title_text")
                    )

                    if (!resolved.author.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "लेखक: ${resolved.author}" else "By ${resolved.author}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = effectiveText.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = effectiveSurface.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(effectiveSurface.copy(alpha = 0.35f))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(heroImg)
                                .crossfade(true)
                                .build(),
                            contentDescription = resolved.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 10f)
                                .testTag("scraped_hero_image")
                        )
                    }

                    if (isLoading && scrapedArticle == null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = effectiveSurface.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = VartaSaffron,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "${article.source} से समाचार लोड हो रहा है..." else "Loading dispatch from ${article.source}...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = effectiveText.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (it == 4) 0.65f else 1f)
                                    .height(15.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(effectiveSurface.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else {
                        if (!resolved.isScrapedFromWeb) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = effectiveSurface.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    VartaSaffron.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "संक्षिप्त पूर्वावलोकन" else "Summary preview",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = effectiveText.copy(alpha = 0.8f)
                                    )
                                    TextButton(
                                        onClick = onRetry,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "पूरा लेख लोड करें" else "Load Full Story",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = VartaSaffron
                                        )
                                    }
                                }
                            }
                        }

                        // Key Highlights Box
                        if (resolved.keyHighlights.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = effectiveSurface.copy(alpha = 0.45f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    VartaSaffron.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(VartaSaffron, shape = CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "मुख्य बिंदु" else "KEY TAKEAWAYS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.2.sp
                                                ),
                                                color = VartaSaffron
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val takeawaysText = resolved.keyHighlights.joinToString("\n") { "• $it" }
                                                copyToClipboard("${resolved.title}\n\nKey Takeaways:\n$takeawaysText", "Takeaways")
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Key Takeaways",
                                                tint = VartaSaffron,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    resolved.keyHighlights.forEach { point ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "—",
                                                color = VartaSaffron,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = point,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    lineHeight = 22.sp
                                                ),
                                                color = effectiveText
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Paragraphs
                        val displayParagraphs = if (paragraphs.isNotEmpty()) {
                            paragraphs
                        } else {
                            listOf(article.description.ifBlank { article.title })
                        }

                        displayParagraphs.forEachIndexed { index, paragraph ->
                            val isCurrentlySpoken = speechStatus != SpeechStatus.IDLE && currentSpeakingIndex == index

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isCurrentlySpoken) VartaSaffron.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                    .padding(if (isCurrentlySpoken) 8.dp else 0.dp)
                            ) {
                                if (index == 0) {
                                    LeadParagraph(
                                        text = paragraph,
                                        fontSize = fontSizeMode.leadSize,
                                        lineHeight = fontSizeMode.lineHeight,
                                        fontFamily = fontType.family,
                                        textColor = effectiveText
                                    )
                                } else {
                                    Text(
                                        text = paragraph,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = fontSizeMode.lineHeight,
                                            letterSpacing = 0.25.sp,
                                            fontSize = fontSizeMode.bodySize,
                                            fontFamily = fontType.family
                                        ),
                                        color = effectiveText.copy(alpha = 0.94f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (index == 1 && !pullQuote.isNullOrBlank() && displayParagraphs.size >= 4) {
                                PullQuoteCard(
                                    quote = pullQuote,
                                    surfaceColor = effectiveSurface,
                                    textColor = effectiveText
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (index > 0 && index % 4 == 0 && index < displayParagraphs.size - 1) {
                                EditorialSectionDivider()
                                Spacer(modifier = Modifier.height(18.dp))
                            }

                            val inlineImg = resolved.inlineImages.getOrNull(index)
                            if (inlineImg != null && NewsImageHelper.isValidEditorialImage(inlineImg.url)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(effectiveSurface.copy(alpha = 0.3f))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(inlineImg.url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = inlineImg.caption ?: "Article image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                    )
                                }
                                if (!inlineImg.caption.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = inlineImg.caption,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = effectiveText.copy(alpha = 0.75f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }

                        // Bottom Original Report Attribution
                        Spacer(modifier = Modifier.height(28.dp))
                        HorizontalDivider(
                            modifier = Modifier.width(48.dp),
                            thickness = 2.5.dp,
                            color = VartaSaffron
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = effectiveSurface.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                effectiveSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 90.dp)
                                .testTag("end_of_article_attribution")
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "मूल समाचार" else "ORIGINAL REPORT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = VartaSaffron
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "${article.source} द्वारा प्रकाशित" else "Published by ${article.source}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = effectiveText,
                                    modifier = Modifier.testTag("end_of_article_source_text")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                SelectionContainer {
                                    Text(
                                        text = resolved.finalUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        ),
                                        color = effectiveText.copy(alpha = 0.6f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("end_of_article_link_text")
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(resolved.finalUrl))
                                            context.startActivity(browserIntent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VartaSaffron,
                                            contentColor = Color(0xFF0B0B0C)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "${article.source} पर पढ़ें" else "Read on ${article.source}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val fullText = "${resolved.title}\n\n${displayParagraphs.joinToString("\n\n")}\n\nSource: ${article.source}\nLink: ${resolved.finalUrl}"
                                            copyToClipboard(fullText, "Article Text")
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = VartaSaffron
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "कॉपी" else "Copy",
                                            color = effectiveText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating "Back to Top" Action Button
                if (showBackToTop) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch { scrollState.animateScrollTo(0) }
                        },
                        containerColor = VartaSaffron,
                        contentColor = Color(0xFF0B0B0C),
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = if (speechStatus != SpeechStatus.IDLE) 80.dp else 24.dp, end = 20.dp)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Scroll to top",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Floating Audio Reader Bar Dock (Bottom)
        androidx.compose.animation.AnimatedVisibility(
            visible = speechStatus != SpeechStatus.IDLE,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF19191E),
                contentColor = Color(0xFFF5F1E8),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, VartaSaffron.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = VartaSaffron,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "समाचार सुनाया जा रहा है" else "Reading Dispatch aloud",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VartaSaffron
                                )
                            )
                            val totalParas = if (paragraphs.isNotEmpty()) paragraphs.size else 1
                            Text(
                                text = if (language == AppLanguage.HINDI) "पैराग्राफ ${currentSpeakingIndex + 1} / $totalParas" else "Paragraph ${currentSpeakingIndex + 1} of $totalParas",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFFA8A399)
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Speed multiplier button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF222229),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val nextRate = when (speechRate) {
                                        0.75f -> 1.0f
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 0.75f
                                    }
                                    speechHelper.setSpeed(nextRate)
                                }
                        ) {
                            Text(
                                text = "${speechRate}x",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VartaSaffron,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { speechHelper.previousParagraph() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous paragraph",
                                tint = Color(0xFFF5F1E8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (speechStatus == SpeechStatus.PLAYING) {
                                    speechHelper.pause()
                                } else {
                                    speechHelper.resume()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(VartaSaffron, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (speechStatus == SpeechStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (speechStatus == SpeechStatus.PLAYING) "Pause" else "Play",
                                tint = Color(0xFF0B0B0C),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { speechHelper.nextParagraph() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next paragraph",
                                tint = Color(0xFFF5F1E8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { speechHelper.stop() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Audio",
                                tint = Color(0xFFA8A399),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadParagraph(
    text: String,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    fontFamily: FontFamily,
    textColor: Color
) {
    if (text.isBlank()) return

    val firstChar = text.first().toString()
    val remainder = text.drop(1)

    val annotated = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = VartaSaffron,
                fontSize = (fontSize.value * 1.55f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        ) {
            append(firstChar)
        }
        withStyle(
            style = SpanStyle(
                color = textColor.copy(alpha = 0.96f),
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily
            )
        ) {
            append(remainder)
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = (lineHeight.value * 1.08f).sp,
            letterSpacing = 0.2.sp
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun PullQuoteCard(
    quote: String,
    surfaceColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = surfaceColor.copy(alpha = 0.32f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(VartaSaffron, shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "“$quote”",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 26.sp
                    ),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "— Dispatch Highlight",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = VartaSaffron
                )
            }
        }
    }
}

@Composable
private fun EditorialSectionDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.8.dp
        )
        Text(
            text = "  ✦   ✦   ✦  ",
            color = VartaSaffron.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.8.dp
        )
    }
}
