package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import com.anggrayudi.storage.media.MediaType
import com.w2sv.data.model.FileType
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.MoveFile
import slimber.log.i

internal class NonMediaFileObserver(
    private val fileTypes: List<FileType.NonMedia>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit
) :
    FileObserver(
        MediaType.DOWNLOADS.readUri!!,
        contentResolver,
        onNewMoveFile
    ) {

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${fileTypes.map { it.identifier }}" }
    }

    override fun getNavigatableFileIfMatching(
        mediaStoreFile: MediaStoreFile
    ): MoveFile? =
        fileTypes
            .firstOrNull { it.matchesFileExtension(mediaStoreFile.columnData.fileExtension) }
            ?.let { fileType ->
                MoveFile(
                    type = fileType,
                    sourceKind = FileType.Source.Kind.Download,
                    mediaStoreFile = mediaStoreFile
                )
            }
}