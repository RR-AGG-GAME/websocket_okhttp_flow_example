package com.example.websocketflow.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.widget.Toast
import org.koin.androidx.compose.koinViewModel
import com.example.websocketflow.audiotranscription.AudioTranscriptionViewModel
import com.example.websocketflow.audiotranscription.AudioTranscriptionState

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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { 
                onTextChange(inputText + "\n")
            })
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
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
fun InvoiceCreationScreen(
    viewModel: AudioTranscriptionViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    
    val currentState = uiState
    val isRecording = currentState is AudioTranscriptionState.Recording || 
                     currentState is AudioTranscriptionState.Transcribing
    val isTyping = currentState.inputText.isNotEmpty()
    val transcriptionResult = when (currentState) {
        is AudioTranscriptionState.Transcribing -> currentState.transcriptionResult
        is AudioTranscriptionState.Ready -> currentState.transcriptionResult
        else -> ""
    }
    val errorMessage = when (currentState) {
        is AudioTranscriptionState.Error -> currentState.message
        else -> ""
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && activity != null) {
            viewModel.startRecording(activity)
        } else {
            Toast.makeText(
                context,
                "Audio recording permission is required",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    LaunchedEffect(transcriptionResult) {
        if (transcriptionResult.isNotEmpty() && isRecording) {
            inputText = transcriptionResult
            viewModel.updateInputText(transcriptionResult)
        }
    }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ChatInputField(
            inputText = inputText,
            onTextChange = { 
                inputText = it
                viewModel.updateInputText(it)
            },
            isRecording = isRecording,
            isTyping = isTyping,
            onSend = {
                viewModel.sendMessage()
                inputText = ""
            },
            onVoiceStart = {
                if (activity != null) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onVoiceStop = {
                viewModel.stopRecording()
            }
        )
    }
}
