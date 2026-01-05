package com.w2sv.navigator.notifications

import android.app.Notification
import com.w2sv.navigator.domain.notifications.ForegroundNotificationProvider
import com.w2sv.navigator.notifications.controller.EmptyArgs
import com.w2sv.navigator.notifications.controller.FileNavigatorIsRunningNotificationController
import javax.inject.Inject

internal class ForegroundNotificationProviderImpl @Inject constructor(
    private val controller: FileNavigatorIsRunningNotificationController
) : ForegroundNotificationProvider {
    override fun notification(): Notification =
        controller.build(EmptyArgs)
    override val notificationId: Int = controller.id
}
