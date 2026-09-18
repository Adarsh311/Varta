package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
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
        NewsViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = uiState.isDarkMode) {
                VartaAppScreen(viewModel = viewModel)
            }
        }
    }
}
