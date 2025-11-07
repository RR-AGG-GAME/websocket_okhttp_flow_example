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
        val transcriptionResult: String,
        override val inputText: String
    ) : AudioTranscriptionState
    
    data class Error(
        val message: String,
        override val inputText: String = ""
    ) : AudioTranscriptionState
    
    data class Ready(
        val transcriptionResult: String,
        override val inputText: String
    ) : AudioTranscriptionState
}

