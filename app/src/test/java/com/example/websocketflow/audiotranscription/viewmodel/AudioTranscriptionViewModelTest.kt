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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            skipItems(1) // Skip initial value
            viewModel.updateInputText(testText)
            advanceUntilIdle()

            // Assert
            val state = awaitItem()
            assertEquals(testText, state.inputText)
            assertTrue(state.inputText.isNotEmpty())
        }
    }

    @Test
    fun `updateInputText with empty string should set to Idle`() = runTest {
        // Arrange
        viewModel.updateInputText("Hello")
        advanceUntilIdle()

        // Act
        viewModel.updateInputText("")
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `clearError should clear transcription and inputText`() = runTest {
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
    fun `sendMessage with empty input should not change state`() = runTest {
        // Act
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `sendMessage with non-empty input should clear inputText`() = runTest {
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
            assertTrue(state is TranscriptionUiState.Idle)
        }
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Arrange
        mockManager.setState(defaultErrorState.copy(message = "Test error"))
        advanceUntilIdle()

        // Act
        viewModel.clearError()
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state is TranscriptionUiState.Error)
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

    @Test
    fun `multiple updateInputText calls should update state correctly`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // Skip initial value
            viewModel.updateInputText("First")
            advanceUntilIdle()
            awaitItem() // Wait for first update
            
            viewModel.updateInputText("Second")
            advanceUntilIdle()
            awaitItem() // Wait for second update
            
            viewModel.updateInputText("Third")
            advanceUntilIdle()
            
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
    fun `clearError should clear inputText`() = runTest {
        viewModel.updateInputText("User typed text")
        advanceUntilIdle()

        viewModel.clearError()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
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
        viewModel.uiState.test {
            skipItems(1) // Skip initial value
            viewModel.updateInputText("Some text")
            advanceUntilIdle()
            
            val state = awaitItem()
            assertTrue(state.inputText.isNotEmpty())
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
    fun `startRecording should transition to Recording state`() = runTest {
        // Arrange
        mockManager.setState(defaultRecordingState)
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            skipItems(1) // Skip initial value
            val state = awaitItem()
            assertTrue(state is TranscriptionUiState.Recording)
        }
    }

    @Test
    fun `Transcribing state should update inputText`() = runTest {
        // Arrange & Assert
        viewModel.uiState.test {
            awaitItem() // Wait for initial state
            val recognitionState = defaultTranscribingState.copy(transcriptionResult = "partial transcription")
            mockManager.setState(recognitionState)
            advanceUntilIdle()
            
            val state = awaitItem()
            assertTrue(actual = state is TranscriptionUiState.Transcribing)
            assertEquals(expected = "partial transcription", actual = state.inputText)
        }
    }

    @Test
    fun `Ready state should update inputText`() = runTest {
        // Arrange
        val recognitionState = defaultReadyState.copy(transcriptionResult = "final transcription")
        mockManager.setState(recognitionState)
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            skipItems(1) // Skip initial value
            val state = awaitItem()
            assertEquals("final transcription", state.inputText)
        }
    }

    @Test
    fun `Error state should show error message`() = runTest {
        // Arrange
        val recognitionState = defaultErrorState.copy(message = "Test error message")
        mockManager.setState(recognitionState)
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            skipItems(1) // Skip initial value
            val state = awaitItem()
            assertTrue(state is TranscriptionUiState.Error)
            assertEquals("Test error message", state.errorMessage)
        }
    }

    @Test
    fun `updateInputText during Recording should not update`() = runTest {
        // Arrange & Act & Assert
        viewModel.uiState.test {
            awaitItem() // Wait for initial state
            mockManager.setState(defaultRecordingState)
            advanceUntilIdle()
            awaitItem() // Wait for Recording state
            
            viewModel.updateInputText("Should not update")
            advanceUntilIdle()
            
            val state = awaitItem()
            assertTrue(actual = state is TranscriptionUiState.Recording)
            assertEquals(expected = "", actual = state.inputText)
        }
    }

    @Test
    fun `sendMessage during Recording should stop recording`() = runTest {
        // Arrange
        viewModel.updateInputText("Test message")
        advanceUntilIdle()
        
        viewModel.uiState.test {
            awaitItem() // Wait for initial state
            mockManager.setState(defaultRecordingState)
            advanceUntilIdle()
            awaitItem() // Wait for Recording state
            
            mockManager.stopRecordingCalled = false
            mockManager.clearTranscriptionCalled = false

            // Act
            viewModel.sendMessage()
            advanceUntilIdle()

            // Assert
            assertTrue(actual = mockManager.stopRecordingCalled)
            assertTrue(actual = mockManager.clearTranscriptionCalled)
        }
    }

    @Test
    fun `sendMessage during Transcribing should stop recording`() = runTest {
        // Arrange
        viewModel.updateInputText("Test message")
        advanceUntilIdle()
        
        viewModel.uiState.test {
            awaitItem() // Wait for initial state
            mockManager.setState(defaultTranscribingState.copy(transcriptionResult = "transcription"))
            advanceUntilIdle()
            awaitItem() // Wait for Transcribing state
            
            mockManager.stopRecordingCalled = false
            mockManager.clearTranscriptionCalled = false

            // Act
            viewModel.sendMessage()
            advanceUntilIdle()

            // Assert
            assertTrue(actual = mockManager.stopRecordingCalled)
            assertTrue(actual = mockManager.clearTranscriptionCalled)
        }
    }

    @Test
    fun `sendMessage when Idle should not call stopRecording`() = runTest {
        // Arrange
        viewModel.updateInputText("Test message")
        advanceUntilIdle()
        mockManager.setState(defaultIdleState)
        advanceUntilIdle()
        mockManager.stopRecordingCalled = false

        // Act
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert
        assertFalse(mockManager.stopRecordingCalled)
        assertTrue(mockManager.clearTranscriptionCalled)
    }


    @Test
    fun `state transitions from Idle to Recording to Transcribing`() = runTest {
        // Arrange & Act
        viewModel.uiState.test {
            skipItems(1) // Skip initial value

            mockManager.setState(defaultRecordingState)
            advanceUntilIdle()
            val recordingState = awaitItem()

            mockManager.setState(defaultTranscribingState.copy(transcriptionResult = "test"))
            advanceUntilIdle()
            val transcribingState = awaitItem()

            // Assert
            assertTrue(recordingState is TranscriptionUiState.Recording)
            assertTrue(transcribingState is TranscriptionUiState.Transcribing)
            assertEquals("test", transcribingState.inputText)
        }
    }

    @Test
    fun `multiple Transcribing updates should update inputText`() = runTest {
        // Arrange & Act & Assert
        viewModel.uiState.test {
            awaitItem() // Wait for initial state
            
            mockManager.setState(defaultTranscribingState.copy(transcriptionResult = "first"))
            advanceUntilIdle()
            val firstState = awaitItem()
            assertTrue(actual = firstState is TranscriptionUiState.Transcribing)
            assertEquals(expected = "first", actual = firstState.inputText)

            mockManager.setState(defaultTranscribingState.copy(transcriptionResult = "second"))
            advanceUntilIdle()
            val secondState = awaitItem()
            assertTrue(actual = secondState is TranscriptionUiState.Transcribing)
            assertEquals(expected = "second", actual = secondState.inputText)
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

