package com.example.myapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myapp.R
import com.example.myapp.adapters.PlaylistTrackAdapter
import com.example.myapp.models.spotify_api.SpotifyTrackFull
import com.example.myapp.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PlaylistDetailsActivity : AppCompatActivity() {

    private lateinit var playlistNameDetailsTextView: TextView
    private lateinit var playlistOwnerDetailsTextView: TextView // Added for owner info
    private lateinit var playlistImageDetailsView: ImageView
    private lateinit var playlistTracksRecyclerView: RecyclerView
    private lateinit var playlistTrackAdapter: PlaylistTrackAdapter

    private var playlistId: String? = null
    private var playlistName: String? = null
    private var playlistImageUrl: String? = null
    private var playlistOwner: String? = null // Added for owner info


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_details)

        playlistNameDetailsTextView = findViewById(R.id.playlistNameDetailsTextView)
        playlistOwnerDetailsTextView = findViewById(R.id.playlistOwnerDetailsTextView)
        playlistImageDetailsView = findViewById(R.id.playlistImageDetailsView)
        playlistTracksRecyclerView = findViewById(R.id.playlistTracksRecyclerView)

        playlistId = intent.getStringExtra("PLAYLIST_ID")
        playlistName = intent.getStringExtra("PLAYLIST_NAME")
        playlistImageUrl = intent.getStringExtra("PLAYLIST_IMAGE_URL")
        playlistOwner = intent.getStringExtra("PLAYLIST_OWNER_NAME") // Retrieve owner

        playlistNameDetailsTextView.text = playlistName ?: "Playlist Details"
        title = playlistName ?: "Playlist" // Set activity title
        playlistOwnerDetailsTextView.text = "By ${playlistOwner ?: "Unknown Owner"}"


        if (!playlistImageUrl.isNullOrEmpty()) {
            playlistImageDetailsView.load(playlistImageUrl) {
                placeholder(R.drawable.ic_album_placeholder)
                error(R.drawable.ic_album_placeholder)
            }
        } else {
            playlistImageDetailsView.setImageResource(R.drawable.ic_album_placeholder)
        }

        setupRecyclerView()

        if (playlistId != null) {
            fetchPlaylistTracks(playlistId!!)
        } else {
            Toast.makeText(this, "Playlist ID missing.", Toast.LENGTH_SHORT).show()
            Log.e("PlaylistDetailsActivity", "Playlist ID is null.")
        }
    }

    private fun setupRecyclerView() {
        playlistTrackAdapter = PlaylistTrackAdapter(emptyList()) { track ->
            val trackUri = track.uri
            if (trackUri.isNullOrEmpty()) {
                Toast.makeText(this, "Cannot play this track (missing URI)", Toast.LENGTH_SHORT).show()
                return@PlaylistTrackAdapter
            }
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("TRACK_URI_TO_PLAY", trackUri)
            startActivity(intent)
        }
        playlistTracksRecyclerView.layoutManager = LinearLayoutManager(this)
        playlistTracksRecyclerView.adapter = playlistTrackAdapter
    }

    private fun fetchPlaylistTracks(id: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val spotifyToken = sharedPreferences.getString("spotify_access_token", null)

        if (spotifyToken == null) {
            Toast.makeText(this, "Spotify token not found. Please log in.", Toast.LENGTH_LONG).show()
            // TODO: Redirect to LoginActivity or handle re-authentication
            return
        }

        lifecycleScope.launch {
            try {
                val playlistTracksPagingObject = RetrofitClient.spotifyApiService.getPlaylistTracks("Bearer $spotifyToken", id)
                val tracks = playlistTracksPagingObject.items.mapNotNull { it.track }
                playlistTrackAdapter.updateData(tracks)
                Log.d("PlaylistDetailsActivity", "Fetched ${tracks.size} tracks for playlist $id")
            } catch (e: HttpException) {
                Log.e("PlaylistDetailsActivity", "API Error: ${e.code()} ${e.message()}", e)
                Toast.makeText(this@PlaylistDetailsActivity, "Error fetching playlist tracks: ${e.message()}", Toast.LENGTH_LONG).show()
                if (e.code() == 401 || e.code() == 403) {
                    // Token expired or invalid - potentially clear token and redirect to login
                    Toast.makeText(this@PlaylistDetailsActivity, "Spotify token invalid. Please re-link.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetailsActivity", "Error fetching playlist tracks", e)
                Toast.makeText(this@PlaylistDetailsActivity, "Could not fetch playlist tracks.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
