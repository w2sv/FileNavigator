package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.moving.MoveFile
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

typealias SourceTypeToAutoMoveConfig = Map<SourceType, AutoMoveConfig>

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val enabledSourceTypeToAutoMoveConfigStateFlow: StateFlow<SourceTypeToAutoMoveConfig>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit,
    handler: Handler
) :
    FileObserver(
        contentObserverUri = fileType.simpleStorageMediaType.readUri!!,
        contentResolver = contentResolver,
        onNewMoveFileListener = onNewMoveFile,
        handler = handler
    ) {

    private val enabledSourceTypeToAutoMoveConfig: SourceTypeToAutoMoveConfig
        get() = enabledSourceTypeToAutoMoveConfigStateFlow.value

    init {
        i { "Initialized ${fileType.logIdentifier} MediaFileObserver with types: ${enabledSourceTypeToAutoMoveConfig.keys.map { it.name }}" }
    }

    override val logIdentifier: String
        get() = "${this.javaClass.simpleName}.${fileType.logIdentifier}"

    override fun getMoveFileIfMatchingConstraints(
        mediaStoreFile: MediaStoreFile
    ): MoveFile? {
        if (fileType.matchesFileExtension(mediaStoreFile.columnData.fileExtension)) {
            val sourceType = mediaStoreFile.columnData.getSourceType()

            if (enabledSourceTypeToAutoMoveConfig.contains(sourceType)) {
                return MoveFile(
                    mediaStoreFile = mediaStoreFile,
                    fileAndSourceType = FileAndSourceType(fileType, sourceType),
                    moveMode = enabledSourceTypeToAutoMoveConfig
                        .getValue(sourceType)
                        .moveMode
                )
            }
        }
        return null
    }
}