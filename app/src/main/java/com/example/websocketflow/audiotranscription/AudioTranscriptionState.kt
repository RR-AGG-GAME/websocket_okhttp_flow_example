package com.example.websocketflow.audiotranscription

data class AudioTranscriptionState(
    val isRecording: Boolean = false,
    val transcriptionResult: String = "",
    val errorMessage: String = "",
    val inputText: String = "",
    val isTyping: Boolean = false
) {
    val hasError: Boolean
        get() = errorMessage.isNotEmpty()
    
    val hasTranscription: Boolean
        get() = transcriptionResult.isNotEmpty()
    
    val canRecord: Boolean
        get() = !isRecording && !hasError
}

