package com.example.websocketflow.audiotranscription

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    
    data object Recording : SpeechRecognitionState
    
    data class Transcribing(
        val transcriptionResult: String
    ) : SpeechRecognitionState
    
    data class Error(
        val message: String
    ) : SpeechRecognitionState
    
    data class Ready(
        val transcriptionResult: String
    ) : SpeechRecognitionState
}

