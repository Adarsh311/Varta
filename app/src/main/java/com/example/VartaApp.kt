package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

class VartaApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        com.example.util.NewsNotificationManager.createNotificationChannel(this)
        com.example.worker.VartaBackgroundWorker.schedulePeriodicSync(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("varta_image_cache"))
                    .maxSizeBytes(64L * 1024 * 1024) // 64 MB
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}
