package com.example.websocketflow.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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

