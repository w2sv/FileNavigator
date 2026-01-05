package com.w2sv.database.entity

import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.w2sv.common.uri.documentUri
import com.w2sv.common.uri.mediaUri
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.ExternalDestination
import com.w2sv.domain.model.movedestination.LocalDestination
import java.time.LocalDateTime

@Entity
internal data class MovedFileEntity(
    val documentUri: Uri,
    val name: String,
    val originalName: String?,
    val type: FileType,
    val sourceType: SourceType,
    @PrimaryKey val moveDateTime: LocalDateTime,
    val autoMoved: Boolean,
    @Embedded("local_")
    val local: Type.Local?,
    @Embedded("external_")
    val external: Type.External?
) {
    sealed interface Type {
        data class Local(val mediaUri: Uri?, val moveDestination: Uri) : Type
        data class External(val providerPackageName: String?, val providerAppLabel: String?) : Type
    }

    constructor(movedFile: MovedFile) : this(
        documentUri = movedFile.documentUri.uri,
        name = movedFile.name,
        originalName = movedFile.originalName,
        type = movedFile.fileType,
        sourceType = movedFile.sourceType,
        moveDateTime = movedFile.moveDateTime,
        autoMoved = movedFile.autoMoved,
        local = movedFile.localOrNull?.let {
            Type.Local(it.mediaUri?.uri, movedFile.moveDestination.documentUri.uri)
        },
        external = movedFile.externalOrNull?.let {
            Type.External(
                providerPackageName = it.moveDestination.providerPackageName,
                providerAppLabel = it.moveDestination.providerAppLabel
            )
        }
    )

    fun asExternal(): MovedFile =
        when {
            local != null -> {
                MovedFile.Local(
                    documentUri = documentUri.documentUri,
                    mediaUri = local.mediaUri?.mediaUri,
                    name = name,
                    originalName = originalName,
                    fileType = type,
                    sourceType = sourceType,
                    moveDestination = LocalDestination(local.moveDestination.documentUri),
                    moveDateTime = moveDateTime,
                    autoMoved = autoMoved
                )
            }

            else -> {
                MovedFile.External(
                    moveDestination = ExternalDestination(
                        documentUri = documentUri.documentUri,
                        providerPackageName = external?.providerPackageName,
                        providerAppLabel = external?.providerAppLabel
                    ),
                    name = name,
                    originalName = originalName,
                    fileType = type,
                    sourceType = sourceType,
                    moveDateTime = moveDateTime
                )
            }
        }
}
