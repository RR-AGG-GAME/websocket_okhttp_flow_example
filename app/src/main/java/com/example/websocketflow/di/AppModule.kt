package com.example.websocketflow.di

import com.example.websocketflow.audiotranscription.manager.SpeechRecognitionManager
import com.example.websocketflow.audiotranscription.manager.SpeechRecognitionManagerImpl
import com.example.websocketflow.audiotranscription.viewmodel.AudioTranscriptionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SpeechRecognitionManager> { SpeechRecognitionManagerImpl(androidContext()) }
    
    viewModel { AudioTranscriptionViewModel(get()) }
}

