package com.cryptic.roundnews.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptic.roundnews.api.client.NewsRepository
import com.cryptic.roundnews.api.response.ApiResponse
import com.cryptic.roundnews.api.response.Article
import com.cryptic.roundnews.api.response.SearchIn
import com.cryptic.roundnews.api.response.SortBy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val totalResults: Int = 0,
    val currentPage: Int = 1,
    val errorMessage: String? = null,
    val isLoadingMore: Boolean = false
)

class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var currentQuery: String = ""
    private var currentSortBy: SortBy = SortBy.PUBLISHED_AT

    init {
        // Load initial news content when the ViewModel is created
        searchNews("Android")
    }

    fun searchNews(
        query: String,
        sortBy: SortBy = SortBy.PUBLISHED_AT,
        pageSize: Int = 20
    ) {
        if (query.isBlank()) return

        currentQuery = query
        currentSortBy = sortBy

        viewModelScope.launch {
            // Start loading and clear previous errors/articles for a new search
            _uiState.update { it.copy(isLoading = true, errorMessage = null, articles = emptyList()) }

            repository.searchNews(query, sortBy, pageSize, 1)
                .collect { result ->
                    result.onSuccess { response ->
                        // The API call was successful, now check the response content
                        when (response) {
                            is ApiResponse.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        articles = response.articles,
                                        totalResults = response.totalResults,
                                        currentPage = 1
                                    )
                                }
                            }
                            is ApiResponse.Error -> {
                                // The API returned a handled error (e.g., invalid parameter)
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = response.message
                                    )
                                }
                            }
                        }
                    }.onFailure { exception ->
                        // The API call failed (e.g., network error, parsing error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.localizedMessage ?: "An unexpected error occurred"
                            )
                        }
                    }
                }
        }
    }

    fun loadMoreNews() {
        val currentState = _uiState.value
        // Prevent loading more if already loading, query is blank, or all results are fetched
        if (currentState.isLoadingMore || currentQuery.isBlank() || currentState.articles.size >= currentState.totalResults) {
            return
        }

        val nextPage = currentState.currentPage + 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            repository.searchNews(currentQuery, currentSortBy, 20, nextPage)
                .collect { result ->
                    result.onSuccess { response ->
                        when (response) {
                            is ApiResponse.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoadingMore = false,
                                        // Append the new articles to the existing list
                                        articles = it.articles + response.articles,
                                        currentPage = nextPage
                                    )
                                }
                            }
                            is ApiResponse.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoadingMore = false,
                                        errorMessage = response.message
                                    )
                                }
                            }
                        }
                    }.onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                errorMessage = exception.localizedMessage
                            )
                        }
                    }
                }
        }
    }

    fun searchNewsAdvanced(
        query: String,
        searchIn: List<SearchIn>? = null,
        sources: List<String>? = null,
        domains: List<String>? = null,
        excludeDomains: List<String>? = null,
        from: String? = null,
        to: String? = null,
        language: String? = null,
        sortBy: SortBy = SortBy.PUBLISHED_AT
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, articles = emptyList()) }

            repository.searchNewsAdvanced(
                query = query,
                searchIn = searchIn,
                sources = sources,
                domains = domains,
                excludeDomains = excludeDomains,
                from = from,
                to = to,
                language = language,
                sortBy = sortBy
            ).collect { result ->
                result.onSuccess { response ->
                    when (response) {
                        is ApiResponse.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    articles = response.articles,
                                    totalResults = response.totalResults,
                                    currentPage = 1 // Advanced search always resets to page 1
                                )
                            }
                        }
                        is ApiResponse.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = response.message
                                )
                            }
                        }
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage
                        )
                    }
                }
            }
        }
    }

    /**
     * Clears the current error message from the UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}