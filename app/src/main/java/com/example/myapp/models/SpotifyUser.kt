package com.example.myapp.models

data class SpotifyUser(
    val display_name: String?, // Made nullable in case it's missing
    val id: String,
    val email: String? // Optional: if you request email scope
    // val images: List<ImageObject>? // Optional: if you need user profile images
)

// Example of an ImageObject if you were to include images
// data class ImageObject(
//     val url: String,
//     val height: Int?,
//     val width: Int?
// )
