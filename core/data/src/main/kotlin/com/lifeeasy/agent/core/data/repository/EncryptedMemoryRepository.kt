// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.data.repository

import com.lifeeasy.agent.core.data.local.dao.EncryptedMemoryDao
import com.lifeeasy.agent.core.data.local.entity.EncryptedMemoryEntity
import com.lifeeasy.agent.core.domain.memory.MemoryItem
import com.lifeeasy.agent.core.domain.memory.MemoryRepository
import com.lifeeasy.agent.core.security.AndroidKeystoreCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Encrypts before persistence and decrypts only when a consumer observes/reads memories. */
class EncryptedMemoryRepository(
    private val dao: EncryptedMemoryDao,
    private val cipher: AndroidKeystoreCipher = AndroidKeystoreCipher(),
    private val clock: () -> Long = System::currentTimeMillis
) : MemoryRepository {

    override fun observeMemories(): Flow<List<MemoryItem>> = dao.observeAll().map { entities ->
        entities.map { entity ->
            MemoryItem(
                id = entity.id,
                content = cipher.decrypt(entity.ciphertextBase64).toString(Charsets.UTF_8),
                createdAtEpochMillis = entity.createdAtEpochMillis,
                updatedAtEpochMillis = entity.updatedAtEpochMillis
            )
        }
    }

    override suspend fun save(id: String, content: String) {
        require(id.isNotBlank()) { "Memory id must not be blank" }
        require(content.isNotBlank()) { "Memory content must not be blank" }
        val now = clock()
        val previous = dao.findById(id)
        val encoded = cipher.encrypt(content.toByteArray(Charsets.UTF_8))
        dao.upsert(
            EncryptedMemoryEntity(
                id = id,
                ciphertextBase64 = encoded,
                createdAtEpochMillis = previous?.createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now
            )
        )
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()
}
