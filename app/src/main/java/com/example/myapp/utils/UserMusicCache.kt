package com.example.myapp.utils

import com.example.myapp.models.spotify_api.SpotifyPlaylistSimple
import com.example.myapp.models.spotify_api.SpotifyTrackFull

object UserMusicCache {
    val userPlaylists = mutableListOf<SpotifyPlaylistSimple>()
    val userSavedTracks = mutableListOf<SpotifyTrackFull>()

    fun setPlaylists(playlists: List<SpotifyPlaylistSimple>) {
        userPlaylists.clear()
        userPlaylists.addAll(playlists)
    }

    fun setSavedTracks(tracks: List<SpotifyTrackFull>) {
        userSavedTracks.clear()
        userSavedTracks.addAll(tracks)
    }

    fun clearCache() {
        userPlaylists.clear()
        userSavedTracks.clear()
    }
}
