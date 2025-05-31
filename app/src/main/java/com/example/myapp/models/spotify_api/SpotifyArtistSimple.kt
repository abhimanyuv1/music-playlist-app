package com.example.myapp.models.spotify_api

// Represents a simplified artist object as returned by various Spotify API endpoints
data class SpotifyArtistSimple(
    val id: String,
    val name: String,
    val uri: String,
    val external_urls: Map<String, String>? // e.g., {"spotify": "https://open.spotify.com/artist/..."}
    // href: String? // Link to the full artist object
)
