package com.w2sv.common.util

import slimber.log.i

inline fun <T> T.log(makeMessage: (T) -> String = { it.toString() }): T =
    also { i { makeMessage(this) } }

val Any.logIdentifier: String
    get() = this::class.java.simpleName
