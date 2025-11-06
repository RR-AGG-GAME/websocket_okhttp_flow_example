package com.example.websocketflow.audiotranscription

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AudioTranscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AudioTranscriptionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AudioTranscriptionViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(false, initialState.isRecording)
            assertEquals("", initialState.transcriptionResult)
            assertEquals("", initialState.errorMessage)
            assertEquals("", initialState.inputText)
            assertEquals(false, initialState.isTyping)
        }
    }

    @Test
    fun `updateInputText should update inputText and set isTyping to true`() = runTest {
        val testText = "Hello World"
        
        viewModel.updateInputText(testText)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(testText, state.inputText)
            assertTrue(state.isTyping)
        }
    }

    @Test
    fun `updateInputText with empty string should set isTyping to false`() = runTest {
        viewModel.updateInputText("Hello")
        advanceUntilIdle()
        
        viewModel.updateInputText("")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertFalse(state.isTyping)
        }
    }

    @Test
    fun `clearTranscription should clear transcriptionResult and errorMessage but keep inputText`() = runTest {
        viewModel.updateInputText("Test transcription")
        advanceUntilIdle()

        viewModel.clearTranscription()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.transcriptionResult)
            assertEquals("", state.errorMessage)
            // inputText should remain unchanged
            assertEquals("Test transcription", state.inputText)
        }
    }

    @Test
    fun `sendMessage with empty input should not change state`() = runTest {
        viewModel.sendMessage()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertFalse(state.isTyping)
        }
    }

    @Test
    fun `sendMessage with non-empty input should clear all fields`() = runTest {
        viewModel.updateInputText("Test message")
        advanceUntilIdle()

        viewModel.sendMessage()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertEquals("", state.transcriptionResult)
            assertEquals("", state.errorMessage)
            assertFalse(state.isTyping)
        }
    }

    @Test
    fun `clearError should clear errorMessage`() = runTest {
        viewModel.clearError()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.errorMessage)
        }
    }

    @Test
    fun `stopRecording should set isRecording to false and isTyping to false`() = runTest {
        viewModel.stopRecording()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isRecording)
            assertFalse(state.isTyping)
        }
    }

    @Test
    fun `multiple updateInputText calls should update state correctly`() = runTest {
        viewModel.updateInputText("First")
        advanceUntilIdle()
        
        viewModel.updateInputText("Second")
        advanceUntilIdle()
        
        viewModel.updateInputText("Third")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Third", state.inputText)
            assertTrue(state.isTyping)
        }
    }

    @Test
    fun `sendMessage should handle multiple consecutive calls`() = runTest {
        viewModel.updateInputText("Message 1")
        advanceUntilIdle()
        
        viewModel.sendMessage()
        advanceUntilIdle()
        
        viewModel.updateInputText("Message 2")
        advanceUntilIdle()
        
        viewModel.sendMessage()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertFalse(state.isTyping)
        }
    }

    @Test
    fun `clearTranscription should not affect inputText`() = runTest {
        viewModel.updateInputText("User typed text")
        advanceUntilIdle()

        viewModel.clearTranscription()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("User typed text", state.inputText)
            assertEquals("", state.transcriptionResult)
            assertEquals("", state.errorMessage)
        }
    }

    @Test
    fun `sendMessage should reset state even after multiple updates`() = runTest {
        viewModel.updateInputText("First")
        advanceUntilIdle()
        
        viewModel.updateInputText("Second")
        advanceUntilIdle()
        
        viewModel.updateInputText("Final message")
        advanceUntilIdle()

        viewModel.sendMessage()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertEquals("", state.transcriptionResult)
            assertEquals("", state.errorMessage)
            assertFalse(state.isTyping)
        }
    }

    @Test
    fun `isTyping should be false when inputText is empty`() = runTest {
        viewModel.updateInputText("Test")
        advanceUntilIdle()
        
        viewModel.updateInputText("")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isTyping)
            assertEquals("", state.inputText)
        }
    }

    @Test
    fun `isTyping should be true when inputText is not empty`() = runTest {
        viewModel.updateInputText("Some text")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isTyping)
            assertTrue(state.inputText.isNotEmpty())
        }
    }
}

