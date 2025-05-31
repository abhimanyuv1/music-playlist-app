package com.example.myapp.models.spotify_api

// Represents a Saved Track object as returned by "Get Current User's Saved Tracks"
data class SpotifySavedTrack(
    val added_at: String, // ISO 8601 timestamp
    val track: SpotifyTrackFull // The full track object
)
