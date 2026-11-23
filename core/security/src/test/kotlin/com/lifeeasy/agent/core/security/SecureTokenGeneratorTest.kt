package com.lifeeasy.agent.core.security

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureTokenGeneratorTest {
    @Test fun generatesDistinctTokens() {
        val generator = SecureTokenGenerator()
        val first = generator.generate()
        val second = generator.generate()
        assertNotEquals(first, second)
        assertTrue(first.length >= 40)
    }

    @Test fun rejectsUnsafeLengths() {
        assertThrows(IllegalArgumentException::class.java) {
            SecureTokenGenerator().generate(8)
        }
    }
}
