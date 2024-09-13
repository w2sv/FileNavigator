package com.w2sv.navigator.observing

import android.content.Context
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.moving.model.MediaIdWithMediaType
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import com.w2sv.navigator.observing.model.MediaStoreFileData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class NonMediaFileObserver(
    private val enabledFileTypesStateFlow: StateFlow<Set<FileType.NonMedia>>,
    fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    moveFileNotificationManager: MoveFileNotificationManager,
    mediaStoreDataProducer: MediaStoreDataProducer,
    context: Context,
    handler: Handler,
    blacklistedMediaUris: SharedFlow<MediaIdWithMediaType>
) :
    FileObserver(
        mediaType = MediaType.DOWNLOADS,
        context = context,
        moveFileNotificationManager = moveFileNotificationManager,
        mediaStoreDataProducer = mediaStoreDataProducer,
        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
        handler = handler,
        blacklistedMediaUris = blacklistedMediaUris
    ) {

    private val enabledFileTypes: Set<FileType.NonMedia>
        get() = enabledFileTypesStateFlow.value

    override val logIdentifier: String
        get() = this.javaClass.simpleName

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${enabledFileTypes.map { it.logIdentifier }}" }
    }

    override fun enabledFileAndSourceTypeOrNull(
        mediaStoreFileData: MediaStoreFileData
    ): FileAndSourceType? =
        enabledFileTypes
            .firstOrNull { it.fileExtensions.contains(mediaStoreFileData.extension) }
            ?.let { fileType ->
                FileAndSourceType(fileType, SourceType.Download)
            }
}