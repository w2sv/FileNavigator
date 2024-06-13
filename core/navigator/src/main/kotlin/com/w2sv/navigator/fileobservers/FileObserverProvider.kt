package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.os.Handler
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.navigator.moving.MoveFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileObserverProvider @Inject constructor(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithSynchronousInitial(scope)
    }

    operator fun invoke(
        handler: Handler,
        contentResolver: ContentResolver,
        onNewMoveFile: (MoveFile) -> Unit
    ): List<FileObserver> {
        return buildList {
            addAll(
                getMediaFileObservers(
                    contentResolver = contentResolver,
                    onNewMoveFile = onNewMoveFile,
                    handler = handler
                )
            )
            getNonMediaFileObserver(
                contentResolver = contentResolver,
                onNewMoveFile = onNewMoveFile,
                handler = handler
            )?.let {
                add(it)
            }
        }
    }

    private fun getMediaFileObservers(
        contentResolver: ContentResolver,
        onNewMoveFile: (MoveFile) -> Unit,
        handler: Handler
    ): List<MediaFileObserver> =
        FileType.Media.values
            .filter { fileTypeConfigMapStateFlow.value.getValue(it).enabled }
            .map { mediaFileType ->
                MediaFileObserver(
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
                    handler = handler
                )
            }

    private fun getNonMediaFileObserver(
        contentResolver: ContentResolver,
        onNewMoveFile: (MoveFile) -> Unit,
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
                        handler = handler
                    )
                } else {
                    null
                }
            }
}