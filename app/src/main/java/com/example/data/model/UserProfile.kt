package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String, // "user_me" or "user_friend"
    val displayName: String,
    val nickname: String = "",
    val avatarUri: String? = null,
    val avatarEmoji: String = "🦊",
    val statusText: String = "Here for the memories ✨",
    val isOnline: Boolean = true,
    val lastActiveMillis: Long = System.currentTimeMillis(),
    val isTyping: Boolean = false,
    val streakDays: Int = 14,
    val longestStreak: Int = 42,
    val lastInteractionDate: String = "2026-09-28",
    val anniversaryDateMillis: Long = 1695859200000L, // ~1 year friendship anniversary
    val themeName: String = "LAVENDER_DUSK",
    val isDarkMode: Boolean = false,
    val isSystemTheme: Boolean = true,
    val pinCode: String? = null,
    val isPinLockEnabled: Boolean = false,
    val notificationSound: Boolean = true,
    val messagePreview: Boolean = true,
    val streakReminder: Boolean = true
)
