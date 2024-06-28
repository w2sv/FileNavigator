package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.navigator.mediastore.MediaStoreFile
import com.w2sv.navigator.mediastore.MediaStoreFileRetriever
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal typealias FileTypeToAutoMoveConfig = Map<FileType.NonMedia, AutoMoveConfig>

internal class NonMediaFileObserver(
    private val enabledFileTypeToAutoMoveConfigStateFlow: StateFlow<FileTypeToAutoMoveConfig>,
    context: Context,
    newMoveFileNotificationManager: NewMoveFileNotificationManager,
    mediaStoreFileRetriever: MediaStoreFileRetriever,
    handler: Handler
) :
    FileObserver(
        context = context,
        newMoveFileNotificationManager = newMoveFileNotificationManager,
        mediaStoreFileRetriever = mediaStoreFileRetriever,
        handler = handler
    ) {

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${enabledFileTypeToAutoMoveConfig.keys.map { it.logIdentifier }}" }
    }

    private val enabledFileTypeToAutoMoveConfig: FileTypeToAutoMoveConfig
        get() = enabledFileTypeToAutoMoveConfigStateFlow.value

    override val logIdentifier: String
        get() = this.javaClass.simpleName

    override fun getMoveFileIfMatchingConstraints(
        mediaStoreFile: MediaStoreFile
    ): MoveFile? =
        enabledFileTypeToAutoMoveConfig
            .keys
            .firstOrNull { it.matchesFileExtension(mediaStoreFile.columnData.fileExtension) }
            ?.let { fileType ->
                MoveFile(
                    mediaStoreFile = mediaStoreFile,
                    fileAndSourceType = FileAndSourceType(fileType, SourceType.Download),
                    moveMode = enabledFileTypeToAutoMoveConfig.getValue(fileType).moveMode
                )
            }
}