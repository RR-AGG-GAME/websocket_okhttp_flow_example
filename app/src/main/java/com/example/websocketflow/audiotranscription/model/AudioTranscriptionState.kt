package com.example.websocketflow.audiotranscription.model

sealed interface AudioTranscriptionState {
    val inputText: String
    
    data object Idle : AudioTranscriptionState {
        override val inputText: String = ""
    }
    
    data class Recording(
        override val inputText: String = ""
    ) : AudioTranscriptionState
    
    data class Transcribing(
        override val inputText: String
    ) : AudioTranscriptionState
    
    data class Error(
        val errorMessage: String,
        override val inputText: String = ""
    ) : AudioTranscriptionState
    
    data class Ready(
        override val inputText: String
    ) : AudioTranscriptionState
}
