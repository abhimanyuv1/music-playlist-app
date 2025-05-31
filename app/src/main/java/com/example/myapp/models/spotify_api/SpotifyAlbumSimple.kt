package com.example.myapp.models.spotify_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Represents a simplified album object as returned by various Spotify API endpoints
@Parcelize
data class SpotifyAlbumSimple(
    val id: String,
    val name: String,
    val album_type: String, // e.g., "ALBUM", "SINGLE", "COMPILATION"
    val artists: List<SpotifyArtistSimple>, // List of simplified artist objects
    val images: List<SpotifyImage>, // Album cover art in various sizes
    val uri: String,
    val release_date: String?,
    val release_date_precision: String?
    // external_urls: Map<String, String>?,
    // href: String? // Link to the full album object
) : Parcelable
