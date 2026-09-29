package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KindredApp
import com.example.data.model.BucketItem
import com.example.data.model.ChatMessage
import com.example.data.model.DailyQuestion
import com.example.data.model.MessageType
import com.example.data.model.SharedMemory
import com.example.data.model.SharedNote
import com.example.data.model.UserProfile
import com.example.media.AudioPlayerManager
import com.example.media.AudioRecorderManager
import com.example.media.PlaybackState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KindredViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as KindredApp
    private val repository = app.repository

    val activeUserId: StateFlow<String> = repository.activeUserId
    val isAutoFriendResponseEnabled: StateFlow<Boolean> = repository.isAutoFriendResponseEnabled

    private val audioRecorderManager = AudioRecorderManager(application)
    private val audioPlayerManager = AudioPlayerManager()

    val audioPlaybackState: StateFlow<PlaybackState> = audioPlayerManager.playbackState

    val isRecordingVoice = MutableStateFlow(false)
    val recordingDurationSec = MutableStateFlow(0)
    private var recordTimerJob: Job? = null

    // Navigation & UI state
    val selectedTab = MutableStateFlow(0) // 0: Chat, 1: Memories, 2: Bucket, 3: Notes, 4: Duo Hub, 5: Settings
    val searchQuery = MutableStateFlow("")
    val searchFilter = MutableStateFlow("ALL") // ALL, PHOTOS, VOICE, FILES, LINKS
    val replyingMessage = MutableStateFlow<ChatMessage?>(null)
    val editingMessage = MutableStateFlow<ChatMessage?>(null)
    val isSearching = MutableStateFlow(false)

    // Security PIN Lock
    val isLocked = MutableStateFlow(false)

    // Data streams
    val allMessages: StateFlow<List<ChatMessage>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMessages: StateFlow<List<ChatMessage>> = combine(
        allMessages,
        searchQuery,
        searchFilter
    ) { messages, query, filter ->
        var list = messages
        if (query.isNotBlank()) {
            list = list.filter {
                it.content.contains(query, ignoreCase = true) ||
                        (it.fileName?.contains(query, ignoreCase = true) == true)
            }
        }
        when (filter) {
            "PHOTOS" -> list.filter { it.messageType == MessageType.IMAGE || it.messageType == MessageType.GIF }
            "VOICE" -> list.filter { it.messageType == MessageType.VOICE }
            "FILES" -> list.filter { it.messageType == MessageType.FILE }
            "LINKS" -> list.filter { it.content.contains("http://") || it.content.contains("https://") }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProfiles: StateFlow<List<UserProfile>> = repository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeUser: StateFlow<UserProfile?> = combine(allProfiles, activeUserId) { profiles, id ->
        profiles.firstOrNull { it.userId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val friendUser: StateFlow<UserProfile?> = combine(allProfiles, activeUserId) { profiles, id ->
        profiles.firstOrNull { it.userId != id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sharedMemories: StateFlow<List<SharedMemory>> = repository.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bucketItems: StateFlow<List<BucketItem>> = repository.getAllBucketItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedNotes: StateFlow<List<SharedNote>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyQuestions: StateFlow<List<DailyQuestion>> = repository.getAllQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayQuestion: StateFlow<DailyQuestion?> = dailyQuestions.combine(MutableStateFlow(Unit)) { questions, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        questions.firstOrNull { it.dateKey == todayStr } ?: questions.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            // Check if PIN lock is enabled on startup
            activeUser.collect { user ->
                if (user?.isPinLockEnabled == true && !user.pinCode.isNullOrBlank()) {
                    isLocked.value = true
                }
            }
        }
    }

    // Persona Switching
    fun switchUser() {
        val next = if (activeUserId.value == "user_me") "user_friend" else "user_me"
        repository.switchActiveUser(next)
    }

    // Messages
    fun sendTextMessage(content: String) {
        if (content.isBlank()) return
        val reply = replyingMessage.value
        viewModelScope.launch {
            repository.sendMessage(
                content = content.trim(),
                type = MessageType.TEXT,
                replyTo = reply
            )
            replyingMessage.value = null
        }
    }

    fun sendMediaMessage(
        type: MessageType,
        mediaUri: String,
        fileName: String? = null,
        fileSize: Long = 0L,
        voiceDurationMs: Long = 0L,
        caption: String = ""
    ) {
        val reply = replyingMessage.value
        viewModelScope.launch {
            repository.sendMessage(
                content = caption.ifBlank {
                    when (type) {
                        MessageType.IMAGE -> "Photo"
                        MessageType.VOICE -> "Voice Message"
                        MessageType.FILE -> fileName ?: "File"
                        MessageType.GIF -> "GIF"
                        else -> ""
                    }
                },
                type = type,
                mediaUri = mediaUri,
                fileName = fileName,
                fileSize = fileSize,
                voiceDurationMs = voiceDurationMs,
                replyTo = reply
            )
            replyingMessage.value = null
        }
    }

    fun editMessage(id: Long, newText: String) {
        if (newText.isBlank()) return
        viewModelScope.launch {
            repository.editMessage(id, newText.trim())
            editingMessage.value = null
        }
    }

    fun deleteMessage(id: Long, forEveryone: Boolean) {
        viewModelScope.launch {
            repository.deleteMessage(id, forEveryone)
        }
    }

    fun toggleReaction(messageId: Long, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(messageId, emoji)
        }
    }

    // Voice recording
    fun startVoiceRecording() {
        val started = audioRecorderManager.startRecording()
        if (started) {
            isRecordingVoice.value = true
            recordingDurationSec.value = 0
            recordTimerJob?.cancel()
            recordTimerJob = viewModelScope.launch {
                while (isActive && isRecordingVoice.value) {
                    delay(1000)
                    recordingDurationSec.value += 1
                }
            }
        }
    }

    fun stopAndSendVoiceRecording() {
        recordTimerJob?.cancel()
        isRecordingVoice.value = false
        val (file, duration) = audioRecorderManager.stopRecording()
        if (file != null && file.exists() && duration > 500) {
            sendMediaMessage(
                type = MessageType.VOICE,
                mediaUri = file.absolutePath,
                fileName = file.name,
                fileSize = file.length(),
                voiceDurationMs = duration
            )
        }
        recordingDurationSec.value = 0
    }

    fun cancelVoiceRecording() {
        recordTimerJob?.cancel()
        isRecordingVoice.value = false
        recordingDurationSec.value = 0
        audioRecorderManager.cancelRecording()
    }

    // Voice Playback
    fun playVoiceMessage(messageId: Long, filePath: String, durationMs: Long) {
        audioPlayerManager.playAudio(messageId, filePath, durationMs)
    }

    fun seekVoicePlayback(positionMs: Long) {
        audioPlayerManager.seekTo(positionMs)
    }

    fun stopVoicePlayback() {
        audioPlayerManager.stopAudio()
    }

    // Memories
    fun addMemory(title: String, description: String, imageUri: String?, location: String, tag: String) {
        viewModelScope.launch {
            repository.addMemory(
                title = title.trim(),
                description = description.trim(),
                imageUri = imageUri,
                dateMillis = System.currentTimeMillis(),
                location = location.trim(),
                tag = tag.ifBlank { "Memory" }
            )
        }
    }

    fun toggleMemoryFavorite(id: Long) {
        viewModelScope.launch { repository.toggleMemoryFavorite(id) }
    }

    fun incrementMemoryHeart(id: Long) {
        viewModelScope.launch { repository.incrementMemoryHeart(id) }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch { repository.deleteMemory(id) }
    }

    // Bucket List
    fun addBucketItem(title: String, description: String, category: String, targetDateMillis: Long?) {
        viewModelScope.launch {
            repository.addBucketItem(
                title = title.trim(),
                description = description.trim(),
                category = category,
                targetDateMillis = targetDateMillis
            )
        }
    }

    fun toggleBucketItem(id: Long, isCompleted: Boolean) {
        viewModelScope.launch { repository.toggleBucketItemCompletion(id, isCompleted) }
    }

    fun deleteBucketItem(id: Long) {
        viewModelScope.launch { repository.deleteBucketItem(id) }
    }

    // Shared Notes
    fun addNote(title: String, content: String, colorHex: String, isChecklist: Boolean) {
        viewModelScope.launch {
            repository.addNote(
                title = title.trim(),
                content = content.trim(),
                colorHex = colorHex,
                isChecklist = isChecklist
            )
        }
    }

    fun updateNote(note: SharedNote) {
        viewModelScope.launch { repository.updateNote(note) }
    }

    fun toggleNotePin(id: Long) {
        viewModelScope.launch { repository.toggleNotePin(id) }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    // Daily Questions
    fun answerDailyQuestion(questionId: Long, answer: String) {
        if (answer.isBlank()) return
        viewModelScope.launch {
            repository.answerDailyQuestion(questionId, answer.trim())
        }
    }

    fun revealDailyQuestion(questionId: Long) {
        viewModelScope.launch {
            repository.revealQuestionManually(questionId)
        }
    }

    // Settings & Profile
    fun updateProfile(displayName: String, nickname: String, statusText: String, avatarEmoji: String) {
        val current = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(
                current.copy(
                    displayName = displayName.trim(),
                    nickname = nickname.trim(),
                    statusText = statusText.trim(),
                    avatarEmoji = avatarEmoji
                )
            )
        }
    }

    fun setTheme(themeName: String) {
        val current = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(current.copy(themeName = themeName))
        }
    }

    fun setDarkMode(isDark: Boolean, isSystem: Boolean) {
        val current = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(current.copy(isDarkMode = isDark, isSystemTheme = isSystem))
        }
    }

    fun setPin(pin: String?) {
        val current = activeUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(
                current.copy(
                    pinCode = pin,
                    isPinLockEnabled = !pin.isNullOrBlank()
                )
            )
        }
    }

    fun unlockApp(pin: String): Boolean {
        val currentPin = activeUser.value?.pinCode
        return if (currentPin == null || currentPin == pin) {
            isLocked.value = false
            true
        } else {
            false
        }
    }

    fun toggleFriendOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            repository.toggleFriendOnline(isOnline)
        }
    }

    fun toggleAutoFriendResponse(enabled: Boolean) {
        repository.isAutoFriendResponseEnabled.value = enabled
    }

    fun setMyTyping(isTyping: Boolean) {
        viewModelScope.launch {
            repository.updateMyTyping(isTyping)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        audioRecorderManager.cancelRecording()
    }
}
