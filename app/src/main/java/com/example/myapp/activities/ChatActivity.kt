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
import com.example.myapp.R
import com.example.myapp.models.ChatMessage
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messagesList = mutableListOf<com.example.myapp.models.ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        // Setup RecyclerView
        chatAdapter = ChatMessageAdapter(messagesList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                Log.d("ChatActivity", "Sending message: $messageText")
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = messageText,
                    timestamp = System.currentTimeMillis(),
                    isUserMessage = true
                )
                messagesList.add(userMessage)
                chatAdapter.notifyItemInserted(messagesList.size - 1)
                chatRecyclerView.scrollToPosition(messagesList.size - 1)

                messageEditText.text.clear()

                // TODO: Later, this message will be processed by AI/Spotify
            }
        }

        // Add a few dummy messages for testing
        messagesList.add(ChatMessage(UUID.randomUUID().toString(),"Hello! How can I help you with Spotify today?", System.currentTimeMillis(),false))
        messagesList.add(ChatMessage(UUID.randomUUID().toString(),"Can you find some good workout music?", System.currentTimeMillis(),true))
        chatAdapter.notifyDataSetChanged()
    }
}

class ChatMessageAdapter(private val messages: List<com.example.myapp.models.ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text
        // TODO: Later, differentiate UI for user vs. bot messages
        // e.g., change background, alignment
        // val layoutParams = holder.messageTextView.layoutParams as LinearLayout.LayoutParams
        // if (message.isUserMessage) {
        //     layoutParams.gravity = Gravity.END
        //     holder.messageTextView.setBackgroundResource(R.drawable.user_message_background) // Example drawable
        // } else {
        //     layoutParams.gravity = Gravity.START
        //     holder.messageTextView.setBackgroundResource(R.drawable.bot_message_background) // Example drawable
        // }
        // holder.messageTextView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size
}
