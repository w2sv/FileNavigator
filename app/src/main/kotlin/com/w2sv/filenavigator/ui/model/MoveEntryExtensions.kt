package com.w2sv.filenavigator.ui.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.child
import com.w2sv.data.model.MoveEntry

interface MovedFileMediaUriRetrievalResult {
    class Success(val mediaUri: Uri) : MovedFileMediaUriRetrievalResult
    data object CouldntFindFile : MovedFileMediaUriRetrievalResult
}

fun MoveEntry.getMovedFileMediaUri(context: Context): MovedFileMediaUriRetrievalResult {
    val documentFile = DocumentFile
        .fromSingleUri(context, destination)!!
        .child(context, fileName, false) ?: return MovedFileMediaUriRetrievalResult.CouldntFindFile

    return MovedFileMediaUriRetrievalResult.Success(
        MediaStore.getMediaUri(
            context,
            documentFile.uri
        )!!
    )
}