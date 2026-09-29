package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatMessage::class,
        SharedMemory::class,
        BucketItem::class,
        SharedNote::class,
        DailyQuestion::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KindredDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun sharedMemoryDao(): SharedMemoryDao
    abstract fun bucketItemDao(): BucketItemDao
    abstract fun sharedNoteDao(): SharedNoteDao
    abstract fun dailyQuestionDao(): DailyQuestionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: KindredDatabase? = null

        fun getDatabase(context: Context): KindredDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KindredDatabase::class.java,
                    "kindred_chat_database.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getDatabase(context))
                        }
                    }
                })
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: KindredDatabase) {
            val now = System.currentTimeMillis()
            val oneHour = 3600000L
            val oneDay = 86400000L

            // 1. Seed User Profiles
            val userMe = UserProfile(
                userId = "user_me",
                displayName = "Alex",
                nickname = "Bestie",
                avatarEmoji = "🦊",
                statusText = "Counting down to our next roadtrip! 🎒",
                isOnline = true,
                lastActiveMillis = now,
                streakDays = 18,
                longestStreak = 45,
                themeName = "LAVENDER_DUSK"
            )
            val userFriend = UserProfile(
                userId = "user_friend",
                displayName = "Sam",
                nickname = "Soulmate",
                avatarEmoji = "🐼",
                statusText = "Listening to our shared playlist 🎧",
                isOnline = true,
                lastActiveMillis = now - 60000L,
                streakDays = 18,
                longestStreak = 45,
                themeName = "LAVENDER_DUSK"
            )
            database.userProfileDao().insertOrUpdateProfile(userMe)
            database.userProfileDao().insertOrUpdateProfile(userFriend)

            // 2. Seed Messages
            val messages = listOf(
                ChatMessage(
                    id = 1,
                    senderId = "user_friend",
                    senderName = "Sam",
                    receiverId = "user_me",
                    content = "Hey Alex! Look at our streak today! 🔥 18 days in a row!",
                    messageType = MessageType.TEXT,
                    timestamp = now - (oneHour * 4),
                    deliveryStatus = DeliveryStatus.SEEN,
                    seenTimestamp = now - (oneHour * 3),
                    reactions = "user_me:🔥;user_friend:🎉"
                ),
                ChatMessage(
                    id = 2,
                    senderId = "user_me",
                    senderName = "Alex",
                    receiverId = "user_friend",
                    content = "OMG yes!! We cannot break this one. 100 days is the goal! 🚀",
                    messageType = MessageType.TEXT,
                    timestamp = now - (oneHour * 3) + 60000L,
                    deliveryStatus = DeliveryStatus.SEEN,
                    seenTimestamp = now - (oneHour * 2),
                    reactions = "user_friend:❤️"
                ),
                ChatMessage(
                    id = 3,
                    senderId = "user_friend",
                    senderName = "Sam",
                    receiverId = "user_me",
                    content = "Did you check today's friendship question in the Duo Hub? It's hilarious 😂",
                    messageType = MessageType.TEXT,
                    timestamp = now - (oneHour * 2),
                    deliveryStatus = DeliveryStatus.SEEN,
                    seenTimestamp = now - oneHour,
                    reactions = "user_me:😂"
                ),
                ChatMessage(
                    id = 4,
                    senderId = "user_me",
                    senderName = "Alex",
                    receiverId = "user_friend",
                    content = "Answering it right now! Also don't forget to check our bucket list, I added the stargazing trip for next weekend 🌌✨",
                    messageType = MessageType.TEXT,
                    timestamp = now - (oneHour * 1),
                    deliveryStatus = DeliveryStatus.SEEN,
                    seenTimestamp = now - (oneHour / 2),
                    reactions = "user_friend:🙌"
                ),
                ChatMessage(
                    id = 5,
                    senderId = "user_friend",
                    senderName = "Sam",
                    receiverId = "user_me",
                    content = "Yesss stargazing! Packing the marshmallows already 🏕️🍡",
                    messageType = MessageType.TEXT,
                    timestamp = now - 180000L,
                    deliveryStatus = DeliveryStatus.DELIVERED,
                    reactions = "user_me:❤️"
                )
            )
            for (m in messages) {
                database.chatMessageDao().insertMessage(m)
            }

            // 3. Seed Memories
            val memories = listOf(
                SharedMemory(
                    id = 1,
                    title = "Midnight Taco Run 🌮",
                    description = "Driving across town at 1 AM singing 2010 pop hits at the top of our lungs.",
                    dateMillis = now - (oneDay * 12),
                    location = "Downtown Taqueria",
                    tag = "Food",
                    heartCount = 12,
                    isFavorite = true,
                    createdBy = "user_friend"
                ),
                SharedMemory(
                    id = 2,
                    title = "Sunrise Over the Cliffs 🌅",
                    description = "Woke up at 4:30 AM freezing cold, but that golden sunrise was totally unforgettable.",
                    dateMillis = now - (oneDay * 45),
                    location = "Pacific Coast Bluffs",
                    tag = "Trip",
                    heartCount = 24,
                    isFavorite = true,
                    createdBy = "user_me"
                ),
                SharedMemory(
                    id = 3,
                    title = "First Escape Room Victory 🗝️",
                    description = "Escaped the haunted library with literally 38 seconds remaining on the clock!",
                    dateMillis = now - (oneDay * 90),
                    location = "Cipher Room Games",
                    tag = "Milestone",
                    heartCount = 19,
                    isFavorite = false,
                    createdBy = "user_friend"
                )
            )
            for (mem in memories) {
                database.sharedMemoryDao().insertMemory(mem)
            }

            // 4. Seed Bucket List
            val bucketList = listOf(
                BucketItem(
                    id = 1,
                    title = "Watch Meteor Shower Under the Stars 🌠",
                    description = "Drive up to the mountain observatory with hot cocoa and camp out.",
                    category = "Adventure",
                    isCompleted = false,
                    targetDateMillis = now + (oneDay * 14)
                ),
                BucketItem(
                    id = 2,
                    title = "Learn to Make Sourdough Bread from Scratch 🥖",
                    description = "Bake crispy artisan loaves and judge whose is better.",
                    category = "Food & Drink",
                    isCompleted = true,
                    completedDateMillis = now - (oneDay * 20)
                ),
                BucketItem(
                    id = 3,
                    title = "Tokyo & Kyoto Cherry Blossom Trip 🌸🏯",
                    description = "Ride the bullet train, eat ramen in side alleys, visit Ghibli Park.",
                    category = "Travel",
                    isCompleted = false,
                    targetDateMillis = now + (oneDay * 180)
                ),
                BucketItem(
                    id = 4,
                    title = "12-Hour Movie Marathon with Cozy Fort 🍿🎬",
                    description = "Build the ultimate living room blanket fort and watch the entire LOTR trilogy extended edition.",
                    category = "Culture & Fun",
                    isCompleted = true,
                    completedDateMillis = now - (oneDay * 5)
                )
            )
            for (b in bucketList) {
                database.bucketItemDao().insertBucketItem(b)
            }

            // 5. Seed Shared Notes
            val notes = listOf(
                SharedNote(
                    id = 1,
                    title = "Our Roadtrip Packing Checklist 🎒🚗",
                    content = "• Bluetooth car adapter\n• Portable tire inflator\n• Sour gummy worms & iced tea\n• Emergency fleece blankets\n• Polaroid camera + 3 film packs\n• Offline Spotify playlist downloaded!",
                    colorHex = "#FFE082", // warm yellow
                    isPinned = true,
                    lastUpdatedMillis = now - (oneDay * 2),
                    isChecklist = true
                ),
                SharedNote(
                    id = 2,
                    title = "Best Inside Jokes & Quotes 💬😂",
                    content = "1. 'Wait, is the salsa supposed to sizzle?'\n2. 'It's not lost, it's just temporarily misplaced in the multiverse.'\n3. 'GPS says turn left into the lake... should I?'",
                    colorHex = "#E1BEE7", // lilac
                    isPinned = true,
                    lastUpdatedMillis = now - (oneDay * 8)
                ),
                SharedNote(
                    id = 3,
                    title = "Boba Order Cheat Sheet 🧋",
                    content = "Alex: Brown sugar milk tea with oat milk, 30% sweetness, boba + egg pudding\n\nSam: Taro milk tea, 50% sweetness, grass jelly + crystal boba",
                    colorHex = "#C8E6C9", // soft mint
                    isPinned = false,
                    lastUpdatedMillis = now - (oneDay * 15)
                )
            )
            for (n in notes) {
                database.sharedNoteDao().insertNote(n)
            }

            // 6. Seed Daily Questions
            val questions = listOf(
                DailyQuestion(
                    id = 1,
                    dateKey = "2026-09-28",
                    questionText = "If we were contestants on The Amazing Race, which one of us would lose the map?",
                    category = "Funny Memories",
                    friend1Answer = "Definitely you, Sam! Remember our hike in 2024?! 😂",
                    friend2Answer = "Hey!! In my defense the GPS took us through that random cornfield!",
                    friend1AnswerTime = now - (oneHour * 2),
                    friend2AnswerTime = now - (oneHour * 1),
                    isRevealed = true
                ),
                DailyQuestion(
                    id = 2,
                    dateKey = "2026-09-27",
                    questionText = "What song instantly makes you think of our friendship?",
                    category = "Friendship Bond",
                    friend1Answer = "Tongue Tied by Grouplove without a doubt 🎶",
                    friend2Answer = "Midnight City! Whenever that synth drop hits!",
                    friend1AnswerTime = now - (oneDay + oneHour),
                    friend2AnswerTime = now - (oneDay + 2 * oneHour),
                    isRevealed = true
                ),
                DailyQuestion(
                    id = 3,
                    dateKey = "2026-09-29",
                    questionText = "What is one dream you haven't told anyone else yet?",
                    category = "Deep Talk",
                    friend1Answer = null,
                    friend2Answer = "Starting a cozy coffee and book café by the sea with a rooftop garden ☕🌊",
                    friend1AnswerTime = null,
                    friend2AnswerTime = now - (oneHour / 4),
                    isRevealed = false
                )
            )
            for (q in questions) {
                database.dailyQuestionDao().insertQuestion(q)
            }
        }
    }
}
