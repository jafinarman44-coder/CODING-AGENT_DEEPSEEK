// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
// Security decisions are explicit and fail closed; do not log sensitive input.
package com.lifeeasy.agent.core.security

enum class SecurityDecision {
    ALLOW,
    REQUIRE_CONSENT,
    REQUIRE_AUTHENTICATION,
    DENY
}

data class SecurityContext(
    val consentGranted: Boolean,
    val authenticated: Boolean,
    val authenticationRequired: Boolean,
    val explicitlyBlocked: Boolean = false
)
