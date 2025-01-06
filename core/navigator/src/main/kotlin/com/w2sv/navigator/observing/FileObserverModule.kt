package com.w2sv.navigator.observing

import android.os.HandlerThread
import com.w2sv.common.util.log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FileObserverHandlerThread

@InstallIn(ServiceComponent::class)
@Module
internal object FileObserverModule {

    @Provides
    @ServiceScoped
    @FileObserverHandlerThread
    fun fileObserverHandlerThread(): HandlerThread =
        HandlerThread("com.w2sv.filenavigator.FileObserverHandlerThread")
            .apply { start() }
            .log { "Initialized ${it.name}" }
}
