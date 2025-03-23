package com.w2sv.navigator.observing

import android.os.Handler
import android.os.HandlerThread
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.NonMediaFileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i
import javax.inject.Inject

typealias FileTypeConfigMap = Map<FileType, FileTypeConfig>

internal class FileObserverFactory @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val mediaFileTypeObserverFactory: MediaFileTypeObserver.Factory,
    private val nonMediaFileObserverFactory: NonMediaFileObserver.Factory,
    @FileObserverHandlerThread private val handlerThread: HandlerThread
) {
    private val navigatorConfigStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .stateInWithBlockingInitial(scope)
    }

    private val fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>
        get() = navigatorConfigStateFlow.mapState { it.fileTypeConfigMap }

    private val enabledFileTypes: Set<FileType>
        get() = navigatorConfigStateFlow.value.enabledFileTypes

    operator fun invoke(): List<FileTypeObserver> {
        val handler = handlerThread.handler()

        return buildList {
            addAll(
                mediaFileTypeObservers(handler)
            )
            nonMediaFileTypeObserver(handler)?.let(::add)
        }
    }

    private fun mediaFileTypeObservers(handler: Handler): List<MediaFileTypeObserver> =
        PresetFileType.Media.values
            .filter { it in enabledFileTypes }
            .map { mediaFileType ->
                mediaFileTypeObserverFactory.invoke(
                    fileType = mediaFileType,
                    sourceTypeConfigMapStateFlow = fileTypeConfigMapStateFlow.mapState { it.getValue(mediaFileType).sourceTypeConfigMap },
                    handler = handler
                )
            }

    private fun nonMediaFileTypeObserver(handler: Handler): NonMediaFileObserver? =
        enabledFileTypes
            .filterIsInstance<NonMediaFileType>()
            .let { enabledNonMediaFileTypes ->
                if (enabledNonMediaFileTypes.isNotEmpty()) {
                    nonMediaFileObserverFactory.invoke(
                        enabledNonMediaFileTypes = enabledNonMediaFileTypes,
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
