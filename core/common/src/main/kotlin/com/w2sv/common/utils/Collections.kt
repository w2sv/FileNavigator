package com.w2sv.common.utils

fun <E> MutableList<E>.reset(elements: Iterable<E>) {
    clear()
    addAll(elements)
}

inline fun <K, V> MutableMap<K, V>.update(k: K, update: (V) -> V) {
    put(k, update(getValue(k)))
}