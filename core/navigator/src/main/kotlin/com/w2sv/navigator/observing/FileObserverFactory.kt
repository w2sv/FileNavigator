package com.w2sv.navigator.observing

import android.os.Handler
import android.os.HandlerThread
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.navigatorconfig.FileTypeConfigMap
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class FileObserverFactory @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @param:GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val mediaFileObserverFactory: MediaFileObserver.Factory,
    private val nonMediaFileObserverFactory: NonMediaFileObserver.Factory,
    @param:FileObserverHandlerThread private val handlerThread: HandlerThread
) {
    private val navigatorConfigStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig.stateInWithBlockingInitial(scope)
    }

    private val navigatorConfig
        get() = navigatorConfigStateFlow.value

    private val fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>
        get() = navigatorConfigStateFlow.mapState { it.fileTypeConfigMap }

    operator fun invoke(): List<FileObserver> {
        val handler = handlerThread.handler()

        return buildList {
            addAll(
                mediaFileTypeObservers(handler)
            )
            nonMediaFileTypeObserver(handler)?.let(::add)
        }
    }

    private fun mediaFileTypeObservers(handler: Handler): List<MediaFileObserver> =
        navigatorConfig
            .enabledFileTypes
            .filter { it.isMediaType }
            .map { mediaFileType ->
                mediaFileObserverFactory.invoke(
                    fileType = mediaFileType,
                    sourceTypeConfigMapStateFlow = fileTypeConfigMapStateFlow.mapState { it.getValue(mediaFileType).sourceTypeConfigMap },
                    handler = handler
                )
            }

    private fun nonMediaFileTypeObserver(handler: Handler): NonMediaFileObserver? =
        navigatorConfig
            .enabledFileTypes
            .filter { !it.isMediaType }
            .let { enabledNonMediaFileTypes ->
                if (enabledNonMediaFileTypes.isNotEmpty()) {
                    nonMediaFileObserverFactory.invoke(
                        enabledNonMediaFileTypesWithExtensions = enabledNonMediaFileTypes,
                        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                        handler = handler
                    )
                } else {
                    null
                }
            }

    fun onDestroy() {
        handlerThread.quit()
        i { "Quit handler thread" }
    }
}

private fun HandlerThread.handler(): Handler =
    Handler(looper)
