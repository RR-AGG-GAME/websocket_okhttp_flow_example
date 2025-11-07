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
            AudioTranscriptionState.Transcribing(recognitionState.transcriptionResult)
        is SpeechRecognitionState.Ready -> 
            AudioTranscriptionState.Ready(recognitionState.transcriptionResult)
        is SpeechRecognitionState.Error -> 
            AudioTranscriptionState.Error(
                errorMessage = recognitionState.message,
                inputText = currentUiState.inputText
            )
    }
    
    fun updateInputText(
        state: AudioTranscriptionState,
        inputText: String
    ): AudioTranscriptionState = when (state) {
        is AudioTranscriptionState.Recording -> AudioTranscriptionState.Recording(inputText)
        is AudioTranscriptionState.Transcribing -> 
            AudioTranscriptionState.Transcribing(inputText)
        is AudioTranscriptionState.Ready -> 
            AudioTranscriptionState.Ready(inputText)
        is AudioTranscriptionState.Error -> 
            AudioTranscriptionState.Error(state.errorMessage, inputText)
        is AudioTranscriptionState.Idle -> AudioTranscriptionState.Idle
    }
}