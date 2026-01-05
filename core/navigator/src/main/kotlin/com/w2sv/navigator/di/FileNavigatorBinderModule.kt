package com.w2sv.navigator.di

import com.w2sv.navigator.NavigatorIntentsImpl
import com.w2sv.navigator.domain.NavigatorIntents
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface FileNavigatorBinderModule {

    @Binds
    fun navigatorIntents(impl: NavigatorIntentsImpl): NavigatorIntents
}
