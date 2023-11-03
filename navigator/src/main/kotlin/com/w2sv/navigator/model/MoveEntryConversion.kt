package com.w2sv.navigator.model

import android.net.Uri
import com.w2sv.data.model.MoveEntry
import java.util.Date

fun getMoveEntry(moveFile: MoveFile, destination: Uri, date: Date): MoveEntry =
    MoveEntry(
        fileName = moveFile.mediaStoreFile.columnData.name,
        originalLocation = moveFile.mediaStoreFile.columnData.volumeRelativeDirPath,
        fileType = moveFile.source.fileType,
        fileSourceKind = moveFile.source.kind,
        destination = destination,
        date = date
    )