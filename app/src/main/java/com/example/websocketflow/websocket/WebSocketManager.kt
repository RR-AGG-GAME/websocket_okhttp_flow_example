package com.example.websocketflow.websocket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager(private val url: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private var webSocket: WebSocket? = null

    fun connect(): kotlinx.coroutines.flow.Flow<WebSocketMessage> {
        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "WebSocketFlowExample/1.0")
                .build()
            client.webSocketFlow(request)
        } catch (e: Exception) {
            println("Error creating WebSocket flow: ${e.message}")
            throw e
        }
    }

    fun sendText(message: String) {
        try {
            if (webSocket != null) {
                webSocket?.send(message)
            } else {
                println("WebSocket is null, cannot send message")
            }
        } catch (e: Exception) {
            println("Error sending text: ${e.message}")
        }
    }

    fun createRawWebSocket() {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "WebSocketFlowExample/1.0")
                .build()
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    println("Raw WebSocket connected")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    println("Raw WebSocket failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            println("Error creating raw WebSocket: ${e.message}")
        }
    }

    fun close() {
        try {
            webSocket?.close(1000, "Closed by client")
        } catch (e: Exception) {
            println("Error closing WebSocket: ${e.message}")
        }
    }
}
