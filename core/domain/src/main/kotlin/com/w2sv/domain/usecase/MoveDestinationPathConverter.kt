package com.w2sv.domain.usecase

import android.content.Context
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoveDestinationPathConverter @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @GlobalScope(AppDispatcher.Default) scope: CoroutineScope
) {
    private val showStorageVolumeNames =
        preferencesRepository.showStorageVolumeNames.stateIn(scope, SharingStarted.Eagerly)

    operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
        when (moveDestination) {
            is LocalDestinationApi -> {
                moveDestination.pathRepresentation(
                    context = context,
                    includeVolumeName = showStorageVolumeNames.value
                )
            }

            is ExternalDestinationApi -> {
                moveDestination.uiRepresentation(
                    context
                )
            }

            else -> throw IllegalArgumentException()
        }
}