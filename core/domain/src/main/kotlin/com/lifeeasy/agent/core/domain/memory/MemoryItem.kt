// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.domain.memory

/** Decrypted in-memory representation. Never persist this model directly. */
data class MemoryItem(
    val id: String,
    val content: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
