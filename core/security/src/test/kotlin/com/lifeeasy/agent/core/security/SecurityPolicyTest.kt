package com.lifeeasy.agent.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityPolicyTest {
    @Test fun explicitBlockAlwaysDenies() {
        assertEquals(
            SecurityDecision.DENY,
            SecurityPolicy.evaluate(SecurityContext(true, true, true, explicitlyBlocked = true))
        )
    }

    @Test fun missingConsentRequiresConsent() {
        assertEquals(
            SecurityDecision.REQUIRE_CONSENT,
            SecurityPolicy.evaluate(SecurityContext(false, true, false))
        )
    }

    @Test fun missingAuthenticationRequiresAuthentication() {
        assertEquals(
            SecurityDecision.REQUIRE_AUTHENTICATION,
            SecurityPolicy.evaluate(SecurityContext(true, false, true))
        )
    }

    @Test fun validContextAllows() {
        assertEquals(
            SecurityDecision.ALLOW,
            SecurityPolicy.evaluate(SecurityContext(true, true, true))
        )
    }
}
