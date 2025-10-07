# WebSocket OkHttp Flow Example (Android)

A robust Android WebSocket client built with OkHttp, Kotlin Coroutines, and Flow for real-time text messaging.

## 🚀 Features

- **Real-time WebSocket Communication** - Connect to WebSocket servers using OkHttp
- **Kotlin Flow Integration** - Receive messages as reactive streams
- **Text-only Messaging** - Send and receive text messages
- **Robust Error Handling** - Comprehensive error handling and logging
- **Connection Management** - Automatic reconnection and timeout handling
- **Clean UI** - Simple interface for testing WebSocket functionality

## 🏗️ Architecture

- **WebSocketManager** - Manages WebSocket connections and message sending
- **WebSocketExtensions** - Flow-based WebSocket listener with error handling
- **WebSocketMessage** - Sealed class for type-safe message handling
- **MainActivity** - UI and lifecycle management

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

