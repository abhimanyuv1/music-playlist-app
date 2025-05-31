package com.example.myapp.models.spotify_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Represents a simplified artist object as returned by various Spotify API endpoints
@Parcelize
data class SpotifyArtistSimple(
    val id: String,
    val name: String,
    val uri: String,
    val external_urls: Map<String, String>? = null // e.g., {"spotify": "https://open.spotify.com/artist/..."}
    // href: String? // Link to the full artist object
) : Parcelable
