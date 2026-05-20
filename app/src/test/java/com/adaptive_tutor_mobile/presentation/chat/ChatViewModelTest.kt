package com.adaptive_tutor_mobile.presentation.chat

import com.adaptive_tutor_mobile.data.remote.api.ChatApi
import com.adaptive_tutor_mobile.data.remote.dto.ChatResponse
import com.adaptive_tutor_mobile.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chatApi: ChatApi = mockk()
    private fun viewModel() = ChatViewModel(chatApi)

    private fun okResponse(answer: String) = Response.success(ChatResponse(answer = answer))
    private fun errResponse(code: Int): Response<ChatResponse> = Response.error(
        code, "err".toResponseBody("text/plain".toMediaType())
    )

    // ── Stare inițială ────────────────────────────────────────────────────────

    @Test
    fun `initial messages list is empty`() {
        assertTrue(viewModel().messages.value.isEmpty())
    }

    @Test
    fun `initial isLoading is false`() {
        assertFalse(viewModel().isLoading.value)
    }

    // ── Mesaje ignorate ───────────────────────────────────────────────────────

    @Test
    fun `blank text is ignored`() = runTest {
        val vm = viewModel()
        vm.sendMessage("   ")
        advanceUntilIdle()
        assertTrue(vm.messages.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `empty string is ignored`() = runTest {
        val vm = viewModel()
        vm.sendMessage("")
        advanceUntilIdle()
        assertTrue(vm.messages.value.isEmpty())
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun `success adds user bubble then AI bubble`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("Bună ziua!")
        val vm = viewModel()
        vm.sendMessage("Salut")
        advanceUntilIdle()

        val msgs = vm.messages.value
        assertEquals(2, msgs.size)
        assertTrue(msgs[0].isUser)
        assertEquals("Salut", msgs[0].content)
        assertFalse(msgs[1].isUser)
        assertEquals("Bună ziua!", msgs[1].content)
        assertFalse(msgs[1].isLoading)
    }

    @Test
    fun `input is trimmed before sending`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("ok")
        val vm = viewModel()
        vm.sendMessage("  Salut  ")
        advanceUntilIdle()
        assertEquals("Salut", vm.messages.value[0].content)
    }

    @Test
    fun `no loading placeholder in list after success`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("ok")
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertTrue(vm.messages.value.none { it.isLoading })
    }

    @Test
    fun `second message after first success sends correctly`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("ok")
        val vm = viewModel()
        vm.sendMessage("msg1")
        advanceUntilIdle()
        vm.sendMessage("msg2")
        advanceUntilIdle()
        assertEquals(4, vm.messages.value.size)
    }

    // ── Erori și fallback ─────────────────────────────────────────────────────

    @Test
    fun `HTTP error shows error code in AI bubble`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns errResponse(503)
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertEquals(2, vm.messages.value.size)
        assertTrue(vm.messages.value[1].content.contains("503"))
    }

    @Test
    fun `exception shows connection error in AI bubble`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } throws RuntimeException("timeout")
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertEquals(2, vm.messages.value.size)
        assertTrue(vm.messages.value[1].content.contains("serverul"))
    }

    @Test
    fun `null body shows fallback message`() = runTest {
        @Suppress("UNCHECKED_CAST")
        coEvery { chatApi.sendMessage(any(), any()) } returns
                (Response.success(null) as Response<ChatResponse>)
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertEquals(
            "Nu am primit un răspuns valid de la server.",
            vm.messages.value.last().content
        )
    }

    @Test
    fun `blank answer shows fallback message`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("   ")
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertEquals(
            "Nu am primit un răspuns valid de la server.",
            vm.messages.value.last().content
        )
    }

    @Test
    fun `no loading placeholder after HTTP error`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns errResponse(500)
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertTrue(vm.messages.value.none { it.isLoading })
    }

    @Test
    fun `no loading placeholder after exception`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } throws RuntimeException()
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertTrue(vm.messages.value.none { it.isLoading })
    }

    // ── Loading gate ──────────────────────────────────────────────────────────

    @Test
    fun `send while loading is ignored`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("R")
        val vm = viewModel()
        vm.sendMessage("primul")
        vm.sendMessage("al doilea") // ignored — loading is true
        advanceUntilIdle()
        assertEquals(2, vm.messages.value.size)
        assertEquals("primul", vm.messages.value[0].content)
    }

    @Test
    fun `isLoading resets to false after success`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("ok")
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `isLoading resets to false after HTTP error`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns errResponse(500)
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `isLoading resets to false after exception`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } throws RuntimeException()
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
    }

    // ── Limita de mesaje (MAX_MESSAGES = 8) ───────────────────────────────────

    @Test
    fun `messages list stays at or below 8 after overflow`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("ok")
        val vm = viewModel()
        // 5 trimiteri × 2 mesaje = 10, trebuie trunchiată la 8
        repeat(5) { i ->
            vm.sendMessage("msg$i")
            advanceUntilIdle()
        }
        assertTrue("Expected <= 8, got ${vm.messages.value.size}", vm.messages.value.size <= 8)
    }

    @Test
    fun `oldest messages removed first when cap exceeded`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("ok")
        val vm = viewModel()
        repeat(5) { i ->
            vm.sendMessage("msg$i")
            advanceUntilIdle()
        }
        // Ultimele mesaje trebuie să fie prezente
        val contents = vm.messages.value.map { it.content }
        assertTrue(contents.contains("msg4") || contents.contains("ok"))
    }

    // ── clearChat ─────────────────────────────────────────────────────────────

    @Test
    fun `clearChat empties messages`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("R")
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        vm.clearChat()
        assertTrue(vm.messages.value.isEmpty())
    }

    @Test
    fun `clearChat resets isLoading to false`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("R")
        val vm = viewModel()
        vm.sendMessage("test")
        advanceUntilIdle()
        vm.clearChat()
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `after clearChat new message starts fresh conversation`() = runTest {
        coEvery { chatApi.sendMessage(any(), any()) } returns okResponse("R")
        val vm = viewModel()
        vm.sendMessage("primul")
        advanceUntilIdle()
        vm.clearChat()
        vm.sendMessage("noul mesaj")
        advanceUntilIdle()
        assertEquals(2, vm.messages.value.size)
        assertEquals("noul mesaj", vm.messages.value[0].content)
    }
}
