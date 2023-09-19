package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.net.Uri
import com.w2sv.data.model.FileType
import com.w2sv.navigator.model.MoveFile
import slimber.log.i

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val sourceKinds: Set<FileType.Source.Kind>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit
) :
    FileObserver(
        fileType.mediaType.readUri!!,
        contentResolver,
        onNewMoveFile
    ) {

    init {
        i { "Initialized ${fileType.identifier} MediaFileObserver with sourceKinds: ${sourceKinds.map { it.name }}" }
    }

    override fun getMoveFileIfMatching(
        mediaStoreFileData: MoveFile.MediaStoreData,
        uri: Uri
    ): MoveFile? {
        if (fileType.matchesFileExtension(mediaStoreFileData.fileExtension)) {
            val sourceKind = mediaStoreFileData.getSourceKind()

            if (sourceKinds.contains(sourceKind)) {
                return MoveFile(
                    uri = uri,
                    type = fileType,
                    sourceKind = sourceKind,
                    data = mediaStoreFileData
                )
            }
        }
        return null
    }
}