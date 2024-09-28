package com.w2sv.domain.model

import java.time.LocalDateTime

/**
 * @param fileName For UI display, possibly for filtering
 * @param fileType & sourceType For icon display, possibly for filtering
 * @param destinationEntry For UI display
 * @param dateTime For UI display
 * @param autoMoved For UI display
 */
data class MoveEntry(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    val destinationEntry: AnyMoveDestinationEntry,
    val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    val fileAndSourceType by lazy {
        FileAndSourceType(fileType, sourceType)
    }
}