package com.example.websocketflow.invoice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.widget.Toast
import com.example.websocketflow.audiotranscription.AudioTranscriptionManager

// Utility method to handle permission result
fun handleAudioPermissionResult(
    isGranted: Boolean, 
    audioManager: AudioTranscriptionManager,
    context: android.content.Context,
    onRecordingStateChange: (Boolean) -> Unit
) {
    if (isGranted) {
        // Permission granted, start recording
        audioManager.startRecording()
        onRecordingStateChange(true)
    } else {
        // Permission denied, show toast message
        Toast.makeText(
            context, 
            "Audio recording permission is required for voice input", 
            Toast.LENGTH_LONG
        ).show()
        onRecordingStateChange(false)
    }
}

@Composable
fun ChatInputField(
    inputText: String,
    onTextChange: (String) -> Unit,
    isRecording: Boolean,
    isTyping: Boolean,
    onSend: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text field
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            label = { Text("Enter customer, items, amount") },
            placeholder = { Text("Enter customer, items, amount") },
            trailingIcon = {
                if (inputText.isNotEmpty() && !isRecording) {
                    IconButton(onClick = { onTextChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedLabelColor = Color(0xFF4CAF50),
                unfocusedLabelColor = Color(0xFF757575)
            ),
            maxLines = 5,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Action button - only show when text field is empty OR when recording/typing
        if (inputText.isEmpty() || isRecording || isTyping) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when {
                            isRecording -> Color(0xFFE57373)
                            isTyping -> Color(0xFF4CAF50)
                            else -> Color.White
                        },
                        shape = CircleShape
                    )
                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    .clickable {
                        when {
                            isRecording -> onVoiceStop()
                            isTyping -> onSend()
                            else -> onVoiceStart()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isRecording -> Icons.Default.Stop
                        isTyping -> Icons.Default.Send
                        else -> Icons.Default.Mic
                    },
                    contentDescription = when {
                        isRecording -> "Stop Recording"
                        isTyping -> "Send"
                        else -> "Voice Input"
                    },
                    tint = when {
                        isRecording -> Color.White
                        isTyping -> Color.White
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreationScreen() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<String>()) }
    
    // Audio transcription manager
    val audioManager = remember { AudioTranscriptionManager(context) }
    val transcriptionResult by audioManager.transcriptionResult.collectAsState()
    val isRecordingAudio by audioManager.isRecording.collectAsState()
    
    // Use the audio manager's recording state
    val isActuallyRecording = isRecordingAudio
    
    // Permission launcher for audio recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handleAudioPermissionResult(isGranted, audioManager, context) { recording ->
            isRecording = recording
        }
    }
    
    // Live transcription - update text field in real-time
    LaunchedEffect(transcriptionResult) {
        if (transcriptionResult.isNotEmpty()) {
            println("Live transcription: '$transcriptionResult'")
            inputText = transcriptionResult
            isTyping = true
        }
    }
    
    // Clear transcription when starting new recording
    LaunchedEffect(isActuallyRecording) {
        if (isActuallyRecording) {
            audioManager.clearTranscription()
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            audioManager.destroy()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel button
            Text(
                text = "Cancel",
                color = Color(0xFF4CAF50),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { /* Handle cancel */ }
            )
            
            // Title
            Text(
                text = "Create new invoice",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            // Settings gear icon
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF4CAF50)
            )
        }
        
        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
        
        // Bottom input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Overall bottom container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                ChatInputField(
                    inputText = if (isRecording && transcriptionResult.isEmpty()) "Recording..." else inputText,
                    onTextChange = { newText ->
                        // Don't allow manual typing while recording
                        if (!isRecording) {
                            inputText = newText
                            isTyping = newText.isNotEmpty()
                        }
                    },
                    isRecording = isActuallyRecording,
                    isTyping = isTyping || inputText.isNotEmpty(),
                    onSend = {
                        if (inputText.isNotEmpty()) {
                            messages = messages + inputText
                            inputText = ""
                            isTyping = false
                        }
                    },
                    onVoiceStart = {
                        // Request audio recording permission first
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onVoiceStop = {
                        isRecording = false
                        isTyping = false
                        audioManager.stopRecording()
                    }
                )
            }
        }
    }
}

