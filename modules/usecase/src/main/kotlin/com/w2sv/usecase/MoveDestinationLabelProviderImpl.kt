package com.w2sv.usecase

import android.content.Context
import com.w2sv.common.di.ApplicationIoScope
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted

@Singleton
internal class MoveDestinationLabelProviderImpl @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @ApplicationIoScope scope: CoroutineScope
) : MoveDestinationLabelProvider {

    private val showStorageVolumeNames =
        preferencesRepository.showStorageVolumeNames.stateIn(scope, SharingStarted.Eagerly)

    override operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
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

            else -> error("Shouldn't occur")
        }
}
