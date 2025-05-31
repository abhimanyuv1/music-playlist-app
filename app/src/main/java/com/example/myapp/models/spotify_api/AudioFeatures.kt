package com.example.myapp.models.spotify_api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioFeatures(
    val id: String,
    val danceability: Float,
    val energy: Float,
    val key: Int,
    val loudness: Float,
    val mode: Int, // 0 for Minor, 1 for Major
    val speechiness: Float,
    val acousticness: Float,
    val instrumentalness: Float,
    val liveness: Float,
    val valence: Float, // Positiveness of the track
    val tempo: Float,
    val uri: String, // The Spotify URI for the track. Some endpoints call it 'track_uri'
    @SerializedName("duration_ms") val durationMs: Int,
    @SerializedName("time_signature") val timeSignature: Int? = null // Optional, but often present
) : Parcelable

// Wrapper class for the API response like {"audio_features": [...]}
@Parcelize
data class AudioFeaturesResponse(
    @SerializedName("audio_features") val audioFeatures: List<AudioFeatures?> // List can contain nulls if some IDs not found
) : Parcelable
