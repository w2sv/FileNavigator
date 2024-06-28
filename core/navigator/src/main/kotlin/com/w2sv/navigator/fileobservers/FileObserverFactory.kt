package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.kotlinutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.navigator.MediaTypeToFileObserver
import com.w2sv.navigator.mediastore.MediaStoreFileRetriever
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileObserverFactory @Inject constructor(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val mediaStoreFileRetriever: MediaStoreFileRetriever,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithSynchronousInitial(scope)
    }

    private val handlerThread by lazy {
        HandlerThread("com.w2sv.filenavigator.ContentObserverThread")
    }
    private val handler by lazy {
        Handler(handlerThread.looper)
    }

    fun quitThread() {
        handlerThread.quit()
    }

    operator fun invoke(): MediaTypeToFileObserver {
        if (!handlerThread.isAlive) {
            handlerThread.start()
        }

        return buildMap {
            putAll(
                mediaFileObservers()
            )
            nonMediaFileObserver()?.let {
                put(MediaType.DOWNLOADS, it)
            }
        }
    }

    private fun mediaFileObservers(): MediaTypeToFileObserver =
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
                    context = context,
                    newMoveFileNotificationManager = newMoveFileNotificationManager,
                    mediaStoreFileRetriever = mediaStoreFileRetriever,
                    handler = handler
                )
            }

    private fun nonMediaFileObserver(): NonMediaFileObserver? =
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
                        context = context,
                        newMoveFileNotificationManager = newMoveFileNotificationManager,
                        mediaStoreFileRetriever = mediaStoreFileRetriever,
                        handler = handler
                    )
                } else {
                    null
                }
            }
}