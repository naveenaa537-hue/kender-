package com.example.data.repository

import android.content.Context
import com.example.data.local.KindredDatabase
import com.example.data.model.BucketItem
import com.example.data.model.ChatMessage
import com.example.data.model.DailyQuestion
import com.example.data.model.DeliveryStatus
import com.example.data.model.MessageType
import com.example.data.model.SharedMemory
import com.example.data.model.SharedNote
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KindredRepository(
    private val database: KindredDatabase,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current active persona for testing and real chat interaction ("user_me" or "user_friend")
    private val _activeUserId = MutableStateFlow("user_me")
    val activeUserId: StateFlow<String> = _activeUserId.asStateFlow()

    // Simulation toggle: whether friend auto-replies or typing indicator simulates
    val isAutoFriendResponseEnabled = MutableStateFlow(true)

    fun switchActiveUser(newUserId: String) {
        _activeUserId.value = newUserId
        scope.launch {
            // When switching to a user, mark messages addressed to them as SEEN
            markMessagesSeenForUser(newUserId)
        }
    }

    // Messages
    fun getAllMessages(): Flow<List<ChatMessage>> = database.chatMessageDao().getAllMessagesFlow()

    fun searchMessages(query: String): Flow<List<ChatMessage>> =
        database.chatMessageDao().searchMessages(query)

    suspend fun sendMessage(
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUri: String? = null,
        fileName: String? = null,
        fileSize: Long = 0L,
        voiceDurationMs: Long = 0L,
        replyTo: ChatMessage? = null
    ): Long {
        val currentSender = _activeUserId.value
        val receiver = if (currentSender == "user_me") "user_friend" else "user_me"
        val senderProfile = database.userProfileDao().getUserProfile(currentSender)
        val senderName = senderProfile?.displayName ?: if (currentSender == "user_me") "Alex" else "Sam"

        val message = ChatMessage(
            senderId = currentSender,
            senderName = senderName,
            receiverId = receiver,
            content = content,
            messageType = type,
            mediaUri = mediaUri,
            fileName = fileName,
            fileSize = fileSize,
            voiceDurationMs = voiceDurationMs,
            timestamp = System.currentTimeMillis(),
            replyToMessageId = replyTo?.id,
            replyToContent = replyTo?.let {
                when (it.messageType) {
                    MessageType.TEXT -> it.content
                    MessageType.IMAGE -> "📷 Photo"
                    MessageType.VOICE -> "🎤 Voice note"
                    MessageType.FILE -> "📎 ${it.fileName ?: "File"}"
                    MessageType.GIF -> "GIF"
                }
            },
            replyToSender = replyTo?.senderName,
            deliveryStatus = DeliveryStatus.SENT
        )

        val insertedId = database.chatMessageDao().insertMessage(message)
        updateFriendshipStreakOnInteraction()

        // Real-time delivery & seen simulation for friend
        scheduleDeliveryAndFriendReaction(insertedId, receiver, content, type)

        return insertedId
    }

    private fun scheduleDeliveryAndFriendReaction(
        messageId: Long,
        receiverId: String,
        content: String,
        type: MessageType
    ) {
        scope.launch {
            // Step 1: Delivered
            delay(600)
            database.chatMessageDao().updateDeliveryStatus(messageId, DeliveryStatus.DELIVERED)

            val friendProfile = database.userProfileDao().getUserProfile(receiverId)
            if (friendProfile?.isOnline == true) {
                // Step 2: Friend views the message
                delay(1200)
                database.chatMessageDao().updateDeliveryStatus(messageId, DeliveryStatus.SEEN)
                database.userProfileDao().updateOnlineStatus(receiverId, true, System.currentTimeMillis())

                // Step 3: If auto-friend interaction enabled and active user is still sender, show typing and reply
                if (isAutoFriendResponseEnabled.value && _activeUserId.value != receiverId) {
                    delay(1500)
                    database.userProfileDao().updateTypingStatus(receiverId, true)
                    delay(2500)
                    database.userProfileDao().updateTypingStatus(receiverId, false)

                    val autoReply = generateSmartFriendReply(content, type)
                    val friendName = friendProfile.displayName
                    val replyMessage = ChatMessage(
                        senderId = receiverId,
                        senderName = friendName,
                        receiverId = _activeUserId.value,
                        content = autoReply,
                        messageType = MessageType.TEXT,
                        timestamp = System.currentTimeMillis(),
                        deliveryStatus = DeliveryStatus.SENT
                    )
                    val replyId = database.chatMessageDao().insertMessage(replyMessage)
                    delay(500)
                    database.chatMessageDao().updateDeliveryStatus(replyId, DeliveryStatus.DELIVERED)
                }
            }
        }
    }

    private fun generateSmartFriendReply(text: String, type: MessageType): String {
        val lower = text.lowercase()
        return when {
            type == MessageType.VOICE -> "Listening to your voice note right now! Love hearing your voice bestie 🎙️❤️"
            type == MessageType.IMAGE -> "Awww this photo is stunning!! Saving this to our memories right away 📸✨"
            type == MessageType.GIF -> "Hahaha you always have the best GIFs ready 😂👏"
            type == MessageType.FILE -> "Got the file! Checking it out now 📄👍"
            lower.contains("hello") || lower.contains("hey") || lower.contains("hi") -> "Heyyy! So glad you messaged! How's your day going? ✨"
            lower.contains("streak") -> "Our streak is untouchable! We're legendary 🔥🙌"
            lower.contains("question") || lower.contains("duo") -> "Checking the Duo Hub right now! Can't wait to see your answer 😄"
            lower.contains("bucket") || lower.contains("trip") -> "YESSS! Let's definitely do it! Adding it to the planner 🎒"
            lower.contains("love") || lower.contains("miss") -> "Awww love you so much bestie! Friendship of a lifetime 💖"
            lower.contains("food") || lower.contains("eat") || lower.contains("hungry") -> "Don't tempt me... I'm already craving boba and tacos! 🧋🌮"
            else -> "Haha absolutely! You always make my day brighter ✨ What else are we planning for this week?"
        }
    }

    suspend fun markMessagesSeenForUser(userId: String) {
        val now = System.currentTimeMillis()
        database.chatMessageDao().markMessagesAsSeen(userId, now)
    }

    suspend fun editMessage(id: Long, newContent: String) {
        database.chatMessageDao().editMessageContent(id, newContent)
    }

    suspend fun deleteMessage(id: Long, forEveryone: Boolean) {
        if (forEveryone) {
            database.chatMessageDao().softDeleteMessage(id)
        } else {
            database.chatMessageDao().hardDeleteMessage(id)
        }
    }

    suspend fun toggleReaction(messageId: Long, emoji: String) {
        val msg = database.chatMessageDao().getMessageById(messageId) ?: return
        val currentReactions = msg.parseReactions().toMutableMap()
        val currentUserId = _activeUserId.value

        if (currentReactions[currentUserId] == emoji) {
            currentReactions.remove(currentUserId)
        } else {
            currentReactions[currentUserId] = emoji
        }

        val serialized = currentReactions.entries.joinToString(";") { "${it.key}:${it.value}" }
        database.chatMessageDao().updateReactions(messageId, serialized)
    }

    // Memories
    fun getAllMemories(): Flow<List<SharedMemory>> = database.sharedMemoryDao().getAllMemoriesFlow()

    suspend fun addMemory(
        title: String,
        description: String,
        imageUri: String?,
        dateMillis: Long,
        location: String,
        tag: String
    ): Long {
        val memory = SharedMemory(
            title = title,
            description = description,
            imageUri = imageUri,
            dateMillis = dateMillis,
            location = location,
            tag = tag,
            createdBy = _activeUserId.value
        )
        return database.sharedMemoryDao().insertMemory(memory)
    }

    suspend fun toggleMemoryFavorite(id: Long) = database.sharedMemoryDao().toggleFavorite(id)
    suspend fun incrementMemoryHeart(id: Long) = database.sharedMemoryDao().incrementHeart(id)
    suspend fun deleteMemory(id: Long) = database.sharedMemoryDao().deleteMemory(id)

    // Bucket List
    fun getAllBucketItems(): Flow<List<BucketItem>> = database.bucketItemDao().getAllBucketItemsFlow()

    suspend fun addBucketItem(
        title: String,
        description: String,
        category: String,
        targetDateMillis: Long? = null
    ): Long {
        val item = BucketItem(
            title = title,
            description = description,
            category = category,
            targetDateMillis = targetDateMillis,
            createdBy = _activeUserId.value
        )
        return database.bucketItemDao().insertBucketItem(item)
    }

    suspend fun toggleBucketItemCompletion(id: Long, isCompleted: Boolean) {
        val completedTime = if (isCompleted) System.currentTimeMillis() else null
        database.bucketItemDao().updateCompletion(id, isCompleted, completedTime)
    }

    suspend fun deleteBucketItem(id: Long) = database.bucketItemDao().deleteBucketItem(id)

    // Shared Notes
    fun getAllNotes(): Flow<List<SharedNote>> = database.sharedNoteDao().getAllNotesFlow()

    suspend fun addNote(
        title: String,
        content: String,
        colorHex: String,
        isChecklist: Boolean
    ): Long {
        val note = SharedNote(
            title = title,
            content = content,
            colorHex = colorHex,
            isChecklist = isChecklist,
            createdBy = _activeUserId.value
        )
        return database.sharedNoteDao().insertNote(note)
    }

    suspend fun updateNote(note: SharedNote) = database.sharedNoteDao().updateNote(note)
    suspend fun toggleNotePin(id: Long) = database.sharedNoteDao().togglePin(id)
    suspend fun deleteNote(id: Long) = database.sharedNoteDao().deleteNote(id)

    // Daily Questions
    fun getAllQuestions(): Flow<List<DailyQuestion>> = database.dailyQuestionDao().getAllQuestionsFlow()

    suspend fun answerDailyQuestion(questionId: Long, answer: String) {
        val now = System.currentTimeMillis()
        if (_activeUserId.value == "user_me") {
            database.dailyQuestionDao().submitFriend1Answer(questionId, answer, now)
        } else {
            database.dailyQuestionDao().submitFriend2Answer(questionId, answer, now)
        }
        // Check if both answered; if so reveal automatically
        val all = database.dailyQuestionDao().getAllQuestionsFlow()
        // Or trigger reveal
        database.dailyQuestionDao().revealQuestion(questionId)
    }

    suspend fun revealQuestionManually(questionId: Long) {
        database.dailyQuestionDao().revealQuestion(questionId)
    }

    // Profiles & Friendship Streaks
    fun getUserProfile(userId: String): Flow<UserProfile?> =
        database.userProfileDao().getUserProfileFlow(userId)

    fun getAllProfiles(): Flow<List<UserProfile>> =
        database.userProfileDao().getAllProfilesFlow()

    suspend fun updateProfile(profile: UserProfile) =
        database.userProfileDao().updateProfile(profile)

    suspend fun toggleFriendOnline(isOnline: Boolean) {
        val friendId = if (_activeUserId.value == "user_me") "user_friend" else "user_me"
        database.userProfileDao().updateOnlineStatus(friendId, isOnline, System.currentTimeMillis())
    }

    suspend fun updateMyTyping(isTyping: Boolean) {
        database.userProfileDao().updateTypingStatus(_activeUserId.value, isTyping)
    }

    private suspend fun updateFriendshipStreakOnInteraction() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val profileMe = database.userProfileDao().getUserProfile("user_me")
        if (profileMe != null && profileMe.lastInteractionDate != todayStr) {
            val newStreak = profileMe.streakDays + 1
            val longest = maxOf(newStreak, profileMe.longestStreak)
            database.userProfileDao().updateStreak("user_me", newStreak, todayStr)
            database.userProfileDao().updateStreak("user_friend", newStreak, todayStr)
        }
    }
}
