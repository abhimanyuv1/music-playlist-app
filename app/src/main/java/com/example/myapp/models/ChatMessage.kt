package com.example.myapp.models

data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isUserMessage: Boolean
)
