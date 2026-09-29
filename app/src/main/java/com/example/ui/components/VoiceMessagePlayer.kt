package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.PlaybackState
import kotlin.math.sin

@Composable
fun VoiceMessagePlayer(
    messageId: Long,
    filePath: String,
    durationMs: Long,
    playbackState: PlaybackState,
    onPlay: () -> Unit,
    onSeek: (Long) -> Unit,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    val isCurrentPlaying = playbackState.messageId == messageId
    val isPlaying = isCurrentPlaying && playbackState.isPlaying
    val currentMs = if (isCurrentPlaying) playbackState.currentPositionMs else 0L
    val totalMs = if (isCurrentPlaying && playbackState.durationMs > 0) playbackState.durationMs else durationMs

    val progress = if (totalMs > 0) (currentMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f) else 0f

    // Stylized pseudo-waveform bars based on messageId
    val barCount = 28
    val waveformBars = remember(messageId) {
        List(barCount) { index ->
            val angle = (index * 0.7f + (messageId % 10))
            0.25f + 0.75f * ((sin(angle) + 1f) / 2f)
        }
    }

    val activeBarColor = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
    val inactiveBarColor = activeBarColor.copy(alpha = 0.3f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Pause Circle Button
        Surface(
            shape = CircleShape,
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(42.dp)
        ) {
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Interactive Waveform
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .pointerInput(totalMs) {
                        detectTapGestures { offset ->
                            val clickedRatio = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((clickedRatio * totalMs).toLong())
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = (canvasWidth / barCount) * 0.65f
                val spacing = (canvasWidth / barCount) * 0.35f

                waveformBars.forEachIndexed { i, heightRatio ->
                    val x = i * (barWidth + spacing)
                    val barHeight = canvasHeight * heightRatio
                    val y = (canvasHeight - barHeight) / 2f

                    val barRatio = i.toFloat() / barCount.toFloat()
                    val barColor = if (barRatio <= progress) activeBarColor else inactiveBarColor

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Duration text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentSec = (currentMs / 1000).toInt()
                val totalSec = (totalMs / 1000).toInt()

                Text(
                    text = String.format("%d:%02d", currentSec / 60, currentSec % 60),
                    fontSize = 11.sp,
                    color = activeBarColor.copy(alpha = 0.8f)
                )

                Text(
                    text = String.format("%d:%02d", totalSec / 60, totalSec % 60),
                    fontSize = 11.sp,
                    color = activeBarColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
