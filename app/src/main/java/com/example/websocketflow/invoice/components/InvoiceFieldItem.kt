package com.example.websocketflow.invoice.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun InvoiceFieldItem(
    icon: ImageVector? = null,
    iconText: String? = null,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = InvoiceDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = InvoiceColors.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(InvoiceDimens.IconSize)
                )
            }
            iconText != null -> {
                Text(
                    text = iconText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = InvoiceColors.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(InvoiceDimens.IconSize)
                )
            }
        }
        Spacer(modifier = Modifier.width(InvoiceDimens.SpacingMedium))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = InvoiceColors.White.copy(alpha = 0.9f)
        )
    }
}

