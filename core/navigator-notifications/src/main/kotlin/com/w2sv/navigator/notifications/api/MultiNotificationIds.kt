package com.w2sv.navigator.notifications.api

/**
 * Manages unique notification IDs for multi-instance notifications.
 * IDs are reused after cancellation.
 */
internal class MultiNotificationIds(startId: Int) {

    private val freeIds = ArrayDeque<Int>()
    private val activeIds = mutableSetOf<Int>()
    private var nextId = startId

    val count: Int
        get() = activeIds.size

    fun next(): Int {
        val id = freeIds.removeFirstOrNull() ?: nextId++
        activeIds += id
        return id
    }

    fun cancel(id: Int) {
        if (activeIds.remove(id)) {
            freeIds += id
        }
    }
}
