package com.w2sv.navigator.notifications.api

import junit.framework.TestCase.assertEquals
import org.junit.Test

class MultiNotificationIdsTest {

    @Test
    fun `next returns sequential ids starting from startId`() {
        val ids = MultiNotificationIds(startId = 100)

        assertEquals(100, ids.next())
        assertEquals(101, ids.next())
        assertEquals(102, ids.next())
        assertEquals(3, ids.count)
    }

    @Test
    fun `cancel decreases count`() {
        val ids = MultiNotificationIds(startId = 1)

        val id1 = ids.next()
        ids.cancel(id1)

        assertEquals(0, ids.count)
    }

    @Test
    fun `cancelled ids are reused`() {
        val ids = MultiNotificationIds(startId = 10)

        val id1 = ids.next()
        ids.cancel(id1)
        val reused = ids.next()

        assertEquals(id1, reused)
    }

    @Test
    fun `cancelling unknown id does nothing`() {
        val ids = MultiNotificationIds(startId = 0)

        ids.cancel(42)

        assertEquals(0, ids.count)
        assertEquals(0, ids.next())
    }
}
