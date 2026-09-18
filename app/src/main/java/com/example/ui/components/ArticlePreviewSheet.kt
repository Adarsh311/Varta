package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.NewsArticle
import com.example.ui.theme.VartaSaffron
import com.example.util.DateTimeFormatterHelper
import com.example.util.NewsImageHelper
import com.example.util.TextDivisionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlePreviewSheet(
    article: NewsArticle?,
    onDismiss: () -> Unit,
    onBookmarkToggle: (NewsArticle) -> Unit,
    onShare: (NewsArticle) -> Unit
) {
    if (article == null) return
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imageUrl = NewsImageHelper.getValidHeroImage(article)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .testTag("article_preview_sheet")
        ) {
            // Header Row: Source, Time, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = VartaSaffron.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = article.source.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = VartaSaffron,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Article Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Headline
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Published ${DateTimeFormatterHelper.formatPublishedDate(article.pubDate, article.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Snippet Description divided into readable paragraphs
            if (article.description.isNotBlank()) {
                val previewParagraphs = TextDivisionHelper.divideIntoReadingParagraphs(listOf(article.description))
                previewParagraphs.forEach { p ->
                    Text(
                        text = p,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                        context.startActivity(browserIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VartaSaffron,
                        contentColor = Color(0xFF0B0B0C)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("open_original_article_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Read on ${article.source.take(16)}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                OutlinedButton(
                    onClick = { onBookmarkToggle(article) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("preview_bookmark_toggle")
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) VartaSaffron else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { onShare(article) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("preview_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
