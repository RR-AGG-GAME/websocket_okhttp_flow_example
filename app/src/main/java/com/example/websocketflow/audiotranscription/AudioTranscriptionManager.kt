package com.example.websocketflow.audiotranscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioTranscriptionManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _transcriptionResult = MutableStateFlow("")
    val transcriptionResult: StateFlow<String> = _transcriptionResult.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()
    
    private val _isContinuousMode = MutableStateFlow(false)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()
    
    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()
    
    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private val maxRecordingTime = 180 // 3 minutes in seconds
    
    fun toggleContinuousMode() {
        _isContinuousMode.value = !_isContinuousMode.value
    }
    
    private fun startTimer() {
        if (_isContinuousMode.value) {
            _remainingTime.value = maxRecordingTime
            timerHandler = Handler(Looper.getMainLooper())
            timerRunnable = object : Runnable {
                override fun run() {
                    if (_remainingTime.value > 0 && _isRecording.value) {
                        _remainingTime.value = _remainingTime.value - 1
                        timerHandler?.postDelayed(this, 1000)
                    } else if (_remainingTime.value <= 0 && _isRecording.value) {
                        // Auto-stop after 3 minutes
                        stopRecording()
                        _errorMessage.value = "Recording stopped after 3 minutes"
                    }
                }
            }
            timerHandler?.post(timerRunnable!!)
        }
    }
    
    private fun stopTimer() {
        timerHandler?.removeCallbacks(timerRunnable!!)
        timerRunnable = null
        timerHandler = null
        _remainingTime.value = 0
    }
    
    fun startRecording() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "Speech recognition not available on this device"
            return
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isRecording.value = true
                _errorMessage.value = ""
                startTimer()
            }
            
            override fun onBeginningOfSpeech() {
                // Speech started
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Convert dB to 0-1 range for visualization
                val normalizedLevel = (rmsdB + 20) / 20f // Assuming -20dB to 0dB range
                _audioLevel.value = normalizedLevel.coerceIn(0f, 1f)
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                if (!_isContinuousMode.value) {
                    _isRecording.value = false
                    _audioLevel.value = 0f
                    stopTimer()
                }
                // In continuous mode, keep recording even after speech ends
            }
            
            override fun onError(error: Int) {
                _isRecording.value = false
                stopTimer()
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
                _errorMessage.value = errorMsg
            }
            
            override fun onResults(results: Bundle?) {
                _isRecording.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _transcriptionResult.value = matches[0]
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _transcriptionResult.value = matches[0]
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Event occurred
            }
        })
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
    }
    
    fun stopRecording() {
        speechRecognizer?.stopListening()
        _isRecording.value = false
        _audioLevel.value = 0f
        stopTimer()
    }
    
    fun clearTranscription() {
        _transcriptionResult.value = ""
        _errorMessage.value = ""
    }
    
    fun destroy() {
        stopTimer()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
