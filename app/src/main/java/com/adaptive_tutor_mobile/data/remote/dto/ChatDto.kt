package com.adaptive_tutor_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Request models ────────────────────────────────────────────────────────────

data class ChatHistoryItem(
    val role: String,       // "user" | "assistant"
    val content: String
)

data class ChatContextData(
    val page: String,
    val userType: String
)

data class ChatRequest(
    val message: String,
    val history: List<ChatHistoryItem>,
    val context: ChatContextData
)

// ── Response model ────────────────────────────────────────────────────────────

data class ChatResponse(
    val response: String
)
