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
    fun `map Idle should return Idle with preserved inputText`() {
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
    fun `map Recording should return Recording with preserved inputText`() {
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
    fun `map Transcribing should return Transcribing with transcription result`() {
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
    fun `map Error should return Error with error message and preserved inputText`() {
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
    fun `updateInput should update inputText for all states`() {
        // Arrange
        val newText = "new text"

        // Act & Assert - Idle
        val idleResult = TranscriptionUiStateMapper.updateInput(defaultIdleState.copy(inputText = "old"), newText)
        assertTrue(idleResult is TranscriptionUiState.Idle)
        assertEquals(newText, idleResult.inputText)

        // Act & Assert - Recording
        val recordingResult = TranscriptionUiStateMapper.updateInput(defaultRecordingState.copy(inputText = "old"), newText)
        assertTrue(recordingResult is TranscriptionUiState.Recording)
        assertEquals(newText, recordingResult.inputText)

        // Act & Assert - Transcribing
        val transcribingResult = TranscriptionUiStateMapper.updateInput(defaultTranscribingState.copy(inputText = "old"), newText)
        assertTrue(transcribingResult is TranscriptionUiState.Transcribing)
        assertEquals(newText, transcribingResult.inputText)

        // Act & Assert - Error
        val errorResult = TranscriptionUiStateMapper.updateInput(defaultErrorState.copy(inputText = "old"), newText)
        assertTrue(errorResult is TranscriptionUiState.Error)
        val errorState = errorResult as TranscriptionUiState.Error
        assertEquals("error message", errorState.errorMessage)
        assertEquals(newText, errorState.inputText)
    }
}

