package com.example.myapp.models.spotify_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Represents the response from Spotify API when tracks are added to a playlist,
// which typically includes a snapshot_id.
@Parcelize
data class SpotifySnapshotResponse(
    val snapshot_id: String
) : Parcelable
