package com.example.websocketflow.di

import com.example.websocketflow.audiotranscription.AudioTranscriptionViewModel
import com.example.websocketflow.audiotranscription.SpeechRecognitionService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SpeechRecognitionService() }
    
    viewModel { AudioTranscriptionViewModel(get()) }
}

