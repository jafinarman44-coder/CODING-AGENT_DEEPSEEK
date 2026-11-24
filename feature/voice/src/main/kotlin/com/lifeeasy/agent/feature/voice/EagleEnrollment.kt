// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.voice

import com.lifeeasy.agent.core.security.AndroidKeystoreCipher

/**
 * Persists an embedding produced by a real on-device speaker-embedding engine.
 * This class deliberately does not pretend to record audio or generate a voiceprint.
 */
interface VoiceProfileReader {
    fun readVoiceprintForVerification(): ByteArray?
}

class EagleEnrollment(
    private val store: VoiceProfileStore,
    private val cipher: AndroidKeystoreCipher = AndroidKeystoreCipher(VOICE_KEY_ALIAS)
) : VoiceProfileReader {
    fun enroll(phraseSamples: List<String>, voiceprint: ByteArray) {
        require(phraseSamples.size in MIN_PHRASES..MAX_PHRASES) {
            "Enrollment requires 3 to 5 separate phrase samples"
        }
        require(phraseSamples.all { it.isNotBlank() && it.length <= MAX_PHRASE_LENGTH }) {
            "Each phrase sample must be non-empty and no longer than 500 characters"
        }
        require(voiceprint.isNotEmpty() && voiceprint.size <= MAX_VOICEPRINT_BYTES) {
            "Voiceprint payload is empty or exceeds the 1 MB safety limit"
        }
        store.saveEncryptedProfile(cipher.encrypt(voiceprint))
    }

    fun isEnrolled(): Boolean = !store.readEncryptedProfile().isNullOrBlank()

    fun removeEnrollment() = store.clearProfile()

    /** Decryption is restricted to the on-device verification pipeline. */
    override fun readVoiceprintForVerification(): ByteArray? =
        store.readEncryptedProfile()?.let(cipher::decrypt)

    private companion object {
        const val VOICE_KEY_ALIAS = "lifeeasy.voiceprint.aes.v1"
        const val MIN_PHRASES = 3
        const val MAX_PHRASES = 5
        const val MAX_PHRASE_LENGTH = 500
        const val MAX_VOICEPRINT_BYTES = 1_048_576
    }
}
