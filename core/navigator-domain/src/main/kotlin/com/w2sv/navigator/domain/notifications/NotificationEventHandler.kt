package com.w2sv.navigator.domain.notifications

interface NotificationEventHandler {
    operator fun invoke(event: NotificationEvent)
}
