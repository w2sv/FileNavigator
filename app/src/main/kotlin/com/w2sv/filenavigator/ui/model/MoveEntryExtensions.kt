package com.w2sv.filenavigator.ui.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Immutable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.child
import com.w2sv.domain.model.MoveEntry

@Immutable
interface MovedFileMediaUriRetrievalResult {
    @Immutable
    data class Success(val mediaUri: Uri) : MovedFileMediaUriRetrievalResult

    @Immutable
    data object CouldntFindFile : MovedFileMediaUriRetrievalResult
}

//TODO: test
fun MoveEntry.getMovedFileMediaUri(context: Context): MovedFileMediaUriRetrievalResult {
    val documentFile = DocumentFile
        .fromSingleUri(context, destinationDocumentUri)!!
        .child(context, fileName, false) ?: return MovedFileMediaUriRetrievalResult.CouldntFindFile

    return MovedFileMediaUriRetrievalResult.Success(
        MediaStore.getMediaUri(
            context,
            documentFile.uri
        )!!
    )
}