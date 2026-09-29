package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SharedMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedMemoryDao {
    @Query("SELECT * FROM shared_memories ORDER BY dateMillis DESC")
    fun getAllMemoriesFlow(): Flow<List<SharedMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: SharedMemory): Long

    @Update
    suspend fun updateMemory(memory: SharedMemory)

    @Query("DELETE FROM shared_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("UPDATE shared_memories SET heartCount = heartCount + 1 WHERE id = :id")
    suspend fun incrementHeart(id: Long)

    @Query("UPDATE shared_memories SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("SELECT COUNT(*) FROM shared_memories")
    suspend fun getMemoryCount(): Int
}
