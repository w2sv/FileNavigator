package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.mediastore.MediaStoreData
import com.w2sv.navigator.mediastore.MediaStoreDataProducer
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class NonMediaFileObserver(
    private val enabledFileTypesStateFlow: StateFlow<Set<FileType.NonMedia>>,
    fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    newMoveFileNotificationManager: NewMoveFileNotificationManager,
    mediaStoreDataProducer: MediaStoreDataProducer,
    context: Context,
    handler: Handler
) :
    FileObserver(
        context = context,
        newMoveFileNotificationManager = newMoveFileNotificationManager,
        mediaStoreDataProducer = mediaStoreDataProducer,
        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
        handler = handler
    ) {

    private val enabledFileTypes: Set<FileType.NonMedia>
        get() = enabledFileTypesStateFlow.value

    override val logIdentifier: String
        get() = this.javaClass.simpleName

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${enabledFileTypes.map { it.logIdentifier }}" }
    }

    override fun enabledFileAndSourceTypeOrNull(
        mediaStoreData: MediaStoreData
    ): FileAndSourceType? =
        enabledFileTypes
            .firstOrNull { it.matchesFileExtension(mediaStoreData.fileExtension) }
            ?.let { fileType ->
                FileAndSourceType(fileType, SourceType.Download)
            }
}