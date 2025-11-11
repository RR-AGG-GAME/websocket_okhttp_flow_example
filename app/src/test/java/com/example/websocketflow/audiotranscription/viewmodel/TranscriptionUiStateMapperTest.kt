package com.example.websocketflow.audiotranscription.viewmodel

import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TranscriptionUiStateMapperTest {

    private val defaultIdleState = TranscriptionUiState.Idle()
    private val defaultRecordingState = TranscriptionUiState.Recording()
    private val defaultTranscribingState = TranscriptionUiState.Transcribing("existing text")
    private val defaultErrorState = TranscriptionUiState.Error("error message", "existing text")

    @Test
    fun `map Idle state should return Idle with preserved inputText`() {
        // Arrange
        val recognitionState = SpeechRecognitionState.Idle
        val currentState = defaultIdleState.copy(inputText = "existing text")

        // Act
        val result = TranscriptionUiStateMapper.map(recognitionState, currentUiState = currentState)

        // Assert
        assertTrue(result is TranscriptionUiState.Idle)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `map Ready state should return Idle with preserved inputText`() {
        // Arrange
        val recognitionState = SpeechRecognitionState.Ready("final transcription")
        val currentState = defaultRecordingState.copy(inputText = "existing text")

        // Act
        val result = TranscriptionUiStateMapper.map(recognitionState, currentUiState = currentState)

        // Assert
        assertTrue(result is TranscriptionUiState.Idle)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `map Recording state should return Recording with preserved inputText`() {
        // Arrange
        val recognitionState = SpeechRecognitionState.Recording
        val currentState = defaultIdleState.copy(inputText = "existing text")

        // Act
        val result = TranscriptionUiStateMapper.map(recognitionState, currentUiState = currentState)

        // Assert
        assertTrue(result is TranscriptionUiState.Recording)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `map Transcribing state should return Transcribing with transcription result`() {
        // Arrange
        val recognitionState = SpeechRecognitionState.Transcribing("partial transcription")
        val currentState = defaultRecordingState.copy(inputText = "old text")

        // Act
        val result = TranscriptionUiStateMapper.map(recognitionState, currentUiState = currentState)

        // Assert
        assertTrue(result is TranscriptionUiState.Transcribing)
        assertEquals("partial transcription", result.inputText)
    }

    @Test
    fun `map Error state should return Error with error message and preserved inputText`() {
        // Arrange
        val recognitionState = SpeechRecognitionState.Error("Test error message")
        val currentState = defaultTranscribingState

        // Act
        val result = TranscriptionUiStateMapper.map(recognitionState, currentUiState = currentState)

        // Assert
        assertTrue(result is TranscriptionUiState.Error)
        val errorState = result as TranscriptionUiState.Error
        assertEquals("Test error message", errorState.errorMessage)
        assertEquals("existing text", errorState.inputText)
    }

    @Test
    fun `updateInput on Idle should return Idle with new text`() {
        // Arrange
        val state = defaultIdleState.copy(inputText = "old text")
        val newText = "new text"

        // Act
        val result = TranscriptionUiStateMapper.updateInput(state, newText)

        // Assert
        assertTrue(result is TranscriptionUiState.Idle)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput on Recording should return Recording with new text`() {
        // Arrange
        val state = defaultRecordingState.copy(inputText = "old text")
        val newText = "new text"

        // Act
        val result = TranscriptionUiStateMapper.updateInput(state, newText)

        // Assert
        assertTrue(result is TranscriptionUiState.Recording)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput on Transcribing should return Transcribing with new text`() {
        // Arrange
        val state = defaultTranscribingState.copy(inputText = "old text")
        val newText = "new text"

        // Act
        val result = TranscriptionUiStateMapper.updateInput(state, newText)

        // Assert
        assertTrue(result is TranscriptionUiState.Transcribing)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput on Error should return Error with same error message and new text`() {
        // Arrange
        val state = defaultErrorState.copy(inputText = "old text")
        val newText = "new text"

        // Act
        val result = TranscriptionUiStateMapper.updateInput(state, newText)

        // Assert
        assertTrue(result is TranscriptionUiState.Error)
        val errorState = result as TranscriptionUiState.Error
        assertEquals("error message", errorState.errorMessage)
        assertEquals(newText, errorState.inputText)
    }

    @Test
    fun `updateInput with empty string should work correctly`() {
        // Arrange
        val state = defaultTranscribingState.copy(inputText = "some text")

        // Act
        val result = TranscriptionUiStateMapper.updateInput(state, "")

        // Assert
        assertTrue(result is TranscriptionUiState.Transcribing)
        assertEquals("", result.inputText)
    }

    @Test
    fun `map should preserve inputText across all state transitions`() {
        // Arrange
        val preservedText = "preserved text"
        val recognitionStates = listOf(
            SpeechRecognitionState.Idle,
            SpeechRecognitionState.Recording,
            SpeechRecognitionState.Error("error")
        )
        var currentState: TranscriptionUiState = defaultIdleState.copy(inputText = preservedText)

        // Act & Assert
        recognitionStates.forEach { recognitionState ->
            currentState = TranscriptionUiStateMapper.map(recognitionState, currentUiState = currentState)
            assertEquals(preservedText, currentState.inputText)
        }
    }
}

