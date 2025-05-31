package com.example.myapp.models.spotify_api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
) : Parcelable
