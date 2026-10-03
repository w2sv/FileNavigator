package com.w2sv.navigator.observing

import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.navigator.domain.moving.MediaStoreEntry

internal class DownloadsObserver(private val fileTypes: Collection<FileType>, handler: Handler, environment: FileObserverEnvironment) :
    FileObserver(
        mediaType = MediaType.DOWNLOADS,
        blacklistSize = 16,
        handler = handler,
        environment = environment
    ) {

    override fun enabledFileAndSourceTypeOrNull(mediaStoreEntry: MediaStoreEntry): FileAndSourceType? {
        val compound = mediaStoreEntry.compoundFileExtension
        if (compound.isNotEmpty()) {
            fileTypes.firstOrNull { it.fileExtensions.contains(compound) }
                ?.let { return FileAndSourceType(it, SourceType.Download) }
        }
        return fileTypes
            .firstOrNull { it.fileExtensions.contains(mediaStoreEntry.fileExtension) }
            ?.let { fileType -> FileAndSourceType(fileType, SourceType.Download) }
    }
}
