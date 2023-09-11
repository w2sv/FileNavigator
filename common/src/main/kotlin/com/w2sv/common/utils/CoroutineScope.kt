package com.w2sv.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// TODO: export to AndroidUtils
fun <T> CoroutineScope.collectFromFlow(
    flow: Flow<T>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    flowCollector: FlowCollector<T>
): Job =
    launch(context, start) {
        flow.collect(flowCollector)
    }