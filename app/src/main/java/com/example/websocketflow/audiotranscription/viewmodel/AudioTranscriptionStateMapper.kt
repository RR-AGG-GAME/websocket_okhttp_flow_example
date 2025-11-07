package com.example.websocketflow.audiotranscription.viewmodel

import com.example.websocketflow.audiotranscription.model.AudioTranscriptionState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState

object AudioTranscriptionStateMapper {
    fun map(
        recognitionState: SpeechRecognitionState,
        currentUiState: AudioTranscriptionState
    ): AudioTranscriptionState = when (recognitionState) {
        is SpeechRecognitionState.Idle -> AudioTranscriptionState.Idle
        is SpeechRecognitionState.Recording -> 
            AudioTranscriptionState.Recording(currentUiState.inputText)
        is SpeechRecognitionState.Transcribing -> 
            AudioTranscriptionState.Transcribing(
                recognitionState.transcriptionResult,
                currentUiState.inputText
            )
        is SpeechRecognitionState.Ready -> 
            AudioTranscriptionState.Ready(
                recognitionState.transcriptionResult,
                currentUiState.inputText
            )
        is SpeechRecognitionState.Error -> 
            AudioTranscriptionState.Error(
                recognitionState.message,
                currentUiState.inputText
            )
    }
    
    fun updateInputText(
        state: AudioTranscriptionState,
        inputText: String
    ): AudioTranscriptionState = when (state) {
        is AudioTranscriptionState.Recording -> AudioTranscriptionState.Recording(inputText)
        is AudioTranscriptionState.Transcribing -> 
            AudioTranscriptionState.Transcribing(state.transcriptionResult, inputText)
        is AudioTranscriptionState.Ready -> 
            AudioTranscriptionState.Ready(state.transcriptionResult, inputText)
        is AudioTranscriptionState.Error -> 
            AudioTranscriptionState.Error(state.message, inputText)
        is AudioTranscriptionState.Idle -> AudioTranscriptionState.Idle
    }
}