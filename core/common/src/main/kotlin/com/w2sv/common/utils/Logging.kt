package com.w2sv.common.utils

import slimber.log.i

inline fun <T> T.log(makeMessage: (T) -> String = { it.toString() }): T =
    also { i { makeMessage(this) } }