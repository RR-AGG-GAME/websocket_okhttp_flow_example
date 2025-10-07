# WebSocket OkHttp Flow Example (Android)

This sample Android Studio project demonstrates using OkHttp WebSocket with Kotlin Coroutines and Flow,
and handles text + binary messages (images, audio, video) via a simple meta+binary framing.

Features:
- Connect to a WebSocket server using OkHttp
- Receive text and binary frames as a Kotlin Flow
- Send text or pick files (image/audio/video) to send as binary frames (with metadata JSON preceding)
- Minimal UI to send and view logs

Notes:
- This demo uses a simple convention: send a JSON meta text message with type="binary_meta" before sending raw binary.
- For real production use, implement chunking, checksums, authentication, and robust framing.
- Replace the example server URL in `MainActivity.kt` with your WebSocket server that understands the framing.

To run:
- Open this folder in Android Studio
- Sync Gradle and run on a device/emulator with internet permission

