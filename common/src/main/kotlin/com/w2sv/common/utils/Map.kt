package com.w2sv.common.utils

import kotlinx.coroutines.flow.StateFlow

fun <T> MutableMap<T, Int>.increment(key: T, by: Int) {
    this[key] = getValue(key) + by
}

fun <T> MutableMap<T, Int>.decrement(key: T, by: Int) {
    this[key] = getValue(key) - by
}