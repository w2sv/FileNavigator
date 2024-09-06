package com.w2sv.navigator

import android.content.Context
import com.w2sv.androidutils.isServiceRunning
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FileNavigatorModule {

    @Singleton
    @Provides
    fun fileNavigatorIsRunning(@ApplicationContext context: Context): FileNavigator.IsRunning =
        FileNavigator.IsRunning(mutableStateFlow = MutableStateFlow(context.isServiceRunning<FileNavigator>()))
}