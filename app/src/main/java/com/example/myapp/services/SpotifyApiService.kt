package com.example.myapp.services

import com.example.myapp.models.SpotifyUser
import com.example.myapp.models.spotify_api.SpotifyPagingObject
import com.example.myapp.models.spotify_api.SpotifyPlaylistSimple
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body

interface SpotifyApiService {
    @GET("v1/me")
    suspend fun getCurrentUserProfile(@Header("Authorization") token: String): SpotifyUser

    @GET("v1/me/playlists")
    suspend fun getCurrentUserPlaylists(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SpotifyPagingObject<SpotifyPlaylistSimple>

    @GET("v1/me/tracks")
    suspend fun getCurrentUserSavedTracks(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SpotifyPagingObject<com.example.myapp.models.spotify_api.SpotifySavedTrack> // Fully qualify for clarity

    @GET("v1/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Header("Authorization") token: String,
        @Path("playlist_id") playlistId: String,
        @Query("fields") fields: String = "items(track(id,name,uri,duration_ms,explicit,album(name,images,artists(name)),artists(id,name)))", // More detailed fields
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SpotifyPagingObject<com.example.myapp.models.spotify_api.SpotifyPlaylistTrack>

    @GET("v1/audio-features")
    suspend fun getAudioFeaturesForTracks(
        @Header("Authorization") token: String,
        @Query("ids") ids: String // Comma-separated list of track IDs, max 100
    ): com.example.myapp.models.spotify_api.AudioFeaturesResponse

    @POST("v1/users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String,
        @Body playlistCreationRequest: com.example.myapp.models.api_requests.PlaylistCreationRequest
    ): SpotifyPlaylistSimple // Returns the created playlist (often a simple version)

    @POST("v1/playlists/{playlist_id}/tracks")
    suspend fun addTracksToPlaylist(
        @Header("Authorization") token: String,
        @Path("playlist_id") playlistId: String,
        @Body addTracksRequest: com.example.myapp.models.api_requests.AddTracksToPlaylistRequest
    ): com.example.myapp.models.spotify_api.SpotifySnapshotResponse
}
