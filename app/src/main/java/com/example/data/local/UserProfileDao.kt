package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserProfileFlow(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET isOnline = :isOnline, lastActiveMillis = :lastActive WHERE userId = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastActive: Long)

    @Query("UPDATE user_profiles SET isTyping = :isTyping WHERE userId = :userId")
    suspend fun updateTypingStatus(userId: String, isTyping: Boolean)

    @Query("UPDATE user_profiles SET streakDays = :streakDays, lastInteractionDate = :lastDate WHERE userId = :userId")
    suspend fun updateStreak(userId: String, streakDays: Int, lastDate: String)

    @Query("UPDATE user_profiles SET themeName = :themeName, isDarkMode = :isDark, isSystemTheme = :isSystem WHERE userId = :userId")
    suspend fun updateTheme(userId: String, themeName: String, isDark: Boolean, isSystem: Boolean)

    @Query("UPDATE user_profiles SET pinCode = :pin, isPinLockEnabled = :isEnabled WHERE userId = :userId")
    suspend fun updatePin(userId: String, pin: String?, isEnabled: Boolean)
}
