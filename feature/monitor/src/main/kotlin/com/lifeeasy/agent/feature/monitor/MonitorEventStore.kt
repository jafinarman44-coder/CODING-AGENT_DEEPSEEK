// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.monitor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A bounded in-memory activity log for real local agent executions. */
data class AgentMonitorEvent(
    val title: String,
    val detail: String,
    val timestamp: Long,
    val success: Boolean
)

object MonitorEventStore {
    private const val MAX_EVENTS = 30
    private val lock = Any()
    private val mutableEvents = MutableStateFlow<List<AgentMonitorEvent>>(emptyList())
    val events: StateFlow<List<AgentMonitorEvent>> = mutableEvents.asStateFlow()

    fun record(title: String, detail: String, success: Boolean, timestamp: Long = System.currentTimeMillis()) {
        synchronized(lock) {
            mutableEvents.value = (listOf(AgentMonitorEvent(title, detail, timestamp, success)) + mutableEvents.value)
                .take(MAX_EVENTS)
        }
    }

    fun clear() {
        synchronized(lock) { mutableEvents.value = emptyList() }
    }
}
