package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.os.Handler
import com.w2sv.domain.model.FileType
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.moving.MoveFile
import slimber.log.i

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val sourceKinds: Set<FileType.Source.Kind>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit,
    handler: Handler
) :
    FileObserver(
        contentObserverUri = fileType.simpleStorageMediaType.readUri!!,
        contentResolver = contentResolver,
        onNewMoveFileListener = onNewMoveFile,
        handler = handler
    ) {

    init {
        i { "Initialized ${fileType.name} MediaFileObserver with sourceKinds: ${sourceKinds.map { it.name }}" }
    }

    override fun getLogIdentifier(): String =
        "${this.javaClass.simpleName}.${fileType.name}"

    override fun getMoveFileIfMatchingConstraints(
        mediaStoreFile: MediaStoreFile
    ): MoveFile? {
        if (fileType.matchesFileExtension(mediaStoreFile.columnData.fileExtension)) {
            val sourceKind = mediaStoreFile.columnData.getSourceKind()

            if (sourceKinds.contains(sourceKind)) {
                return MoveFile(
                    mediaStoreFile = mediaStoreFile,
                    fileType = fileType,
                    sourceKind = sourceKind
                )
            }
        }
        return null
    }
}