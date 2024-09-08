package com.w2sv.common.utils

import slimber.log.i

inline fun <T> T.log(makeMessage: (T) -> String): T =
    also { i { makeMessage(this) } }