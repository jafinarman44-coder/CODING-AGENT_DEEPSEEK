// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.security

import java.security.SecureRandom

/** Generates cryptographically strong URL-safe random tokens. */
class SecureTokenGenerator(private val random: SecureRandom = SecureRandom()) {
    fun generate(byteCount: Int = 32): String {
        require(byteCount in 16..128) { "byteCount must be between 16 and 128" }
        val bytes = ByteArray(byteCount)
        random.nextBytes(bytes)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
