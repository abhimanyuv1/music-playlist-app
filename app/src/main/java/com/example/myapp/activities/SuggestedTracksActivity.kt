package com.example.myapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.SuggestedTrackAdapter
import com.example.myapp.models.api_requests.PlaylistCreationRequest
import com.example.myapp.models.spotify_api.SpotifyTrackFull // Import the correct model
import com.example.myapp.utils.RetrofitClient
import com.example.myapp.utils.UserMusicCache
import kotlinx.coroutines.launch
import retrofit2.HttpException


class SuggestedTracksActivity : AppCompatActivity() {

    private lateinit var detectedMoodTextView: TextView
    private lateinit var suggestedTracksRecyclerView: RecyclerView
    private lateinit var suggestedTrackAdapter: SuggestedTrackAdapter
    private lateinit var createPlaylistButton: Button
    private var suggestedTracksList = mutableListOf<SpotifyTrackFull>()
    private var newlyCreatedPlaylistId: String? = null // To store ID for next step

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggested_tracks)

        detectedMoodTextView = findViewById(R.id.detectedMoodTextView)
        suggestedTracksRecyclerView = findViewById(R.id.suggestedTracksRecyclerView)
        createPlaylistButton = findViewById(R.id.createPlaylistButton)

        val detectedMood = intent.getStringExtra("DETECTED_MOOD") ?: "Unknown Mood"
        detectedMoodTextView.text = "Songs for your $detectedMood mood:"

        val tracksFromIntent: ArrayList<SpotifyTrackFull>? = intent.getParcelableArrayListExtra("SUGGESTED_TRACKS")

        if (tracksFromIntent != null && tracksFromIntent.isNotEmpty()) {
            suggestedTracksList.addAll(tracksFromIntent)
            suggestedTracksRecyclerView.visibility = View.VISIBLE // Show RecyclerView
        } else {
            detectedMoodTextView.text = "No suggestions found for '$detectedMood', or an error occurred."
            createPlaylistButton.isEnabled = false
            suggestedTracksRecyclerView.visibility = View.GONE // Hide RecyclerView
        }

        suggestedTrackAdapter = SuggestedTrackAdapter(suggestedTracksList) { track ->
            val trackUri = track.uri
            if (trackUri.isNullOrEmpty()) {
                Toast.makeText(this, "Cannot play this track (missing URI)", Toast.LENGTH_SHORT).show()
                return@SuggestedTrackAdapter
            }
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("TRACK_URI_TO_PLAY", trackUri)
            startActivity(intent)
        }
        suggestedTracksRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestedTracksRecyclerView.adapter = suggestedTrackAdapter

        createPlaylistButton.setOnClickListener {
            handleCreatePlaylist()
        }
    }

    private fun handleCreatePlaylist() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("spotify_access_token", null)
        val userId = UserMusicCache.currentSpotifyUser?.id

        if (token == null) {
            Toast.makeText(this, "Spotify token not found. Please re-link your account.", Toast.LENGTH_LONG).show()
            // Optionally, redirect to MainActivity or LoginActivity
            return
        }

        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please re-link Spotify.", Toast.LENGTH_LONG).show()
            return
        }

        val editText = EditText(this).apply {
            val mood = intent.getStringExtra("DETECTED_MOOD") ?: "My"
            hint = "$mood Mix Playlist" // Set hint for the EditText
            setText("$mood Mix") // Pre-fill with a default name based on mood
        }

        AlertDialog.Builder(this)
            .setTitle("New Playlist Name")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                var playlistName = editText.text.toString().trim()
                if (playlistName.isEmpty()) {
                    // Use mood as default name if input is empty after pre-fill was cleared
                    playlistName = "${intent.getStringExtra("DETECTED_MOOD") ?: "My"} Mix"
                }
                initiatePlaylistCreation(playlistName, token, userId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun initiatePlaylistCreation(playlistName: String, token: String, userId: String) {
        lifecycleScope.launch {
            val requestBody = PlaylistCreationRequest(
                name = playlistName,
                public = false, // Default to private
                description = "Playlist created with MyApp for a '$playlistName' mood."
            )
            try {
                val createdPlaylist = RetrofitClient.spotifyApiService.createPlaylist(
                    "Bearer $token",
                    userId,
                    requestBody
                )
                Log.d("SuggestedTracksActivity", "Playlist created: ${createdPlaylist.name} (ID: ${createdPlaylist.id})")
                newlyCreatedPlaylistId = createdPlaylist.id

                // Now add tracks to this new playlist
                val trackUris = suggestedTrackAdapter.tracks.mapNotNull { it.uri }.filter { it.isNotBlank() }
                if (trackUris.isNotEmpty()) {
                    val addTracksRequestBody = AddTracksToPlaylistRequest(uris = trackUris)
                    try {
                        val snapshotResponse = RetrofitClient.spotifyApiService.addTracksToPlaylist(
                            "Bearer $token",
                            newlyCreatedPlaylistId!!,
                            addTracksRequestBody
                        )
                        Log.d("SuggestedTracksActivity", "Tracks added to '${createdPlaylist.name}', snapshot: ${snapshotResponse.snapshot_id}")
                        Toast.makeText(this@SuggestedTracksActivity, "Playlist '${createdPlaylist.name}' created and tracks added!", Toast.LENGTH_LONG).show()
                    } catch (addTracksEx: HttpException) {
                        Log.e("SuggestedTracksActivity", "API Error adding tracks: ${addTracksEx.code()} ${addTracksEx.message()}", addTracksEx)
                        Toast.makeText(this@SuggestedTracksActivity, "Playlist created, but failed to add tracks: ${addTracksEx.message()}", Toast.LENGTH_LONG).show()
                    } catch (addTracksEx: Exception) {
                        Log.e("SuggestedTracksActivity", "Error adding tracks: ${addTracksEx.message}", addTracksEx)
                        Toast.makeText(this@SuggestedTracksActivity, "Playlist created, but failed to add tracks.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@SuggestedTracksActivity, "Playlist '${createdPlaylist.name}' created (no tracks to add).", Toast.LENGTH_LONG).show()
                }

                createPlaylistButton.isEnabled = false
                createPlaylistButton.text = getString(R.string.playlist_created)

            } catch (e: HttpException) {
                Log.e("SuggestedTracksActivity", "API Error creating playlist: ${e.code()} ${e.message()}", e)
                Toast.makeText(this@SuggestedTracksActivity, "Failed to create playlist: ${e.message()}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("SuggestedTracksActivity", "Error creating playlist", e)
                Toast.makeText(this@SuggestedTracksActivity, "Could not create playlist.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
