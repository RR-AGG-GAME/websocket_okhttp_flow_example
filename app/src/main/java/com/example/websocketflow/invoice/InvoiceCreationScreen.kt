package com.example.websocketflow.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
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


@Composable
fun InvoiceCreationCard(
    isRecording: Boolean,
    recordingTime: String = "0:00 / 2:00"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF424242)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Ready to create an invoice?",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Just say or type:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Customer name
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Customer name",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Customer name",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Item description
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Item description",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Item description",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Price
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "£",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Due in
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Due in",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Due in",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Text and voice prompts are processed by third parties.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Voice input indicator
            if (isRecording) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Listening...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Progress dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = recordingTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
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
    val listState = rememberLazyListState()
    
    val currentState = uiState
    val inputText = currentState.inputText
    val isRecording = currentState is TranscriptionUiState.Recording || 
                     currentState is TranscriptionUiState.Transcribing
    val isTyping = currentState.inputText.isNotEmpty()
    val errorMessage = when (currentState) {
        is TranscriptionUiState.Error -> currentState.errorMessage
        else -> ""
    }
    
    // Scroll to bottom initially to show card when empty
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        // Spacer at 0, card at 1 - always consistent
        listState.animateScrollToItem(1)
    }
    
    // Auto-scroll to show newest message when messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            // Spacer at 0, card at 1, messages start at 2
            // Newest message is at index: 1 (card) + messages.size = messages.size + 1
            val newestMessageIndex = messages.size + 1
            if (newestMessageIndex < listState.layoutInfo.totalItemsCount) {
                listState.animateScrollToItem(newestMessageIndex)
            }
        }
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
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            // Spacer - always present, always full height to push card to bottom
            item(key = "spacer") {
                Spacer(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .fillMaxWidth()
                )
            }
            
            // Card at index 1 - always at this position
            item(key = "card") {
                InvoiceCreationCard(
                    isRecording = isRecording,
                    recordingTime = "0:00 / 2:00"
                )
            }
            
            // Messages appear just below the card (index 2, 3, 4...)
            items(messages, key = { it }) { message ->
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
