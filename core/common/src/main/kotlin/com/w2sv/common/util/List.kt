package com.w2sv.common.util

fun <T> List<T>.replaceLast(replacement: T): List<T> {
    return mutate {
        try {
            this[lastIndex] = replacement
        } catch (_: IndexOutOfBoundsException) {}
    }
}

fun <T> List<T>.mutate(block: MutableList<T>.() -> Unit): List<T> =  // TODO: kotlinutils
    toMutableList().apply { block() }
