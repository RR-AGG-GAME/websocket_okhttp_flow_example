package com.example.websocketflow.audiotranscription.manager

import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognitionManager {
    val state: StateFlow<SpeechRecognitionState>
    
    fun startRecording()
    fun stopRecording()
    fun clearTranscription()
    fun destroy()
}

