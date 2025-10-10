package com.example.websocketflow.websocket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*

/**
 * Coroutine-friendly Flow for WebSocket text messages only.
 */
fun OkHttpClient.webSocketFlow(request: Request): Flow<WebSocketMessage> = callbackFlow {
    val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("✅ WebSocket connected")
            try {
                trySend(WebSocketMessage.Text("Connected to WebSocket server"))
            } catch (e: Exception) {
                println("Error sending connection message: ${e.message}")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                trySend(WebSocketMessage.Text(text))
            } catch (e: Exception) {
                println("Error sending message: ${e.message}")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("WebSocket closing: $code - $reason")
            try {
                webSocket.close(1000, null)
            } catch (e: Exception) {
                println("Error closing WebSocket: ${e.message}")
            }
            close()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WebSocket failure: ${t.message}")
            try {
                close(t)
            } catch (e: Exception) {
                println("Error closing flow: ${e.message}")
            }
        }
    }

    try {
        val ws = newWebSocket(request, listener)
        awaitClose { 
            try {
                ws.close(1000, "Closed by client")
            } catch (e: Exception) {
                println("Error in awaitClose: ${e.message}")
            }
        }
    } catch (e: Exception) {
        println("Error creating WebSocket: ${e.message}")
        close(e)
    }
}
