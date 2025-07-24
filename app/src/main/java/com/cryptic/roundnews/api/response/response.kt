package com.cryptic.roundnews.api.response

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * A sealed class representing the API response.
 * @JsonClassDiscriminator("status") tells the serializer to look at the "status" field
 * to decide which subclass to use.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("status") // <-- THE #1 MOST IMPORTANT ANNOTATION
sealed class ApiResponse {

    /**
     * This class is used when the JSON object has "status": "ok".
     * @SerialName("ok") links this class to that specific value.
     */
    @Serializable
    @SerialName("ok") // <-- #2 This links the class to the value "ok"
    data class Success(
        val totalResults: Int,
        val articles: List<Article>
    ) : ApiResponse()

    /**
     * This class is used when the JSON object has "status": "error".
     * @SerialName("error") links this class to that specific value.
     */
    @Serializable
    @SerialName("error") // <-- #3 This links the class to the value "error"
    data class Error(
        val code: String?,
        val message: String?
    ) : ApiResponse()
}


@Serializable
data class Article(
    val source: Source,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

@Serializable
data class Source(
    val id: String?,
    val name: String
)

@Serializable
data class SourcesResponse(
    val status: String,
    val sources: List<SourceDetails>
)

@Serializable
data class SourceDetails(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val category: String,
    val language: String,
    val country: String
)

@Serializable
data class NewsErrorResponse(
    val status: String,
    val code: String?,
    val message: String?
)

enum class SortBy(val value: String) {
    RELEVANCY("relevancy"),
    POPULARITY("popularity"),
    PUBLISHED_AT("publishedAt")
}

enum class SearchIn(val value: String) {
    TITLE("title"),
    DESCRIPTION("description"),
    CONTENT("content")
}