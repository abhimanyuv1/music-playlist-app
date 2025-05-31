package com.example.myapp.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.SuggestedTrackAdapter
import com.example.myapp.adapters.SuggestedTrackDisplay

class SuggestedTracksActivity : AppCompatActivity() {

    private lateinit var detectedMoodTextView: TextView
    private lateinit var suggestedTracksRecyclerView: RecyclerView
    private lateinit var suggestedTrackAdapter: SuggestedTrackAdapter
    private var dummyTracks = mutableListOf<SuggestedTrackDisplay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggested_tracks)

        detectedMoodTextView = findViewById(R.id.detectedMoodTextView)
        suggestedTracksRecyclerView = findViewById(R.id.suggestedTracksRecyclerView)

        val detectedMood = intent.getStringExtra("DETECTED_MOOD") ?: "Unknown Mood"
        detectedMoodTextView.text = "Songs for your $detectedMood mood:"

        loadDummyTracksForMood(detectedMood)

        suggestedTrackAdapter = SuggestedTrackAdapter(dummyTracks)
        suggestedTracksRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestedTracksRecyclerView.adapter = suggestedTrackAdapter
    }

    private fun loadDummyTracksForMood(mood: String) {
        dummyTracks.clear()
        when (mood.lowercase()) {
            "happy" -> {
                dummyTracks.add(SuggestedTrackDisplay("Walking on Sunshine", "Katrina & The Waves"))
                dummyTracks.add(SuggestedTrackDisplay("Good Day Sunshine", "The Beatles"))
                dummyTracks.add(SuggestedTrackDisplay("Happy", "Pharrell Williams"))
            }
            "sad" -> {
                dummyTracks.add(SuggestedTrackDisplay("Hallelujah", "Leonard Cohen"))
                dummyTracks.add(SuggestedTrackDisplay("Mad World", "Gary Jules"))
                dummyTracks.add(SuggestedTrackDisplay("Fix You", "Coldplay"))
            }
            "energetic", "workout" -> {
                dummyTracks.add(SuggestedTrackDisplay("Eye of the Tiger", "Survivor"))
                dummyTracks.add(SuggestedTrackDisplay("Can't Stop the Feeling!", "Justin Timberlake"))
                dummyTracks.add(SuggestedTrackDisplay("Don't Stop Me Now", "Queen"))
            }
            "chill", "relaxed", "calm" -> {
                dummyTracks.add(SuggestedTrackDisplay("Weightless", "Marconi Union"))
                dummyTracks.add(SuggestedTrackDisplay("Teardrop", "Massive Attack"))
                dummyTracks.add(SuggestedTrackDisplay("Better Together", "Jack Johnson"))
            }
            "rock" -> {
                dummyTracks.add(SuggestedTrackDisplay("Bohemian Rhapsody", "Queen"))
                dummyTracks.add(SuggestedTrackDisplay("Stairway to Heaven", "Led Zeppelin"))
            }
            "pop" -> {
                dummyTracks.add(SuggestedTrackDisplay("Blinding Lights", "The Weeknd"))
                dummyTracks.add(SuggestedTrackDisplay("Shape of You", "Ed Sheeran"))
            }
            "electronic" -> {
                dummyTracks.add(SuggestedTrackDisplay("Strobe", "deadmau5"))
                dummyTracks.add(SuggestedTrackDisplay("Around the World", "Daft Punk"))
            }
            "classical" -> {
                dummyTracks.add(SuggestedTrackDisplay("Für Elise", "Beethoven"))
                dummyTracks.add(SuggestedTrackDisplay("Clair de Lune", "Debussy"))
            }
            "focus", "study" -> {
                dummyTracks.add(SuggestedTrackDisplay("Ambient 1: Music for Airports", "Brian Eno"))
                dummyTracks.add(SuggestedTrackDisplay("Nuvole Bianche", "Ludovico Einaudi"))
            }
            else -> {
                dummyTracks.add(SuggestedTrackDisplay("No specific tracks for this mood yet.", "System"))
            }
        }
    }
}
