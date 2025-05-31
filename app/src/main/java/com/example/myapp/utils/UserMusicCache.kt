package com.example.myapp.utils

import com.example.myapp.models.spotify_api.AudioFeatures
import com.example.myapp.models.spotify_api.SpotifyPlaylistSimple
import com.example.myapp.models.spotify_api.SpotifyTrackFull
import com.example.myapp.models.SpotifyUser // Assuming SpotifyUser is in com.example.myapp.models

object UserMusicCache {
    val userPlaylists = mutableListOf<SpotifyPlaylistSimple>()
    val userSavedTracks = mutableListOf<SpotifyTrackFull>()
    val audioFeaturesCache = mutableMapOf<String, AudioFeatures>() // Key: Track ID
    var currentSpotifyUser: SpotifyUser? = null

    fun setPlaylists(playlists: List<SpotifyPlaylistSimple>) {
        userPlaylists.clear()
        userPlaylists.addAll(playlists)
    }

    fun setSavedTracks(tracks: List<SpotifyTrackFull>) {
        userSavedTracks.clear()
        userSavedTracks.addAll(tracks)
    }

    fun cacheAudioFeatures(featuresList: List<AudioFeatures>) {
        featuresList.forEach { features ->
            // Ensure features.id is not null or empty if that's possible from your API contract
            audioFeaturesCache[features.id] = features
        }
    }

    fun clearCache() {
        userPlaylists.clear()
        userSavedTracks.clear()
        audioFeaturesCache.clear()
        currentSpotifyUser = null
    }
}
