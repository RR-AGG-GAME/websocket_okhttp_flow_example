package com.example.websocketflow.audiotranscription.viewmodel

import app.cash.turbine.test
import app.cash.turbine.awaitItem
import app.cash.turbine.skipItems
import com.example.websocketflow.audiotranscription.manager.SpeechRecognitionManager
import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutinesTestExtension::class, InstantExecutorExtension::class)
class AudioTranscriptionViewModelTest {

    private val mockStateFlow = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    private val mockManager: SpeechRecognitionManager = mock {
        whenever(it.state).thenReturn(mockStateFlow.asStateFlow())
    }
    private val viewModel by lazy { AudioTranscriptionViewModel(mockManager) }

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
        mockStateFlow.value = SpeechRecognitionState.Idle
        viewModel.updateInputText("Existing text")
        advanceUntilIdle()

        // Act
        viewModel.startRecording()
        advanceUntilIdle()

        // Assert
        verify(mockManager).startRecording()
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.inputText)
        }
    }

    @Test
    fun `Error state should show error message`() = runTest {
        // Arrange
        mockStateFlow.value = SpeechRecognitionState.Error("Test error message")
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is TranscriptionUiState.Error)
            val uiErrorState = state as TranscriptionUiState.Error
            assertEquals("Test error message", uiErrorState.errorMessage)
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
        // Arrange
        mockStateFlow.value = SpeechRecognitionState.Idle

        // Act
        viewModel.stopRecording()
        advanceUntilIdle()

        // Assert
        verify(mockManager).stopRecording()
    }
}

