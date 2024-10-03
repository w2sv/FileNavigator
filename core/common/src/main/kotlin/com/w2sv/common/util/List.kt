package com.w2sv.common.util

fun <T> List<T>.replaceLast(replacement: T): List<T> {
    return toMutableList().apply { this[lastIndex] = replacement }
}