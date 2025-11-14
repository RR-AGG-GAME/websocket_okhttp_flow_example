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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
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

// Constants
private object InvoiceColors {
    val SentMessageBackground = Color(0xFF4CAF50)
    val ReceivedMessageBackground = Color(0xFFE0E0E0)
    val CardBackground = Color(0xFF424242)
    val PrimaryGreen = Color(0xFF4CAF50)
    val RecordingRed = Color(0xFFE57373)
    val BorderGray = Color(0xFFE0E0E0)
    val TextGray = Color(0xFF757575)
    val White = Color.White
    val Black = Color.Black
}

private object InvoiceDimens {
    val CardWidthFraction = 0.8f
    val CardCornerRadius = 16.dp
    val InputFieldCornerRadius = 24.dp
    val CardPadding = 24.dp
    val MessagePadding = 16.dp
    val IconSize = 20.dp
    val ActionButtonSize = 48.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 16.dp
    val ProgressDotSize = 4.dp
    val ProgressDotSpacing = 4.dp
    val ProgressDotCount = 6
}

// Data Models
data class ChatMessage(
    val text: String,
    val isSent: Boolean
)

// Reusable Composables

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = InvoiceDimens.SpacingSmall),
        horizontalArrangement = if (message.isSent) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(InvoiceDimens.CardWidthFraction),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isSent) {
                    InvoiceColors.SentMessageBackground
                } else {
                    InvoiceColors.ReceivedMessageBackground
                }
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(InvoiceDimens.MessagePadding),
                color = if (message.isSent) InvoiceColors.White else InvoiceColors.Black
            )
        }
    }
}

@Composable
fun InvoiceFieldItem(
    icon: ImageVector? = null,
    iconText: String? = null,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = InvoiceDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = InvoiceColors.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(InvoiceDimens.IconSize)
                )
            }
            iconText != null -> {
                Text(
                    text = iconText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = InvoiceColors.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(InvoiceDimens.IconSize)
                )
            }
        }
        Spacer(modifier = Modifier.width(InvoiceDimens.SpacingMedium))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = InvoiceColors.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun RecordingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = InvoiceDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Listening",
            tint = InvoiceColors.White,
            modifier = Modifier.size(InvoiceDimens.SpacingLarge)
        )
        Spacer(modifier = Modifier.width(InvoiceDimens.SpacingSmall))
        Text(
            text = "Listening...",
            style = MaterialTheme.typography.bodySmall,
            color = InvoiceColors.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.width(InvoiceDimens.SpacingLarge))
        Row(
            horizontalArrangement = Arrangement.spacedBy(InvoiceDimens.ProgressDotSpacing)
        ) {
            repeat(InvoiceDimens.ProgressDotCount) {
                Box(
                    modifier = Modifier
                        .size(InvoiceDimens.ProgressDotSize)
                        .background(InvoiceColors.White, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    isRecording: Boolean,
    isTyping: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(InvoiceDimens.ActionButtonSize)
            .background(
                color = when {
                    isRecording -> InvoiceColors.RecordingRed
                    isTyping -> InvoiceColors.PrimaryGreen
                    else -> InvoiceColors.White
                },
                shape = CircleShape
            )
            .border(1.dp, InvoiceColors.BorderGray, CircleShape)
            .clickable(onClick = onClick),
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
                isRecording || isTyping -> InvoiceColors.White
                else -> Color.Gray
            },
            modifier = Modifier.size(InvoiceDimens.IconSize)
        )
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
    onVoiceStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(inputText, selection = TextRange(inputText.length)))
    }

    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText, selection = TextRange(inputText.length))
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
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
                .heightIn(min = InvoiceDimens.ActionButtonSize, max = 200.dp),
            shape = RoundedCornerShape(InvoiceDimens.InputFieldCornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InvoiceColors.PrimaryGreen,
                unfocusedBorderColor = InvoiceColors.BorderGray,
                focusedContainerColor = InvoiceColors.White,
                unfocusedContainerColor = InvoiceColors.White,
                focusedLabelColor = InvoiceColors.PrimaryGreen,
                unfocusedLabelColor = InvoiceColors.TextGray
            ),
            maxLines = 5,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                val newText = textFieldValue.text + "\n"
                textFieldValue = TextFieldValue(newText, selection = TextRange(newText.length))
                onTextChange(newText)
            })
        )

        Spacer(modifier = Modifier.width(InvoiceDimens.SpacingSmall))

        if (inputText.isEmpty() || isRecording || isTyping) {
            ActionButton(
                isRecording = isRecording,
                isTyping = isTyping,
                onClick = {
                    when {
                        isRecording -> onVoiceStop()
                        isTyping -> onSend()
                        else -> onVoiceStart()
                    }
                }
            )
        }
    }
}

@Composable
fun InvoiceCreationCard(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = InvoiceDimens.SpacingSmall),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(InvoiceDimens.CardWidthFraction),
            shape = RoundedCornerShape(InvoiceDimens.CardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = InvoiceColors.CardBackground
            )
        ) {
            Column(
                modifier = Modifier.padding(InvoiceDimens.CardPadding)
            ) {
                Text(
                    text = "Ready to create an invoice?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = InvoiceColors.White,
                    modifier = Modifier.padding(bottom = InvoiceDimens.SpacingSmall)
                )

                Text(
                    text = "Just say or type:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InvoiceColors.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = InvoiceDimens.SpacingLarge)
                )

                InvoiceFieldItem(
                    icon = Icons.Default.Person,
                    label = "Customer name"
                )

                InvoiceFieldItem(
                    icon = Icons.Default.Description,
                    label = "Item description"
                )

                InvoiceFieldItem(
                    iconText = "£",
                    label = "Price"
                )

                InvoiceFieldItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Due in"
                )

                Spacer(modifier = Modifier.height(InvoiceDimens.SpacingLarge))

                Text(
                    text = "Text and voice prompts are processed by third parties.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InvoiceColors.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = InvoiceDimens.SpacingMedium)
                )

                if (isRecording) {
                    RecordingIndicator()
                }
            }
        }
    }
}

// Main Screen
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
                    isRecording = isRecording
                )
            }

            items(messages, key = { "${it.text}_${it.isSent}" }) { message ->
                MessageBubble(message = message)
            }
        }

        Spacer(modifier = Modifier.height(InvoiceDimens.SpacingSmall))

        ChatInputField(
            inputText = inputText,
            onTextChange = { viewModel.updateInputText(it) },
            isRecording = isRecording,
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
        isTyping = true,
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
