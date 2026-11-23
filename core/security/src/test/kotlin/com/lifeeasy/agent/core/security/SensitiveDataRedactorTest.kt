package com.lifeeasy.agent.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveDataRedactorTest {
    @Test fun redactsCredentialsAndContactDetails() {
        val result = SensitiveDataRedactor.redact(
            "Authorization: Bearer abc.def token=xyz password=hunter2 email=user@example.com phone=+8801712345678"
        )
        assertFalse(result.contains("abc.def"))
        assertFalse(result.contains("hunter2"))
        assertFalse(result.contains("user@example.com"))
        assertTrue(result.contains("[REDACTED]"))
        assertTrue(result.contains("[EMAIL]"))
    }
}
