package com.example.websocketflow.di

import com.example.websocketflow.audiotranscription.AudioTranscriptionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AudioTranscriptionViewModel() }
}

