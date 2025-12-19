package com.example.websocketflow.invoice.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RecordingTimer(
    elapsedSeconds: Int,
    maxSeconds: Int = InvoiceDimens.RecordingMaxDurationSeconds,
    modifier: Modifier = Modifier
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val maxMinutes = maxSeconds / 60
    val maxSecs = maxSeconds % 60
    
    Text(
        text = String.format("%d:%02d/%d:%02d", minutes, seconds, maxMinutes, maxSecs),
        style = MaterialTheme.typography.bodySmall,
        color = InvoiceColors.TextGray,
        modifier = modifier
    )
}

