package com.example.myapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.PlaylistAdapter
import com.example.myapp.adapters.SavedTrackAdapter
import com.example.myapp.models.spotify_api.SpotifyPlaylistSimple
import com.example.myapp.models.spotify_api.SpotifyTrackFull
import com.example.myapp.utils.RetrofitClient
import com.example.myapp.utils.SpotifyConstants
import com.example.myapp.utils.UserMusicCache
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    private lateinit var spotifyUserNameTextView: TextView
    private lateinit var buttonLinkSpotify: Button
    private lateinit var buttonOpenChat: Button
    private lateinit var buttonOpenMusicPlayer: Button

    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    // private var userPlaylists = mutableListOf<SpotifyPlaylistSimple>() // Data now primarily in adapter/cache

    private lateinit var savedTracksRecyclerView: RecyclerView
    private lateinit var savedTrackAdapter: SavedTrackAdapter
    // private var userSavedTracks = mutableListOf<SpotifyTrackFull>() // Data now primarily in adapter/cache

    private lateinit var mainLoadingProgressBar: ProgressBar
    private lateinit var playlistsEmptyTextView: TextView
    private lateinit var savedTracksEmptyTextView: TextView
    private lateinit var mainErrorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in (app login, not Spotify)
        val appSharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val isAppLoggedIn = appSharedPreferences.getBoolean("isLoggedIn", false)

        if (!isAppLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        spotifyUserNameTextView = findViewById(R.id.spotifyUserNameTextView)
        buttonLinkSpotify = findViewById(R.id.buttonLinkSpotify)
        buttonOpenChat = findViewById(R.id.buttonOpenChat)
        buttonOpenMusicPlayer = findViewById(R.id.buttonOpenMusicPlayer)
        playlistsRecyclerView = findViewById(R.id.playlistsRecyclerView)
        savedTracksRecyclerView = findViewById(R.id.savedTracksRecyclerView)
        mainLoadingProgressBar = findViewById(R.id.mainLoadingProgressBar)
        playlistsEmptyTextView = findViewById(R.id.playlistsEmptyTextView)
        savedTracksEmptyTextView = findViewById(R.id.savedTracksEmptyTextView)
        mainErrorTextView = findViewById(R.id.mainErrorTextView)

        setupPlaylistRecyclerView()
        setupSavedTracksRecyclerView()

        buttonLinkSpotify.setOnClickListener {
            val builder = AuthorizationRequest.Builder(
                SpotifyConstants.CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                SpotifyConstants.REDIRECT_URI
            )
            builder.setScopes(arrayOf(
                "user-read-email",        // For user profile
                "playlist-read-private",  // For reading user's private playlists
                "user-library-read",      // For reading user's saved tracks & potentially audio features if not covered by open
                "app-remote-control",     // For Spotify App Remote SDK playback control
                "playlist-modify-public", // To create/modify public playlists
                "playlist-modify-private" // To create/modify private playlists
                // "user-top-read" // Example for future use: user's top artists and tracks
            ))
            // builder.setShowDialog(true) // Optional: to force the auth dialog to always show
            val request = builder.build()
            AuthorizationClient.openLoginActivity(this, SpotifyConstants.REQUEST_CODE, request)
        }

        buttonOpenChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        buttonOpenMusicPlayer.setOnClickListener {
            startActivity(Intent(this, MusicPlayerActivity::class.java))
        }

        updateSpotifyStatus()
    }

    private fun setupPlaylistRecyclerView() {
        // Initialize with empty list, data will be loaded
        playlistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            Log.d("MainActivity", "Clicked on playlist: ${playlist.name}, ID: ${playlist.id}")
            val intent = Intent(this, PlaylistDetailsActivity::class.java).apply {
                putExtra("PLAYLIST_ID", playlist.id)
                putExtra("PLAYLIST_NAME", playlist.name)
                putExtra("PLAYLIST_IMAGE_URL", playlist.images.firstOrNull()?.url)
                putExtra("PLAYLIST_OWNER_NAME", playlist.owner.display_name)
            }
            startActivity(intent)
        }
        playlistsRecyclerView.layoutManager = LinearLayoutManager(this)
        playlistsRecyclerView.adapter = playlistAdapter
    }

    private fun setupSavedTracksRecyclerView() {
        // Initialize with empty list
        savedTrackAdapter = SavedTrackAdapter(emptyList()) { track ->
            Log.d("MainActivity", "Clicked on saved track: ${track.name}")
            val trackUri = track.uri
            if (trackUri.isNullOrEmpty()) {
                Toast.makeText(this, "Cannot play this track (missing URI)", Toast.LENGTH_SHORT).show()
                return@SavedTrackAdapter
            }
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("TRACK_URI_TO_PLAY", trackUri)
            startActivity(intent)
        }
        savedTracksRecyclerView.layoutManager = LinearLayoutManager(this)
        savedTracksRecyclerView.adapter = savedTrackAdapter
    }

    private fun fetchSpotifyUserProfile(token: String) {
        lifecycleScope.launch {
            // This is the primary data loading sequence initiation
            mainLoadingProgressBar.visibility = View.VISIBLE
            mainErrorTextView.visibility = View.GONE
            playlistsRecyclerView.visibility = View.GONE
            savedTracksRecyclerView.visibility = View.GONE
            playlistsEmptyTextView.visibility = View.GONE
            savedTracksEmptyTextView.visibility = View.GONE

            try {
                val user = RetrofitClient.spotifyApiService.getCurrentUserProfile("Bearer $token")
                UserMusicCache.currentSpotifyUser = user
                spotifyUserNameTextView.text = "Spotify User: ${user.display_name ?: user.id}"
                buttonLinkSpotify.text = "Spotify Linked"
                Log.d("MainActivity", "Spotify User: ${user.display_name}, ID: ${user.id}, Email: ${user.email}")

                fetchUserPlaylists(token)
                fetchUserSavedTracks(token) // This will also trigger audio features and potentially hide progress bar

            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error (User Profile): ${e.message()}", e)
                mainErrorTextView.text = "Error loading Spotify profile: ${e.message()}"
                mainErrorTextView.visibility = View.VISIBLE
                mainLoadingProgressBar.visibility = View.GONE
                if (e.code() == 401 || e.code() == 403) {
                    clearSpotifyTokenAndData()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching Spotify profile", e)
                mainErrorTextView.text = "Error loading Spotify profile. Please try again."
                mainErrorTextView.visibility = View.VISIBLE
                mainLoadingProgressBar.visibility = View.GONE
            }
            // Note: mainLoadingProgressBar might be hidden by the last call in the sequence (fetchUserSavedTracks/fetchAndCacheAudioFeatures)
        }
    }

    private fun fetchUserPlaylists(token: String) {
        lifecycleScope.launch {
            // playlistsRecyclerView.visibility = View.GONE // Already handled by fetchSpotifyUserProfile initial state
            // playlistsEmptyTextView.visibility = View.GONE
            try {
                val playlistsPagingObject = RetrofitClient.spotifyApiService.getCurrentUserPlaylists("Bearer $token")
                val playlists = playlistsPagingObject.items
                playlistAdapter.updateData(playlists)
                UserMusicCache.setPlaylists(playlists)
                Log.d("MainActivity", "Fetched ${playlists.size} playlists.")
                if (playlists.isEmpty()) {
                    playlistsEmptyTextView.visibility = View.VISIBLE
                    playlistsRecyclerView.visibility = View.GONE
                } else {
                    playlistsEmptyTextView.visibility = View.GONE
                    playlistsRecyclerView.visibility = View.VISIBLE
                }
            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error (Playlists): ${e.message()}", e)
                mainErrorTextView.text = (mainErrorTextView.text.toString() + "\nError loading playlists: ${e.message()}").trim()
                mainErrorTextView.visibility = View.VISIBLE
                playlistsEmptyTextView.visibility = View.GONE
                playlistsRecyclerView.visibility = View.GONE
                if (e.code() == 401 || e.code() == 403) {
                    clearSpotifyTokenAndData() // This might hide mainErrorTextView if not careful
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching user playlists", e)
                mainErrorTextView.text = (mainErrorTextView.text.toString() + "\nCould not load playlists.").trim()
                mainErrorTextView.visibility = View.VISIBLE
                playlistsEmptyTextView.visibility = View.GONE
                playlistsRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun fetchUserSavedTracks(token: String) {
        lifecycleScope.launch {
            // savedTracksRecyclerView.visibility = View.GONE // Handled by fetchSpotifyUserProfile
            // savedTracksEmptyTextView.visibility = View.GONE
            try {
                val savedTracksPagingObject = RetrofitClient.spotifyApiService.getCurrentUserSavedTracks("Bearer $token")
                // userSavedTracks.clear() // Not needed as adapter manages its own list now
                val tracks = savedTracksPagingObject.items.mapNotNull { it.track }
                savedTrackAdapter.updateData(tracks)
                UserMusicCache.setSavedTracks(tracks)
                Log.d("MainActivity", "Fetched ${tracks.size} saved tracks.")

                if (tracks.isEmpty()) {
                    savedTracksEmptyTextView.visibility = View.VISIBLE
                    savedTracksRecyclerView.visibility = View.GONE
                    mainLoadingProgressBar.visibility = View.GONE // Hide if no tracks to fetch features for
                } else {
                    savedTracksEmptyTextView.visibility = View.GONE
                    savedTracksRecyclerView.visibility = View.VISIBLE
                    val currentToken = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("spotify_access_token", null)
                    if (currentToken != null) {
                        fetchAndCacheAudioFeatures(tracks, currentToken) // This will hide progress bar
                    } else {
                        mainLoadingProgressBar.visibility = View.GONE // Hide if no token for audio features
                    }
                }
            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error (Saved Tracks): ${e.message()}", e)
                mainErrorTextView.text = (mainErrorTextView.text.toString() + "\nError loading saved tracks: ${e.message()}").trim()
                mainErrorTextView.visibility = View.VISIBLE
                savedTracksEmptyTextView.visibility = View.GONE
                savedTracksRecyclerView.visibility = View.GONE
                mainLoadingProgressBar.visibility = View.GONE
                 if (e.code() == 401 || e.code() == 403) {
                    clearSpotifyTokenAndData()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching saved tracks", e)
                mainErrorTextView.text = (mainErrorTextView.text.toString() + "\nCould not load saved tracks.").trim()
                mainErrorTextView.visibility = View.VISIBLE
                savedTracksEmptyTextView.visibility = View.GONE
                savedTracksRecyclerView.visibility = View.GONE
                mainLoadingProgressBar.visibility = View.GONE
            }
        }
    }

    private fun fetchAndCacheAudioFeatures(tracks: List<SpotifyTrackFull>, token: String) {
        if (tracks.isEmpty()) {
            mainLoadingProgressBar.visibility = View.GONE
            return
        }
        val trackIds = tracks.mapNotNull { it.id }
        if (trackIds.isEmpty()){
            mainLoadingProgressBar.visibility = View.GONE
            return
        }
        // Keep progress bar visible if it was already visible for initial load

        lifecycleScope.launch {
            var allBatchesSuccessful = true
            trackIds.chunked(100).forEach { batchIds ->
                try {
                    val response = RetrofitClient.spotifyApiService.getAudioFeaturesForTracks(
                        "Bearer $token",
                        batchIds.joinToString(",")
                    )
                    val validFeatures = response.audioFeatures.filterNotNull()
                    if (validFeatures.isNotEmpty()) {
                        UserMusicCache.cacheAudioFeatures(validFeatures)
                        Log.d("MainActivity", "Cached ${validFeatures.size} audio features for batch.")
                    } else {
                        Log.d("MainActivity", "No valid audio features returned for batch.")
                    }
                } catch (e: HttpException) {
                    allBatchesSuccessful = false
                    Log.e("MainActivity", "API Error (Audio Features for batch): ${e.code()} ${e.message()}", e)
                    if (e.code() == 401 || e.code() == 403) {
                         clearSpotifyTokenAndData()
                         return@launch
                    }
                } catch (e: Exception) {
                    allBatchesSuccessful = false
                    Log.e("MainActivity", "Error fetching audio features for batch", e)
                }
            }
            if (!allBatchesSuccessful) {
                 Toast.makeText(this@MainActivity, "Could not fetch all audio features.", Toast.LENGTH_SHORT).show()
            }
            mainLoadingProgressBar.visibility = View.GONE // All main data loading done
        }
    }

    private fun updateSpotifyStatus() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val spotifyToken = sharedPreferences.getString("spotify_access_token", null)

        if (spotifyToken != null) {
            // Initial UI state for loading is set within fetchSpotifyUserProfile now
            fetchSpotifyUserProfile(spotifyToken)
        } else {
            clearSpotifyTokenAndData()
        }
    }

    private fun clearSpotifyTokenAndData() {
        val editor = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit()
        editor.remove("spotify_access_token")
        editor.apply()
        spotifyUserNameTextView.text = "Spotify User: Not Linked"
        buttonLinkSpotify.text = "Link Spotify Account"

        // Clear data and hide views
        playlistAdapter.updateData(emptyList())
        savedTrackAdapter.updateData(emptyList())
        UserMusicCache.clearCache()

        mainLoadingProgressBar.visibility = View.GONE
        playlistsRecyclerView.visibility = View.GONE
        savedTracksRecyclerView.visibility = View.GONE
        playlistsEmptyTextView.visibility = View.GONE
        savedTracksEmptyTextView.visibility = View.GONE
        mainErrorTextView.visibility = View.GONE
    }

    private fun clearSpotifyToken() { // Kept if needed for more granular control, but clearSpotifyTokenAndData is more comprehensive
        val editor = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit()
        editor.remove("spotify_access_token")
        editor.apply()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == SpotifyConstants.REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val editor = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit()
                    editor.putString("spotify_access_token", response.accessToken)
                    editor.apply()
                    Log.d("MainActivity", "Spotify token received: ${response.accessToken}")
                    updateSpotifyStatus()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("MainActivity", "Spotify auth error: ${response.error}")
                    clearSpotifyTokenAndData() // Clear data on auth error too
                }
                else -> {
                    Log.d("MainActivity", "Spotify auth result: ${response.type}")
                }
            }
        }
    }
}
