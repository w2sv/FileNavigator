package com.w2sv.navigator.observing

import android.content.Context
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfigMap
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

/**
 * @param enabledNonMediaFileTypes [FileObserver]s will be relaunched when the user makes a change about the en-/disabled FileTypes,
 * therefore we don't need a [StateFlow] here to react to changes.
 */
internal class NonMediaFileObserver @AssistedInject constructor(
    @Assisted private val enabledNonMediaFileTypes: Collection<FileType>,
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
        getAutoMoveConfig = { fileType, sourceType ->
            fileTypeConfigMapStateFlow
                .value
                .getValue(fileType)
                .sourceTypeConfigMap
                .getValue(sourceType)
                .autoMoveConfig
        },
        handler = handler,
        blacklistedMediaUris = blacklistedMediaUris,
        scope = scope
    ) {

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            enabledNonMediaFileTypesWithExtensions: Collection<FileType>,
            fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
            handler: Handler
        ): NonMediaFileObserver
    }

    override val logIdentifier: String
        get() = this.javaClass.simpleName

    init {
        i { "Initialized NonMediaFileObserver with fileTypes: $enabledNonMediaFileTypes" }
    }

    override fun determineMatchingEnabledFileAndSourceTypeOrNull(mediaStoreFileData: MediaStoreFileData): FileAndSourceType? =
        enabledNonMediaFileTypes
            .firstOrNull { it.fileExtensions.contains(mediaStoreFileData.extension) }
            ?.let { fileType -> FileAndSourceType(fileType, SourceType.Download) }
}
