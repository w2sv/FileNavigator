package com.w2sv.navigator.observing

import android.os.Handler
import android.os.HandlerThread
import com.w2sv.core.logging.log
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigFlow
import com.w2sv.kotlinutils.keysWhereToSet
import com.w2sv.navigator.di.FileObserverHandlerThread
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import slimber.log.i

internal class FileObserverProvider @Inject constructor(
    private val navigatorConfigFlow: NavigatorConfigFlow,
    @FileObserverHandlerThread private val handlerThread: HandlerThread,
    private val environment: FileObserverEnvironmentImpl
) {
    suspend operator fun invoke(): List<FileObserver> {
        val handler = Handler(handlerThread.looper)
        val navigatorConfig = navigatorConfigFlow.first()

        return buildList {
            addAll(mediaObservers(navigatorConfig, handler))
            downloadsObserver(navigatorConfig, handler)?.let(::add)
        }
    }

    private fun mediaObservers(navigatorConfig: NavigatorConfig, handler: Handler): List<MediaFileObserver> =
        navigatorConfig
            .enabledFileTypes
            .filter { it.isMediaType }
            .map { fileType ->
                val sourceTypes = navigatorConfig.fileTypeConfig(fileType).sourceTypeConfigMap.keysWhereToSet { it.enabled }
                MediaFileObserver(
                    fileType = fileType,
                    enabledSourceTypes = sourceTypes,
                    handler = handler,
                    environment = environment
                )
                    .log { "Provided ${it.logIdentifier} | sourceTypes=$sourceTypes" }
            }

    private fun downloadsObserver(navigatorConfig: NavigatorConfig, handler: Handler): DownloadsObserver? =
        navigatorConfig
            .enabledFileTypes
            .filter { !it.isMediaType }
            .let { enabledNonMediaTypes ->
                if (enabledNonMediaTypes.isNotEmpty()) {
                    DownloadsObserver(
                        fileTypes = enabledNonMediaTypes,
                        handler = handler,
                        environment = environment
                    )
                        .log {
                            "Provided ${it.logIdentifier} | fileTypes=${
                                enabledNonMediaTypes.map { fileType ->
                                    fileType.presetTypeOrNull
                                }
                            }"
                        }
                } else {
                    null
                }
            }

    fun tearDown() {
        i { "Quitting handler thread" }
        handlerThread.quitSafely()
    }
}
