package com.adaptive_tutor_mobile.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.BuildConfig
import com.adaptive_tutor_mobile.data.remote.api.ChatApi
import com.adaptive_tutor_mobile.data.remote.dto.ChatContextData
import com.adaptive_tutor_mobile.data.remote.dto.ChatHistoryItem
import com.adaptive_tutor_mobile.data.remote.dto.ChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ── UI model (ce se afișează în bule) ────────────────────────────────────────

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val isLoading: Boolean = false  // true pentru placeholder-ul de "se încarcă"
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatApi: ChatApi
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Istoricul trimis la API — conține doar schimburile anterioare, nu mesajul curent
    private val conversationHistory = mutableListOf<ChatHistoryItem>()

    private companion object {
        const val LOADING_MSG_ID = "msg_loading_placeholder"
    }

    /**
     * Trimite un mesaj nou. Dacă e deja în curs o cerere, ignoră apelul.
     *
     * @param text        Textul scris de utilizator.
     * @param page        Pagina curentă (folosită în `context` pentru AI).
     * @param userType    Tipul utilizatorului ("student", "teacher", etc.).
     */
    fun sendMessage(
        text: String,
        page: String = "student-dashboard",
        userType: String = "student"
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isLoading.value) return

        // Adaugă bula utilizatorului + placeholder de loading imediat în UI
        val userMsg = ChatMessage(content = trimmed, isUser = true)
        val loadingMsg = ChatMessage(id = LOADING_MSG_ID, content = "", isUser = false, isLoading = true)
        _messages.value = _messages.value + userMsg + loadingMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val request = ChatRequest(
                    message = trimmed,
                    history = conversationHistory.toList(),
                    context = ChatContextData(page = page, userType = userType)
                )
                val response = chatApi.sendMessage(
                    apiKey = BuildConfig.AI_API_KEY,
                    request = request
                )

                if (response.isSuccessful) {
                    val aiText = response.body()?.response
                        ?.takeIf { it.isNotBlank() }
                        ?: "Nu am primit un răspuns valid de la server."

                    // Actualizează istoricul cu schimbul tocmai efectuat
                    conversationHistory.add(ChatHistoryItem("user", trimmed))
                    conversationHistory.add(ChatHistoryItem("assistant", aiText))

                    replaceLoadingWith(ChatMessage(content = aiText, isUser = false))
                } else {
                    val code = response.code()
                    replaceLoadingWith(
                        ChatMessage(
                            content = "Eroare $code. Încearcă din nou.",
                            isUser = false
                        )
                    )
                }
            } catch (e: Exception) {
                replaceLoadingWith(
                    ChatMessage(
                        content = "Nu s-a putut contacta serverul. Verifică conexiunea la internet.",
                        isUser = false
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Golește istoricul — util dacă utilizatorul vrea să înceapă o nouă conversație. */
    fun clearChat() {
        _messages.value = emptyList()
        conversationHistory.clear()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun replaceLoadingWith(msg: ChatMessage) {
        _messages.value = _messages.value
            .filterNot { it.id == LOADING_MSG_ID }
            .plus(msg)
    }
}
