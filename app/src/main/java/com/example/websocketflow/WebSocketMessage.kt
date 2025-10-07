package com.example.websocketflow

sealed class WebSocketMessage {
    data class Text(val text: String) : WebSocketMessage()
    data class Binary(val bytes: ByteArray, val contentType: String? = null) : WebSocketMessage()
}

