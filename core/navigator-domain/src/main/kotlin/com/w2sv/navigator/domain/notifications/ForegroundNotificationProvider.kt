package com.w2sv.navigator.domain.notifications

import android.app.Notification

// TODO: reconsider naming
interface ForegroundNotificationProvider {
    fun notification(): Notification
    val notificationId: Int
}
