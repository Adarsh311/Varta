package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NewsRepository
import com.example.data.local.VartaDatabase
import com.example.network.ArticleScraperService
import com.example.network.NewsFeedService
import com.example.ui.NewsViewModel
import com.example.ui.NewsViewModelFactory
import com.example.ui.VartaAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels {
        val database = VartaDatabase.getDatabase(applicationContext)
        val feedService = NewsFeedService()
        val scraperService = ArticleScraperService()
        val repository = NewsRepository(
            feedService = feedService,
            articleDao = database.articleDao(),
            scraperService = scraperService
        )
        NewsViewModelFactory(repository, applicationContext)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setImportantNotificationsEnabled(this, isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.util.NewsNotificationManager.createNotificationChannel(this)
        com.example.worker.VartaBackgroundWorker.schedulePeriodicSync(applicationContext)
        viewModel.initNotificationState(this)
        requestNotificationPermissionOnLaunch()
        handleNotificationIntent(intent)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = uiState.isDarkMode) {
                VartaAppScreen(viewModel = viewModel)
            }
        }
    }

    private fun requestNotificationPermissionOnLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            val articleId = it.getStringExtra("EXTRA_ARTICLE_ID")
            val articleTitle = it.getStringExtra("EXTRA_ARTICLE_TITLE")
            val articleLink = it.getStringExtra("EXTRA_ARTICLE_LINK")
            val articleSource = it.getStringExtra("EXTRA_ARTICLE_SOURCE") ?: "VĀRTA WIRE"
            val articleImage = it.getStringExtra("EXTRA_ARTICLE_IMAGE")
            val articleDesc = it.getStringExtra("EXTRA_ARTICLE_DESC") ?: ""
            val articleCategory = it.getStringExtra("EXTRA_ARTICLE_CATEGORY") ?: "india"

            if (!articleId.isNullOrBlank() && !articleTitle.isNullOrBlank() && !articleLink.isNullOrBlank()) {
                viewModel.openArticleFromNotification(
                    id = articleId,
                    title = articleTitle,
                    link = articleLink,
                    source = articleSource,
                    imageUrl = articleImage,
                    desc = articleDesc,
                    category = articleCategory
                )
            }
        }
    }
}

