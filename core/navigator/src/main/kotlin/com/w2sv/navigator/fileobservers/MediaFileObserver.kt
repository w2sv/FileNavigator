package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import com.w2sv.domain.model.FileType
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.MoveFile
import slimber.log.i

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val sourceKinds: Set<FileType.Source.Kind>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit
) :
    FileObserver(
        fileType.simpleStorageMediaType.readUri!!,
        contentResolver,
        onNewMoveFile
    ) {

    init {
        i { "Initialized ${fileType.name} MediaFileObserver with sourceKinds: ${sourceKinds.map { it.name }}" }
    }

    override fun getLogIdentifier(): String =
        "${this.javaClass.simpleName}.${fileType.name}"

    override fun getMoveFileIfMatching(
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