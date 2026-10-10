package com.w2sv.navigator.observing

/**
 * A bounded FIFO container that keeps only the most recently added elements.
 *
 * When the capacity is exceeded, the oldest element is automatically evicted.
 * Useful for small “recently seen” buffers such as deduplication or suppression windows.
 *
 * Invariant: the number of elements never exceeds [maxSize].
 *
 * Thread-safe: all public methods are synchronized.
 */
internal class RecentSet<E>(private val maxSize: Int) {
    private val deque = ArrayDeque<E>(maxSize)

    @get:Synchronized
    val isFull: Boolean
        get() = deque.size == maxSize

    @get:Synchronized
    val size: Int
        get() = deque.size

    @Synchronized
    fun add(element: E) {
        if (deque.size == maxSize) {
            deque.removeFirst()
        }
        deque.addLast(element)
    }

    @Synchronized
    fun addAll(elements: Collection<E>) {
        elements.forEach(::add)
    }

    /**
     * Replaces the first element matching [predicate] with [element].
     * Returns true if an element was replaced.
     */
    @Synchronized
    fun replaceIf(predicate: (E) -> Boolean, element: E): Boolean {
        val removed = removeIf(predicate)
        add(element)
        return removed
    }

    @Synchronized
    operator fun contains(element: E): Boolean =
        deque.contains(element)

    @Synchronized
    fun removeIf(predicate: (E) -> Boolean): Boolean =
        deque.removeIf(predicate)

    @Synchronized
    fun clear() =
        deque.clear()

    @Synchronized
    override fun toString(): String =
        deque.toString()
}
