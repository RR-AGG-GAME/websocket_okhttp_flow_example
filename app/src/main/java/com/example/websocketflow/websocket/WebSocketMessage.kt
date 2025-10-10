package com.example.websocketflow.websocket

sealed class WebSocketMessage {
    data class Text(val text: String) : WebSocketMessage()
}
