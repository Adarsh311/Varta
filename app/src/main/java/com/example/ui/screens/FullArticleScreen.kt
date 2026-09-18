package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.model.NewsArticle
import com.example.model.ScrapedArticle
import com.example.ui.theme.VartaSaffron
import com.example.util.DateTimeFormatterHelper
import com.example.util.NewsImageHelper
import com.example.util.TextDivisionHelper

enum class ReaderFontSize(val label: String, val bodySize: TextUnit, val lineHeight: TextUnit, val leadSize: TextUnit) {
    COMPACT("A", 15.5.sp, 25.sp, 17.5.sp),
    STANDARD("Aa", 17.sp, 28.sp, 19.sp),
    COMFORT("A+", 19.sp, 31.sp, 21.sp)
}

@Composable
fun FullArticleScreen(
    article: NewsArticle,
    scrapedArticle: ScrapedArticle?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit,
    onRetry: () -> Unit
) {
    BackHandler { onBack() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var fontSizeMode by remember { mutableStateOf(ReaderFontSize.STANDARD) }

    val readProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_article_screen")
    ) {
        // Pinned Top Bar: Back, Reader font size (Aa), Bookmark, Share, Open in Browser
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("full_article_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Reader Font Size Toggle ("Aa")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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

                IconButton(
                    onClick = {
                        val targetUrl = scrapedArticle?.finalUrl ?: article.link
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open in browser",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onBookmarkToggle(article) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("full_article_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark article",
                        tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onShare(article) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("full_article_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share article",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Reading Progress Line (2.5dp height)
        LinearProgressIndicator(
            progress = { readProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp),
            color = VartaSaffron,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
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

            // Strict image consistency: preserve the exact image displayed on the list card,
            // or upgrade to authentic source photograph if card was a temporary fallback
            val heroImg = when {
                // 1. If article already has an authentic source image from the feed, keep it strictly identical to the card
                !article.imageUrl.isNullOrBlank() &&
                    !NewsImageHelper.isFallbackStockImage(article.imageUrl) &&
                    NewsImageHelper.isValidEditorialImage(article.imageUrl) -> article.imageUrl

                // 2. If article was a fallback, but the scraper fetched the authentic original source photo, upgrade to it
                !scrapedArticle?.heroImageUrl.isNullOrBlank() &&
                    !NewsImageHelper.isFallbackStockImage(scrapedArticle?.heroImageUrl) &&
                    NewsImageHelper.isValidEditorialImage(scrapedArticle?.heroImageUrl) -> scrapedArticle!!.heroImageUrl!!

                !resolved.heroImageUrl.isNullOrBlank() &&
                    !NewsImageHelper.isFallbackStockImage(resolved.heroImageUrl) &&
                    NewsImageHelper.isValidEditorialImage(resolved.heroImageUrl) -> resolved.heroImageUrl!!

                // 3. Keep the exact image from the card so the image NEVER changes when opening the article
                !article.imageUrl.isNullOrBlank() -> article.imageUrl

                // 4. Scraped image fallback
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .testTag("scraped_article_scroll_content")
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Source badge & Reading metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VartaSaffron.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = article.source.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 11.sp
                                ),
                                color = VartaSaffron,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$readingMinutes min read",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Article Headline
                Text(
                    text = resolved.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("full_article_title_text")
                )

                // Author byline
                if (!resolved.author.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "By ${resolved.author}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Image of the Full Article
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
                    // Smooth inline loading skeleton while paragraphs are being fetched
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
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
                                text = "Loading dispatch from ${article.source}...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    // Key Highlights Card (if available or extracted)
                    if (resolved.keyHighlights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                VartaSaffron.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(VartaSaffron, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "KEY TAKEAWAYS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        ),
                                        color = VartaSaffron
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
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
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Article Paragraphs with editorial styling
                    val displayParagraphs = if (paragraphs.isNotEmpty()) {
                        paragraphs
                    } else {
                        listOf(article.description.ifBlank { article.title })
                    }

                    displayParagraphs.forEachIndexed { index, paragraph ->
                        if (index == 0) {
                            // Lead Paragraph with styled Drop-Cap opening
                            LeadParagraph(
                                text = paragraph,
                                fontSize = fontSizeMode.leadSize,
                                lineHeight = fontSizeMode.lineHeight
                            )
                        } else {
                            // Standard well-proportioned reading paragraph
                            Text(
                                text = paragraph,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = fontSizeMode.lineHeight,
                                    letterSpacing = 0.25.sp,
                                    fontSize = fontSizeMode.bodySize,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.94f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Generous editorial paragraph separation
                        Spacer(modifier = Modifier.height(16.dp))

                        // Midway Editorial Pull Quote (after paragraph 2)
                        if (index == 1 && !pullQuote.isNullOrBlank() && displayParagraphs.size >= 4) {
                            PullQuoteCard(quote = pullQuote)
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Ornamental Section Divider every 4 paragraphs
                        if (index > 0 && index % 4 == 0 && index < displayParagraphs.size - 1) {
                            EditorialSectionDivider()
                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        // If inline image exists for this paragraph section, display it
                        val inlineImg = resolved.inlineImages.getOrNull(index)
                        if (inlineImg != null && NewsImageHelper.isValidEditorialImage(inlineImg.url)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                        }
                    }

                    // Source and Link Attribution at the very end
                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(
                        modifier = Modifier.width(48.dp),
                        thickness = 2.5.dp,
                        color = VartaSaffron
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 36.dp)
                            .testTag("end_of_article_attribution")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "ORIGINAL REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = VartaSaffron
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Published by ${article.source}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("end_of_article_link_text")
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
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
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Read on ${article.source}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
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
    lineHeight: TextUnit
) {
    if (text.isBlank()) return

    // If text starts with a letter, style the first letter as a distinguished drop-cap
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.96f),
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
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
private fun PullQuoteCard(quote: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Saffron Accent Bar
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
                    color = MaterialTheme.colorScheme.onSurface
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
