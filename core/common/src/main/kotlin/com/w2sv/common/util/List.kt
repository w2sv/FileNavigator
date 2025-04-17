package com.w2sv.common.util

fun <T> List<T>.replaceLast(replacement: T): List<T> {
    return mutate {
        try {
            this[lastIndex] = replacement
        } catch (_: IndexOutOfBoundsException) {}
    }
}

// TODO: kotlinutils
fun <T> List<T>.mutate(block: MutableList<T>.() -> Unit): List<T> =
    toMutableList().apply { block() }
