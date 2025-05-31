package com.example.myapp.models.spotify_api

// Generic model for Spotify API Paging Object
data class SpotifyPagingObject<T>(
    val href: String,       // A link to the Web API endpoint returning the full result of the request.
    val items: List<T>,     // The requested data.
    val limit: Int,         // The maximum number of items in the response (as set in the query or by default).
    val next: String?,      // URL to the next page of items. (null if none)
    val offset: Int,        // The offset of the items returned (as set in the query or by default).
    val previous: String?,  // URL to the previous page of items. (null if none)
    val total: Int          // The total number of items available to return.
)
