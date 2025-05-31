package com.example.myapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapp.R
import com.example.myapp.utils.RetrofitClient
import com.example.myapp.utils.SpotifyConstants
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

        buttonLinkSpotify.setOnClickListener {
            val builder = AuthorizationRequest.Builder(
                SpotifyConstants.CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                SpotifyConstants.REDIRECT_URI
            )
            builder.setScopes(arrayOf("playlist-read-private", "user-library-read", "user-read-email")) // Added email scope
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

    private fun fetchSpotifyUserProfile(token: String) {
        lifecycleScope.launch {
            try {
                val user = RetrofitClient.spotifyApiService.getCurrentUserProfile("Bearer $token")
                spotifyUserNameTextView.text = "Spotify User: ${user.display_name ?: user.id}"
                buttonLinkSpotify.text = "Spotify Linked" // Or hide it: buttonLinkSpotify.visibility = View.GONE
                Log.d("MainActivity", "Spotify User: ${user.display_name}, ID: ${user.id}, Email: ${user.email}")
            } catch (e: HttpException) {
                Log.e("MainActivity", "Spotify API error: ${e.message()}", e)
                if (e.code() == 401 || e.code() == 403) { // Unauthorized or Forbidden
                    clearSpotifyToken()
                    spotifyUserNameTextView.text = "Spotify User: Token expired. Please link again."
                    buttonLinkSpotify.text = "Link Spotify Account"
                } else {
                    spotifyUserNameTextView.text = "Spotify User: Error fetching data"
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching Spotify profile", e)
                spotifyUserNameTextView.text = "Spotify User: Error fetching data"
            }
        }
    }

    private fun updateSpotifyStatus() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val spotifyToken = sharedPreferences.getString("spotify_access_token", null)

        if (spotifyToken != null) {
            fetchSpotifyUserProfile(spotifyToken)
        } else {
            spotifyUserNameTextView.text = "Spotify User: Not Linked"
            buttonLinkSpotify.text = "Link Spotify Account"
        }
    }

    private fun clearSpotifyToken() {
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
                    updateSpotifyStatus() // Refresh UI with new token
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("MainActivity", "Spotify auth error: ${response.error}")
                    spotifyUserNameTextView.text = "Spotify User: Auth Error"
                }
                else -> {
                    Log.d("MainActivity", "Spotify auth result: ${response.type}")
                }
            }
        }
    }
}
