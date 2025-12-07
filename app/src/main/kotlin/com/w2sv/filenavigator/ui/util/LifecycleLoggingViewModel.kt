package com.w2sv.filenavigator.ui.util

import androidx.lifecycle.ViewModel
import com.w2sv.common.util.logIdentifier
import slimber.log.i

abstract class LifecycleLoggingViewModel : ViewModel() {

    init {
        i { "Lifecycle: ${this.logIdentifier}.init" }
    }

    override fun onCleared() {
        i { "Lifecycle: ${this.logIdentifier}.onCleared" }
        super.onCleared()
    }
}
