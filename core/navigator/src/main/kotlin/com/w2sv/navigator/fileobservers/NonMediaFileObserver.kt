package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.mediastore.MoveFile
import com.w2sv.navigator.mediastore.MediaStoreFileProducer
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class NonMediaFileObserver(
    private val enabledFileTypesStateFlow: StateFlow<Set<FileType.NonMedia>>,
    fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    newMoveFileNotificationManager: NewMoveFileNotificationManager,
    mediaStoreFileProducer: MediaStoreFileProducer,
    context: Context,
    handler: Handler
) :
    FileObserver(
        context = context,
        newMoveFileNotificationManager = newMoveFileNotificationManager,
        mediaStoreFileProducer = mediaStoreFileProducer,
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
        moveFile: MoveFile
    ): FileAndSourceType? =
        enabledFileTypes
            .firstOrNull { it.matchesFileExtension(moveFile.mediaStoreData.fileExtension) }
            ?.let { fileType ->
                FileAndSourceType(fileType, SourceType.Download)
            }
}