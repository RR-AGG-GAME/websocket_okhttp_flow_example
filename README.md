# WebSocket OkHttp Flow Example (Android)

A robust Android WebSocket client built with OkHttp, Kotlin Coroutines, and Flow for real-time text messaging.

## 🚀 Features

- **Real-time WebSocket Communication** - Connect to WebSocket servers using OkHttp
- **Kotlin Flow Integration** - Receive messages as reactive streams
- **Text-only Messaging** - Send and receive text messages
- **Robust Error Handling** - Comprehensive error handling and logging
- **Connection Management** - Automatic reconnection and timeout handling
- **Clean UI** - Simple interface for testing WebSocket functionality

## 📱 Usage

1. **Connect to WebSocket Server**
   - Configure your WebSocket server URL in MainActivity.kt
   - Automatically connects on app launch

2. **Send Messages**
   - Type text in the input field
   - Tap "Send Text" button
   - Messages are sent via WebSocket

3. **Receive Messages**
   - Incoming messages appear in the log area
   - Connection status is displayed
   - Error messages are logged for debugging

## 🔧 How It Works

This project uses a **custom extension function** approach to convert OkHttp WebSocket into a Kotlin Flow:

```kotlin
// Extension function on OkHttpClient
fun OkHttpClient.webSocketFlow(request: Request): Flow<WebSocketMessage> = callbackFlow {
    val listener = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            trySend(WebSocketMessage.Text(text))
        }
        // ... other callbacks
    }
    val ws = newWebSocket(request, listener)
    awaitClose { ws.close(1000, "Closed by client") }
}
```

**How it works:**
1. **Extension Function** - Adds `webSocketFlow()` method to `OkHttpClient`
2. **CallbackFlow** - Converts WebSocket callbacks into Flow emissions
3. **trySend()** - Sends messages to the Flow stream
4. **awaitClose()** - Handles cleanup when Flow is cancelled
5. **Flow Collection** - MainActivity collects messages reactively

**Benefits:**
- **Reactive** - Messages arrive as Flow emissions
- **Lifecycle-aware** - Automatically handles connection cleanup
- **Type-safe** - Sealed class for different message types
- **Error handling** - Built-in exception handling

