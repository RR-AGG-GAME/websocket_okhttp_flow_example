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
        onDispose { singleTapJob?.cancel() }
    }
    
    val handleClick = remember(isMicButton, isStopButton) {
        createDoubleTapHandler(
            isMicButton = isMicButton,
            isStopButton = isStopButton,
            context = context,
            view = view,
            coroutineScope = coroutineScope,
            getFirstTapTime = { firstTapTime },
            setFirstTapTime = { firstTapTime = it },
            getSingleTapJob = { singleTapJob },
            setSingleTapJob = { singleTapJob = it },
            onAction = onClick
        )
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
                    isMicButton -> createAccessibleModifier(
                        singleTapMessage = context.getString(R.string.mic_button_single_tap_announcement),
                        doubleTapMessage = context.getString(R.string.mic_button_double_tap_announcement),
                        onClick = onClick,
                        coroutineScope = coroutineScope,
                        view = view,
                        interactionSource = interactionSource,
                        handleClick = handleClick
                    )
                    isStopButton -> createAccessibleModifier(
                        singleTapMessage = context.getString(R.string.stop_button_single_tap_announcement),
                        doubleTapMessage = context.getString(R.string.stop_button_double_tap_announcement),
                        onClick = onClick,
                        coroutineScope = coroutineScope,
                        view = view,
                        interactionSource = interactionSource,
                        handleClick = handleClick
                    )
                    else -> Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(bounded = true),
                        onClick = onClick
                    )
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

private fun createDoubleTapHandler(
    isMicButton: Boolean,
    isStopButton: Boolean,
    context: android.content.Context,
    view: android.view.View,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    getFirstTapTime: () -> Long?,
    setFirstTapTime: (Long?) -> Unit,
    getSingleTapJob: () -> kotlinx.coroutines.Job?,
    setSingleTapJob: (kotlinx.coroutines.Job?) -> Unit,
    onAction: () -> Unit
): () -> Unit {
    return {
        if (isMicButton || isStopButton) {
            val currentTime = System.currentTimeMillis()
            getSingleTapJob()?.cancel()
            
            val previousTapTime = getFirstTapTime()
            if (previousTapTime != null && (currentTime - previousTapTime) < 2000) {
                val announcement = if (isMicButton) {
                    context.getString(R.string.mic_button_double_tap_announcement)
                } else {
                    context.getString(R.string.stop_button_double_tap_announcement)
                }
                try {
                    view.announceForAccessibility(announcement)
                } catch (e: Exception) {
                }
                setFirstTapTime(null)
                onAction()
            } else {
                setFirstTapTime(currentTime)
                val newJob = coroutineScope.launch {
                    delay(2000)
                    if (getFirstTapTime() == currentTime) {
                        setFirstTapTime(null)
                    }
                }
                setSingleTapJob(newJob)
            }
        } else {
            onAction()
        }
    }
}

@Composable
private fun createAccessibleModifier(
    singleTapMessage: String,
    doubleTapMessage: String,
    onClick: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    view: android.view.View,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource,
    handleClick: () -> Unit
): Modifier {
    return Modifier
        .semantics {
            role = Role.Button
            contentDescription = singleTapMessage
            onClick(label = "") {
                coroutineScope.launch {
                    try {
                        view.post {
                            view.announceForAccessibility(doubleTapMessage)
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
