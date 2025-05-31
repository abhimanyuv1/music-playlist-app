package com.example.myapp.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.example.myapp.R
import com.example.myapp.models.ChatMessage
import com.example.myapp.models.spotify_api.SpotifyTrackFull
import com.example.myapp.utils.UserMusicCache
import java.util.UUID
import kotlin.collections.ArrayList

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

    private val moodKeywords = mapOf(
        "happy" to "Happy",
        "joyful" to "Happy",
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
        moodRelatedKeywords.add(lowercasedMood) // Add the mood itself as a keyword (e.g. "Happy" -> "happy")


        for (track in UserMusicCache.userSavedTracks) {
            if (suggestions.size >= 10) break // Limit suggestions

            val trackNameLower = track.name.lowercase()
            val albumNameLower = track.album.name.lowercase()
            val artistNamesLower = track.artists.joinToString(" ") { it.name.lowercase() }

            for (keyword in moodRelatedKeywords) {
                if (trackNameLower.contains(keyword) ||
                    albumNameLower.contains(keyword) ||
                    artistNamesLower.contains(keyword)) {
                    suggestions.add(track)
                    break // Add track once if any keyword matches
                }
            }
        }
        // TODO: Later, could also check UserMusicCache.userPlaylists
        // For playlists, if playlist.name.lowercase().contains(lowercasedMood),
        // you might add a placeholder or fetch its tracks (more complex).
        // For now, focusing on saved tracks.

        return suggestions.distinctBy { it.id } // Ensure unique tracks
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

        if (message.isUserMessage) {
            holder.messageContainer.gravity = Gravity.END
            holder.messageTextView.setBackgroundResource(R.drawable.user_message_background)
        } else {
            holder.messageContainer.gravity = Gravity.START
            holder.messageTextView.setBackgroundResource(R.drawable.bot_message_background)
        }
    }

    override fun getItemCount(): Int = messages.size
}
