package com.w2sv.navigator.observing

import android.content.Context
import android.os.Handler
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.navigator.moving.model.MediaIdWithMediaType
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import com.w2sv.navigator.observing.model.MediaStoreFileData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class MediaFileObserver @AssistedInject constructor(
    @Assisted private val fileType: FileType.Media,
    @Assisted fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    @Assisted handler: Handler,
    moveFileNotificationManager: MoveFileNotificationManager,
    mediaStoreDataProducer: MediaStoreDataProducer,
    @ApplicationContext context: Context,
    blacklistedMediaUris: SharedFlow<MediaIdWithMediaType>
) :
    FileObserver(
        mediaType = fileType.simpleStorageMediaType,
        context = context,
        moveFileNotificationManager = moveFileNotificationManager,
        mediaStoreDataProducer = mediaStoreDataProducer,
        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
        handler = handler,
        blacklistedMediaUris = blacklistedMediaUris
    ) {

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            fileType: FileType.Media,
            fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
            handler: Handler
        ): MediaFileObserver
    }

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
        mediaStoreFileData: MediaStoreFileData
    ): FileAndSourceType? {
        val sourceType = mediaStoreFileData.sourceType()

        return if (enabledSourceTypes.contains(sourceType)) {
            FileAndSourceType(fileType, sourceType)
        } else
            null
    }
}