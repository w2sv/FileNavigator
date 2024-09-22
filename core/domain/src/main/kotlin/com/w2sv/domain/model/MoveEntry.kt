package com.w2sv.domain.model

import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import java.time.LocalDateTime

/**
 * @param fileName For UI display, possibly for filtering
 * @param fileType & sourceType For icon display, possibly for filtering
 * @param destination For UI display
 * @param movedFileDocumentUri For checking whether file still exists
 * @param movedFileMediaUri For viewing file, null for cloud destinations
 * @param dateTime For UI display
 * @param autoMoved For UI display
 */
data class MoveEntry(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    val destination: MoveDestination,
    val movedFileDocumentUri: DocumentUri,
    val movedFileMediaUri: MediaUri?,
    val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    val fileAndSourceType by lazy {
        FileAndSourceType(fileType, sourceType)
    }
}