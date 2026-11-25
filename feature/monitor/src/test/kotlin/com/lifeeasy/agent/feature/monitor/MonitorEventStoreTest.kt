// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.monitor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitorEventStoreTest {
    @Test fun recordsNewestEventFirst() {
        MonitorEventStore.clear()
        MonitorEventStore.record("first", "one", true, 1L)
        MonitorEventStore.record("second", "two", false, 2L)
        assertEquals("second", MonitorEventStore.events.value.first().title)
        assertEquals(2, MonitorEventStore.events.value.size)
    }

    @Test fun retainsOnlyThirtyEvents() {
        MonitorEventStore.clear()
        repeat(40) { MonitorEventStore.record("event-$it", "detail", true, it.toLong()) }
        assertEquals(30, MonitorEventStore.events.value.size)
        assertEquals("event-39", MonitorEventStore.events.value.first().title)
        assertTrue(MonitorEventStore.events.value.none { it.title == "event-0" })
    }
}
