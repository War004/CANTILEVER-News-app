package com.cryptic.roundnews.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cryptic.roundnews.viewModel.NewsUiState
import com.cryptic.roundnews.viewModel.NewsViewModel
import com.cryptic.roundnews.api.response.Article
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.cryptic.roundnews.api.response.Source
import com.cryptic.roundnews.ui.theme.RoundNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel) {
    // Collect the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // State for the text field
    var query by remember { mutableStateOf("") }
    // State to track the selected category chip
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("business", "entertainment", "health", "science", "sports", "technology")

    // Scroll behavior for the TopAppBar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            // If the user starts typing, deselect any active chip
                            if (selectedCategory != null) {
                                selectedCategory = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon"
                            )
                        },
                        shape = CircleShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (query.isNotBlank()) {
                                    viewModel.searchNews(query)
                                    // Ensure chip is deselected on search
                                    selectedCategory = null
                                }
                                focusManager.clearFocus()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement filter functionality */ }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter News"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                // If the user clicks the selected chip, deactivate it and load default news
                                selectedCategory = null
                                viewModel.searchNews("")
                            } else {
                                // If the user clicks a new chip, select it and search for that category
                                selectedCategory = category
                                query = category
                                focusManager.clearFocus()
                                viewModel.searchNews(category)
                            }
                        },
                        label = { Text(text = category) }
                    )
                }
            }

            // News Content
            NewsContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onLoadMore = { viewModel.loadMoreNews() }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsContent(modifier: Modifier = Modifier, uiState: NewsUiState, onLoadMore: () -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {

        // A full-screen loading indicator is only shown when the list is empty and we are loading.
        if (uiState.isLoading && uiState.articles.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        // A full-screen error message is only shown if the initial load fails.
        else if (uiState.articles.isEmpty() && uiState.errorMessage != null) {
            Log.d("MainScreen", uiState.errorMessage)
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
        // Otherwise, we show the list.
        else {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(uiState.articles) { article ->
                    NewsArticleItem(article = article)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // This item is the footer of the list.
                // It shows a loading indicator when fetching more articles,
                // or an error message if that fetch fails.
                item {
                    // Show loading spinner if we are loading more
                    if (uiState.isLoadingMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    // Show error message if loading more failed
                    else if (uiState.errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Failed to load more: ${uiState.errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            // The side effect to trigger loading more articles.
            // It's crucial that it doesn't trigger if there's already an error state.
            LaunchedEffect(listState, uiState.errorMessage, uiState.isLoadingMore) {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .collect { lastIndex ->
                        if (
                            !uiState.isLoadingMore &&       // Don't load if already loading
                            uiState.errorMessage == null && // Don't load if there's an error
                            lastIndex != null &&
                            lastIndex >= uiState.articles.size - 5 // Your threshold to trigger load more
                        ) {
                            onLoadMore()
                        }
                    }
            }
        }
    }
}


@Composable
fun NewsArticleItem(article: Article) {

    val context = LocalContext.current

    Card(
        onClick = {
            if (article.url.isNotBlank()) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                try {
                    customTabsIntent.launchUrl(context, Uri.parse(article.url))
                } catch (e: Exception) {
                    Log.e("NewsArticleItem", "Failed to open URL ${article.url}: ${e.message}")
                    Toast.makeText(
                         context,
                        "Could not open article. Invalid URL or no browser found.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.urlToImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Article Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state is AsyncImagePainter.State.Error) {
                            Text("No Image", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        } else {
                            Text("Loading...", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                } else {
                    SubcomposeAsyncImageContent()
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.source.name,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title?: "No title",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.author ?: "Unknown author",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsArticleItemPreview() {
    RoundNewsTheme {
        NewsArticleItem(
            article = Article(
                source = Source(id = null, name = "TechCrunch"),
                author = "John Doe",
                title = "The Future of AI: What to Expect in the Next Decade",
                description = "A deep dive into the advancements of artificial intelligence.",
                url = "",
                urlToImage = "",
                publishedAt = "2023-10-27T10:00:00Z",
                content = ""
            )
        )
    }
}