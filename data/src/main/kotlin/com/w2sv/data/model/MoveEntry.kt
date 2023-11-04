package com.w2sv.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class MoveEntry(
    val fileName: String,
    val originalLocation: String,
    val fileType: FileType,
    val fileSourceKind: FileType.Source.Kind,
    val destination: Uri,
    @PrimaryKey val dateTime: LocalDateTime
)