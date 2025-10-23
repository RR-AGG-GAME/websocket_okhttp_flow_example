package com.example.websocketflow.invoice

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreationScreen() {
    var inputText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    
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
        
        // Spacer to push bottom content down
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom input area with overall container
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Standard OutlinedTextField
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { 
                            inputText = it
                            isTyping = it.isNotEmpty()
                        },
                        label = { 
                            Text("Enter customer, items, amount") 
                        },
                        placeholder = { 
                            Text("Enter customer, items, amount") 
                        },
                        trailingIcon = {
                            if (inputText.isNotEmpty() && !isRecording) {
                                IconButton(
                                    onClick = { 
                                        inputText = ""
                                        isTyping = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray
                                    )
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
                        keyboardActions = KeyboardActions(
                            onSend = { 
                                // Handle send
                                inputText = ""
                                isTyping = false
                            }
                        )
                    )
                
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Dynamic button with three states: voice, send, stop
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = when {
                                    isRecording -> Color(0xFFE57373) // Red for stop
                                    isTyping -> Color(0xFF4CAF50) // Green for send
                                    else -> Color.White // White for voice
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = CircleShape
                            )
                        .clickable { 
                            when {
                                isRecording -> {
                                    // Stop recording
                                    isRecording = false
                                    isTyping = false
                                }
                                isTyping -> {
                                    // Send message
                                    inputText = ""
                                    isTyping = false
                                }
                                else -> {
                                    // Start recording
                                    isRecording = true
                                    isTyping = false
                                }
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
    }
}

