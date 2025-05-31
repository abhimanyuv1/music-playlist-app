package com.example.myapp.models.api_requests

data class AddTracksToPlaylistRequest(
    val uris: List<String>, // List of Spotify track URIs, e.g., ["spotify:track:4iV5W9uYEdYUVa79Axb7Rh", "spotify:track:1301WleyT98MSxVHPZCA6M"]
    val position: Int? = null // The position to insert the tracks, a zero-based index. If omitted, the tracks will be appended.
)
