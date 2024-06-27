package com.w2sv.navigator.di

import android.app.NotificationManager
import android.content.Context
import com.w2sv.androidutils.getNotificationManager
import com.w2sv.androidutils.isServiceRunning
import com.w2sv.navigator.FileNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NavigatorModule {

    @Singleton
    @Provides
    fun notificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getNotificationManager()

    @Singleton
    @Provides
    fun fileNavigatorIsRunning(@ApplicationContext context: Context): FileNavigator.IsRunning =
        FileNavigator.IsRunning(mutableStateFlow = MutableStateFlow(context.isServiceRunning<FileNavigator>()))
}