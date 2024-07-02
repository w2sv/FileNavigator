package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.kotlinutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.navigator.MediaTypeToFileObserver
import com.w2sv.navigator.mediastore.MediaStoreDataProducer
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

typealias FileTypeConfigMap = Map<FileType, FileTypeConfig>

@Singleton
internal class FileObserverFactory @Inject constructor(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val mediaStoreDataProducer: MediaStoreDataProducer,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    private val fileTypeConfigMapStateFlow by lazy {
        navigatorConfigDataSource.navigatorConfig
            .map { it.fileTypeConfigMap }
            .stateInWithSynchronousInitial(scope)
    }
    private val fileTypeConfigMap
        get() = fileTypeConfigMapStateFlow.value

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
            .filter { fileTypeConfigMap.getValue(it).enabled }
            .associate { mediaFileType ->
                mediaFileType.simpleStorageMediaType to MediaFileObserver(
                    fileType = mediaFileType,
                    fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                    context = context,
                    newMoveFileNotificationManager = newMoveFileNotificationManager,
                    mediaStoreDataProducer = mediaStoreDataProducer,
                    handler = handler
                )
            }

    private fun nonMediaFileObserver(): NonMediaFileObserver? =
        fileTypeConfigMapStateFlow.mapState { fileTypeConfigMap ->
            FileType.NonMedia.values.filter { fileTypeConfigMap.getValue(it).enabled }.toSet()
        }
            .let { enabledFileTypesStateFlow ->
                if (enabledFileTypesStateFlow.value.isNotEmpty()) {
                    NonMediaFileObserver(
                        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
                        enabledFileTypesStateFlow = enabledFileTypesStateFlow,
                        context = context,
                        newMoveFileNotificationManager = newMoveFileNotificationManager,
                        mediaStoreDataProducer = mediaStoreDataProducer,
                        handler = handler
                    )
                } else {
                    null
                }
            }
}