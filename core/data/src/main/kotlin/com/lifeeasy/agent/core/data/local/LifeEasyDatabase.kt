// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lifeeasy.agent.core.data.local.dao.EncryptedMemoryDao
import com.lifeeasy.agent.core.data.local.entity.EncryptedMemoryEntity

@Database(
    entities = [EncryptedMemoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LifeEasyDatabase : RoomDatabase() {
    abstract fun encryptedMemoryDao(): EncryptedMemoryDao
}
