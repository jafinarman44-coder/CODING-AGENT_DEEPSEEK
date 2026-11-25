// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.agent.runtime

/** Monotonic clock abstraction makes execution-budget tests deterministic. */
fun interface ElapsedClock {
    fun nowMillis(): Long
}

object SystemElapsedClock : ElapsedClock {
    override fun nowMillis(): Long = System.nanoTime() / 1_000_000L
}

class WatchdogAgent(
    private val clock: ElapsedClock = SystemElapsedClock,
    val maxSteps: Int = 8,
    val maxDurationMillis: Long = 15_000L
) {
    init {
        require(maxSteps > 0) { "maxSteps must be positive" }
        require(maxDurationMillis > 0) { "maxDurationMillis must be positive" }
    }

    fun start(): Long = clock.nowMillis()
    fun mark(): Long = clock.nowMillis()

    fun validatePlan(stepCount: Int): Boolean = stepCount in 1..maxSteps

    fun hasTimeRemaining(startedAt: Long): Boolean =
        clock.nowMillis() - startedAt < maxDurationMillis
}
