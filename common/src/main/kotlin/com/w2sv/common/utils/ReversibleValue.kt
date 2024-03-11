package com.w2sv.common.utils

data class ReversibleValue<T>(var value: T) {
    var previous: T = value
        private set

    fun sync() {
        previous = value
    }

    fun reset() {
        value = previous
    }
}