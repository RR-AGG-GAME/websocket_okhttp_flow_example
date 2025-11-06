package com.example.websocketflow.audiotranscription

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioTranscriptionViewModel(
    private val speechRecognitionService: SpeechRecognitionService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AudioTranscriptionState>(AudioTranscriptionState.Idle)
    val uiState: StateFlow<AudioTranscriptionState> = _uiState.asStateFlow()

    init {
        observeSpeechRecognition()
    }

    private fun observeSpeechRecognition() {
        viewModelScope.launch {
            speechRecognitionService.state.collect { serviceState ->
                val currentUiState = _uiState.value
                _uiState.value = when (serviceState) {
                    is SpeechRecognitionState.Idle -> {
                        when (currentUiState) {
                            is AudioTranscriptionState.Recording,
                            is AudioTranscriptionState.Transcribing -> {
                                if (currentUiState.inputText.isNotEmpty()) {
                                    AudioTranscriptionState.Ready(
                                        transcriptionResult = "",
                                        inputText = currentUiState.inputText
                                    )
                                } else {
                                    AudioTranscriptionState.Idle
                                }
                            }
                            else -> currentUiState
                        }
                    }
                    is SpeechRecognitionState.Recording -> {
                        AudioTranscriptionState.Recording(currentUiState.inputText)
                    }
                    is SpeechRecognitionState.Transcribing -> {
                        AudioTranscriptionState.Transcribing(
                            transcriptionResult = serviceState.transcriptionResult,
                            inputText = serviceState.transcriptionResult
                        )
                    }
                    is SpeechRecognitionState.Ready -> {
                        AudioTranscriptionState.Ready(
                            transcriptionResult = serviceState.transcriptionResult,
                            inputText = serviceState.transcriptionResult
                        )
                    }
                    is SpeechRecognitionState.Error -> {
                        AudioTranscriptionState.Error(
                            message = serviceState.message,
                            inputText = currentUiState.inputText
                        )
                    }
                }
            }
        }
    }

    fun startRecording(context: Context) {
        clearTranscription()
        speechRecognitionService.startRecording(context)
    }

    fun stopRecording() {
        speechRecognitionService.stopRecording()
    }

    fun clearTranscription() {
        speechRecognitionService.clearTranscription()
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is AudioTranscriptionState.Error -> AudioTranscriptionState.Error(
                message = "",
                inputText = currentState.inputText
            )
            else -> {
                if (currentState.inputText.isNotEmpty()) {
                    AudioTranscriptionState.Ready(
                        transcriptionResult = "",
                        inputText = currentState.inputText
                    )
                } else {
                    AudioTranscriptionState.Idle
                }
            }
        }
    }

    fun updateInputText(text: String) {
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is AudioTranscriptionState.Recording -> AudioTranscriptionState.Recording(text)
            is AudioTranscriptionState.Transcribing -> AudioTranscriptionState.Transcribing(
                transcriptionResult = currentState.transcriptionResult,
                inputText = text
            )
            is AudioTranscriptionState.Ready -> AudioTranscriptionState.Ready(
                transcriptionResult = currentState.transcriptionResult,
                inputText = text
            )
            is AudioTranscriptionState.Error -> AudioTranscriptionState.Error(
                message = currentState.message,
                inputText = text
            )
            is AudioTranscriptionState.Idle -> {
                if (text.isNotEmpty()) {
                    AudioTranscriptionState.Ready(
                        transcriptionResult = "",
                        inputText = text
                    )
                } else {
                    AudioTranscriptionState.Idle
                }
            }
        }
    }

    fun sendMessage() {
        val currentState = _uiState.value
        if (currentState.inputText.isNotEmpty()) {
            speechRecognitionService.clearTranscription()
            _uiState.value = AudioTranscriptionState.Idle
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is AudioTranscriptionState.Error) {
            _uiState.value = if (currentState.inputText.isNotEmpty()) {
                AudioTranscriptionState.Ready(
                    transcriptionResult = "",
                    inputText = currentState.inputText
                )
            } else {
                AudioTranscriptionState.Idle
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionService.destroy()
    }
}
