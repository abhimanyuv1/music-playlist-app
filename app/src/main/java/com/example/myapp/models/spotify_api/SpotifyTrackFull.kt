package com.example.myapp.models.spotify_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Represents a full track object as returned by Spotify API (e.g., in Saved Tracks)
@Parcelize
data class SpotifyTrackFull(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtistSimple>,
    val album: SpotifyAlbumSimple, // Simplified album object
    val uri: String,
    val duration_ms: Int,
    val explicit: Boolean,
    val popularity: Int?,
    val preview_url: String?,
    val track_number: Int?,
    val disc_number: Int?,
    val external_urls: Map<String, String>? = null
    // available_markets: List<String>?
    // external_ids: Map<String, String>? // e.g., {"isrc": "..."}
    // is_local: Boolean
) : Parcelable
