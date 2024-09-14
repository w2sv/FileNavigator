package com.w2sv.navigator.observing

import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.kotlinutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.navigator.MediaTypeToFileObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject

typealias FileTypeConfigMap = Map<FileType, FileTypeConfig>

internal class FileObserverFactory @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val mediaFileObserverFactory: MediaFileObserver.Factory,
    private val nonMediaFileObserverFactory: NonMediaFileObserver.Factory
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithSynchronousInitial(scope)
    }

    operator fun invoke(handler: Handler): MediaTypeToFileObserver {
        return buildMap {
            putAll(
                mediaFileObservers(handler)
            )
            nonMediaFileObserver(handler)?.let {
                put(MediaType.DOWNLOADS, it)
            }
        }
    }

    private fun mediaFileObservers(handler: Handler): MediaTypeToFileObserver =
        FileType.Media.values
            .filter { fileTypeConfigMapStateFlow.value.getValue(it).enabled }
            .associate { mediaFileType ->
                mediaFileType.simpleStorageMediaType to mediaFileObserverFactory.invoke(
                    fileType = mediaFileType,
                    fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                    handler = handler
                )
            }

    private fun nonMediaFileObserver(handler: Handler): NonMediaFileObserver? =
        fileTypeConfigMapStateFlow
            .mapState { fileTypeConfigMap ->
                FileType.NonMedia.values.filter { fileTypeConfigMap.getValue(it).enabled }.toSet()
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
}