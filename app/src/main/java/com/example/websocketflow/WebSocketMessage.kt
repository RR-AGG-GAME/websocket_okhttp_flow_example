package com.example.websocketflow

sealed class WebSocketMessage {
    data class Text(val text: String) : WebSocketMessage()
}

