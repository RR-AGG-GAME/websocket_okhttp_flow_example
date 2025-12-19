package com.example.websocketflow.invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Extension function for ChatInputField to handle focus gain accessibility
 */
@Composable
fun Modifier.chatInputFieldFocusAccessibility(
    onFocusGained: () -> Unit
): Modifier {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        onFocusGained()
    }
    
    return this
        .focusRequester(focusRequester)
        .semantics {
            contentDescription = "Chat input field. Double tap to start voice input."
            stateDescription = "Ready for text or voice input"
        }
}

/**
 * Extension function for mic button with single tap accessibility
 */
@Composable
fun Modifier.micButtonSingleTapAccessibility(
    onClick: () -> Unit,
    contentDescription: String = "Voice input button. Single tap to start recording."
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    
    return this
        .clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = onClick
        )
        .semantics {
            this.contentDescription = contentDescription
            this.onClick(label = "Start voice recording") {
                onClick()
                true
            }
        }
        .testTag("mic_button_single_tap")
}

/**
 * Extension function for mic button with double tap accessibility
 */
@Composable
fun Modifier.micButtonDoubleTapAccessibility(
    onDoubleTap: () -> Unit,
    onSingleTap: () -> Unit,
    contentDescription: String = "Voice input button. Double tap to start recording, single tap for options."
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    return this
        .clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTapTime < 300) { // 300ms window for double tap
                    tapCount++
                    if (tapCount == 2) {
                        onDoubleTap()
                        tapCount = 0
                    }
                } else {
                    tapCount = 1
                    // Delay single tap to check for double tap
                    coroutineScope.launch {
                        delay(300)
                        if (tapCount == 1) {
                            onSingleTap()
                        }
                        tapCount = 0
                    }
                }
                lastTapTime = currentTime
            }
        )
        .semantics {
            this.contentDescription = contentDescription
            this.onClick(label = "Double tap to start voice recording") {
                onDoubleTap()
                true
            }
        }
        .testTag("mic_button_double_tap")
}

/**
 * Helper composable for Box with mic button accessibility
 */
@Composable
fun AccessibleMicButton(
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = if (onDoubleClick != null) {
            modifier.micButtonDoubleTapAccessibility(
                onDoubleTap = onDoubleClick,
                onSingleTap = onClick
            )
        } else {
            modifier.micButtonSingleTapAccessibility(onClick = onClick)
        }
    ) {
        content()
    }
}

