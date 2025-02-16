package com.w2sv.navigator.moving.api.activity

import com.w2sv.common.util.LoggingComponentActivity
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal abstract class AbstractMoveActivity : LoggingComponentActivity() {

    @Inject
    lateinit var moveResultChannel: MoveResultChannel

    protected fun sendMoveResultBundleAndFinishAndRemoveTask(
        moveFailure: MoveResult.Failure,
        notificationResources: NotificationResources? = null
    ) {
        moveResultChannel.trySend(moveFailure bundleWith notificationResources)
        finishAndRemoveTask()
    }
}
