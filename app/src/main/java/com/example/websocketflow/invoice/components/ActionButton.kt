package com.example.websocketflow.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.websocketflow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActionButton(
    isRecording: Boolean,
    isTyping: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    
    var firstTapTime by remember { mutableStateOf<Long?>(null) }
    var singleTapJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    val isMicButton = !isRecording && !isTyping
    val isStopButton = isRecording
    
    DisposableEffect(Unit) {
        onDispose {
            singleTapJob?.cancel()
        }
    }
    
    val handleClick: () -> Unit = {
        if (isMicButton) {
            val currentTime = System.currentTimeMillis()
            singleTapJob?.cancel()
            
            val previousTapTime = firstTapTime
            if (previousTapTime != null && (currentTime - previousTapTime) < 2000) {
                firstTapTime = null
                try {
                    view.announceForAccessibility(context.getString(R.string.mic_button_double_tap_announcement))
                } catch (e: Exception) {
                }
                onClick()
            } else {
                firstTapTime = currentTime
                singleTapJob = coroutineScope.launch {
                    delay(2000)
                    if (firstTapTime != null && firstTapTime == currentTime) {
                        firstTapTime = null
                    }
                }
            }
        } else if (isStopButton) {
            val currentTime = System.currentTimeMillis()
            singleTapJob?.cancel()
            
            val previousTapTime = firstTapTime
            if (previousTapTime != null && (currentTime - previousTapTime) < 2000) {
                firstTapTime = null
                try {
                    view.announceForAccessibility(context.getString(R.string.stop_button_double_tap_announcement))
                } catch (e: Exception) {
                }
                onClick()
            } else {
                firstTapTime = currentTime
                singleTapJob = coroutineScope.launch {
                    delay(2000)
                    if (firstTapTime != null && firstTapTime == currentTime) {
                        firstTapTime = null
                    }
                }
            }
        } else {
            onClick()
        }
    }
    
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
            .then(
                when {
                    isMicButton -> {
                        Modifier
                            .semantics {
                                role = Role.Button
                                contentDescription = context.getString(R.string.mic_button_single_tap_announcement)
                                onClick(label = "") {
                                    coroutineScope.launch {
                                        try {
                                            view.post {
                                                view.announceForAccessibility(context.getString(R.string.mic_button_double_tap_announcement))
                                            }
                                            delay(2000)
                                            onClick()
                                        } catch (e: Exception) {
                                        }
                                    }
                                    true
                                }
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = rememberRipple(bounded = true),
                                onClick = handleClick
                            )
                    }
                    isStopButton -> {
                        Modifier
                            .semantics {
                                role = Role.Button
                                contentDescription = context.getString(R.string.stop_button_single_tap_announcement)
                                onClick(label = "") {
                                    coroutineScope.launch {
                                        try {
                                            view.post {
                                                view.announceForAccessibility(context.getString(R.string.stop_button_double_tap_announcement))
                                            }
                                            delay(2000)
                                            onClick()
                                        } catch (e: Exception) {
                                        }
                                    }
                                    true
                                }
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = rememberRipple(bounded = true),
                                onClick = handleClick
                            )
                    }
                    else -> {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(bounded = true),
                            onClick = onClick
                        )
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                isRecording -> Icons.Default.Stop
                isTyping -> Icons.Default.Send
                else -> Icons.Default.Mic
            },
            contentDescription = null,
            tint = when {
                isRecording || isTyping -> InvoiceColors.White
                else -> Color.Gray
            },
            modifier = Modifier.size(InvoiceDimens.IconSize)
        )
    }
}
