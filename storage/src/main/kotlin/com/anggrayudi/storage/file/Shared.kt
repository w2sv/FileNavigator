package com.anggrayudi.storage.file

import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.extension.awaitUiResultWithPending
import com.anggrayudi.storage.extension.trimFileSeparator
import com.anggrayudi.storage.file.DocumentFileCompat.removeForbiddenCharsFromFilename

inline fun <T> resolveFileConflict(
    file: T,
    mode: CreateMode,
    onConflict: SingleFileConflictCallback<T>?,
    recreate: T.() -> T?,
    exists: T.() -> Boolean,
    isFile: T.() -> Boolean,
    onFileResolved: (T) -> Unit
): CreateMode {
    var createMode = mode

    if (onConflict != null && file.exists()) {
        createMode = awaitUiResultWithPending(onConflict.uiScope) {
            onConflict.onFileConflict(file, SingleFileConflictCallback.FileConflictAction(it))
        }.toCreateMode(true)
    }

    if (createMode != CreateMode.CREATE_NEW && file.exists()) {
        when {
            createMode == CreateMode.REPLACE -> file.recreate()
            createMode != CreateMode.SKIP_IF_EXISTS && file.isFile() -> file
            else -> null
        }
            ?.let { resolvedFile -> onFileResolved(resolvedFile) }
        return createMode
    }

    return createMode
}

internal data class FileCreationInfo(
    val cleanName: String,
    val subFolder: String,
    val baseFileName: String,
    val fullFileName: String,
    val mimeType: String,
    val extension: String
) {
    companion object {
        fun infer(
            baseDirName: String,
            mimeType: String?
        ): FileCreationInfo {
            val cleanName = baseDirName.removeForbiddenCharsFromFilename().trimFileSeparator()
            val subFolder = cleanName.substringBeforeLast('/', "")
            val filename = cleanName.substringAfterLast('/')

            val extensionByName = MimeType.getExtensionFromFileName(cleanName)
            val extension = if (
                extensionByName.isNotEmpty() &&
                (mimeType == null || mimeType == MimeType.UNKNOWN || mimeType == MimeType.BINARY_FILE)
            ) {
                extensionByName
            } else {
                MimeType.getExtensionFromMimeTypeOrFileName(mimeType, cleanName)
            }

            val baseFileName = filename.removeSuffix(".$extension")
            val fullFileName = "$baseFileName.$extension".trimEnd('.')
            val resolvedMimeType = MimeType.getMimeTypeFromExtension(extension).let {
                if (it == MimeType.UNKNOWN) MimeType.BINARY_FILE else it
            }

            return FileCreationInfo(
                cleanName = cleanName,
                subFolder = subFolder,
                baseFileName = baseFileName,
                fullFileName = fullFileName,
                mimeType = resolvedMimeType,
                extension = extension
            )
        }
    }
}
