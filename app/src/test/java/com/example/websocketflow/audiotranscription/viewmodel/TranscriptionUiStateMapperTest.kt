package com.example.websocketflow.audiotranscription.viewmodel

import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranscriptionUiStateMapperTest {

    @Test
    fun `map Idle state should return Idle with preserved inputText`() {
        val currentState = TranscriptionUiState.Idle("existing text")
        val result = TranscriptionUiStateMapper.map(SpeechRecognitionState.Idle, currentState)
        
        assertTrue(result is TranscriptionUiState.Idle)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `map Ready state should return Idle with preserved inputText`() {
        val currentState = TranscriptionUiState.Recording("existing text")
        val result = TranscriptionUiStateMapper.map(
            SpeechRecognitionState.Ready("final transcription"),
            currentState
        )
        
        assertTrue(result is TranscriptionUiState.Idle)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `map Recording state should return Recording with preserved inputText`() {
        val currentState = TranscriptionUiState.Idle("existing text")
        val result = TranscriptionUiStateMapper.map(SpeechRecognitionState.Recording, currentState)
        
        assertTrue(result is TranscriptionUiState.Recording)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `map Transcribing state should return Transcribing with transcription result`() {
        val currentState = TranscriptionUiState.Recording("old text")
        val transcription = "partial transcription"
        val result = TranscriptionUiStateMapper.map(
            SpeechRecognitionState.Transcribing(transcription),
            currentState
        )
        
        assertTrue(result is TranscriptionUiState.Transcribing)
        assertEquals(transcription, result.inputText)
    }

    @Test
    fun `map Error state should return Error with error message and preserved inputText`() {
        val currentState = TranscriptionUiState.Transcribing("existing text")
        val errorMessage = "Test error message"
        val result = TranscriptionUiStateMapper.map(
            SpeechRecognitionState.Error(errorMessage),
            currentState
        )
        
        assertTrue(result is TranscriptionUiState.Error)
        assertEquals(errorMessage, result.errorMessage)
        assertEquals("existing text", result.inputText)
    }

    @Test
    fun `updateInput on Idle should return Idle with new text`() {
        val state = TranscriptionUiState.Idle("old text")
        val newText = "new text"
        val result = TranscriptionUiStateMapper.updateInput(state, newText)
        
        assertTrue(result is TranscriptionUiState.Idle)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput on Recording should return Recording with new text`() {
        val state = TranscriptionUiState.Recording("old text")
        val newText = "new text"
        val result = TranscriptionUiStateMapper.updateInput(state, newText)
        
        assertTrue(result is TranscriptionUiState.Recording)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput on Transcribing should return Transcribing with new text`() {
        val state = TranscriptionUiState.Transcribing("old text")
        val newText = "new text"
        val result = TranscriptionUiStateMapper.updateInput(state, newText)
        
        assertTrue(result is TranscriptionUiState.Transcribing)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput on Error should return Error with same error message and new text`() {
        val state = TranscriptionUiState.Error("error message", "old text")
        val newText = "new text"
        val result = TranscriptionUiStateMapper.updateInput(state, newText)
        
        assertTrue(result is TranscriptionUiState.Error)
        assertEquals("error message", result.errorMessage)
        assertEquals(newText, result.inputText)
    }

    @Test
    fun `updateInput with empty string should work correctly`() {
        val state = TranscriptionUiState.Transcribing("some text")
        val result = TranscriptionUiStateMapper.updateInput(state, "")
        
        assertTrue(result is TranscriptionUiState.Transcribing)
        assertEquals("", result.inputText)
    }

    @Test
    fun `map should preserve inputText across all state transitions`() {
        val preservedText = "preserved text"
        val states = listOf(
            SpeechRecognitionState.Idle,
            SpeechRecognitionState.Recording,
            SpeechRecognitionState.Error("error")
        )
        
        var currentState: TranscriptionUiState = TranscriptionUiState.Idle(preservedText)
        states.forEach { recognitionState ->
            currentState = TranscriptionUiStateMapper.map(recognitionState, currentState)
            assertEquals(preservedText, currentState.inputText)
        }
    }
}

