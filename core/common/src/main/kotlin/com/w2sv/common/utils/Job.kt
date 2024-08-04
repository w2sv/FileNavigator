package com.w2sv.common.utils

import kotlinx.coroutines.Job
import kotlin.coroutines.cancellation.CancellationException

fun Job.cancelIfActive(cause: CancellationException? = null): Boolean {
    if (isActive) {
        cancel(cause)
        return true
    }
    return false
}