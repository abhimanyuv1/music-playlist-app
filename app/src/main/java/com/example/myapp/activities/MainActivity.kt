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
    private var userPlaylists = mutableListOf<SpotifyPlaylistSimple>()

    private lateinit var savedTracksRecyclerView: RecyclerView
    private lateinit var savedTrackAdapter: SavedTrackAdapter
    private var userSavedTracks = mutableListOf<SpotifyTrackFull>()

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

        setupPlaylistRecyclerView()
        setupSavedTracksRecyclerView()

        buttonLinkSpotify.setOnClickListener {
            val builder = AuthorizationRequest.Builder(
                SpotifyConstants.CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                SpotifyConstants.REDIRECT_URI
            )
            builder.setScopes(arrayOf("playlist-read-private", "user-library-read", "user-read-email", "user-library-read")) // Ensure user-library-read for saved tracks
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
        playlistAdapter = PlaylistAdapter(userPlaylists) { playlist ->
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
        savedTrackAdapter = SavedTrackAdapter(userSavedTracks) { track ->
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
            try {
                val user = RetrofitClient.spotifyApiService.getCurrentUserProfile("Bearer $token")
                spotifyUserNameTextView.text = "Spotify User: ${user.display_name ?: user.id}"
                buttonLinkSpotify.text = "Spotify Linked"
                Log.d("MainActivity", "Spotify User: ${user.display_name}, ID: ${user.id}, Email: ${user.email}")
                fetchUserPlaylists(token)
                fetchUserSavedTracks(token) // Fetch saved tracks after profile
            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error (User Profile): ${e.message()}", e)
                if (e.code() == 401 || e.code() == 403) {
                    clearSpotifyTokenAndData()
                } else {
                    spotifyUserNameTextView.text = "Spotify User: Error fetching user data"
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching Spotify profile", e)
                spotifyUserNameTextView.text = "Spotify User: Error fetching user data"
            }
        }
    }

    private fun fetchUserPlaylists(token: String) {
        lifecycleScope.launch {
            try {
                val playlistsPagingObject = RetrofitClient.spotifyApiService.getCurrentUserPlaylists("Bearer $token")
                // userPlaylists.clear() // Adapter's list is managed by adapter itself now
                // userPlaylists.addAll(playlistsPagingObject.items)
                playlistAdapter.updateData(playlistsPagingObject.items)
                UserMusicCache.setPlaylists(playlistsPagingObject.items) // Populate cache
                Log.d("MainActivity", "Fetched ${playlistsPagingObject.items.size} playlists.")
            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error (Playlists): ${e.message()}", e)
                if (e.code() == 401 || e.code() == 403) {
                    clearSpotifyTokenAndData()
                } else {
                     Toast.makeText(this@MainActivity, "Error fetching playlists: ${e.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching user playlists", e)
                Toast.makeText(this@MainActivity, "Could not fetch playlists.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserSavedTracks(token: String) {
        lifecycleScope.launch {
            try {
                val savedTracksPagingObject = RetrofitClient.spotifyApiService.getCurrentUserSavedTracks("Bearer $token")
                userSavedTracks.clear()
                // We need to map SpotifySavedTrack to SpotifyTrackFull for the adapter
                val tracks = savedTracksPagingObject.items.map { it.track }
                // userSavedTracks.clear() // Adapter's list is managed by adapter itself now
                // userSavedTracks.addAll(tracks)
                savedTrackAdapter.updateData(tracks)
                UserMusicCache.setSavedTracks(tracks) // Populate cache
                Log.d("MainActivity", "Fetched ${savedTracksPagingObject.items.size} saved tracks.")
            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error (Saved Tracks): ${e.message()}", e)
                 if (e.code() == 401 || e.code() == 403) {
                    clearSpotifyTokenAndData()
                } else {
                    Toast.makeText(this@MainActivity, "Error fetching saved tracks: ${e.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching saved tracks", e)
                Toast.makeText(this@MainActivity, "Could not fetch saved tracks.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSpotifyStatus() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val spotifyToken = sharedPreferences.getString("spotify_access_token", null)

        if (spotifyToken != null) {
            fetchSpotifyUserProfile(spotifyToken)
        } else {
            clearSpotifyTokenAndData() // Ensures UI is reset if no token
        }
    }

    private fun clearSpotifyTokenAndData() {
        val editor = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit()
        editor.remove("spotify_access_token")
        editor.apply()
        spotifyUserNameTextView.text = "Spotify User: Not Linked"
        buttonLinkSpotify.text = "Link Spotify Account"
        playlistAdapter.updateData(emptyList())
        savedTrackAdapter.updateData(emptyList())
        UserMusicCache.clearCache() // Clear cache
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
