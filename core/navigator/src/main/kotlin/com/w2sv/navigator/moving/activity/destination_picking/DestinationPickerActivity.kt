package com.w2sv.navigator.moving.activity.destination_picking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.isExternalStorageManger
import com.w2sv.navigator.moving.activity.AbstractMoveActivity
import com.w2sv.navigator.moving.model.MoveResult

internal abstract class DestinationPickerActivity : AbstractMoveActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val moveFailure = preemptiveMoveFailure()) {
            null -> {
                launchPicker()
            }

            else -> {
                finishAndRemoveTask(moveFailure)
            }
        }
    }

    abstract fun launchPicker()

    protected open fun preemptiveMoveFailure(): MoveResult.Failure? = when {
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
        inline fun <reified T : DestinationPickerActivity> makeRestartActivityIntent(
            args: Args,
            context: Context
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    T::class.java
                )
            )
                .putExtra(Args.EXTRA, args)
    }
}