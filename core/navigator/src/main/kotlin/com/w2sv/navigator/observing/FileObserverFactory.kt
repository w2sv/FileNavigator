package com.w2sv.navigator.observing

import android.os.Handler
import android.os.HandlerThread
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import slimber.log.i

typealias FileTypeConfigMap = Map<FileType, FileTypeConfig>

internal class FileObserverFactory @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val mediaFileObserverFactory: MediaFileObserver.Factory,
    private val nonMediaFileObserverFactory: NonMediaFileObserver.Factory,
    @FileObserverHandlerThread private val handlerThread: HandlerThread
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithBlockingInitial(scope)
    }

    operator fun invoke(): List<FileObserver> {
        val handler = handlerThread.handler()

        return buildList {
            addAll(
                mediaFileObservers(handler)
            )
            nonMediaFileObserver(handler)?.let(::add)
        }
    }

    private fun mediaFileObservers(handler: Handler): List<MediaFileObserver> =
        PresetFileType.Media.values
            .filter { fileTypeConfigMapStateFlow.value.getValue(it).enabled }
            .map { mediaFileType ->
                mediaFileObserverFactory.invoke(
                    fileType = mediaFileType,
                    fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                    handler = handler
                )
            }

    private fun nonMediaFileObserver(handler: Handler): NonMediaFileObserver? =
        fileTypeConfigMapStateFlow
            .mapState { fileTypeConfigMap ->
                PresetFileType.NonMedia.values.filter { fileTypeConfigMap.getValue(it).enabled }.toSet()
            }
            .let { enabledFileTypesStateFlow ->
                if (enabledFileTypesStateFlow.value.isNotEmpty()) {
                    nonMediaFileObserverFactory.invoke(
                        enabledFileTypesStateFlow = enabledFileTypesStateFlow,
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
