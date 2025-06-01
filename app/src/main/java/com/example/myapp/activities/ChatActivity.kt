package com.example.myapp.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.view.Gravity
import com.example.myapp.R
import com.example.myapp.models.ChatMessage
import com.example.myapp.models.spotify_api.SpotifyTrackFull
import com.example.myapp.utils.UserMusicCache
import java.util.UUID
import kotlin.collections.ArrayList

// Data class for Mood Profiles (can be moved to models if used elsewhere)
data class MoodProfile(
    val minValence: Float? = null, val maxValence: Float? = null,
    val minEnergy: Float? = null, val maxEnergy: Float? = null,
    val minDanceability: Float? = null, val maxDanceability: Float? = null,
    val minTempo: Float? = null, val maxTempo: Float? = null,
    val minInstrumentalness: Float? = null, val maxInstrumentalness: Float? = null,
    val minSpeechiness: Float? = null, val maxSpeechiness: Float? = null,
    val minAcousticness: Float? = null, val maxAcousticness: Float? = null,
    val minLiveness: Float? = null, val maxLiveness: Float? = null
    // Add other features as needed
)

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messagesList = mutableListOf<com.example.myapp.models.ChatMessage>()

    private lateinit var moodConfirmationLayout: LinearLayout
    private lateinit var yesMoodButton: Button
    private lateinit var noMoodButton: Button
    private var pendingMoodForConfirmation: String? = null

    // Keyword to Mood/Genre mapping
    private val moodKeywords = mapOf(
        "happy" to "Happy", // Valence: High, Energy: Moderate to High
        "joyful" to "Happy",
        "upbeat" to "Happy",
        "sad" to "Sad",       // Valence: Low, Energy: Low to Moderate
        "gloomy" to "Sad",
        "reflective" to "Sad", // Could also be Chill or Focus depending on energy/tempo
        "energetic" to "Energetic", // Energy: High, Tempo: High
        "workout" to "Energetic",
        "pumped" to "Energetic",
        "chill" to "Chill",   // Energy: Low, Valence: Mid, Tempo: Low to Mid
        "relaxed" to "Chill",
        "calm" to "Chill",
        "party" to "Party",   // Danceability: High, Energy: High, Valence: High
        "dance" to "Party",
        "focus" to "Focus",   // Instrumentalness: High, Speechiness: Low, Energy: Low to Mid
        "study" to "Focus",
        "ambient" to "Ambient", // Instrumentalness: High, Energy: Low, Valence: Mid
        "sleep" to "Sleep",   // Energy: Very Low, Acousticness: High, Instrumentalness: High, Valence: Low
        // Genre keywords (can have simpler profiles or rely on Spotify's genre tags more)
        "rock" to "Rock",
        "pop" to "Pop",
        "electronic" to "Electronic",
        "classical" to "Classical",
        "hip hop" to "Hip Hop",
        "jazz" to "Jazz"
    )

    // Mood/Genre to Audio Feature Profile mapping
    private val moodProfiles = mapOf(
        "Happy" to MoodProfile(minValence = 0.65f, minEnergy = 0.5f, maxEnergy = 0.9f),
        "Sad" to MoodProfile(maxValence = 0.35f, maxEnergy = 0.5f),
        "Energetic" to MoodProfile(minEnergy = 0.7f, minTempo = 120f, minDanceability = 0.6f),
        "Chill" to MoodProfile(minValence = 0.3f, maxValence = 0.7f, minEnergy = 0.2f, maxEnergy = 0.6f, maxTempo = 120f),
        "Party" to MoodProfile(minEnergy = 0.7f, minValence = 0.6f, minDanceability = 0.7f),
        "Focus" to MoodProfile(minEnergy = 0.1f, maxEnergy = 0.5f, minInstrumentalness = 0.6f, maxSpeechiness = 0.2f, maxLiveness = 0.3f),
        "Ambient" to MoodProfile(minEnergy = 0.0f, maxEnergy = 0.4f, minInstrumentalness = 0.7f, maxValence = 0.6f),
        "Sleep" to MoodProfile(maxEnergy = 0.2f, minAcousticness = 0.7f, minInstrumentalness = 0.5f, maxValence = 0.3f),
        "Rock" to MoodProfile(minEnergy = 0.5f, maxEnergy = 0.9f), // Simplified, rock is diverse
        "Pop" to MoodProfile(minDanceability = 0.5f, minValence = 0.4f), // Simplified
        "Electronic" to MoodProfile(minEnergy = 0.5f, minDanceability = 0.5f), // Simplified
        "Hip Hop" to MoodProfile(minEnergy = 0.4f, minDanceability = 0.6f), // Simplified
        // Classical and Jazz are very diverse, relying on genre tags might be better than simple audio features.
        // For now, let's keep them simple or omit if too broad for this feature-based filtering.
        "Classical" to MoodProfile(minInstrumentalness = 0.7f, maxEnergy = 0.5f),
        "Jazz" to MoodProfile(minInstrumentalness = 0.3f, maxEnergy = 0.6f) // Very broad
    )


    // Old moodKeywords map for reference during transition if needed or for fallback
    private val simpleMoodKeywords = mapOf(
        "happy" to "Happy", // Valence: High, Energy: Moderate to High
        "upbeat" to "Happy",
        "sad" to "Sad",
        "gloomy" to "Sad",
        "reflective" to "Sad",
        "energetic" to "Energetic",
        "workout" to "Energetic",
        "pumped" to "Energetic",
        "chill" to "Chill",
        "relaxed" to "Chill",
        "calm" to "Chill",
        "rock" to "Rock",
        "pop" to "Pop",
        "electronic" to "Electronic",
        "classical" to "Classical",
        "focus" to "Focus",
        "study" to "Focus"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        moodConfirmationLayout = findViewById(R.id.moodConfirmationLayout)
        yesMoodButton = findViewById(R.id.yesMoodButton)
        noMoodButton = findViewById(R.id.noMoodButton)

        // Setup RecyclerView
        chatAdapter = ChatMessageAdapter(messagesList)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = chatAdapter

        // Initial UI state for mood confirmation
        showMoodConfirmation(false)

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = messageText,
                    timestamp = System.currentTimeMillis(),
                    isUserMessage = true
                )
                messagesList.add(userMessage)
                chatAdapter.notifyItemInserted(messagesList.size - 1)
                chatRecyclerView.scrollToPosition(messagesList.size - 1)

                // Keyword detection
                var detectedMoodOrGenre: String? = null
                val lowercasedMessage = messageText.lowercase()
                for ((keyword, mood) in moodKeywords) {
                    if (lowercasedMessage.contains(keyword)) {
                        detectedMoodOrGenre = mood
                        break
                    }
                }

                Log.d("ChatActivity", "User message: $messageText, Detected mood/genre: $detectedMoodOrGenre")

                if (detectedMoodOrGenre != null) {
                    val botResponseText = "Okay, I see you're in the mood for some $detectedMoodOrGenre music!"
                    val botMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = botResponseText,
                        timestamp = System.currentTimeMillis() + 1, // Ensure slightly different timestamp
                        isUserMessage = false
                    )
                    messagesList.add(botMessage)
                    chatAdapter.notifyItemInserted(messagesList.size - 1)
                    chatRecyclerView.scrollToPosition(messagesList.size - 1)

                    // Show mood confirmation UI instead of navigating directly
                    showMoodConfirmation(true, detectedMoodOrGenre)
                }
                messageEditText.text.clear()
            }
        }

        yesMoodButton.setOnClickListener {
            showMoodConfirmation(false)
            pendingMoodForConfirmation?.let { mood ->
                val suggestedTracks = getTrackSuggestions(mood)
                Log.d("ChatActivity", "Confirmed mood: $mood, Suggested tracks count: ${suggestedTracks.size}")
                val intent = Intent(this, SuggestedTracksActivity::class.java).apply {
                    putExtra("DETECTED_MOOD", mood)
                    putParcelableArrayListExtra("SUGGESTED_TRACKS", ArrayList(suggestedTracks))
                }
                startActivity(intent)
            }
        }

        noMoodButton.setOnClickListener {
            showMoodConfirmation(false)
            val clarificationMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "My mistake! Could you please tell me more specifically what kind of music you're looking for?",
                timestamp = System.currentTimeMillis(),
                isUserMessage = false
            )
            messagesList.add(clarificationMessage)
            chatAdapter.notifyItemInserted(messagesList.size - 1)
            chatRecyclerView.scrollToPosition(messagesList.size - 1)
        }


        // Add a few dummy messages for testing
        messagesList.add(ChatMessage(UUID.randomUUID().toString(),"Hello! How can I help you with Spotify today?", System.currentTimeMillis(),false))
        chatAdapter.notifyDataSetChanged()
    }

    private fun showMoodConfirmation(show: Boolean, detectedMood: String? = null) {
        if (show && detectedMood != null) {
            pendingMoodForConfirmation = detectedMood
            moodConfirmationLayout.visibility = View.VISIBLE
            messageEditText.isEnabled = false
            sendButton.isEnabled = false
        } else {
            pendingMoodForConfirmation = null
            moodConfirmationLayout.visibility = View.GONE
            messageEditText.isEnabled = true
            sendButton.isEnabled = true
        }
    }

    private fun getTrackSuggestions(mood: String): List<SpotifyTrackFull> {
        val suggestions = mutableListOf<SpotifyTrackFull>()
        val lowercasedMood = mood.lowercase()

        // Keywords associated with the general mood term from moodKeywords map
        val moodRelatedKeywords = moodKeywords.entries
            .filter { it.value.equals(mood, ignoreCase = true) }
            .map { it.key.lowercase() }
            .toMutableSet()
        moodRelatedKeywords.add(lowercasedMood)

        val targetProfile = moodProfiles[mood] // Get the MoodProfile for the detected mood

        // Primary filtering using Audio Features if a profile exists
        if (targetProfile != null) {
            Log.d("ChatActivity", "Using Audio Feature profile for mood: $mood")
            for (track in UserMusicCache.userSavedTracks) {
                if (suggestions.size >= 15) break // Limit total suggestions

                UserMusicCache.audioFeaturesCache[track.id]?.let { audioFeatures ->
                    var matchesAllCriteria = true

                    targetProfile.minValence?.let { if (audioFeatures.valence < it) matchesAllCriteria = false }
                    targetProfile.maxValence?.let { if (audioFeatures.valence > it) matchesAllCriteria = false }
                    targetProfile.minEnergy?.let { if (audioFeatures.energy < it) matchesAllCriteria = false }
                    targetProfile.maxEnergy?.let { if (audioFeatures.energy > it) matchesAllCriteria = false }
                    targetProfile.minDanceability?.let { if (audioFeatures.danceability < it) matchesAllCriteria = false }
                    targetProfile.maxDanceability?.let { if (audioFeatures.danceability > it) matchesAllCriteria = false }
                    targetProfile.minTempo?.let { if (audioFeatures.tempo < it) matchesAllCriteria = false }
                    targetProfile.maxTempo?.let { if (audioFeatures.tempo > it) matchesAllCriteria = false }
                    targetProfile.minInstrumentalness?.let { if (audioFeatures.instrumentalness < it) matchesAllCriteria = false }
                    targetProfile.maxInstrumentalness?.let { if (audioFeatures.instrumentalness > it) matchesAllCriteria = false }
                    targetProfile.minSpeechiness?.let { if (audioFeatures.speechiness < it) matchesAllCriteria = false }
                    targetProfile.maxSpeechiness?.let { if (audioFeatures.speechiness > it) matchesAllCriteria = false }
                    targetProfile.minAcousticness?.let { if (audioFeatures.acousticness < it) matchesAllCriteria = false }
                    targetProfile.maxAcousticness?.let { if (audioFeatures.acousticness > it) matchesAllCriteria = false }
                    targetProfile.minLiveness?.let { if (audioFeatures.liveness < it) matchesAllCriteria = false }
                    targetProfile.maxLiveness?.let { if (audioFeatures.liveness > it) matchesAllCriteria = false }

                    if (matchesAllCriteria) {
                        suggestions.add(track)
                    }
                }
            }
            Log.d("ChatActivity", "Found ${suggestions.size} tracks based on audio features for $mood.")
        }

        // Fallback or secondary: Keyword search if few/no results from audio features, or if no profile
        if (suggestions.size < 5 || targetProfile == null) {
            Log.d("ChatActivity", "Fallback to keyword search for mood: $mood. Current suggestions: ${suggestions.size}")
            val keywordSuggestions = mutableListOf<SpotifyTrackFull>()
            for (track in UserMusicCache.userSavedTracks) {
                if (keywordSuggestions.size + suggestions.size >= 15) break

                val trackNameLower = track.name.lowercase()
                val albumNameLower = track.album.name.lowercase()
                val artistNamesLower = track.artists.joinToString(" ") { it.name.lowercase() }

                for (keyword in moodRelatedKeywords) {
                    if (trackNameLower.contains(keyword) ||
                        albumNameLower.contains(keyword) ||
                        artistNamesLower.contains(keyword)) {
                        keywordSuggestions.add(track)
                        break
                    }
                }
            }
            suggestions.addAll(keywordSuggestions) // Add keyword-based suggestions
        }

        return suggestions.distinctBy { it.id }.take(15) // Ensure unique tracks and limit final count
    }
}

class ChatMessageAdapter(private val messages: List<com.example.myapp.models.ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        // The root view of item_chat_message.xml is expected to be a LinearLayout or FrameLayout
        val messageContainer: ViewGroup = itemView as ViewGroup
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text

        val layoutParams = holder.messageTextView.layoutParams as LinearLayout.LayoutParams

        if (message.isUserMessage) {
            layoutParams.gravity = Gravity.END
            holder.messageTextView.setBackgroundResource(R.drawable.user_message_background)
        } else {
            layoutParams.gravity = Gravity.START
            holder.messageTextView.setBackgroundResource(R.drawable.bot_message_background)
        }
        holder.messageTextView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size
}
