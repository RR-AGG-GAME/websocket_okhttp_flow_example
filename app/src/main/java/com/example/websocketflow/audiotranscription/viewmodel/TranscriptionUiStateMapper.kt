package com.example.websocketflow.audiotranscription.viewmodel

import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState

object TranscriptionUiStateMapper {
    fun map(
        recognitionState: SpeechRecognitionState,
        currentUiState: TranscriptionUiState
    ): TranscriptionUiState = when (recognitionState) {
        is SpeechRecognitionState.Idle -> 
            TranscriptionUiState.Idle(currentUiState.inputText)
        is SpeechRecognitionState.Ready -> 
            // Ready state should use the transcription result, not current inputText
            TranscriptionUiState.Idle(recognitionState.transcriptionResult)
        is SpeechRecognitionState.Recording -> 
            TranscriptionUiState.Recording(currentUiState.inputText)
        is SpeechRecognitionState.Transcribing -> 
            TranscriptionUiState.Transcribing(recognitionState.transcriptionResult)
        is SpeechRecognitionState.Error -> 
            TranscriptionUiState.Error(
                errorMessage = recognitionState.message,
                inputText = currentUiState.inputText
            )
    }
    
    fun updateInput(
        state: TranscriptionUiState,
        text: String
    ): TranscriptionUiState = when (state) {
        is TranscriptionUiState.Idle -> TranscriptionUiState.Idle(text)
        is TranscriptionUiState.Recording -> TranscriptionUiState.Recording(text)
        is TranscriptionUiState.Transcribing -> TranscriptionUiState.Transcribing(text)
        is TranscriptionUiState.Error -> TranscriptionUiState.Error(state.errorMessage, text)
    }
}