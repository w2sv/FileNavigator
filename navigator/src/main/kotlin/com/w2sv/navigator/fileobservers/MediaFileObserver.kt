package com.w2sv.navigator.fileobservers

import android.content.Context
import android.net.Uri
import com.w2sv.data.model.FileType
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.MoveFile
import com.w2sv.navigator.R
import slimber.log.i

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val sourceKinds: Set<FileType.Source.Kind>,
    context: Context,
    getNotificationParameters: (Int) -> FileNavigator.NotificationParameters,
    getDefaultMoveDestination: (FileType.Source) -> Uri?
) :
    FileObserver(
        fileType.mediaType.readUri!!,
        context,
        getNotificationParameters,
        getDefaultMoveDestination
    ) {

    init {
        i { "Initialized ${fileType::class.java.simpleName} MediaTypeObserver with originKinds: ${sourceKinds.map { it.name }}" }
    }

    override fun showNotificationIfApplicable(
        uri: Uri,
        mediaStoreFileData: MoveFile.MediaStoreData
    ) {
        if (fileType.matchesFileExtension(mediaStoreFileData.fileExtension)) {
            val sourceKind = mediaStoreFileData.getSourceKind()

            if (sourceKinds.contains(sourceKind)) {
                showDetectedNewFileNotification(
                    MoveFile(
                        uri = uri,
                        type = fileType,
                        sourceKind = sourceKind,
                        data = mediaStoreFileData
                    )
                )
            }
        }
    }

    override fun getNotificationTitleFormatArg(moveFile: MoveFile): String {
        moveFile.type as FileType.Media

        return when (moveFile.data.getSourceKind()) {
            FileType.Source.Kind.Screenshot -> context.getString(
                R.string.new_screenshot
            )

            FileType.Source.Kind.Camera -> context.getString(
                when (moveFile.type) {
                    FileType.Media.Image -> R.string.new_photo
                    FileType.Media.Video -> R.string.new_video
                    else -> throw Error()
                }
            )

            FileType.Source.Kind.Download -> context.getString(
                R.string.newly_downloaded_template,
                context.getString(moveFile.type.fileDeclarationRes)
            )

            FileType.Source.Kind.OtherApp -> context.getString(
                R.string.new_third_party_file_template,
                moveFile.data.dirName,
                context.getString(moveFile.type.fileDeclarationRes)
            )
        }
    }
}