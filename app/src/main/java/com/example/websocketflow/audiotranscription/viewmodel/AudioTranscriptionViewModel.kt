package com.example.websocketflow.audiotranscription.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.websocketflow.audiotranscription.manager.SpeechRecognitionManager
import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
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
    
    val uiState: StateFlow<TranscriptionUiState> = combine(
        speechRecognitionManager.state.scan(
            initial = TranscriptionUiState.Idle() as TranscriptionUiState
        ) { current: TranscriptionUiState, recognitionState: SpeechRecognitionState ->
            TranscriptionUiStateMapper.map(recognitionState, current)
        },
        _inputText
    ) { mappedState: TranscriptionUiState, inputText: String ->
        TranscriptionUiStateMapper.updateInput(mappedState, inputText)
    }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = TranscriptionUiState.Idle()
        )

    fun startRecording() {
        _inputText.value = ""
        speechRecognitionManager.startRecording()
    }

    fun stopRecording() {
        speechRecognitionManager.stopRecording()
    }

    fun updateInputText(text: String) {
        val currentState = uiState.value
        if (currentState is TranscriptionUiState.Recording) return
        _inputText.value = text
    }

    fun sendMessage() {
        if (_inputText.value.isEmpty()) return
        val currentState = uiState.value
        if (currentState is TranscriptionUiState.Recording || 
            currentState is TranscriptionUiState.Transcribing) {
            speechRecognitionManager.stopRecording()
        }
        speechRecognitionManager.clearTranscription()
        _inputText.value = ""
    }

    fun clearError() {
        speechRecognitionManager.clearTranscription()
        _inputText.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
    }
}