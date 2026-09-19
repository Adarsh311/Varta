package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NewsNotificationManager {
    const val CHANNEL_ID = "varta_important_news_channel"
    private const val CHANNEL_NAME = "Important & Breaking Dispatches"
    private const val CHANNEL_DESC = "Urgent headline dispatches and major breaking updates"
    private const val PREFS_NAME = "varta_settings_prefs"
    private const val KEY_IMPORTANT_NOTIFICATIONS_ENABLED = "important_notifications_enabled"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun areSystemNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun openSystemNotificationSettings(context: Context) {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                action = "android.settings.ACTION_APP_NOTIFICATION_REDUCE_DELAY"
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }

    fun isImportantNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IMPORTANT_NOTIFICATIONS_ENABLED, true)
    }

    fun setImportantNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IMPORTANT_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    suspend fun showImportantNewsNotification(
        context: Context,
        article: NewsArticle
    ) {
        if (!isImportantNotificationsEnabled(context)) return

        createNotificationChannel(context)

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val bitmap = fetchBitmap(context, article.imageUrl)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ARTICLE_ID", article.id)
            putExtra("EXTRA_ARTICLE_TITLE", article.title)
            putExtra("EXTRA_ARTICLE_LINK", article.link)
            putExtra("EXTRA_ARTICLE_SOURCE", article.source)
            putExtra("EXTRA_ARTICLE_IMAGE", article.imageUrl)
            putExtra("EXTRA_ARTICLE_DESC", article.description)
            putExtra("EXTRA_ARTICLE_CATEGORY", article.category)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            article.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🚨 BREAKING: ${article.title}")
            .setContentText("${article.source} — ${article.description.ifBlank { "Tap to read full dispatch." }}")
            .setSubText("VĀRTA WIRE")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (bitmap != null) {
            notificationBuilder
                .setLargeIcon(bitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setBigContentTitle("🚨 BREAKING: ${article.title}")
                        .setSummaryText("${article.source} — ${article.description.ifBlank { "Major national dispatch." }}")
                )
        } else {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(article.description.ifBlank { article.title })
                    .setBigContentTitle("🚨 BREAKING: ${article.title}")
                    .setSummaryText("Reported by ${article.source}")
            )
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(article.id.hashCode(), notificationBuilder.build())
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("last_notified_article_id", article.id).apply()
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    suspend fun notifyIfNewBreakingStory(context: Context, article: NewsArticle) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotified = prefs.getString("last_notified_article_id", null)
        if (lastNotified != article.id) {
            showImportantNewsNotification(context, article)
        }
    }

    private suspend fun fetchBitmap(context: Context, imageUrl: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) return@withContext null
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Needed for Notification Bitmaps
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
