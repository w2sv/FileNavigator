package com.w2sv.navigator.moving.api.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.isExternalStorageManger
import com.w2sv.navigator.moving.model.MoveResult

internal abstract class AbstractDestinationPickerActivity : AbstractMoveActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preemptiveMoveFailure()?.let { sendMoveResultBundleAndFinishAndRemoveTask(it) } ?: run { launchPicker() }
    }

    abstract fun launchPicker()

    @CallSuper
    protected open fun preemptiveMoveFailure(): MoveResult.Failure? =
        when {
            !isExternalStorageManger -> MoveResult.ManageAllFilesPermissionMissing
            else -> null
        }

    interface Args : Parcelable {
        val pickerStartDestination: DocumentUri?

        companion object {
            const val EXTRA = "com.w2sv.navigator.extra.DestinationPickerActivity.Args"
        }
    }

    companion object {
        inline fun <reified T : AbstractDestinationPickerActivity> makeRestartActivityIntent(args: Args, context: Context): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    T::class.java
                )
            )
                .putExtra(Args.EXTRA, args)
    }
}
