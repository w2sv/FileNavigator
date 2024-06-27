package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.kotlinutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.navigator.MediaTypeToFileObserver
import com.w2sv.navigator.mediastore.MediaStoreFileProvider
import com.w2sv.navigator.moving.MoveFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileObserverProvider @Inject constructor(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val mediaStoreFileProvider: MediaStoreFileProvider
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithSynchronousInitial(scope)
    }

    operator fun invoke(
        handler: Handler,
        contentResolver: ContentResolver,
        onNewMoveFile: (MoveFile) -> Unit,
        cancelMostRecentNotification: () -> Unit
    ): MediaTypeToFileObserver {
        return buildMap {
            putAll(
                mediaFileObservers(
                    contentResolver = contentResolver,
                    onNewMoveFile = onNewMoveFile,
                    cancelMostRecentNotification = cancelMostRecentNotification,
                    handler = handler
                )
            )
            nonMediaFileObserver(
                contentResolver = contentResolver,
                onNewMoveFile = onNewMoveFile,
                cancelMostRecentNotification = cancelMostRecentNotification,
                handler = handler
            )?.let {
                put(MediaType.DOWNLOADS, it)
            }
        }
    }

    private fun mediaFileObservers(
        contentResolver: ContentResolver,
        onNewMoveFile: (MoveFile) -> Unit,
        cancelMostRecentNotification: () -> Unit,
        handler: Handler
    ): MediaTypeToFileObserver =
        FileType.Media.values
            .filter { fileTypeConfigMapStateFlow.value.getValue(it).enabled }
            .associate { mediaFileType ->
                mediaFileType.simpleStorageMediaType to MediaFileObserver(
                    fileType = mediaFileType,
                    enabledSourceTypeToAutoMoveConfigStateFlow = fileTypeConfigMapStateFlow
                        .mapState { fileTypeConfigMap ->
                            fileTypeConfigMap
                                .getValue(mediaFileType)
                                .sourceTypeConfigMap
                                .filterValues { it.enabled }
                                .mapValues { it.value.autoMoveConfig }
                        },
                    contentResolver = contentResolver,
                    onNewMoveFile = onNewMoveFile,
                    cancelMostRecentNotification = cancelMostRecentNotification,
                    mediaStoreFileProvider = mediaStoreFileProvider,
                    handler = handler
                )
            }

    private fun nonMediaFileObserver(
        contentResolver: ContentResolver,
        onNewMoveFile: (MoveFile) -> Unit,
        cancelMostRecentNotification: () -> Unit,
        handler: Handler
    ): NonMediaFileObserver? =
        FileType.NonMedia.values
            .filter { fileTypeConfigMapStateFlow.value.getValue(it).enabled }
            .let { enabledFileTypes ->
                if (enabledFileTypes.isNotEmpty()) {
                    NonMediaFileObserver(
                        enabledFileTypeToAutoMoveConfigStateFlow = fileTypeConfigMapStateFlow.mapState { fileTypeConfigMap ->
                            enabledFileTypes.associateWith { fileType ->
                                fileTypeConfigMap.getValue(fileType)
                                    .sourceTypeConfigMap
                                    .getValue(SourceType.Download)
                                    .autoMoveConfig
                            }
                        },
                        contentResolver = contentResolver,
                        onNewMoveFile = onNewMoveFile,
                        cancelMostRecentNotification = cancelMostRecentNotification,
                        mediaStoreFileProvider = mediaStoreFileProvider,
                        handler = handler
                    )
                } else {
                    null
                }
            }
}