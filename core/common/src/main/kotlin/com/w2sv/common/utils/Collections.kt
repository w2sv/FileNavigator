package com.w2sv.common.utils

fun <E> MutableList<E>.reset(elements: Iterable<E>) {
    clear()
    addAll(elements)
}