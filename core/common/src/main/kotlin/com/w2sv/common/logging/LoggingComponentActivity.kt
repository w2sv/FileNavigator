package com.w2sv.common.logging

import android.os.Bundle
import androidx.activity.ComponentActivity
import slimber.log.i

/**
 * A [ComponentActivity] that logs upon reaching its most important lifecycle states.
 */
abstract class LoggingComponentActivity : ComponentActivity() {

    private val logIdentifier
        get() = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i { "Lifecycle: $logIdentifier onCreate" }
    }

    override fun onStart() {
        super.onStart()
        i { "Lifecycle: $logIdentifier onStart" }
    }

    override fun onResume() {
        super.onResume()
        i { "Lifecycle: $logIdentifier onResume" }
    }

    override fun onPause() {
        super.onPause()
        i { "Lifecycle: $logIdentifier onPause" }
    }

    override fun onStop() {
        super.onStop()
        i { "Lifecycle: $logIdentifier onStop" }
    }

    override fun onDestroy() {
        super.onDestroy()
        i { "Lifecycle: $logIdentifier onDestroy" }
    }
}
