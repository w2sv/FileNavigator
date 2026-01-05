package com.w2sv.navigator.moving.activity

import android.app.Activity
import com.w2sv.navigator.di.MoveOperationSummaryChannel
import com.w2sv.navigator.domain.moving.MoveOperationSummary
import com.w2sv.navigator.domain.moving.MoveResult
import javax.inject.Inject

class MoveActivityFinisher @Inject constructor(private val moveOperationSummaryChannel: MoveOperationSummaryChannel) {

    fun finishOnError(activity: Activity, error: MoveResult) {
        moveOperationSummaryChannel.trySend(MoveOperationSummary(error))
        activity.finishAndRemoveTask()
    }
}
