package com.w2sv.navigator.notifications.di

import com.w2sv.navigator.domain.notifications.ForegroundNotificationProvider
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import com.w2sv.navigator.notifications.ForegroundNotificationProviderImpl
import com.w2sv.navigator.notifications.NotificationEventHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface NotificationBinderModule {

    @Binds
    fun notificationEventHandler(impl: NotificationEventHandlerImpl): NotificationEventHandler

    @Binds
    fun foregroundNotificationProvider(impl: ForegroundNotificationProviderImpl): ForegroundNotificationProvider
}
