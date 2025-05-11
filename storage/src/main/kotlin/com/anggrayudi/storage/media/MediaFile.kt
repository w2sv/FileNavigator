package com.anggrayudi.storage.media

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.FileWrapper
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.extension.awaitUiResultWithPending
import com.anggrayudi.storage.extension.closeQuietly
import com.anggrayudi.storage.extension.getString
import com.anggrayudi.storage.extension.isRawFile
import com.anggrayudi.storage.extension.openInputStream
import com.anggrayudi.storage.extension.replaceCompletely
import com.anggrayudi.storage.extension.sendAll
import com.anggrayudi.storage.extension.sendAndClose
import com.anggrayudi.storage.extension.startCoroutineTimer
import com.anggrayudi.storage.extension.toInt
import com.anggrayudi.storage.extension.trimFileSeparator
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.DocumentFileCompat.removeForbiddenCharsFromFilename
import com.anggrayudi.storage.file.FileSize
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.copyToFile
import com.anggrayudi.storage.file.copyToFolder
import com.anggrayudi.storage.file.forceDelete
import com.anggrayudi.storage.file.getBasePath
import com.anggrayudi.storage.file.getStorageId
import com.anggrayudi.storage.file.hasEnoughSpace
import com.anggrayudi.storage.file.makeFile
import com.anggrayudi.storage.file.makeFolder
import com.anggrayudi.storage.file.moveFileTo
import com.anggrayudi.storage.file.openOutputStream
import com.anggrayudi.storage.file.toSingleFileError
import com.anggrayudi.storage.result.SingleFileError
import com.anggrayudi.storage.result.SingleFileResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

typealias OnWriteAccessDenied = (mediaFile: MediaFile, sender: IntentSender) -> Unit

/**
 * Created on 06/09/20
 * @author Anggrayudi H
 *
 * @param onWriteAccessDenied For inquiring the user's consent to modify other app's files upon write access having been denied
 * whilst carrying out a file operation depending on it. Only called starting from Android 10.
 * @see RecoverableSecurityException
 * @see [android.app.Activity.startIntentSenderForResult]
 */
class MediaFile @JvmOverloads constructor(
    context: Context,
    val uri: Uri,
    var onWriteAccessDenied: OnWriteAccessDenied? = null
) {

    /**
     * For construction from a [File].
     * @see MediaFile
     */
    @JvmOverloads
    constructor(
        context: Context,
        rawFile: File,
        onWriteAccessDenied: OnWriteAccessDenied? = null
    ) : this(
        context = context,
        uri = Uri.fromFile(rawFile),
        onWriteAccessDenied = onWriteAccessDenied
    )

    private val context = context.applicationContext

    /**
     * Some media files do not return file extension. This function helps you to fix this kind of issue.
     */
    @Suppress("DEPRECATION")
    val fullName: String
        get() = if (isRawFile) {
            file()?.name.orEmpty()
        } else {
            val mimeType = getColumnInfoString(MediaStore.MediaColumns.MIME_TYPE)
            val displayName = getColumnInfoString(MediaStore.MediaColumns.DISPLAY_NAME).orEmpty()
            MimeType.getFullFileName(displayName, mimeType)
        }

    /**
     * @see [fullName]
     */
    @Suppress("DEPRECATION")
    val name: String?
        get() = file()?.name ?: getColumnInfoString(MediaStore.MediaColumns.DISPLAY_NAME)

    val baseName: String
        get() = MimeType.getBaseFileName(fullName)

    val extension: String
        get() = MimeType.getExtensionFromFileName(fullName)

    /**
     * @see [mimeType]
     */
    @Suppress("DEPRECATION")
    val type: String?
        get() = file()?.name?.let {
            MimeType.getMimeTypeFromExtension(
                MimeType.getExtensionFromFileName(
                    it
                )
            )
        }
            ?: getColumnInfoString(MediaStore.MediaColumns.MIME_TYPE)

    /**
     * Advanced version of [type]. Returns [MimeType.UNKNOWN] if the mime type is not found.
     */
    val mimeType: String
        get() = getColumnInfoString(MediaStore.MediaColumns.MIME_TYPE)
            ?: MimeType.getMimeTypeFromExtension(extension)

    @Suppress("DEPRECATION")
    var length: Long
        get() = file()?.length() ?: getColumnInfoLong(MediaStore.MediaColumns.SIZE)
        set(value) {
            try {
                val contentValues =
                    ContentValues(1).apply { put(MediaStore.MediaColumns.SIZE, value) }
                context.contentResolver.update(uri, contentValues, null, null)
            } catch (e: SecurityException) {
                handleSecurityException(e)
            }
        }

    val formattedSize: String
        get() = Formatter.formatFileSize(context, length)

    /**
     * The URI presents in SAF database, but the file is not found or has zero length.
     * @see hasZeroLength
     */
    val isEmpty: Boolean
        get() = presentsInSafDatabase && hasZeroLength

    val presentsInSafDatabase: Boolean
        get() = context.contentResolver.query(uri, null, null, null, null)?.use {
            it.count > 0
        } == true

    /**
     * @see isEmpty
     */
    val hasZeroLength: Boolean
        get() = uri.openInputStream(context)?.use { stream -> stream.available() == 0 } != false

    /**
     * `true` if this file was created with [File]. Only works on API 28 and lower.
     */
    val isRawFile: Boolean
        get() = uri.isRawFile

    val lastModified: Long
        get() = file()?.lastModified()
            ?: getColumnInfoLong(MediaStore.MediaColumns.DATE_MODIFIED)

    val owner: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getColumnInfoString(
                MediaStore.MediaColumns.OWNER_PACKAGE_NAME
            )
        } else {
            null
        }

    /**
     * Check if this media is owned by your app.
     */
    val isMine: Boolean
        get() = owner == context.packageName

    /**
     * @return `null` in Android 10+ or if you try to read files from SD Card or you want to convert a file picked
     * from [Intent.ACTION_OPEN_DOCUMENT] or [Intent.ACTION_CREATE_DOCUMENT].
     * @see documentFile
     */
    fun file(): File? =
        if (isRawFile) uri.path?.let { File(it) } else null

    fun documentFile(): DocumentFile? =
        absolutePath
            .let {
                if (it.isEmpty()) {
                    null
                } else {
                    DocumentFileCompat.fromFullPath(
                        context,
                        it
                    )
                }
            }

    val absolutePath: String by lazy {
        val file = file()
        when {
            file != null -> file.path
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                try {
                    context.contentResolver.query(
                        uri,
                        arrayOf(MediaStore.MediaColumns.DATA),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            cursor.getString(MediaStore.MediaColumns.DATA)
                        } else {
                            ""
                        }
                    }.orEmpty()
                } catch (_: Exception) {
                    ""
                }
            }

            else -> {
                val projection = arrayOf(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    MediaStore.MediaColumns.DISPLAY_NAME
                )
                context.contentResolver.query(uri, projection, null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val relativePath =
                                cursor.getString(MediaStore.MediaColumns.RELATIVE_PATH)
                                    ?: return@lazy ""
                            val name = cursor.getString(MediaStore.MediaColumns.DISPLAY_NAME)
                            "${SimpleStorage.externalStoragePath}/$relativePath/$name".trimEnd('/')
                                .replaceCompletely("//", "/")
                        } else {
                            ""
                        }
                    }.orEmpty()
            }
        }
    }

    val basePath: String
        get() = absolutePath.substringAfter(SimpleStorage.externalStoragePath).trimFileSeparator()

    /**
     * @see MediaStore.MediaColumns.RELATIVE_PATH
     */
    @Suppress("DEPRECATION")
    val relativePath: String
        @SuppressLint("InlinedApi")
        get() {
            val file = file()
            return when {
                file != null -> {
                    file.path.substringBeforeLast('/')
                        .replaceFirst(SimpleStorage.externalStoragePath, "")
                        .trimFileSeparator() + "/"
                }

                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                    try {
                        context.contentResolver.query(
                            uri,
                            arrayOf(MediaStore.MediaColumns.DATA),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val realFolderAbsolutePath =
                                    cursor.getString(MediaStore.MediaColumns.DATA).orEmpty()
                                        .substringBeforeLast('/')
                                realFolderAbsolutePath.replaceFirst(
                                    SimpleStorage.externalStoragePath,
                                    ""
                                ).trimFileSeparator() + "/"
                            } else {
                                ""
                            }
                        }.orEmpty()
                    } catch (_: Exception) {
                        ""
                    }
                }

                else -> {
                    val projection = arrayOf(MediaStore.MediaColumns.RELATIVE_PATH)
                    context.contentResolver.query(uri, projection, null, null, null)
                        ?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                cursor.getString(MediaStore.MediaColumns.RELATIVE_PATH)
                            } else {
                                ""
                            }
                        }.orEmpty()
                }
            }
        }

    fun delete(): Boolean {
        val file = file()
        return if (file != null) {
            file.delete() || !file.exists()
        } else {
            try {
                context.contentResolver.delete(uri, null, null) > 0
            } catch (e: SecurityException) {
                handleSecurityException(e)
                false
            }
        }
    }

    /**
     * Please note that this function does not move file if you input `newName` as `Download/filename.mp4`.
     * If you want to move media files, please use [moveFileTo] instead.
     */
    fun renameTo(newName: String): Boolean {
        val file = file()
        val contentValues =
            ContentValues(1).apply { put(MediaStore.MediaColumns.DISPLAY_NAME, newName) }
        return if (file != null) {
            context.contentResolver.update(uri, contentValues, null, null)
            file.renameTo(File(file.parent, newName))
        } else {
            throw UnsupportedOperationException("Cannot rename media files on Android 10+")
        }
    }

    /**
     * Set to `true` if the file is being written to prevent users from accessing it.
     * @see MediaStore.MediaColumns.IS_PENDING
     */
    var isPending: Boolean
        @RequiresApi(Build.VERSION_CODES.Q)
        get() = getColumnInfoInt(MediaStore.MediaColumns.IS_PENDING) == 1
        @RequiresApi(Build.VERSION_CODES.Q)
        set(value) {
            val contentValues =
                ContentValues(1).apply { put(MediaStore.MediaColumns.IS_PENDING, value.toInt()) }
            try {
                context.contentResolver.update(uri, contentValues, null, null)
            } catch (e: SecurityException) {
                handleSecurityException(e)
            }
        }

    private fun handleSecurityException(
        e: SecurityException,
        scope: ProducerScope<SingleFileResult>? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
            onWriteAccessDenied?.invoke(this, e.userAction.actionIntent.intentSender)
        } else {
            scope?.trySend(SingleFileResult.Error(SingleFileError.StoragePermissionMissing()))
        }
    }

    @UiThread
    fun openFileIntent(authority: String) =
        Intent(Intent.ACTION_VIEW)
            .setData(
                if (isRawFile) {
                    FileProvider.getUriForFile(
                        context,
                        authority,
                        File(uri.path!!)
                    )
                } else {
                    uri
                }
            )
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * @param append if `false` and the file already exists, it will recreate the file.
     */
    @WorkerThread
    @JvmOverloads
    fun openOutputStream(append: Boolean = true): OutputStream? {
        return try {
            val file = file()
            if (file != null) {
                FileOutputStream(file, append)
            } else {
                context.contentResolver.openOutputStream(uri, if (append) "wa" else "w")
            }
        } catch (_: IOException) {
            null
        }
    }

    @WorkerThread
    fun openInputStream(): InputStream? {
        return try {
            val file = file()
            if (file != null) {
                FileInputStream(file)
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (_: IOException) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun moveTo(relativePath: String): Boolean {
        val contentValues =
            ContentValues(1).apply { put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath) }
        return try {
            context.contentResolver.update(uri, contentValues, null, null) > 0
        } catch (e: SecurityException) {
            handleSecurityException(e)
            false
        }
    }

    @WorkerThread
    fun moveTo(
        targetFolder: DocumentFile,
        fileDescription: FileDescription? = null,
        updateInterval: Long = 500,
        checkIfEnoughSpaceOnTarget: Boolean = true,
        onConflict: SingleFileConflictCallback<DocumentFile>
    ): Flow<SingleFileResult> =
        channelFlow {
            documentFile()?.let {
                sendAll(
                    it.moveFileTo(
                        context = context,
                        targetFolder = targetFolder,
                        fileDescription = fileDescription,
                        updateInterval = updateInterval,
                        checkIfEnoughSpaceOnTarget = checkIfEnoughSpaceOnTarget,
                        onConflict = onConflict
                    )
                )
                return@channelFlow
            }

            if (checkIfEnoughSpaceOnTarget) {
                enoughSpaceOnTarget(targetFolder)?.let {
                    sendAndClose(SingleFileResult.Error(it))
                    return@channelFlow
                }
            }

            val targetDirectory = if (fileDescription?.subFolder.isNullOrEmpty()) {
                targetFolder
            } else {
                val directory = targetFolder.makeFolder(
                    context,
                    fileDescription.subFolder,
                    CreateMode.REUSE
                )
                if (directory == null) {
                    sendAndClose(SingleFileResult.Error(SingleFileError.TargetNotWritable))
                    return@channelFlow
                } else {
                    directory
                }
            }

            val cleanFileName = MimeType.getFullFileName(
                fileDescription?.name ?: name.orEmpty(),
                fileDescription?.mimeType ?: type
            )
                .removeForbiddenCharsFromFilename().trimFileSeparator()
            val conflictResolution =
                handleFileConflict(targetDirectory, cleanFileName, this, onConflict)
            if (conflictResolution == SingleFileConflictCallback.ConflictResolution.SKIP) {
                close()
                return@channelFlow
            }

            try {
                val targetFile = createTargetFile(
                    targetDirectory,
                    cleanFileName,
                    fileDescription?.mimeType ?: type,
                    conflictResolution.toCreateMode(),
                    this
                )
                if (targetFile == null) {
                    close()
                    return@channelFlow
                }
                createFileStreams(targetFile, this) { inputStream, outputStream ->
                    copyFileStream(
                        inputStream,
                        outputStream,
                        targetFile,
                        updateInterval,
                        true,
                        this
                    )
                }
            } catch (e: SecurityException) {
                handleSecurityException(e, this)
            } catch (e: Exception) {
                trySend(e.toSingleFileError())
            }
            close()
        }

    @WorkerThread
    fun copyToFolder(
        targetFolder: DocumentFile,
        fileDescription: FileDescription? = null,
        updateInterval: Long = 500,
        checkIfEnoughSpaceOnTarget: Boolean = true,
        onConflict: SingleFileConflictCallback<DocumentFile>
    ): Flow<SingleFileResult> =
        channelFlow {
            if (checkIfEnoughSpaceOnTarget) {
                enoughSpaceOnTarget(targetFolder)?.let {
                    sendAndClose(SingleFileResult.Error(it))
                    return@channelFlow
                }
            }

            documentFile()?.let { documentFile ->
                sendAll(
                    documentFile.copyToFolder(
                        context = context,
                        targetFolder = targetFolder,
                        fileDescription = fileDescription,
                        updateInterval = updateInterval,
                        checkIfEnoughSpaceOnTarget = false,
                        onConflict = onConflict
                    )
                )
                close()
                return@channelFlow
            }

            val targetDirectory = if (fileDescription?.subFolder.isNullOrEmpty()) {
                targetFolder
            } else {
                targetFolder.makeFolder(
                    context = context,
                    name = fileDescription.subFolder,
                    mode = CreateMode.REUSE
                )
                    .also {
                        if (it == null) {
                            sendAndClose(SingleFileResult.Error(SingleFileError.TargetNotWritable))
                            return@channelFlow
                        }
                    }!!
            }

            val cleanFileName = MimeType.getFullFileName(
                fileDescription?.name ?: name.orEmpty(),
                fileDescription?.mimeType ?: type
            )
                .removeForbiddenCharsFromFilename().trimFileSeparator()
            val conflictResolution =
                handleFileConflict(targetDirectory, cleanFileName, this, onConflict)
            if (conflictResolution == SingleFileConflictCallback.ConflictResolution.SKIP) {
                close()
                return@channelFlow
            }

            try {
                val targetFile = createTargetFile(
                    targetDirectory,
                    cleanFileName,
                    fileDescription?.mimeType ?: type,
                    conflictResolution.toCreateMode(),
                    this
                )
                    .also {
                        if (it == null) {
                            close()
                            return@channelFlow
                        }
                    }!!
                createFileStreams(targetFile, this) { inputStream, outputStream ->
                    copyFileStream(
                        inputStream,
                        outputStream,
                        targetFile,
                        updateInterval,
                        false,
                        this
                    )
                }
            } catch (e: SecurityException) {
                handleSecurityException(e, this)
            } catch (e: Exception) {
                trySend(e.toSingleFileError())
            }
            close()
        }

    @WorkerThread
    fun copyToFile(
        targetFile: DocumentFile,
        updateInterval: Long = 500,
        checkIfEnoughSpaceOnTarget: Boolean = true,
        checkIfTargetWritable: Boolean = true,
        deleteOnSuccess: Boolean
    ): Flow<SingleFileResult> =
        channelFlow {
            if (checkIfEnoughSpaceOnTarget) {
                enoughSpaceOnTarget(targetFile)?.let {
                    sendAndClose(SingleFileResult.Error(it))
                    return@channelFlow
                }
            }

            documentFile()?.let { documentFile ->
                documentFile.copyToFile(
                    context = context,
                    targetFile = targetFile,
                    updateInterval = updateInterval,
                    scope = this,
                    deleteSourceFileOnSuccess = deleteOnSuccess,
                    checkIfTargetWritable = checkIfTargetWritable
                )
                close()
                return@channelFlow
            }

            try {
                createFileStreams(targetFile, this) { inputStream, outputStream ->
                    copyFileStream(
                        inputStream,
                        outputStream,
                        targetFile,
                        updateInterval,
                        false,
                        this
                    )
                }
            } catch (e: SecurityException) {
                handleSecurityException(e, this)
            } catch (e: Exception) {
                send(e.toSingleFileError())
            } finally {
                close()
            }
        }

    private fun enoughSpaceOnTarget(target: DocumentFile): SingleFileError.NotEnoughSpaceOnTarget? =
        target.hasEnoughSpace(context, length)

    private fun createTargetFile(
        targetDirectory: DocumentFile,
        fileName: String,
        mimeType: String?,
        mode: CreateMode,
        scope: ProducerScope<SingleFileResult>
    ): DocumentFile? {
        try {
            val absolutePath = DocumentFileCompat.buildAbsolutePath(
                context,
                targetDirectory.getStorageId(context),
                targetDirectory.getBasePath(context)
            )
            val targetFolder = DocumentFileCompat.mkdirs(context, absolutePath)
            if (targetFolder == null) {
                scope.trySend(SingleFileResult.Error(SingleFileError.TargetNotWritable))
                return null
            }

            val targetFile = targetFolder.makeFile(context, fileName, mimeType, mode)
            if (targetFile == null) {
                scope.trySend(SingleFileResult.Error(SingleFileError.TargetNotWritable))
            } else {
                return targetFile
            }
        } catch (e: SecurityException) {
            handleSecurityException(e, scope)
        } catch (e: Exception) {
            scope.trySend(e.toSingleFileError())
        }
        return null
    }

    private inline fun createFileStreams(
        targetFile: DocumentFile,
        scope: ProducerScope<SingleFileResult>,
        onStreamsReady: (InputStream, OutputStream) -> Unit
    ) {
        val outputStream = targetFile.openOutputStream(context)
        if (outputStream == null) {
            scope.trySend(SingleFileResult.Error(SingleFileError.TargetNotFound))
            return
        }

        val inputStream = openInputStream()
        if (inputStream == null) {
            scope.trySend(SingleFileResult.Error(SingleFileError.SourceNotFound))
            outputStream.closeQuietly()
            return
        }

        onStreamsReady(inputStream, outputStream)
    }

    private fun copyFileStream(
        inputStream: InputStream,
        outputStream: OutputStream,
        targetFile: DocumentFile,
        updateInterval: Long,
        deleteSourceFileWhenComplete: Boolean,
        scope: ProducerScope<SingleFileResult>
    ) {
        var timer: Job? = null
        try {
            var bytesMoved = 0L
            var writeSpeed = 0
            val srcSize = length
            // using timer on small file is useless. We set minimum 10MB.
            if (updateInterval > 0 && srcSize > 10 * FileSize.MB) {
                timer = startCoroutineTimer(repeatMillis = updateInterval) {
                    scope.trySend(
                        SingleFileResult.InProgress(
                            bytesMoved * 100f / srcSize,
                            bytesMoved,
                            writeSpeed
                        )
                    )
                    writeSpeed = 0
                }
            }
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = inputStream.read(buffer)
            while (read != -1) {
                outputStream.write(buffer, 0, read)
                bytesMoved += read
                writeSpeed += read
                read = inputStream.read(buffer)
            }
            timer?.cancel()
            if (deleteSourceFileWhenComplete) {
                delete()
            }
            scope.trySend(SingleFileResult.Completed(FileWrapper.Document(targetFile)))
        } finally {
            timer?.cancel()
            inputStream.closeQuietly()
            outputStream.closeQuietly()
        }
    }

    private fun handleFileConflict(
        targetFolder: DocumentFile,
        fileName: String,
        scope: ProducerScope<SingleFileResult>,
        onConflict: SingleFileConflictCallback<DocumentFile>
    ): SingleFileConflictCallback.ConflictResolution {
        targetFolder.child(context, fileName)?.let { targetFile ->
            val resolution = awaitUiResultWithPending(onConflict.uiScope) {
                onConflict.onFileConflict(
                    targetFile,
                    SingleFileConflictCallback.FileConflictAction(it)
                )
            }
            if (resolution == SingleFileConflictCallback.ConflictResolution.REPLACE) {
                if (!targetFile.forceDelete(context)) {
                    scope.trySend(SingleFileResult.Error(SingleFileError.TargetNotWritable))
                    return SingleFileConflictCallback.ConflictResolution.SKIP
                }
            }
            return resolution
        }
        return SingleFileConflictCallback.ConflictResolution.CREATE_NEW
    }

    private fun getColumnInfoString(column: String): String? {
        context.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(column)
                if (columnIndex != -1) {
                    return cursor.getString(columnIndex)
                }
            }
        }
        return null
    }

    private fun getColumnInfoLong(column: String): Long {
        context.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(column)
                if (columnIndex != -1) {
                    return cursor.getLong(columnIndex)
                }
            }
        }
        return 0
    }

    @Suppress("SameParameterValue")
    private fun getColumnInfoInt(column: String): Int {
        context.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(column)
                if (columnIndex != -1) {
                    return cursor.getInt(columnIndex)
                }
            }
        }
        return 0
    }

    override fun equals(other: Any?) =
        other === this || other is MediaFile && other.uri == uri

    override fun hashCode() =
        uri.hashCode()

    override fun toString() =
        uri.toString()
}
