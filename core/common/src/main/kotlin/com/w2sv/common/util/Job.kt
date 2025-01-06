package com.w2sv.common.util

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job

fun Job.cancelIfActive(cause: CancellationException? = null): Boolean { // TODO: xport to KotlinUtils
    if (isActive) {
        cancel(cause)
        return true
    }
    return false
}
