package com.w2sv.navigator.observing

import android.content.ContentResolver
import com.w2sv.core.logging.log
import javax.inject.Inject
import slimber.log.i

internal class FileObserverManager @Inject constructor(
    private val fileObserverProvider: FileObserverProvider,
    private val contentResolver: ContentResolver
) {
    private var activeObservers: List<FileObserver>? = null

    suspend fun registerFileObservers() {
        i { "Registering file observers" }
        activeObservers = fileObserverProvider()
            .onEach { observer ->
                contentResolver.registerContentObserver(
                    checkNotNull(observer.mediaType.readUri),
                    true,
                    observer
                )
            }
            .log { "Registered ${it.size} FileObserver(s)" }
    }

    private fun unregisterFileObservers() {
        activeObservers?.forEach(contentResolver::unregisterContentObserver)
        i { "Unregistered fileObservers" }
    }

    suspend fun reregisterFileObservers() {
        unregisterFileObservers()
        registerFileObservers()
    }

    fun tearDown() {
        unregisterFileObservers()
        fileObserverProvider.tearDown()
    }
}
