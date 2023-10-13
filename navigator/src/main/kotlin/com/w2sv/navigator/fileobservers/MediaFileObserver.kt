package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import com.w2sv.data.model.FileType
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.NavigatableFile
import slimber.log.i

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val sourceKinds: Set<FileType.Source.Kind>,
    contentResolver: ContentResolver,
    onNewMoveFile: (NavigatableFile) -> Unit
) :
    FileObserver(
        fileType.simpleStorageMediaType.readUri!!,
        contentResolver,
        onNewMoveFile
    ) {

    init {
        i { "Initialized ${fileType.identifier} MediaFileObserver with sourceKinds: ${sourceKinds.map { it.name }}" }
    }

    override fun getMoveFileIfMatching(
        mediaStoreFile: MediaStoreFile
    ): NavigatableFile? {
        if (fileType.matchesFileExtension(mediaStoreFile.columnData.fileExtension)) {
            val sourceKind = mediaStoreFile.columnData.getSourceKind()

            if (sourceKinds.contains(sourceKind)) {
                return NavigatableFile(
                    type = fileType,
                    sourceKind = sourceKind,
                    mediaStoreFile = mediaStoreFile
                )
            }
        }
        return null
    }
}