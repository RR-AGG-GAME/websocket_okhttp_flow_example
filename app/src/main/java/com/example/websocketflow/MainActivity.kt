package com.example.websocketflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var manager: WebSocketManager
    private lateinit var input: EditText
    private lateinit var btnSend: Button
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.input)
        btnSend = findViewById(R.id.btnSend)
        logView = findViewById(R.id.log)

        appendLog("App started - initializing WebSocket...")

        try {
            val wsUrl = "" // web socket url
            manager = WebSocketManager(wsUrl)
            manager.createRawWebSocket()
            appendLog("WebSocket manager created")

            // Collect incoming messages with error handling
            lifecycleScope.launch {
                try {
                    manager.connect().collect { msg ->
                        when (msg) {
                            is WebSocketMessage.Text -> appendLog("Received: ${msg.text}")
                            else -> appendLog("Received unknown message type")
                        }
                    }
                } catch (e: Exception) {
                    appendLog("Error in message collection: ${e.message}")
                }
            }
            appendLog("Message collection started")
        } catch (e: Exception) {
            appendLog("Error initializing WebSocket: ${e.message}")
        }

        btnSend.setOnClickListener {
            try {
                val text = input.text.toString()
                if (text.isNotEmpty()) {
                    manager.sendText(text)
                    appendLog("Sent: $text")
                    input.text.clear()
                } else {
                    appendLog("Please enter a message")
                }
            } catch (e: Exception) {
                appendLog("Error sending message: ${e.message}")
            }
        }
    }

    private fun appendLog(text: String) {
        logView.append(text + "\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.close()
    }
}
