package com.w2sv.navigator.observing

import android.content.Context
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
import com.w2sv.navigator.moving.model.MediaIdWithMediaType
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

typealias FileTypeConfigMap = Map<FileType, FileTypeConfig>

internal class FileObserverFactory @Inject constructor(  // TODO: try to inject directly into FileObserver base class
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val mediaStoreDataProducer: MediaStoreDataProducer,
    private val moveFileNotificationManager: MoveFileNotificationManager,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val blacklistedMediaUris: SharedFlow<MediaIdWithMediaType>
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithSynchronousInitial(scope)
    }
    private val fileTypeConfigMap
        get() = fileTypeConfigMapStateFlow.value

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
            .filter { fileTypeConfigMap.getValue(it).enabled }
            .associate { mediaFileType ->
                mediaFileType.simpleStorageMediaType to MediaFileObserver(
                    fileType = mediaFileType,
                    fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                    context = context,
                    moveFileNotificationManager = moveFileNotificationManager,
                    mediaStoreDataProducer = mediaStoreDataProducer,
                    handler = handler,
                    blacklistedMediaUris = blacklistedMediaUris
                )
            }

    private fun nonMediaFileObserver(handler: Handler): NonMediaFileObserver? =
        fileTypeConfigMapStateFlow.mapState { fileTypeConfigMap ->
            FileType.NonMedia.values.filter { fileTypeConfigMap.getValue(it).enabled }.toSet()
        }
            .let { enabledFileTypesStateFlow ->
                if (enabledFileTypesStateFlow.value.isNotEmpty()) {
                    NonMediaFileObserver(
                        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                        enabledFileTypesStateFlow = enabledFileTypesStateFlow,
                        context = context,
                        moveFileNotificationManager = moveFileNotificationManager,
                        mediaStoreDataProducer = mediaStoreDataProducer,
                        handler = handler,
                        blacklistedMediaUris = blacklistedMediaUris
                    )
                } else {
                    null
                }
            }
}