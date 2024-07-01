package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.navigator.mediastore.MediaStoreFile
import com.w2sv.navigator.mediastore.MediaStoreFileRetriever
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal typealias SourceTypeToAutoMoveConfig = Map<SourceType, AutoMoveConfig>

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    private val enabledSourceTypeToAutoMoveConfigStateFlow: StateFlow<SourceTypeToAutoMoveConfig>,
    context: Context,
    newMoveFileNotificationManager: NewMoveFileNotificationManager,
    mediaStoreFileRetriever: MediaStoreFileRetriever,
    handler: Handler
) :
    FileObserver(
        context = context,
        newMoveFileNotificationManager = newMoveFileNotificationManager,
        mediaStoreFileRetriever = mediaStoreFileRetriever,
        handler = handler
    ) {

    private val enabledSourceTypeToAutoMoveConfig: SourceTypeToAutoMoveConfig
        get() = enabledSourceTypeToAutoMoveConfigStateFlow.value

    init {
        i { "Initialized ${fileType.logIdentifier} MediaFileObserver with sources: ${enabledSourceTypeToAutoMoveConfig.keys.map { it.name }}" }
    }

    override val logIdentifier: String
        get() = "${this.javaClass.simpleName}.${fileType.logIdentifier}"

    override fun getMoveFileIfMatchingConstraints(
        mediaStoreFile: MediaStoreFile
    ): MoveFile? {
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
        return null
    }
}