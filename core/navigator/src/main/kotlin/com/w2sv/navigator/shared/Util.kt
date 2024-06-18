package com.w2sv.navigator.shared

import slimber.log.i

internal fun emitDiscardedLog(reason: () -> String) {
    i { "DISCARDED: ${reason()}" }
}