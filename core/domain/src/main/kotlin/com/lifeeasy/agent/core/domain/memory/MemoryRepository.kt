// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.domain.memory

import kotlinx.coroutines.flow.Flow

/** Domain contract for encrypted persistent memories. */
interface MemoryRepository {
    fun observeMemories(): Flow<List<MemoryItem>>
    suspend fun save(id: String, content: String)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}
