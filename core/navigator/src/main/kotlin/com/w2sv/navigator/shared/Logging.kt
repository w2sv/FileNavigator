package com.w2sv.navigator.shared

import slimber.log.i

internal inline fun discardedLog(reason: () -> String) {
    i { "DISCARDED: ${reason()}" }
}
