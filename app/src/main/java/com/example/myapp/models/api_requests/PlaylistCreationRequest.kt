package com.example.myapp.models.api_requests

data class PlaylistCreationRequest(
    val name: String,
    val public: Boolean = false, // Default to private
    val collaborative: Boolean = false,
    val description: String? = null
)
