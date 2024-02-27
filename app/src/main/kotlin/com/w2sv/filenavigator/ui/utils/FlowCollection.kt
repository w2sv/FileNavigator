package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <T> CollectFromFlow(flow: Flow<T>, collector: FlowCollector<T>) {
    LaunchedEffect(flow) {
        flow.collect(collector)
    }
}

@Composable
fun <T> CollectLatestFromFlow(flow: Flow<T>, action: suspend (value: T) -> Unit) {
    LaunchedEffect(flow) {
        flow.collectLatest(action)
    }
}