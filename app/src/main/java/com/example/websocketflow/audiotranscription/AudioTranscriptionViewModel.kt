package com.example.websocketflow.audiotranscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioTranscriptionViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(AudioTranscriptionState())
    val uiState: StateFlow<AudioTranscriptionState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _uiState.value = _uiState.value.copy(
                    isRecording = true,
                    errorMessage = ""
                )
            }

            override fun onBeginningOfSpeech() {
                // Speech started
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - no visualization needed
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                _uiState.value = _uiState.value.copy(isRecording = false)
            }

            override fun onError(error: Int) {
                _uiState.value = _uiState.value.copy(isRecording = false)
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error occurred"
                }
                _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
            }

            override fun onResults(results: Bundle?) {
                _uiState.value = _uiState.value.copy(isRecording = false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val transcription = matches[0]
                    _uiState.value = _uiState.value.copy(
                        transcriptionResult = transcription,
                        inputText = if (_uiState.value.isRecording) transcription else _uiState.value.inputText,
                        isTyping = true
                    )
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val transcription = matches[0]
                    if (_uiState.value.isRecording) {
                        _uiState.value = _uiState.value.copy(
                            transcriptionResult = transcription,
                            inputText = transcription,
                            isTyping = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            transcriptionResult = transcription
                        )
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Event occurred
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(
            inputText = text,
            isTyping = text.isNotEmpty()
        )
    }

    fun startRecording(context: Context) {
        viewModelScope.launch {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Speech recognition not available on this device"
                )
                return@launch
            }

            clearTranscription()
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            speechRecognizer?.stopListening()
            _uiState.value = _uiState.value.copy(
                isRecording = false,
                isTyping = false
            )
        }
    }

    fun clearTranscription() {
        _uiState.value = _uiState.value.copy(
            transcriptionResult = "",
            errorMessage = ""
        )
    }

    fun sendMessage() {
        val currentText = _uiState.value.inputText
        if (currentText.isNotEmpty()) {
            clearTranscription()
            _uiState.value = _uiState.value.copy(
                inputText = "",
                isTyping = false,
                transcriptionResult = "",
                errorMessage = ""
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
