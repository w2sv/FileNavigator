package com.w2sv.navigator.moving

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources

internal abstract class DestinationPickerActivity : ComponentActivity() {

    abstract var moveResultListener: MoveResultListener

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
        !isExternalStorageManger -> MoveResult.Failure.ManageAllFilesPermissionMissing
        else -> null
    }

    protected fun finishAndRemoveTask(
        moveFailure: MoveResult.Failure? = null,
        notificationResources: NotificationResources? = null
    ) {
        moveFailure?.let {
            moveResultListener.onPreMoveCancellation(
                moveFailure = it,
                notificationResources = notificationResources
            )
        }
        finishAndRemoveTask()
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