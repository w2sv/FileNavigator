package com.w2sv.navigator.fileobservers

import android.content.Context
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.navigator.mediastore.MoveFile
import com.w2sv.navigator.mediastore.MediaStoreFileProducer
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class MediaFileObserver(
    private val fileType: FileType.Media,
    fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    newMoveFileNotificationManager: NewMoveFileNotificationManager,
    mediaStoreFileProducer: MediaStoreFileProducer,
    context: Context,
    handler: Handler
) :
    FileObserver(
        context = context,
        newMoveFileNotificationManager = newMoveFileNotificationManager,
        mediaStoreFileProducer = mediaStoreFileProducer,
        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
        handler = handler
    ) {

    private val enabledSourceTypesStateFlow: StateFlow<Set<SourceType>> =
        fileTypeConfigMapStateFlow.mapState { fileTypeConfigMap ->
            val fileTypeConfig = fileTypeConfigMap.getValue(fileType)
            fileType.sourceTypes
                .filter { fileTypeConfig.sourceTypeConfigMap.getValue(it).enabled }
                .toSet()
        }

    private val enabledSourceTypes
        get() = enabledSourceTypesStateFlow.value

    override val logIdentifier: String
        get() = "${this.javaClass.simpleName}.${fileType.logIdentifier}"

    init {
        i { "Initialized ${fileType.logIdentifier} MediaFileObserver with sources ${enabledSourceTypes.map { it.name }}" }
    }

    override fun enabledFileAndSourceTypeOrNull(
        moveFile: MoveFile
    ): FileAndSourceType? {
        val sourceType = moveFile.mediaStoreData.sourceType()

        return if (enabledSourceTypes.contains(sourceType)) {
            FileAndSourceType(fileType, sourceType)
        } else
            null
    }
}