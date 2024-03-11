package com.w2sv.filenavigator.ui.utils

// =============
// Map
// =============

fun <T> MutableMap<T, Boolean>.toggle(key: T) {
    put(key, !getValue(key))
}