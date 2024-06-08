package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.moving.MoveFile
import slimber.log.i

internal class NonMediaFileObserver(
    private val enabledFileTypeToAutoMoveConfig: Map<FileType.NonMedia, AutoMoveConfig>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit,
    handler: Handler
) :
    FileObserver(
        contentObserverUri = MediaType.DOWNLOADS.readUri!!,
        contentResolver = contentResolver,
        onNewMoveFileListener = onNewMoveFile,
        handler = handler
    ) {

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${enabledFileTypeToAutoMoveConfig.keys.map { it.logIdentifier }}" }
    }

    override fun getLogIdentifier(): String = this.javaClass.simpleName

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