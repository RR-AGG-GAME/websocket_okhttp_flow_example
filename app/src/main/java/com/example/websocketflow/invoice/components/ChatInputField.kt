package com.example.websocketflow.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputField(
    inputText: String,
    onTextChange: (String) -> Unit,
    isRecording: Boolean,
    isTranscribing: Boolean,
    isTyping: Boolean,
    onSend: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(inputText, selection = TextRange(inputText.length)))
    }

    // Timer state for recording
    var elapsedSeconds by remember { mutableStateOf(0) }
    
    // Timer that runs when recording starts and only auto-stops at 2 minutes
    LaunchedEffect(isRecording) {
        if (isRecording) {
            // Reset timer when recording starts
            elapsedSeconds = 0
            var currentSeconds = 0
            
            // Run timer until 2 minutes
            while (currentSeconds < InvoiceDimens.RecordingMaxDurationSeconds) {
                kotlinx.coroutines.delay(1000)
                currentSeconds++
                elapsedSeconds = currentSeconds
            }
            
            // Auto-stop when 2 minutes reached
            onVoiceStop()
        } else {
            // Reset timer when recording stops
            elapsedSeconds = 0
        }
    }

    LaunchedEffect(inputText) {
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(inputText, selection = TextRange(inputText.length))
        }
    }

    // Show recording UI when recording
    if (isRecording) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // "Listening..." and timer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Listening...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InvoiceColors.TextGray
                )
                RecordingTimer(elapsedSeconds = elapsedSeconds)
            }
            
            Spacer(modifier = Modifier.height(InvoiceDimens.SpacingMedium))
            
            // Stop button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ActionButton(
                    isRecording = true,
                    isTyping = false,
                    onClick = onVoiceStop
                )
            }
        }
    } else if (isTranscribing) {
        // Transcribing state
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simple loading indicator using dots animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    var scale by remember { mutableStateOf(0.5f) }
                    LaunchedEffect(index) {
                        // Stagger the animation for each dot
                        kotlinx.coroutines.delay(index * 200L)
                        while (true) {
                            scale = 1f
                            kotlinx.coroutines.delay(300)
                            scale = 0.5f
                            kotlinx.coroutines.delay(300)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = InvoiceColors.PrimaryGreen.copy(alpha = scale),
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.width(InvoiceDimens.SpacingMedium))
            Text(
                text = "Transcribing...",
                style = MaterialTheme.typography.bodyMedium,
                color = InvoiceColors.TextGray
            )
        }
    } else {
        // Normal text input field
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onTextChange(newValue.text)
                },
                label = { Text("Enter customer, items, amount") },
                placeholder = { Text("Enter customer, items, amount") },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
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

            if (inputText.isEmpty() || isTyping) {
                ActionButton(
                    isRecording = false,
                    isTyping = isTyping,
                    onClick = {
                        when {
                            isTyping -> onSend()
                            else -> onVoiceStart()
                        }
                    }
                )
            }
        }
    }
}

