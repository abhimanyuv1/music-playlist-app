package com.example.myapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
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
    private lateinit var playlistOwnerDetailsTextView: TextView
    private lateinit var playlistImageDetailsView: ImageView
    private lateinit var playlistTracksRecyclerView: RecyclerView
    private lateinit var playlistTrackAdapter: PlaylistTrackAdapter
    private lateinit var playlistDetailsLoadingProgressBar: ProgressBar
    private lateinit var playlistTracksEmptyTextView: TextView
    private lateinit var playlistDetailsErrorTextView: TextView

    private var playlistId: String? = null
    // private var playlistName: String? = null // Already set to title
    // private var playlistImageUrl: String? = null // Already loaded
    // private var playlistOwner: String? = null // Already displayed


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_details)

        playlistNameDetailsTextView = findViewById(R.id.playlistNameDetailsTextView)
        playlistOwnerDetailsTextView = findViewById(R.id.playlistOwnerDetailsTextView)
        playlistImageDetailsView = findViewById(R.id.playlistImageDetailsView)
        playlistTracksRecyclerView = findViewById(R.id.playlistTracksRecyclerView)
        playlistDetailsLoadingProgressBar = findViewById(R.id.playlistDetailsLoadingProgressBar)
        playlistTracksEmptyTextView = findViewById(R.id.playlistTracksEmptyTextView)
        playlistDetailsErrorTextView = findViewById(R.id.playlistDetailsErrorTextView)

        playlistId = intent.getStringExtra("PLAYLIST_ID")
        val playlistNameFromIntent = intent.getStringExtra("PLAYLIST_NAME")
        val playlistImageUrlFromIntent = intent.getStringExtra("PLAYLIST_IMAGE_URL")
        val playlistOwnerFromIntent = intent.getStringExtra("PLAYLIST_OWNER_NAME")

        playlistNameDetailsTextView.text = playlistNameFromIntent ?: "Playlist Details"
        title = playlistNameFromIntent ?: "Playlist"
        playlistOwnerDetailsTextView.text = "By ${playlistOwnerFromIntent ?: "Unknown Owner"}"


        if (!playlistImageUrlFromIntent.isNullOrEmpty()) {
            playlistImageDetailsView.load(playlistImageUrlFromIntent) {
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
            playlistDetailsErrorTextView.text = "Playlist ID missing."
            playlistDetailsErrorTextView.visibility = View.VISIBLE
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
            playlistDetailsErrorTextView.text = "Spotify token not found. Please log in via main screen."
            playlistDetailsErrorTextView.visibility = View.VISIBLE
            playlistDetailsLoadingProgressBar.visibility = View.GONE
            return
        }

        // Initial UI state for loading tracks
        playlistDetailsLoadingProgressBar.visibility = View.VISIBLE
        playlistTracksRecyclerView.visibility = View.GONE
        playlistTracksEmptyTextView.visibility = View.GONE
        playlistDetailsErrorTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val playlistTracksPagingObject = RetrofitClient.spotifyApiService.getPlaylistTracks("Bearer $spotifyToken", id)
                val tracks = playlistTracksPagingObject.items.mapNotNull { it.track }
                playlistTrackAdapter.updateData(tracks)
                Log.d("PlaylistDetailsActivity", "Fetched ${tracks.size} tracks for playlist $id")

                if (tracks.isEmpty()) {
                    playlistTracksEmptyTextView.visibility = View.VISIBLE
                    playlistTracksRecyclerView.visibility = View.GONE
                } else {
                    playlistTracksEmptyTextView.visibility = View.GONE
                    playlistTracksRecyclerView.visibility = View.VISIBLE
                }
            } catch (e: HttpException) {
                Log.e("PlaylistDetailsActivity", "API Error: ${e.code()} ${e.message()}", e)
                playlistDetailsErrorTextView.text = "Error fetching playlist tracks: ${e.message()}"
                playlistDetailsErrorTextView.visibility = View.VISIBLE
                if (e.code() == 401 || e.code() == 403) {
                    Toast.makeText(this@PlaylistDetailsActivity, "Spotify token invalid. Please re-link on main screen.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetailsActivity", "Error fetching playlist tracks", e)
                playlistDetailsErrorTextView.text = "Could not fetch playlist tracks."
                playlistDetailsErrorTextView.visibility = View.VISIBLE
            } finally {
                playlistDetailsLoadingProgressBar.visibility = View.GONE
            }
        }
    }
}
