package com.w2sv.navigator.notifications

import android.app.NotificationManager
import android.content.Context
import com.w2sv.androidutils.getNotificationManager
import com.w2sv.navigator.notifications.api.MultiInstanceNotificationManager
import com.w2sv.navigator.notifications.appnotifications.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.appnotifications.movefile.MoveFileNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
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
    @ElementsIntoSet
    fun multiInstanceAppNotificationManagers(
        moveFileNotificationManager: MoveFileNotificationManager,
        autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager
    ): Set<MultiInstanceNotificationManager<*>> =
        setOf(moveFileNotificationManager, autoMoveDestinationInvalidNotificationManager)
}
