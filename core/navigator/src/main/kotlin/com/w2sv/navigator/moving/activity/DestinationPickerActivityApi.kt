package com.w2sv.navigator.moving.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.w2sv.common.logging.LoggingComponentActivity
import com.w2sv.common.uri.DocumentUri

internal abstract class DestinationPickerActivityApi : LoggingComponentActivity() {

    abstract fun launchPicker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchPicker()
    }

    interface Args : Parcelable {
        val startDestination: DocumentUri?

        companion object {
            const val EXTRA = "com.w2sv.navigator.extra.AbstractDestinationPickerActivity.Args"
        }
    }

    companion object {
        inline fun <reified T : DestinationPickerActivityApi> makeRestartActivityIntent(args: Args, context: Context): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    T::class.java
                )
            )
                .putExtra(Args.EXTRA, args)
    }
}
