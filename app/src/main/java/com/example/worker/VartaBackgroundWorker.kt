package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.model.NewsCategory
import com.example.network.NewsFeedService
import com.example.util.NewsNotificationManager
import java.util.concurrent.TimeUnit

class VartaBackgroundWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            if (!NewsNotificationManager.isImportantNotificationsEnabled(applicationContext)) {
                return Result.success()
            }

            val feedService = NewsFeedService()
            val result = feedService.fetchCategoryFeed(NewsCategory.TOP_STORIES)
                .mapCatching { list ->
                    if (list.isNotEmpty()) list else feedService.fetchCategoryFeed(NewsCategory.INDIA).getOrThrow()
                }

            result.getOrNull()?.firstOrNull()?.let { latestArticle ->
                NewsNotificationManager.notifyIfNewBreakingStory(applicationContext, latestArticle)
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "varta_background_news_sync"

        fun schedulePeriodicSync(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<VartaBackgroundWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
