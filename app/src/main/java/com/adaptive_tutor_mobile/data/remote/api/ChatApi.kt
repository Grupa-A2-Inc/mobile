package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.ChatRequest
import com.adaptive_tutor_mobile.data.remote.dto.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatApi {

    @POST("ai/api/v1/chat/customer-support")
    suspend fun sendMessage(
        @Header("X-API-KEY") apiKey: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
