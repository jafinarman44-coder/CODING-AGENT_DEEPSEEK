// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Only authenticated ciphertext is persisted; plaintext memory is never a Room column. */
@Entity(tableName = "encrypted_memory")
data class EncryptedMemoryEntity(
    @PrimaryKey val id: String,
    val ciphertextBase64: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
