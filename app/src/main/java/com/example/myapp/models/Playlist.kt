package com.example.myapp.models

data class Playlist(
    val id: String,
    val name: String,
    val description: String?, // Can be null
    val ownerName: String,
    val imageUrl: String?, // Can be null
    var tracks: List<Track> = emptyList() // List of Track objects
)
