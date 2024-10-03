package com.w2sv.common.util

// TODO: KotlinUtils
inline fun <K, V, RK, RV> Map<K, V>.map(transform: (Map.Entry<K, V>) -> Pair<RK, RV>): Map<RK, RV> =
    entries.associate(transform)

inline fun <K, V> Map<K, V>.copy(transform: MutableMap<K, V>.() -> Unit): Map<K, V> =
    toMutableMap().apply(transform)

inline fun <K, V> MutableMap<K, V>.update(k: K, transform: (V) -> V) {
    this[k] = transform(getValue(k))
}