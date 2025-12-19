package com.example.websocketflow.invoice.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Constants
object InvoiceColors {
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

object InvoiceDimens {
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
    val RecordingMaxDurationSeconds = 120 // 2 minutes
}

// Data Models
data class ChatMessage(
    val text: String,
    val isSent: Boolean
)

