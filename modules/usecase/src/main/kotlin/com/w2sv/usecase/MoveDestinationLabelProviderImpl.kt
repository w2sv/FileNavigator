package com.w2sv.usecase

import android.content.Context
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MoveDestinationLabelProviderImpl @Inject constructor(private val preferencesRepository: PreferencesRepository) :
    MoveDestinationLabelProvider {

    override operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
        when (moveDestination) {
            is LocalDestinationApi -> {
                moveDestination.pathRepresentation(
                    context = context,
                    includeVolumeName = preferencesRepository.appSettings.value.showStorageVolumeNames
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
