package com.example.websocketflow.audiotranscription.viewmodel

import app.cash.turbine.test
import app.cash.turbine.awaitItem
import app.cash.turbine.skipItems
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AudioTranscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AudioTranscriptionViewModel
    private lateinit var mockManager: MockSpeechRecognitionManager

    private val defaultIdleState = SpeechRecognitionState.Idle
    private val defaultRecordingState = SpeechRecognitionState.Recording
    private val defaultTranscribingState = SpeechRecognitionState.Transcribing("partial transcription")
    private val defaultReadyState = SpeechRecognitionState.Ready("final transcription")
    private val defaultErrorState = SpeechRecognitionState.Error("Test error message")

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockManager = MockSpeechRecognitionManager()
        viewModel = AudioTranscriptionViewModel(mockManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        // Arrange & Act
        viewModel.uiState.test {
            val initialState = awaitItem()

            // Assert
            assertTrue(initialState is TranscriptionUiState.Idle)
            assertEquals("", initialState.inputText)
        }
    }

    @Test
    fun `updateInputText should update inputText`() = runTest {
        // Arrange
        val testText = "Hello World"

        // Act
        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateInputText(testText)
            advanceUntilIdle()

            // Assert
            val state = awaitItem()
            assertEquals(testText, state.inputText)
        }
    }

    @Test
    fun `startRecording should clear inputText and call manager`() = runTest {
        // Arrange
        viewModel.updateInputText("Existing text")
        advanceUntilIdle()

        // Act
        viewModel.startRecording()
        advanceUntilIdle()

        // Assert
        assertTrue(mockManager.startRecordingCalled)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
        }
    }

    @Test
    fun `Error state should show error message`() = runTest {
        // Arrange
        mockManager.setState(defaultErrorState.copy(message = "Test error message"))
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is TranscriptionUiState.Error)
            val errorState = state as TranscriptionUiState.Error
            assertEquals("Test error message", errorState.errorMessage)
        }
    }

    @Test
    fun `sendMessage should clear inputText`() = runTest {
        // Arrange
        viewModel.updateInputText("Test message")
        advanceUntilIdle()

        // Act
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
        }
    }

    @Test
    fun `clearError should clear inputText`() = runTest {
        // Arrange
        viewModel.updateInputText("Test transcription")
        advanceUntilIdle()

        // Act
        viewModel.clearError()
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
        }
    }

    @Test
    fun `stopRecording should call manager stopRecording`() = runTest {
        // Act
        viewModel.stopRecording()
        advanceUntilIdle()

        // Assert
        assertTrue(mockManager.stopRecordingCalled)
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

