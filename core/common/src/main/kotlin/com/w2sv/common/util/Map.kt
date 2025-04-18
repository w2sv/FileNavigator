package com.w2sv.common.util

import com.w2sv.kotlinutils.filterToSet

// TODO kotlinutils

fun <K, V> Map<K, V>.filterKeysByValue(predicate: (V) -> Boolean): List<K> =
    keys.filter { predicate(getValue(it)) }

fun <K, V> Map<K, V>.filterKeysByValueToSet(predicate: (V) -> Boolean): Set<K> =
    keys.filterToSet { predicate(getValue(it)) }

fun <K, V> syncMapKeys(
    source: Map<K, V>,
    target: MutableMap<K, V>,
    valueOnAddedKeys: V
) {
    val addedKeys = source.keys - target.keys
    val removedKeys = target.keys - source.keys

    addedKeys.forEach { target[it] = valueOnAddedKeys }
    removedKeys.forEach { target.remove(it) }
}
