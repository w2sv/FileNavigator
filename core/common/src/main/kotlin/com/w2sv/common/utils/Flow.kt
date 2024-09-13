package com.w2sv.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> MutableSharedFlow<T>.emit(
    value: T,
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
) {
    scope.launch(context, start) {
        emit(value)
    }
}

// TODO: KotlinUtils

fun <T> Flow<T>.collectOn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    collector: FlowCollector<T>
) {
    scope.launch(context, start) {
        collect(collector)
    }
}

fun <T> Flow<T>.collectLatestOn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend (value: T) -> Unit
) {
    scope.launch(context, start) {
        collectLatest(action)
    }
}