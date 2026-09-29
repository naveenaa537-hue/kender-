package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BucketItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BucketItemDao {
    @Query("SELECT * FROM bucket_items ORDER BY isCompleted ASC, id DESC")
    fun getAllBucketItemsFlow(): Flow<List<BucketItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBucketItem(item: BucketItem): Long

    @Update
    suspend fun updateBucketItem(item: BucketItem)

    @Query("DELETE FROM bucket_items WHERE id = :id")
    suspend fun deleteBucketItem(id: Long)

    @Query("UPDATE bucket_items SET isCompleted = :completed, completedDateMillis = :completedTime WHERE id = :id")
    suspend fun updateCompletion(id: Long, completed: Boolean, completedTime: Long?)
}
