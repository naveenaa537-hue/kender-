package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessage
import com.example.data.model.DeliveryStatus
import com.example.data.model.MessageType
import com.example.media.PlaybackState
import com.example.ui.theme.SeenBlue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean,
    playbackState: PlaybackState,
    onPlayAudio: (Long, String, Long) -> Unit,
    onSeekAudio: (Long) -> Unit,
    onReply: (ChatMessage) -> Unit,
    onEdit: (ChatMessage) -> Unit,
    onDelete: (ChatMessage, Boolean) -> Unit,
    onToggleReaction: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val bubbleColor = if (isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val reactionsMap = remember(message.reactions) {
        message.parseReactions()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            // Main Bubble Card
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                tonalElevation = if (isMe) 2.dp else 1.dp,
                modifier = Modifier
                    .clip(bubbleShape)
                    .combinedClickable(
                        onClick = {
                            if (showMenu) showMenu = false
                        },
                        onLongClick = {
                            showMenu = true
                        }
                    )
                    .testTag("chat_bubble_${message.id}")
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = if (message.messageType == MessageType.IMAGE || message.messageType == MessageType.GIF) 6.dp else 12.dp,
                        vertical = 8.dp
                    )
                ) {
                    // Reply preview quote
                    if (!message.replyToContent.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(26.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Friend",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = message.replyToContent,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Content according to type
                    when {
                        message.isDeleted -> {
                            Text(
                                text = "🚫 This message was deleted",
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }

                        message.messageType == MessageType.IMAGE || message.messageType == MessageType.GIF -> {
                            val mediaSource = if (message.mediaUri?.startsWith("/") == true) {
                                File(message.mediaUri)
                            } else {
                                message.mediaUri
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(mediaSource)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Shared photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (message.content.isNotBlank() && message.content != "Photo" && message.content != "GIF") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.content,
                                    fontSize = 14.sp,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        message.messageType == MessageType.VOICE -> {
                            VoiceMessagePlayer(
                                messageId = message.id,
                                filePath = message.mediaUri ?: "",
                                durationMs = message.voiceDurationMs,
                                playbackState = playbackState,
                                onPlay = { onPlayAudio(message.id, message.mediaUri ?: "", message.voiceDurationMs) },
                                onSeek = onSeekAudio,
                                isMe = isMe
                            )
                        }

                        message.messageType == MessageType.FILE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = "File",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.fileName ?: "Document",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val sizeKb = message.fileSize / 1024
                                    Text(
                                        text = if (sizeKb > 1024) "${sizeKb / 1024} MB" else "$sizeKb KB",
                                        fontSize = 11.sp,
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        else -> {
                            // Text message
                            Text(
                                text = message.content,
                                fontSize = 15.sp,
                                color = textColor,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Bottom info (time, edited badge, delivery ticks)
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.isEdited) {
                            Text(
                                text = "edited",
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        if (isMe) {
                            when (message.deliveryStatus) {
                                DeliveryStatus.SENDING -> {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Sending",
                                        modifier = Modifier.size(12.dp),
                                        tint = textColor.copy(alpha = 0.6f)
                                    )
                                }
                                DeliveryStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sent",
                                        modifier = Modifier.size(12.dp),
                                        tint = textColor.copy(alpha = 0.7f)
                                    )
                                }
                                DeliveryStatus.DELIVERED -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Delivered",
                                        modifier = Modifier.size(13.dp),
                                        tint = textColor.copy(alpha = 0.7f)
                                    )
                                }
                                DeliveryStatus.SEEN -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Seen",
                                        modifier = Modifier.size(13.dp),
                                        tint = SeenBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reactions Row anchored under bubble
            if (reactionsMap.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reactionsMap.forEach { (userId, emoji) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { onToggleReaction(message.id, emoji) }
                                )
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Long Press Context Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // Quick emoji reaction bar
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("❤️", "😂", "😮", "😢", "🔥", "🎉").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        onToggleReaction(message.id, emoji)
                                        showMenu = false
                                    }
                                )
                                .padding(4.dp)
                        )
                    }
                }

                DropdownMenuItem(
                    text = { Text("Reply") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Reply") },
                    onClick = {
                        onReply(message)
                        showMenu = false
                    }
                )

                if (message.messageType == MessageType.TEXT && !message.isDeleted) {
                    DropdownMenuItem(
                        text = { Text("Copy Text") },
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            showMenu = false
                        }
                    )
                }

                if (isMe && message.messageType == MessageType.TEXT && !message.isDeleted) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                        onClick = {
                            onEdit(message)
                            showMenu = false
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text(if (isMe) "Delete for everyone" else "Delete for me") },
                    onClick = {
                        onDelete(message, isMe)
                        showMenu = false
                    }
                )
            }
        }
    }
}
