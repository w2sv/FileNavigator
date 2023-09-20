package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.net.Uri
import com.anggrayudi.storage.media.MediaType
import com.w2sv.data.model.FileType
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

    override fun getMoveFileIfMatching(
        mediaStoreFileData: MoveFile.MediaStoreData,
        uri: Uri
    ): MoveFile? =
        fileTypes
            .firstOrNull { it.matchesFileExtension(mediaStoreFileData.fileExtension) }
            ?.let { fileType ->
                MoveFile(
                    uri = uri,
                    type = fileType,
                    sourceKind = FileType.Source.Kind.Download,
                    data = mediaStoreFileData
                )
            }
}