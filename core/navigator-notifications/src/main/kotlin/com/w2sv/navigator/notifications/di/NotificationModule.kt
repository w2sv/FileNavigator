package com.w2sv.navigator.notifications.di

import android.app.NotificationManager
import android.content.Context
import com.w2sv.androidutils.service.getNotificationManager
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object NotificationModule {

    @Singleton
    @Provides
    fun notificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getNotificationManager()

    @Singleton
    @Provides
    fun notificationEnvironment(@ApplicationContext context: Context, notificationManager: NotificationManager): NotificationEnvironment =
        NotificationEnvironment(context, notificationManager)
}
