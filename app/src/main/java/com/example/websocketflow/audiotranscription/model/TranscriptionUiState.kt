package com.example.websocketflow.audiotranscription.model

sealed interface TranscriptionUiState {
    val inputText: String
    
    data class Idle(
        override val inputText: String = ""
    ) : TranscriptionUiState
    
    data class Recording(
        override val inputText: String = ""
    ) : TranscriptionUiState
    
    data class Transcribing(
        override val inputText: String
    ) : TranscriptionUiState
    
    data class Error(
        val errorMessage: String,
        override val inputText: String = ""
    ) : TranscriptionUiState
}
