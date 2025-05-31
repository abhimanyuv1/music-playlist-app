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
import android.widget.LinearLayout
import com.example.myapp.R
import com.example.myapp.models.ChatMessage
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messagesList = mutableListOf<com.example.myapp.models.ChatMessage>()

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

        // Setup RecyclerView
        chatAdapter = ChatMessageAdapter(messagesList)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true // To keep chat at the bottom, new messages push old ones up
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = chatAdapter

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

                    // Navigate to SuggestedTracksActivity
                    val intent = Intent(this, SuggestedTracksActivity::class.java)
                    intent.putExtra("DETECTED_MOOD", detectedMoodOrGenre)
                    startActivity(intent)
                }
                messageEditText.text.clear()
            }
        }

        // Add a few dummy messages for testing
        messagesList.add(ChatMessage(UUID.randomUUID().toString(),"Hello! How can I help you with Spotify today?", System.currentTimeMillis(),false))
        // messagesList.add(ChatMessage(UUID.randomUUID().toString(),"Can you find some good workout music?", System.currentTimeMillis(),true))
        chatAdapter.notifyDataSetChanged()
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
