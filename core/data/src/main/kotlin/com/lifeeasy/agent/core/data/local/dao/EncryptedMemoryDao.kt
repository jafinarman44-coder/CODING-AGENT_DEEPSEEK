// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeeasy.agent.core.data.local.entity.EncryptedMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EncryptedMemoryDao {
    @Query("SELECT * FROM encrypted_memory ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<EncryptedMemoryEntity>>

    @Query("SELECT * FROM encrypted_memory WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): EncryptedMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EncryptedMemoryEntity)

    @Query("DELETE FROM encrypted_memory WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM encrypted_memory")
    suspend fun deleteAll()
}
