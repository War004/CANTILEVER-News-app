package com.cryptic.roundnews.api.client

import com.cryptic.roundnews.api.response.SearchIn
import com.cryptic.roundnews.api.response.SortBy
import com.cryptic.roundnews.api.response.SourcesResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import com.cryptic.roundnews.BuildConfig
import com.cryptic.roundnews.api.response.ApiResponse
import com.cryptic.roundnews.viewModel.NewsViewModel

class NewsApiService(private val apiKey: String) {
    private val client = NewsApiClient.client

    /**
     * Search through millions of articles using the Everything endpoint
     */
    suspend fun searchEverything(
        query: String,
        searchIn: List<SearchIn>? = null,
        sources: List<String>? = null,
        domains: List<String>? = null,
        excludeDomains: List<String>? = null,
        from: String? = null,
        to: String? = null,
        language: String? = null,
        sortBy: SortBy = SortBy.PUBLISHED_AT,
        pageSize: Int = 100,
        page: Int = 1
    ): ApiResponse {
        return client.get("everything") {
            header("X-Api-Key", apiKey)
            parameter("q", query)

            searchIn?.let {
                parameter("searchIn", it.joinToString(",") { search -> search.value })
            }
            sources?.let {
                parameter("sources", it.joinToString(","))
            }
            domains?.let {
                parameter("domains", it.joinToString(","))
            }
            excludeDomains?.let {
                parameter("excludeDomains", it.joinToString(","))
            }
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
            language?.let { parameter("language", it) }
            parameter("sortBy", sortBy.value)
            parameter("pageSize", pageSize.coerceIn(1, 100))
            parameter("page", page.coerceAtLeast(1))
        }.body()
    }

    /**
     * Get available news sources
     */
    suspend fun getSources(
        category: String? = null,
        language: String? = null,
        country: String? = null
    ): SourcesResponse {
        return client.get("top-headlines/sources") {
            header("X-Api-Key", apiKey)
            category?.let { parameter("category", it) }
            language?.let { parameter("language", it) }
            country?.let { parameter("country", it) }
        }.body()
    }
}

object NewsApiFactory {
    private val newsApiService by lazy {
        NewsApiService(BuildConfig.NEWS_API_KEY)
    }

    private val newsRepository by lazy {
        NewsRepository(newsApiService)
    }

    fun createNewsViewModel(): NewsViewModel {
        return NewsViewModel(newsRepository)
    }

    /*fun getNewsApiService(): NewsApiService {
        return newsApiService
    }*/
}