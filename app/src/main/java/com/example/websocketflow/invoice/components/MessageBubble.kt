package com.example.websocketflow.invoice.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

