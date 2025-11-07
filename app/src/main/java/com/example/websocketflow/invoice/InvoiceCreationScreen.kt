package com.example.websocketflow.invoice

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.widget.Toast
import org.koin.androidx.compose.koinViewModel
import com.example.websocketflow.audiotranscription.model.AudioTranscriptionState
import com.example.websocketflow.audiotranscription.viewmodel.AudioTranscriptionViewModel

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
    var textFieldValue by remember { 
        mutableStateOf(TextFieldValue(inputText, selection = TextRange(inputText.length)))
    }
    
    // Update TextFieldValue when inputText changes (e.g., from transcription)
    // Set cursor to end of text
    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText, selection = TextRange(inputText.length))
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (!isRecording) {
                    textFieldValue = newValue
                    onTextChange(newValue.text)
                }
            },
            enabled = !isRecording,
            label = { Text("Enter customer, items, amount") },
            placeholder = { Text("Enter customer, items, amount") },
            trailingIcon = {
                if (inputText.isNotEmpty() && !isRecording) {
                    IconButton(onClick = { 
                        textFieldValue = TextFieldValue("")
                        onTextChange("") 
                    }) {
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
                val newText = textFieldValue.text + "\n"
                textFieldValue = TextFieldValue(newText, selection = TextRange(newText.length))
                onTextChange(newText)
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
    val uiState by viewModel.uiState.collectAsState()
    
    val messages = remember { mutableStateListOf<String>() }
    
    val currentState = uiState
    val inputText = currentState.inputText
    val isRecording = currentState is AudioTranscriptionState.Recording || 
                     currentState is AudioTranscriptionState.Transcribing
    val isTyping = currentState.inputText.isNotEmpty()
    val errorMessage = when (currentState) {
        is AudioTranscriptionState.Error -> currentState.errorMessage
        else -> ""
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            Toast.makeText(
                context,
                "Audio recording permission is required",
                Toast.LENGTH_LONG
            ).show()
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
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ChatInputField(
            inputText = inputText,
            onTextChange = { 
                viewModel.updateInputText(it)
            },
            isRecording = isRecording,
            isTyping = isTyping,
            onSend = {
                if (inputText.isNotEmpty()) {
                    messages.add(inputText)
                    viewModel.sendMessage()
                }
            },
                    onVoiceStart = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
            onVoiceStop = {
                viewModel.stopRecording()
            }
        )
    }
}
