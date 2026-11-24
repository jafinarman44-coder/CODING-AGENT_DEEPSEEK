// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.voice

import com.lifeeasy.agent.core.security.AndroidKeystoreCipher
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun interface SecretCodeVerifier {
    fun verify(code: CharArray): Boolean
}

/** Four-digit local code manager. Only an AES-GCM encrypted salted PBKDF2 hash is persisted. */
class SecretCodeManager(
    private val store: SecretCodeStore,
    private val cipher: AndroidKeystoreCipher = AndroidKeystoreCipher(CODE_KEY_ALIAS),
    private val random: SecureRandom = SecureRandom()
) : SecretCodeVerifier {
    fun setCode(code: CharArray) {
        if (!isFourDigitCode(code)) {
            code.fill('\u0000')
            throw IllegalArgumentException("Security code must contain exactly four digits")
        }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        var hash: ByteArray? = null
        try {
            hash = derive(code, salt)
            val payload = listOf(
                FORMAT_VERSION,
                ITERATIONS.toString(),
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash)
            ).joinToString(":")
            store.writeEncryptedCode(cipher.encrypt(payload.toByteArray(StandardCharsets.UTF_8)))
        } finally {
            code.fill('\u0000')
            salt.fill(0)
            hash?.fill(0)
        }
    }

    override fun verify(code: CharArray): Boolean {
        if (!isFourDigitCode(code)) {
            code.fill('\u0000')
            return false
        }
        return try {
            val encrypted = store.readEncryptedCode() ?: return false
            val parts = String(cipher.decrypt(encrypted), StandardCharsets.UTF_8).split(':')
            if (parts.size != 4 || parts[0] != FORMAT_VERSION) return false
            val iterations = parts[1].toIntOrNull() ?: return false
            if (iterations !in MIN_ITERATIONS..MAX_ITERATIONS) return false
            val salt = Base64.getDecoder().decode(parts[2])
            val expected = Base64.getDecoder().decode(parts[3])
            val actual = derive(code, salt)
            try {
                MessageDigest.isEqual(expected, actual)
            } finally {
                salt.fill(0)
                expected.fill(0)
                actual.fill(0)
            }
        } catch (_: Exception) {
            false
        } finally {
            code.fill('\u0000')
        }
    }

    fun hasCode(): Boolean = !store.readEncryptedCode().isNullOrBlank()

    private fun derive(code: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(code, salt, ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun isFourDigitCode(code: CharArray): Boolean =
        code.size == CODE_LENGTH && code.all { it in '0'..'9' }

    companion object {
        private const val CODE_KEY_ALIAS = "lifeeasy.secret-code.aes.v1"
        private const val FORMAT_VERSION = "v1"
        private const val CODE_LENGTH = 4
        private const val SALT_BYTES = 16
        private const val HASH_BITS = 256
        private const val ITERATIONS = 210_000
        private const val MIN_ITERATIONS = 100_000
        private const val MAX_ITERATIONS = 1_000_000
    }
}

interface SecretCodeStore {
    fun writeEncryptedCode(payload: String)
    fun readEncryptedCode(): String?
}
