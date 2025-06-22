package com.w2sv.filenavigator.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.w2sv.common.util.logIdentifier
import slimber.log.i

abstract class LifecycleLoggingViewModel : ViewModel() {

    init {
        i { "Lifecycle callback: ${this.logIdentifier}.init" }
    }

    override fun onCleared() {
        super.onCleared()
        i { "Lifecycle callback: ${this.logIdentifier}.onCleared" }
    }
}
