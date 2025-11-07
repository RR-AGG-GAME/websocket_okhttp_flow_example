package com.example.websocketflow.audiotranscription.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.websocketflow.audiotranscription.manager.SpeechRecognitionManager
import com.example.websocketflow.audiotranscription.model.AudioTranscriptionState
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

class AudioTranscriptionViewModel(
    private val speechRecognitionManager: SpeechRecognitionManager
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    
    init {
        speechRecognitionManager.state
            .onEach(::handleRecognitionState)
            .launchIn(viewModelScope)
    }
    
    private fun handleRecognitionState(recognitionState: SpeechRecognitionState) {
        when (recognitionState) {
            is SpeechRecognitionState.Transcribing -> 
                _inputText.value = recognitionState.transcriptionResult
            is SpeechRecognitionState.Ready -> 
                _inputText.value = recognitionState.transcriptionResult
            else -> { }
        }
    }
    
    val uiState: StateFlow<AudioTranscriptionState> = combine(
        speechRecognitionManager.state.scan(
            initial = AudioTranscriptionState.Idle as AudioTranscriptionState
        ) { current: AudioTranscriptionState, recognitionState: SpeechRecognitionState ->
            AudioTranscriptionStateMapper.map(recognitionState, current)
        },
        _inputText
    ) { mappedState: AudioTranscriptionState, inputText: String ->
        AudioTranscriptionStateMapper.updateInputText(mappedState, inputText)
    }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = AudioTranscriptionState.Idle
        )

    fun startRecording() {
        _inputText.value = ""
        speechRecognitionManager.startRecording()
    }

    fun stopRecording() {
        speechRecognitionManager.stopRecording()
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        if (_inputText.value.isEmpty()) return
        val currentState = uiState.value
        if (currentState is AudioTranscriptionState.Recording || 
            currentState is AudioTranscriptionState.Transcribing) {
            speechRecognitionManager.stopRecording()
        }
        speechRecognitionManager.clearTranscription()
        _inputText.value = ""
    }

    fun clearError() = speechRecognitionManager.clearTranscription()

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
    }
}