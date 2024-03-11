package com.w2sv.common.utils

fun <T> MutableMap<T, Int>.increment(key: T, by: Int) {
    this[key] = getValue(key) + by
}

fun <T> MutableMap<T, Int>.decrement(key: T, by: Int) {
    this[key] = getValue(key) - by
}