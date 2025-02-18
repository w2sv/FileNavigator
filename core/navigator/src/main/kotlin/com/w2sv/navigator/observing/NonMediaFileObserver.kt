package com.w2sv.navigator.observing

import android.content.Context
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.util.logIdentifier
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.moving.model.MediaIdWithMediaType
import com.w2sv.navigator.notifications.appnotifications.movefile.MoveFileNotificationManager
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import com.w2sv.navigator.observing.model.MediaStoreFileData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal class NonMediaFileObserver @AssistedInject constructor(
    @Assisted private val enabledFileTypesStateFlow: StateFlow<Set<FileType.NonMedia>>,
    @Assisted fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    @Assisted handler: Handler,
    moveFileNotificationManager: MoveFileNotificationManager,
    mediaStoreDataProducer: MediaStoreDataProducer,
    @ApplicationContext context: Context,
    blacklistedMediaUris: SharedFlow<MediaIdWithMediaType>,
    @GlobalScope(AppDispatcher.IO) scope: CoroutineScope
) :
    FileObserver(
        mediaType = MediaType.DOWNLOADS,
        context = context,
        moveFileNotificationManager = moveFileNotificationManager,
        mediaStoreDataProducer = mediaStoreDataProducer,
        fileTypeConfigMapStateFlow = fileTypeConfigMapStateFlow,
        handler = handler,
        blacklistedMediaUris = blacklistedMediaUris,
        scope = scope
    ) {

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            enabledFileTypesStateFlow: StateFlow<Set<FileType.NonMedia>>,
            fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
            handler: Handler
        ): NonMediaFileObserver
    }

    private val enabledFileTypes: Set<FileType.NonMedia>
        get() = enabledFileTypesStateFlow.value

    override val logIdentifier: String
        get() = this.javaClass.simpleName

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: ${enabledFileTypes.map { it.logIdentifier }}" }
    }

    override fun enabledFileAndSourceTypeOrNull(mediaStoreFileData: MediaStoreFileData): FileAndSourceType? =
        enabledFileTypes
            .firstOrNull { it.fileExtensions.contains(mediaStoreFileData.extension) }
            ?.let { fileType ->
                FileAndSourceType(fileType, SourceType.Download)
            }
}
