package com.example.myapp.services

import com.example.myapp.models.SpotifyUser
import retrofit2.http.GET
import retrofit2.http.Header

interface SpotifyApiService {
    @GET("v1/me")
    suspend fun getCurrentUserProfile(@Header("Authorization") token: String): SpotifyUser
}
