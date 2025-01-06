package com.w2sv.common.di

import android.content.Context
import android.os.PowerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@InstallIn(SingletonComponent::class)
@Module
object CommonModule {

    @Provides
    @GlobalScope(AppDispatcher.Default)
    fun defaultScope(): CoroutineScope =
        CoroutineScope(Dispatchers.Default)

    @Provides
    @GlobalScope(AppDispatcher.IO)
    fun ioScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO)

    @Provides
    @Singleton
    fun powerManager(@ApplicationContext context: Context): PowerManager =
        context.getSystemService(PowerManager::class.java)
}
