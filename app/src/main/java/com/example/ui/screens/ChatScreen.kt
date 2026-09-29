package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageType
import com.example.media.MediaStorageHelper
import com.example.ui.components.ChatBubble
import com.example.ui.components.EmojiGifSheet
import com.example.ui.components.FriendshipStatusHeader
import com.example.ui.components.MediaAttachmentPicker
import com.example.ui.components.VoiceRecorderBar
import com.example.ui.viewmodel.KindredViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: KindredViewModel,
    onOpenDuoHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeUserId by viewModel.activeUserId.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val friendUser by viewModel.friendUser.collectAsState()
    val messages by viewModel.filteredMessages.collectAsState()
    val playbackState by viewModel.audioPlaybackState.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val recordingDurationSec by viewModel.recordingDurationSec.collectAsState()
    val replyingMessage by viewModel.replyingMessage.collectAsState()
    val editingMessage by viewModel.editingMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var showAttachmentPicker by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Activity Result Launchers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val stored = MediaStorageHelper.copyUriToInternalStorage(context, uri, "photos")
            if (stored != null) {
                viewModel.sendMediaMessage(
                    type = MessageType.IMAGE,
                    mediaUri = stored.localFilePath,
                    fileName = stored.originalFileName,
                    fileSize = stored.fileSize
                )
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val stored = MediaStorageHelper.copyUriToInternalStorage(context, uri, "documents")
            if (stored != null) {
                viewModel.sendMediaMessage(
                    type = MessageType.FILE,
                    mediaUri = stored.localFilePath,
                    fileName = stored.originalFileName,
                    fileSize = stored.fileSize
                )
            }
        }
    }

    // When editingMessage changes, update inputText
    LaunchedEffect(editingMessage) {
        editingMessage?.let {
            inputText = it.content
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            FriendshipStatusHeader(
                activeUser = activeUser,
                friendUser = friendUser,
                isSearching = isSearching,
                onToggleSearch = { viewModel.isSearching.value = !isSearching },
                onSwitchUser = { viewModel.switchUser() },
                onOpenDuoHub = onOpenDuoHub
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Search Bar & Filter Chips (Animated reveal)
            AnimatedVisibility(
                visible = isSearching,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Search messages & files...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("ALL", "PHOTOS", "VOICE", "FILES", "LINKS").forEach { filter ->
                                FilterChip(
                                    selected = searchFilter == filter,
                                    onClick = { viewModel.searchFilter.value = filter },
                                    label = { Text(filter, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💌", fontSize = 32.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your private sanctuary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Messages, voice notes, photos and memories are encrypted and private between only you two.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                isMe = message.senderId == activeUserId,
                                playbackState = playbackState,
                                onPlayAudio = { id, path, dur -> viewModel.playVoiceMessage(id, path, dur) },
                                onSeekAudio = { pos -> viewModel.seekVoicePlayback(pos) },
                                onReply = { viewModel.replyingMessage.value = it },
                                onEdit = { viewModel.editingMessage.value = it },
                                onDelete = { msg, forAll -> viewModel.deleteMessage(msg.id, forAll) },
                                onToggleReaction = { id, emoji -> viewModel.toggleReaction(id, emoji) }
                            )
                        }
                    }
                }
            }

            // Replying banner
            AnimatedVisibility(visible = replyingMessage != null) {
                replyingMessage?.let { reply ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(30.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to ${reply.senderName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = reply.content,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.replyingMessage.value = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Editing banner
            AnimatedVisibility(visible = editingMessage != null) {
                editingMessage?.let { edit ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✏️ Editing message",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                viewModel.editingMessage.value = null
                                inputText = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel edit", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Voice recording bar or Normal Input bar
            if (isRecordingVoice) {
                VoiceRecorderBar(
                    durationSeconds = recordingDurationSec,
                    onCancel = { viewModel.cancelVoiceRecording() },
                    onSend = { viewModel.stopAndSendVoiceRecording() }
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji / GIF button
                        IconButton(
                            onClick = { showEmojiSheet = !showEmojiSheet },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mood,
                                contentDescription = "Emojis and GIFs",
                                tint = if (showEmojiSheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Attach button
                        IconButton(
                            onClick = { showAttachmentPicker = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach media",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Text input box
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            TextField(
                                value = inputText,
                                onValueChange = {
                                    inputText = it
                                    viewModel.setMyTyping(it.isNotBlank())
                                },
                                placeholder = {
                                    Text(
                                        "Message ${friendUser?.displayName ?: "bestie"}...",
                                        fontSize = 14.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_input_field")
                            )
                        }

                        // Send or Mic button
                        if (editingMessage != null) {
                            IconButton(
                                onClick = {
                                    editingMessage?.let {
                                        viewModel.editMessage(it.id, inputText)
                                        inputText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("save_edit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Save edit",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.sendTextMessage(inputText)
                                    inputText = ""
                                    viewModel.setMyTyping(false)
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send message",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.startVoiceRecording() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Record voice note",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Emoji / GIF bottom sheet
            if (showEmojiSheet) {
                EmojiGifSheet(
                    onEmojiSelected = { emoji ->
                        inputText += emoji
                    },
                    onGifSelected = { gifUrl ->
                        viewModel.sendMediaMessage(
                            type = MessageType.GIF,
                            mediaUri = gifUrl
                        )
                        showEmojiSheet = false
                    }
                )
            }

            // Media attachment picker sheet
            if (showAttachmentPicker) {
                MediaAttachmentPicker(
                    onDismiss = { showAttachmentPicker = false },
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPickFile = {
                        filePickerLauncher.launch("*/*")
                    },
                    onPickMemory = {
                        onOpenDuoHub()
                    }
                )
            }
        }
    }
}
