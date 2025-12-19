package com.example.websocketflow.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.widget.Toast
import org.koin.androidx.compose.koinViewModel
import com.example.websocketflow.audiotranscription.model.TranscriptionUiState
import com.example.websocketflow.audiotranscription.viewmodel.AudioTranscriptionViewModel
import com.example.websocketflow.invoice.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreationScreen(
    viewModel: AudioTranscriptionViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    val currentState = uiState
    val inputText = currentState.inputText
    val isRecording = currentState is TranscriptionUiState.Recording
    val isTranscribing = currentState is TranscriptionUiState.Transcribing
    val isTyping = currentState.inputText.isNotEmpty() && !isRecording && !isTranscribing
    val errorMessage = when (currentState) {
        is TranscriptionUiState.Error -> currentState.errorMessage
        else -> ""
    }

    // Scroll to bottom initially to show card when empty
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        listState.animateScrollToItem(1)
    }

    // Auto-scroll to show newest message when messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
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
            .padding(InvoiceDimens.SpacingLarge)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            item(key = "spacer") {
                Spacer(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .fillMaxWidth()
                )
            }

            item(key = "card") {
                InvoiceCreationCard(
                    isRecording = isRecording,
                    onClick = {
                        // Start voice recording when card is tapped
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }

            itemsIndexed(messages, key = { index, message -> "${message.text}_${message.isSent}_$index" }) { index, message ->
                MessageBubble(message = message)
            }
        }

        Spacer(modifier = Modifier.height(InvoiceDimens.SpacingSmall))

        ChatInputField(
            inputText = inputText,
            onTextChange = { viewModel.updateInputText(it) },
            isRecording = isRecording,
            isTranscribing = isTranscribing,
            isTyping = isTyping,
            onSend = {
                if (inputText.isNotEmpty()) {
                    messages.add(ChatMessage(text = inputText, isSent = true))
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

// Preview Functions
@Preview(showBackground = true)
@Composable
private fun MessageBubblePreview() {
    Column {
        MessageBubble(
            message = ChatMessage("This is a sent message", isSent = true)
        )
        MessageBubble(
            message = ChatMessage("This is a received message", isSent = false)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InvoiceFieldItemPreview() {
    Column {
        InvoiceFieldItem(
            icon = Icons.Default.Person,
            label = "Customer name"
        )
        InvoiceFieldItem(
            iconText = "£",
            label = "Price"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordingIndicatorPreview() {
    Box(modifier = Modifier.background(InvoiceColors.CardBackground)) {
        RecordingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonPreview() {
    Row {
        ActionButton(isRecording = false, isTyping = false, onClick = {})
        Spacer(modifier = Modifier.width(8.dp))
        ActionButton(isRecording = true, isTyping = false, onClick = {})
        Spacer(modifier = Modifier.width(8.dp))
        ActionButton(isRecording = false, isTyping = true, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatInputFieldPreview() {
    ChatInputField(
        inputText = "Sample input text",
        onTextChange = {},
        isRecording = false,
        isTranscribing = false,
        isTyping = true,
        onSend = {},
        onVoiceStart = {},
        onVoiceStop = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatInputFieldRecordingPreview() {
    ChatInputField(
        inputText = "",
        onTextChange = {},
        isRecording = true,
        isTranscribing = false,
        isTyping = false,
        onSend = {},
        onVoiceStart = {},
        onVoiceStop = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatInputFieldTranscribingPreview() {
    ChatInputField(
        inputText = "",
        onTextChange = {},
        isRecording = false,
        isTranscribing = true,
        isTyping = false,
        onSend = {},
        onVoiceStart = {},
        onVoiceStop = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun InvoiceCreationCardPreview() {
    InvoiceCreationCard(
        isRecording = false
    )
}

@Preview(showBackground = true)
@Composable
private fun InvoiceCreationCardRecordingPreview() {
    InvoiceCreationCard(
        isRecording = true
    )
}
