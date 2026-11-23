// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.security

/** Deterministic local policy gate. Device heuristics are intentionally not used as proof of compromise. */
object SecurityPolicy {
    fun evaluate(context: SecurityContext): SecurityDecision = when {
        context.explicitlyBlocked -> SecurityDecision.DENY
        !context.consentGranted -> SecurityDecision.REQUIRE_CONSENT
        context.authenticationRequired && !context.authenticated ->
            SecurityDecision.REQUIRE_AUTHENTICATION
        else -> SecurityDecision.ALLOW
    }
}
