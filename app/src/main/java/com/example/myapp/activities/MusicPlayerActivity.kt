package com.example.myapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.utils.SpotifyConstants
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track

class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var albumArtImageView: ImageView
    private lateinit var trackTitleTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var previousButton: ImageButton
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isPlaying = false // Local state, to be updated by PlayerState later if possible
    private var currentTrack: Track? = null

    // Example Test Track URI (Blinding Lights by The Weeknd)
    // private val testTrackUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b" // A more common track - No longer primary way to play
    private var pendingTrackUriToPlay: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        albumArtImageView = findViewById(R.id.albumArtImageView)
        trackTitleTextView = findViewById(R.id.trackTitleTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        previousButton = findViewById(R.id.previousButton)
        playPauseButton = findViewById(R.id.playPauseButton)
        nextButton = findViewById(R.id.nextButton)

        // Initial UI State
        trackTitleTextView.text = "Not Connected"
        artistNameTextView.text = "Spotify"
        albumArtImageView.setImageResource(R.drawable.ic_album_placeholder)
        playPauseButton.isEnabled = false
        nextButton.isEnabled = false
        previousButton.isEnabled = false

        // Check for a track URI passed from another activity
        val trackUriFromIntent = intent.getStringExtra("TRACK_URI_TO_PLAY")
        if (trackUriFromIntent != null) {
            pendingTrackUriToPlay = trackUriFromIntent
            // Update UI immediately for responsiveness, actual play command in connected()
            trackTitleTextView.text = "Loading track..."
            artistNameTextView.text = "" // Will be updated by PlayerState
        }

        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        connectToSpotifyAppRemote()
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    private fun connectToSpotifyAppRemote() {
        if (spotifyAppRemote?.isConnected == true) {
            Log.d("MusicPlayerActivity", "Already connected to Spotify App Remote.")
            return
        }
        val connectionParams = ConnectionParams.Builder(SpotifyConstants.CLIENT_ID)
            .setRedirectUri(SpotifyConstants.REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("MusicPlayerActivity", "Spotify App Remote connected!")
                connected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MusicPlayerActivity", "Spotify App Remote connection failed: ${throwable.message}")
                Toast.makeText(this@MusicPlayerActivity, "Failed to connect to Spotify", Toast.LENGTH_LONG).show()
                trackTitleTextView.text = "Connection Failed"
            }
        })
    }

    private fun connected() {
        playPauseButton.isEnabled = true
        nextButton.isEnabled = true
        previousButton.isEnabled = true
        trackTitleTextView.text = "Connected to Spotify"
        artistNameTextView.text = "Ready to play"

        // Subscribe to PlayerState
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            currentTrack = playerState.track
            isPlaying = !playerState.isPaused
            trackTitleTextView.text = playerState.track.name ?: "Unknown Track"
            artistNameTextView.text = playerState.track.artist.name ?: "Unknown Artist"

            // Update play/pause button icon
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_pause)
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play_arrow)
            }

            // TODO: Update album art using Coil: playerState.track.imageUri - Replaced with ImagesApi
            spotifyAppRemote?.imagesApi?.getImage(playerState.track.imageUri, com.spotify.protocol.types.Image.Dimension.LARGE)
                ?.setResultCallback { bitmap ->
                    albumArtImageView.setImageBitmap(bitmap)
                }?.setErrorCallback { throwable ->
                    Log.e("MusicPlayerActivity", "Error loading album art: ${throwable.message}")
                    albumArtImageView.setImageResource(R.drawable.ic_album_placeholder)
                }
        }

        // Play pending track if any
        if (pendingTrackUriToPlay != null) {
            spotifyAppRemote?.playerApi?.play(pendingTrackUriToPlay!!)
                ?.setResultCallback { Log.d("MusicPlayerActivity", "Playing track from intent: $pendingTrackUriToPlay") }
                ?.setErrorCallback { throwable -> Log.e("MusicPlayerActivity", "Failed to play track from intent $pendingTrackUriToPlay: ${throwable.message}") }
            pendingTrackUriToPlay = null // Clear it after attempting to play
        }
    }

    private fun setupClickListeners() {
        previousButton.setOnClickListener {
            Log.d("MusicPlayerActivity", "Previous button clicked")
            spotifyAppRemote?.playerApi?.skipPrevious()
                ?.setErrorCallback { throwable -> Log.e("MusicPlayerActivity", "Skip previous failed: ${throwable.message}") }
        }

        playPauseButton.setOnClickListener {
            Log.d("MusicPlayerActivity", "Play/Pause button clicked")
            if (spotifyAppRemote == null) {
                Toast.makeText(this, "Spotify not connected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isPlaying) {
                spotifyAppRemote?.playerApi?.pause()
                    ?.setResultCallback { Log.d("MusicPlayerActivity", "Pause command successful") }
                    ?.setErrorCallback { throwable -> Log.e("MusicPlayerActivity", "Pause failed: ${throwable.message}") }
            } else {
                // Only resume if there's a current track loaded (even if paused)
                if (currentTrack != null) {
                    spotifyAppRemote?.playerApi?.resume()
                        ?.setResultCallback { Log.d("MusicPlayerActivity", "Resume command successful") }
                        ?.setErrorCallback { throwable -> Log.e("MusicPlayerActivity", "Resume failed: ${throwable.message}") }
                } else {
                    // Nothing is loaded, and not playing. Button does nothing or could show a message.
                    Toast.makeText(this, "No track selected or loaded.", Toast.LENGTH_SHORT).show()
                    Log.d("MusicPlayerActivity", "Play button clicked, but no current track and not playing.")
                }
            }
            // The PlayerState subscription should update isPlaying and the icon.
        }

        nextButton.setOnClickListener {
            Log.d("MusicPlayerActivity", "Next button clicked")
            spotifyAppRemote?.playerApi?.skipNext()
                ?.setErrorCallback { throwable -> Log.e("MusicPlayerActivity", "Skip next failed: ${throwable.message}") }
        }
    }
}
