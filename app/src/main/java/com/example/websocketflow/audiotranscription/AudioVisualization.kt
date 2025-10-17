package com.example.websocketflow.audiotranscription

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AudioVisualization(
    audioLevel: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = if (isRecording) audioLevel else 0f,
        animationSpec = tween(100),
        label = "audioLevel"
    )
    
    Box(
        modifier = modifier
            .size(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val primaryContainer = MaterialTheme.colorScheme.primaryContainer
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val primary = MaterialTheme.colorScheme.primary
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawAudioVisualization(
                audioLevel = animatedLevel,
                isRecording = isRecording,
                canvasSize = size,
                primaryContainer = primaryContainer,
                surfaceVariant = surfaceVariant,
                primary = primary
            )
        }
    }
}

private fun DrawScope.drawAudioVisualization(
    audioLevel: Float,
    isRecording: Boolean,
    canvasSize: androidx.compose.ui.geometry.Size,
    primaryContainer: Color,
    surfaceVariant: Color,
    primary: Color
) {
    val centerX = canvasSize.width / 2f
    val centerY = canvasSize.height / 2f
    val radius = minOf(canvasSize.width, canvasSize.height) / 2f - 20f
    
    // Background circle
    drawCircle(
        color = if (isRecording) 
            primaryContainer.copy(alpha = 0.3f)
        else 
            surfaceVariant.copy(alpha = 0.5f),
        radius = radius,
        center = Offset(centerX, centerY)
    )
    
    if (isRecording) {
        // Animated waveform bars
        val barCount = 20
        val barWidth = (radius * 2) / barCount
        val maxBarHeight = radius * 0.8f
        
        for (i in 0 until barCount) {
            val barHeight = if (audioLevel > 0) {
                val normalizedIndex = i.toFloat() / barCount
                val wavePattern = sin(normalizedIndex * PI * 4 + System.currentTimeMillis() / 1000.0)
                val amplitude = audioLevel * (0.3f + 0.7f * abs(wavePattern.toFloat()))
                maxBarHeight * amplitude
            } else {
                0f
            }
            
            val x = centerX - radius + (i * barWidth) + barWidth / 2
            val y = centerY - barHeight / 2
            
            drawRect(
                color = primary.copy(
                    alpha = 0.3f + 0.7f * audioLevel
                ),
                topLeft = Offset(x - barWidth / 4, y),
                size = androidx.compose.ui.geometry.Size(barWidth / 2, barHeight)
            )
        }
        
        // Center pulse
        val pulseRadius = radius * 0.3f * (0.5f + 0.5f * audioLevel)
        drawCircle(
            color = primary.copy(alpha = 0.6f),
            radius = pulseRadius,
            center = Offset(centerX, centerY)
        )
    } else {
        // Static microphone icon when not recording
        val micRadius = radius * 0.3f
        drawCircle(
            color = surfaceVariant.copy(alpha = 0.6f),
            radius = micRadius,
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
fun AudioLevelIndicator(
    audioLevel: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = if (isRecording) audioLevel else 0f,
        animationSpec = tween(50),
        label = "levelIndicator"
    )
    
    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(10) { index ->
            val height = (animatedLevel * 20f * (index + 1) / 10f).dp.coerceAtLeast(4.dp)
            val alpha = if (index < animatedLevel * 10) 1f else 0.3f
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .padding(vertical = 2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
