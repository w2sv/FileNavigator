package com.w2sv.navigator.fileobservers

import android.content.Context
import android.net.Uri
import com.anggrayudi.storage.media.MediaType
import com.w2sv.data.model.FileType
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.MoveFile
import com.w2sv.navigator.R
import slimber.log.i

internal class NonMediaFileObserver(
    private val fileTypes: List<FileType.NonMedia>,
    context: Context,
    getNotificationParameters: (Int) -> FileNavigator.NotificationParameters,
    getDefaultMoveDestination: (FileType.Source) -> Uri?
) :
    FileObserver(
        MediaType.DOWNLOADS.readUri!!,
        context,
        getNotificationParameters,
        getDefaultMoveDestination
    ) {

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${fileTypes.map { it::class.java.simpleName }}" }
    }

    override fun showNotificationIfApplicable(
        uri: Uri,
        mediaStoreFileData: MoveFile.MediaStoreData
    ) {
        fileTypes.firstOrNull { it.matchesFileExtension(mediaStoreFileData.fileExtension) }
            ?.let { fileType ->
                showDetectedNewFileNotification(
                    MoveFile(
                        uri = uri,
                        type = fileType,
                        sourceKind = FileType.Source.Kind.Download,
                        data = mediaStoreFileData
                    )
                )
            }
    }

    override fun getNotificationTitleFormatArg(moveFile: MoveFile): String =
        context.getString(R.string.new_file, context.getString(moveFile.type.titleRes))
}