package com.w2sv.common.logging

import androidx.lifecycle.ViewModel
import slimber.log.i

abstract class LoggingViewModel : ViewModel() {

    init {
        i { "Lifecycle: ${this.logIdentifier}.init" }
    }

    override fun onCleared() {
        i { "Lifecycle: ${this.logIdentifier}.onCleared" }
        super.onCleared()
    }
}
