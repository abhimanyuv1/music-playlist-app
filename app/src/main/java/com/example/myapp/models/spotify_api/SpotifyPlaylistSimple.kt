package com.example.myapp.models.spotify_api

// Represents a simplified playlist object as returned by endpoints like "Get Current User's Playlists"
data class SpotifyPlaylistSimple(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>,
    val owner: SpotifyOwner,
    val tracks: TracksInfo, // Contains href and total for tracks
    val uri: String
)

data class TracksInfo(
    val href: String, // A link to the Web API endpoint providing full details of the tracks
    val total: Int     // The total number of tracks in the playlist
)
