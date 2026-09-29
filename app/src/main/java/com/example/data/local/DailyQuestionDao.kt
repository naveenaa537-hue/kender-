package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyQuestionDao {
    @Query("SELECT * FROM daily_questions ORDER BY dateKey DESC")
    fun getAllQuestionsFlow(): Flow<List<DailyQuestion>>

    @Query("SELECT * FROM daily_questions WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getQuestionByDate(dateKey: String): DailyQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: DailyQuestion): Long

    @Update
    suspend fun updateQuestion(question: DailyQuestion)

    @Query("UPDATE daily_questions SET friend1Answer = :answer, friend1AnswerTime = :time WHERE id = :id")
    suspend fun submitFriend1Answer(id: Long, answer: String, time: Long)

    @Query("UPDATE daily_questions SET friend2Answer = :answer, friend2AnswerTime = :time WHERE id = :id")
    suspend fun submitFriend2Answer(id: Long, answer: String, time: Long)

    @Query("UPDATE daily_questions SET isRevealed = 1 WHERE id = :id")
    suspend fun revealQuestion(id: Long)
}
