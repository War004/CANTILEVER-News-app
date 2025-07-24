package com.cryptic.roundnews.api.client

import com.cryptic.roundnews.api.response.ApiResponse
import com.cryptic.roundnews.api.response.SearchIn
import com.cryptic.roundnews.api.response.SortBy
import com.cryptic.roundnews.api.response.SourcesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NewsRepository(
    private val apiService: NewsApiService
) {

    fun searchNews(
        query: String,
        sortBy: SortBy = SortBy.PUBLISHED_AT,
        pageSize: Int = 20,
        page: Int = 1
    ): Flow<Result<ApiResponse>> = flow {
        try {
            val response = apiService.searchEverything(
                query = query,
                sortBy = sortBy,
                pageSize = pageSize,
                page = page
            )
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
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
        sortBy: SortBy = SortBy.PUBLISHED_AT,
        pageSize: Int = 20,
        page: Int = 1
    ): Flow<Result<ApiResponse>> = flow {
        try {
            val response = apiService.searchEverything(
                query = query,
                searchIn = searchIn,
                sources = sources,
                domains = domains,
                excludeDomains = excludeDomains,
                from = from,
                to = to,
                language = language,
                sortBy = sortBy,
                pageSize = pageSize,
                page = page
            )
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getSources(): Flow<Result<SourcesResponse>> = flow {
        try {
            val response = apiService.getSources()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}