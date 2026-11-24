// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.voice

/** Implementations must compare an actual on-device speaker embedding against the enrolled profile. */
fun interface VoiceMatchVerifier {
    fun matches(enrolledVoiceprint: ByteArray, liveVoiceSample: ByteArray): Boolean
}

enum class VoiceGuardDecision {
    ALLOWED,
    ALLOWED_NON_SENSITIVE,
    BLOCKED_NO_ENROLLMENT,
    BLOCKED_VOICE_MISMATCH,
    BLOCKED_INVALID_CODE
}

/** Fail-closed authorization gate. Sensitive commands require both speaker match and 4-digit code. */
class VoiceGuard(
    private val enrollment: VoiceProfileReader,
    private val codeVerifier: SecretCodeVerifier,
    private val voiceMatcher: VoiceMatchVerifier
) {
    fun authorize(
        isSensitiveCommand: Boolean,
        liveVoiceSample: ByteArray,
        secretCode: CharArray
    ): VoiceGuardDecision {
        if (!isSensitiveCommand) {
            secretCode.fill('\u0000')
            liveVoiceSample.fill(0)
            return VoiceGuardDecision.ALLOWED_NON_SENSITIVE
        }
        val enrolled = try {
            enrollment.readVoiceprintForVerification()
        } catch (_: Exception) {
            null
        } ?: run {
            secretCode.fill('\u0000')
            liveVoiceSample.fill(0)
            return VoiceGuardDecision.BLOCKED_NO_ENROLLMENT
        }
        if (enrolled.isEmpty() || liveVoiceSample.isEmpty()) {
            enrolled.fill(0)
            secretCode.fill('\u0000')
            liveVoiceSample.fill(0)
            return VoiceGuardDecision.BLOCKED_VOICE_MISMATCH
        }

        val voiceMatches = try {
            voiceMatcher.matches(enrolled, liveVoiceSample)
        } catch (_: Exception) {
            false
        } finally {
            enrolled.fill(0)
            liveVoiceSample.fill(0)
        }
        if (!voiceMatches) {
            secretCode.fill('\u0000')
            return VoiceGuardDecision.BLOCKED_VOICE_MISMATCH
        }
        val codeIsValid = try {
            codeVerifier.verify(secretCode)
        } catch (_: Exception) {
            false
        } finally {
            secretCode.fill('\u0000')
        }
        return if (codeIsValid) VoiceGuardDecision.ALLOWED
        else VoiceGuardDecision.BLOCKED_INVALID_CODE
    }
}
