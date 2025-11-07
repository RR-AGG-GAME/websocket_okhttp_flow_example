package com.example.websocketflow.audiotranscription.viewmodel

import app.cash.turbine.test
import com.example.websocketflow.audiotranscription.manager.SpeechRecognitionManager
import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private lateinit var mockManager: MockSpeechRecognitionManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockManager = MockSpeechRecognitionManager()
        viewModel = AudioTranscriptionViewModel(mockManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState is TranscriptionUiState.Idle)
            assertEquals("", initialState.inputText)
        }
    }

    @Test
    fun `updateInputText should update inputText`() = runTest {
        val testText = "Hello World"
        
        viewModel.updateInputText(testText)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(testText, state.inputText)
            assertTrue(state.inputText.isNotEmpty())
        }
    }

    @Test
    fun `updateInputText with empty string should set to Idle`() = runTest {
        viewModel.updateInputText("Hello")
        advanceUntilIdle()
        
        viewModel.updateInputText("")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `clearError should clear transcription but keep inputText`() = runTest {
        viewModel.updateInputText("Test transcription")
        advanceUntilIdle()

        viewModel.clearError()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
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
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `sendMessage with non-empty input should clear inputText`() = runTest {
        viewModel.updateInputText("Test message")
        advanceUntilIdle()

        viewModel.sendMessage()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Simulate an error state
        mockManager.setState(SpeechRecognitionState.Error("Test error"))
        advanceUntilIdle()
        
        viewModel.clearError()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state is TranscriptionUiState.Error)
        }
    }

    @Test
    fun `stopRecording should call manager stopRecording`() = runTest {
        viewModel.stopRecording()
        advanceUntilIdle()
        
        assertTrue(mockManager.stopRecordingCalled)
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
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `clearError should not affect inputText`() = runTest {
        viewModel.updateInputText("User typed text")
        advanceUntilIdle()

        viewModel.clearError()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("User typed text", state.inputText)
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
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `inputText should be empty when state is Idle`() = runTest {
        viewModel.updateInputText("Test")
        advanceUntilIdle()
        
        viewModel.updateInputText("")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `inputText should not be empty when text is entered`() = runTest {
        viewModel.updateInputText("Some text")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.inputText.isNotEmpty())
        }
    }

    // Mock implementation for testing
    private class MockSpeechRecognitionManager : SpeechRecognitionManager {
        private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
        override val state: StateFlow<SpeechRecognitionState> = _state

        var stopRecordingCalled = false
        var startRecordingCalled = false
        var clearTranscriptionCalled = false
        var destroyCalled = false

        fun setState(newState: SpeechRecognitionState) {
            _state.value = newState
        }

        override fun startRecording() {
            startRecordingCalled = true
        }

        override fun stopRecording() {
            stopRecordingCalled = true
        }

        override fun clearTranscription() {
            clearTranscriptionCalled = true
            _state.value = SpeechRecognitionState.Idle
        }

        override fun destroy() {
            destroyCalled = true
        }
    }
}

