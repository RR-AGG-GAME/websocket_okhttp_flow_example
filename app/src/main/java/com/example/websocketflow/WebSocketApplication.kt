package com.example.websocketflow

import android.app.Application
import com.example.websocketflow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WebSocketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@WebSocketApplication)
            modules(appModule)
        }
    }
}

