package com.example.myapp.models

data class Track(
    val id: String,
    val name: String,
    val artistName: String,
    val albumName: String,
    val albumArtUrl: String?, // Can be null
    val uri: String,
    val durationMs: Int
)
