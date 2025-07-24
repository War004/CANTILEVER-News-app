package com.cryptic.roundnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.cryptic.roundnews.ui.theme.RoundNewsTheme
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.cryptic.roundnews.api.client.NewsApiFactory
import com.cryptic.roundnews.screens.NewsScreen
import com.cryptic.roundnews.viewModel.NewsViewModel

class MainActivity : ComponentActivity() {

    // A lazy delegate to create the ViewModel
    private val newsViewModel: NewsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return NewsApiFactory.createNewsViewModel() as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoundNewsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Display the NewsScreen and pass the ViewModel
                    NewsScreen(viewModel = newsViewModel)
                }
            }
        }
    }
}