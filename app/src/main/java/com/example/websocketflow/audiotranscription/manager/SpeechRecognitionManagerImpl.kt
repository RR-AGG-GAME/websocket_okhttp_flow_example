package com.example.websocketflow.audiotranscription.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.websocketflow.audiotranscription.model.SpeechRecognitionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SpeechRecognitionManagerImpl(
    private val context: Context
) : SpeechRecognitionManager {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var recordingStartTime: Long = 0
    private var shouldContinueRecording: Boolean = false
    private var isManuallyStopped: Boolean = false
    private var accumulatedTranscription: StringBuilder = StringBuilder()
    private var isRecognizerReady: Boolean = false
    private val RECORDING_MAX_DURATION_MS = 120_000L // 2 minutes
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    override val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()
    
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isRecognizerReady = true
                _state.value = SpeechRecognitionState.Recording
            }

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                val elapsedTime = System.currentTimeMillis() - recordingStartTime
                val isTimeoutError = error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || 
                                    error == SpeechRecognizer.ERROR_NO_MATCH
                
                // If it's a timeout/no_match error and we haven't reached 2 minutes, restart recording
                // These errors are normal during continuous recording when there are pauses
                if (isTimeoutError && shouldContinueRecording && elapsedTime < RECORDING_MAX_DURATION_MS && !isManuallyStopped) {
                    // Keep state as Recording during restart
                    // Restart recording automatically after a small delay to ensure clean restart
                    val currentRecognizer = speechRecognizer
                    speechRecognizer = null
                    currentRecognizer?.let {
                        try { it.stopListening() } catch (e: Exception) { }
                        try { it.destroy() } catch (e: Exception) { }
                    }
                    coroutineScope.launch {
                        delay(200) // Small delay to ensure recognizer is fully destroyed
                        if (shouldContinueRecording && !isManuallyStopped) {
                            // Ensure state is still Recording before restart
                            if (_state.value !is SpeechRecognitionState.Recording) {
                                _state.value = SpeechRecognitionState.Recording
                            }
                            startRecordingInternal()
                        }
                    }
                } else if (error == SpeechRecognizer.ERROR_CLIENT && shouldContinueRecording && elapsedTime < RECORDING_MAX_DURATION_MS && !isManuallyStopped) {
                    // ERROR_CLIENT can occur during restart, ignore and continue
                    val currentRecognizer = speechRecognizer
                    speechRecognizer = null
                    currentRecognizer?.let {
                        try { it.stopListening() } catch (e: Exception) { }
                        try { it.destroy() } catch (e: Exception) { }
                    }
                    coroutineScope.launch {
                        delay(200)
                        if (shouldContinueRecording && !isManuallyStopped) {
                            // Ensure state is still Recording before restart
                            if (_state.value !is SpeechRecognitionState.Recording) {
                                _state.value = SpeechRecognitionState.Recording
                            }
                            startRecordingInternal()
                        }
                    }
                } else {
                    // Stop recording for other errors or if 2 minutes reached
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                    shouldContinueRecording = false
                    if (elapsedTime >= RECORDING_MAX_DURATION_MS) {
                        _state.value = SpeechRecognitionState.Idle
                    } else {
                        _state.value = SpeechRecognitionState.Error(getErrorMessage(error))
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                // Ignore onResults if manually stopped (stopRecording already handled it)
                if (isManuallyStopped) {
                    return
                }
                
                val elapsedTime = System.currentTimeMillis() - recordingStartTime
                val transcription = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                
                // Accumulate transcription but keep recording state active
                if (transcription != null && shouldContinueRecording) {
                    if (accumulatedTranscription.isNotEmpty()) {
                        accumulatedTranscription.append(" ")
                    }
                    accumulatedTranscription.append(transcription)
                }
                
                // If we haven't reached 2 minutes, restart recording to continue
                // Keep state as Recording - don't change to Transcribing
                if (shouldContinueRecording && elapsedTime < RECORDING_MAX_DURATION_MS && !isManuallyStopped) {
                    // Keep state as Recording during restart
                    _state.value = SpeechRecognitionState.Recording
                    val currentRecognizer = speechRecognizer
                    speechRecognizer = null
                    currentRecognizer?.let {
                        try { it.stopListening() } catch (e: Exception) { }
                        try { it.destroy() } catch (e: Exception) { }
                    }
                    // Restart with a small delay to ensure clean restart
                    coroutineScope.launch {
                        delay(200) // Small delay to ensure recognizer is fully destroyed
                        if (shouldContinueRecording && !isManuallyStopped) {
                            // Ensure state is still Recording before restart
                            if (_state.value !is SpeechRecognitionState.Recording) {
                                _state.value = SpeechRecognitionState.Recording
                            }
                            startRecordingInternal()
                        }
                    }
                } else {
                    // 2 minutes reached - use Ready state to set text in input field
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                    shouldContinueRecording = false
                    
                    // Add final transcription result to accumulated text
                    if (transcription != null) {
                        if (accumulatedTranscription.isNotEmpty()) {
                            accumulatedTranscription.append(" ")
                        }
                        accumulatedTranscription.append(transcription)
                    }
                    
                    val finalTranscription = accumulatedTranscription.toString()
                    if (finalTranscription.isNotEmpty()) {
                        // First show transcribing state, then transition to Ready
                        _state.value = SpeechRecognitionState.Transcribing(finalTranscription)
                        coroutineScope.launch {
                            delay(2000) // Show transcribing for 2 seconds
                            _state.value = SpeechRecognitionState.Ready(finalTranscription)
                        }
                    } else {
                        _state.value = SpeechRecognitionState.Idle
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Accumulate partial results but keep recording state active
                // Don't change state to Transcribing - keep it as Recording
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { partialText ->
                    // Store the latest partial result (will be replaced by final result in onResults)
                    // We don't update accumulatedTranscription here because onResults will have the final text
                    // Just keep the state as Recording
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    }
    
    override fun startRecording() {
        shouldContinueRecording = true
        isManuallyStopped = false
        isRecognizerReady = false
        recordingStartTime = System.currentTimeMillis()
        accumulatedTranscription.clear()
        startRecordingInternal()
    }
    
    private fun startRecordingInternal() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _state.value = SpeechRecognitionState.Error("Speech recognition not available on this device")
                shouldContinueRecording = false
                return
            }
            
            // Clean up existing recognizer
            speechRecognizer?.let {
                try { 
                    it.stopListening() 
                } catch (e: Exception) { 
                    // Ignore errors when stopping
                }
                try {
                    it.destroy()
                } catch (e: Exception) {
                    // Ignore errors when destroying
                }
            }
            speechRecognizer = null
            
            // Only set to Idle if this is the first start, not a restart
            if (_state.value !is SpeechRecognitionState.Recording) {
                _state.value = SpeechRecognitionState.Idle
            }
            
            // Create new recognizer
            val newRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            if (newRecognizer == null) {
                _state.value = SpeechRecognitionState.Error("Failed to create speech recognizer")
                shouldContinueRecording = false
                return
            }
            
            speechRecognizer = newRecognizer
            speechRecognizer?.setRecognitionListener(createRecognitionListener())

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                // Set reasonable timeout values (Android has limits, max around 10 seconds)
                // We'll handle continuous recording by restarting onResults/onError
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L) // 10 seconds
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L) // 10 seconds
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L) // 5 seconds
            }

            // Start listening - onReadyForSpeech will confirm it started
            isRecognizerReady = false
            try {
                speechRecognizer?.startListening(intent)
                // Set a timeout to detect if onReadyForSpeech doesn't fire
                coroutineScope.launch {
                    delay(3000) // Wait 3 seconds for onReadyForSpeech
                    // If recognizer didn't become ready and we should still be recording, restart failed
                    if (!isRecognizerReady && 
                        shouldContinueRecording && 
                        !isManuallyStopped &&
                        speechRecognizer != null) {
                        // Restart failed, try again
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                        startRecordingInternal()
                    }
                }
            } catch (e: Exception) {
                _state.value = SpeechRecognitionState.Error("Failed to start listening: ${e.message}")
                speechRecognizer?.destroy()
                speechRecognizer = null
                shouldContinueRecording = false
            }
        } catch (e: Exception) {
            _state.value = SpeechRecognitionState.Error("Failed to start speech recognition: ${e.message}")
            speechRecognizer?.destroy()
            speechRecognizer = null
            shouldContinueRecording = false
        }
    }
    
    override fun stopRecording() {
        shouldContinueRecording = false
        isManuallyStopped = true
        
        speechRecognizer?.let { 
            try { 
                it.stopListening() 
            } catch (e: Exception) { }
            it.destroy()
        }
        speechRecognizer = null
        
        // When user stops, first show Transcribing state, then transition to Ready
        val finalText = accumulatedTranscription.toString()
        if (finalText.isNotEmpty()) {
            // First show transcribing state
            _state.value = SpeechRecognitionState.Transcribing(finalText)
            // Then after a delay, transition to Ready so text appears in input field
            coroutineScope.launch {
                delay(2000) // Show transcribing for 2 seconds
                _state.value = SpeechRecognitionState.Ready(finalText)
            }
        } else {
            _state.value = SpeechRecognitionState.Idle
        }
    }
    
    override fun clearTranscription() {
        _state.value = SpeechRecognitionState.Idle
    }
    
    override fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    private fun getErrorMessage(error: Int) = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech input detected"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Unknown error occurred (code: $error)"
    }
}

