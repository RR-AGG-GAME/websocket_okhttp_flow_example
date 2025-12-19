package com.example.websocketflow.invoice.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role

@Composable
fun InvoiceCreationCard(
    isRecording: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = InvoiceDimens.SpacingSmall),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(InvoiceDimens.CardWidthFraction)
                .semantics {
                    role = Role.Button
                    contentDescription = "Invoice creation card"
                    onClick(label = "Tap to start creating invoice") {
                        onClick()
                        true
                    }
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    onClick = onClick
                )
                .focusable()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(InvoiceDimens.CardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = InvoiceColors.CardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(InvoiceDimens.CardPadding)
                        .semantics(mergeDescendants = true) {}
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
}

