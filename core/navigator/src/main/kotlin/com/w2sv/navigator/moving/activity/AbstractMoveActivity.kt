package com.w2sv.navigator.moving.activity

import androidx.activity.ComponentActivity
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources

internal abstract class AbstractMoveActivity : ComponentActivity() {

    abstract var moveResultChannel: MoveResultChannel

    protected fun finishAndRemoveTask(
        moveFailure: MoveResult.Failure? = null,
        notificationResources: NotificationResources? = null
    ) {
        moveFailure?.let {
            moveResultChannel.trySend(it bundleWith notificationResources)
        }
        finishAndRemoveTask()
    }
}