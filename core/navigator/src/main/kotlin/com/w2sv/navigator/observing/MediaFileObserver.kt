package com.w2sv.navigator.observing

import android.content.Context
import android.os.Handler
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.util.filterKeysByValueToSet
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.SourceTypeConfigMap
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

internal class MediaFileObserver @AssistedInject constructor(
    @Assisted private val fileType: FileType,
    @Assisted private val sourceTypeConfigMapStateFlow: StateFlow<SourceTypeConfigMap>,
    @Assisted handler: Handler,
    moveFileNotificationManager: MoveFileNotificationManager,
    mediaStoreDataProducer: MediaStoreDataProducer,
    @ApplicationContext context: Context,
    blacklistedMediaUris: SharedFlow<MediaIdWithMediaType>,
    @GlobalScope(AppDispatcher.IO) scope: CoroutineScope
) : FileObserver(
    mediaType = fileType.mediaType,
    context = context,
    moveFileNotificationManager = moveFileNotificationManager,
    mediaStoreDataProducer = mediaStoreDataProducer,
    getAutoMoveConfig = { _, sourceType -> sourceTypeConfigMapStateFlow.value.getValue(sourceType).autoMoveConfig },
    handler = handler,
    blacklistedMediaUris = blacklistedMediaUris,
    scope = scope
) {

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            fileType: FileType,
            sourceTypeConfigMapStateFlow: StateFlow<SourceTypeConfigMap>,
            handler: Handler
        ): MediaFileObserver
    }

    /**
     * [FileObserver]s will be relaunched when the user makes a change about the en-/disabled [SourceType]s,
     * therefore we don't need a [StateFlow] here to react to changes.
     */
    private val enabledSourceTypes: Set<SourceType> =
        sourceTypeConfigMapStateFlow.value.filterKeysByValueToSet { it.enabled }

    override val logIdentifier: String
        get() = "${this.javaClass.simpleName}.${fileType.logIdentifier}"

    init {
        i { "Initialized ${fileType.logIdentifier} MediaFileObserver with sources ${enabledSourceTypes.map { it.name }}" }
    }

    override fun determineMatchingEnabledFileAndSourceTypeOrNull(mediaStoreFileData: MediaStoreFileData): FileAndSourceType? {
        val sourceType = mediaStoreFileData.sourceType()

        return if (enabledSourceTypes.contains(sourceType)) {
            FileAndSourceType(fileType, sourceType)
        } else {
            null
        }
    }
}
