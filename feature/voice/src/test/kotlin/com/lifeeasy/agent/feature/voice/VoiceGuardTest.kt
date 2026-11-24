// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceGuardTest {
    @Test fun voiceMismatchBlocksSensitiveCommandBeforeCodeCanAuthorize() {
        val guard = guard(voiceMatches = false, codeValid = true)
        assertEquals(
            VoiceGuardDecision.BLOCKED_VOICE_MISMATCH,
            guard.authorize(true, byteArrayOf(1, 2), charArrayOf('1', '2', '3', '4'))
        )
    }

    @Test fun wrongCodeBlocksEvenWhenVoiceMatches() {
        val guard = guard(voiceMatches = true, codeValid = false)
        assertEquals(
            VoiceGuardDecision.BLOCKED_INVALID_CODE,
            guard.authorize(true, byteArrayOf(1, 2), charArrayOf('1', '2', '3', '4'))
        )
    }

    @Test fun matchingVoiceAndValidCodeAllowSensitiveCommand() {
        val guard = guard(voiceMatches = true, codeValid = true)
        assertEquals(
            VoiceGuardDecision.ALLOWED,
            guard.authorize(true, byteArrayOf(1, 2), charArrayOf('1', '2', '3', '4'))
        )
    }

    @Test fun missingEnrollmentBlocksSensitiveCommand() {
        val guard = VoiceGuard(
            enrollment = FakeEnrollment(enrolled = false),
            codeVerifier = SecretCodeVerifier { true },
            voiceMatcher = VoiceMatchVerifier { _, _ -> true }
        )
        assertEquals(
            VoiceGuardDecision.BLOCKED_NO_ENROLLMENT,
            guard.authorize(true, byteArrayOf(1), charArrayOf('1', '2', '3', '4'))
        )
    }

    @Test fun nonSensitiveCommandDoesNotRequireEnrollmentOrCode() {
        val guard = VoiceGuard(
            enrollment = FakeEnrollment(enrolled = false),
            codeVerifier = SecretCodeVerifier { false },
            voiceMatcher = VoiceMatchVerifier { _, _ -> false }
        )
        assertEquals(
            VoiceGuardDecision.ALLOWED_NON_SENSITIVE,
            guard.authorize(false, byteArrayOf(1), charArrayOf('0', '0', '0', '0'))
        )
    }

    private fun guard(voiceMatches: Boolean, codeValid: Boolean) = VoiceGuard(
        enrollment = FakeEnrollment(enrolled = true),
        codeVerifier = SecretCodeVerifier { entered -> codeValid && entered.contentEquals(charArrayOf('1', '2', '3', '4')) },
        voiceMatcher = VoiceMatchVerifier { _, _ -> voiceMatches }
    )

    private class FakeEnrollment(private val enrolled: Boolean) : VoiceProfileReader {
        override fun readVoiceprintForVerification(): ByteArray? =
            if (enrolled) byteArrayOf(7, 8, 9) else null
    }
}
