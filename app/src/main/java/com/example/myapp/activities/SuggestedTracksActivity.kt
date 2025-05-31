package com.example.myapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.SuggestedTrackAdapter
import com.example.myapp.models.spotify_api.SpotifyTrackFull // Import the correct model

class SuggestedTracksActivity : AppCompatActivity() {

    private lateinit var detectedMoodTextView: TextView
    private lateinit var suggestedTracksRecyclerView: RecyclerView
    private lateinit var suggestedTrackAdapter: SuggestedTrackAdapter
    private var suggestedTracksList = mutableListOf<SpotifyTrackFull>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggested_tracks)

        detectedMoodTextView = findViewById(R.id.detectedMoodTextView)
        suggestedTracksRecyclerView = findViewById(R.id.suggestedTracksRecyclerView)

        val detectedMood = intent.getStringExtra("DETECTED_MOOD") ?: "Unknown Mood"
        detectedMoodTextView.text = "Songs for your $detectedMood mood:"

        val tracksFromIntent: ArrayList<SpotifyTrackFull>? = intent.getParcelableArrayListExtra("SUGGESTED_TRACKS")

        if (tracksFromIntent != null && tracksFromIntent.isNotEmpty()) {
            suggestedTracksList.addAll(tracksFromIntent)
        } else {
            detectedMoodTextView.text = "No suggestions found for '$detectedMood', or an error occurred."
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
    }
}
