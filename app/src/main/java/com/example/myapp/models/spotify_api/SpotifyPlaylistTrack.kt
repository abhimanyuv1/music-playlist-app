package com.example.myapp.models.spotify_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Represents an item in a playlist, which contains track information.
// Note: Spotify API's Get Playlist Items endpoint returns a paging object of these.
@Parcelize
data class SpotifyPlaylistTrack(
    // val added_at: String?, // Timestamp when the track was added. Optional.
    // val added_by: SpotifyUser?, // User who added the track. Optional. Requires SpotifyUser to be Parcelable.
    // val is_local: Boolean, // Whether the track is a local file or not.
    val track: SpotifyTrackFull? // The track information. Can be null if track is unavailable (e.g. deleted).
    // val primary_color: String? // Not typically used for playback.
    // val video_thumbnail: VideoThumbnail? // Not typically used for playback.
) : Parcelable

// Example if you needed added_by with a simplified user object that's also Parcelable
// @Parcelize
// data class SpotifyPlaylistTrackAdder(
//    val id: String,
//    val type: String, // "user"
//    val uri: String
// ) : Parcelable

// Example for video_thumbnail if needed
// @Parcelize
// data class VideoThumbnail(
//    val url: String?
// ) : Parcelable
