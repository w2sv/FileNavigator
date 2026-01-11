package com.w2sv.navigator.di

import android.content.Context
import com.w2sv.androidutils.service.isServiceRunning
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.domain.moving.MediaIdWithMediaType
import com.w2sv.navigator.domain.moving.MoveOperationSummary
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal typealias MoveOperationSummaryChannel = Channel<MoveOperationSummary>

@InstallIn(SingletonComponent::class)
@Module
internal object FileNavigatorModule {

    @Singleton
    @Provides
    fun fileNavigatorIsRunning(@ApplicationContext context: Context): FileNavigator.IsRunning =
        FileNavigator.IsRunning(mutableStateFlow = MutableStateFlow(context.isServiceRunning<FileNavigator>()))

    @Singleton
    @Provides
    fun moveOperationSummaryChannel(): MoveOperationSummaryChannel =
        Channel(Channel.BUFFERED)

    @Singleton
    @Provides
    fun mutableBlacklistedMediaUriSharedFlow(): MutableSharedFlow<MediaIdWithMediaType> =
        MutableSharedFlow()

    @Singleton
    @Provides
    fun blacklistedMediaUriSharedFlow(
        mutableBlacklistedMediaUriSharedFlow: MutableSharedFlow<MediaIdWithMediaType>
    ): SharedFlow<MediaIdWithMediaType> =
        mutableBlacklistedMediaUriSharedFlow.asSharedFlow()
}
