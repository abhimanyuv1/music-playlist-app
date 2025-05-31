package com.example.myapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R

class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var albumArtImageView: ImageView
    private lateinit var trackTitleTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var previousButton: ImageButton
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var isPlaying = false // Simple state for play/pause toggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        albumArtImageView = findViewById(R.id.albumArtImageView)
        trackTitleTextView = findViewById(R.id.trackTitleTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        previousButton = findViewById(R.id.previousButton)
        playPauseButton = findViewById(R.id.playPauseButton)
        nextButton = findViewById(R.id.nextButton)

        // Set placeholder content
        trackTitleTextView.text = "Select a Song"
        artistNameTextView.text = "Artist"
        albumArtImageView.setImageResource(R.drawable.ic_album_placeholder) // Placeholder image

        previousButton.setOnClickListener {
            Log.d("MusicPlayerActivity", "Previous button clicked")
            // TODO: Implement previous track logic
        }

        playPauseButton.setOnClickListener {
            Log.d("MusicPlayerActivity", "Play/Pause button clicked")
            isPlaying = !isPlaying
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_pause)
                // TODO: Implement play logic
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play_arrow)
                // TODO: Implement pause logic
            }
        }

        nextButton.setOnClickListener {
            Log.d("MusicPlayerActivity", "Next button clicked")
            // TODO: Implement next track logic
        }
    }
}
