package com.w2sv.domain.model

import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.MediaUri
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import java.time.LocalDateTime

sealed interface MovedFile {
    val documentUri: DocumentUri
    val name: String
    val originalName: String?
    val fileType: FileType
    val sourceType: SourceType
    val moveDestination: MoveDestinationApi
    val moveDateTime: LocalDateTime
    val autoMoved: Boolean

    val localOrNull: Local?
        get() = this as? Local

    val externalOrNull: External?
        get() = this as? External

    val fileAndSourceType
        get() = FileAndSourceType(fileType, sourceType)

    /**
     * A file that has been moved to a directory [moveDestination] residing on the local file system.
     * As it is a local file, it has a [mediaUri], which is used for file view intent launching.
     */
    data class Local(
        override val documentUri: DocumentUri,
        val mediaUri: MediaUri?,
        override val name: String,
        override val originalName: String?,
        override val fileType: FileType,
        override val sourceType: SourceType,
        override val moveDestination: LocalDestinationApi,
        override val moveDateTime: LocalDateTime,
        override val autoMoved: Boolean
    ) : MovedFile

    /**
     * A file that has been moved to an external provider (may be a cloud provider like Google Drive / Dropbox, but also non-cloud ones like Termux).
     * 'External' basically refers to 'not managed by the SAF'.
     */
    data class External(
        override val name: String,
        override val originalName: String?,
        override val fileType: FileType,
        override val sourceType: SourceType,
        override val moveDestination: ExternalDestinationApi,
        override val moveDateTime: LocalDateTime
    ) : MovedFile {

        override val documentUri: DocumentUri
            get() = moveDestination.documentUri

        override val autoMoved: Boolean = false
    }
}
