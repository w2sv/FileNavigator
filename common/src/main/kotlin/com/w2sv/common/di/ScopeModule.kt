package com.w2sv.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@InstallIn(SingletonComponent::class)
@Module
object ScopeModule {

    @Provides
    @Scope(AppDispatcher.Default)
    fun defaultScope(): CoroutineScope =
        CoroutineScope(Dispatchers.Default)

    @Provides
    @Scope(AppDispatcher.IO)
    fun ioScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO)
}