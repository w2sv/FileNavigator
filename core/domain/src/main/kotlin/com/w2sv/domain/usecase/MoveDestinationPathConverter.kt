package com.w2sv.domain.usecase

import android.content.Context
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted

interface MoveDestinationPathConverter {
    operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String
}

/**
 * For usage in Compose Previews only.
 */
class PreviewMoveDestinationPathConverter : MoveDestinationPathConverter {
    override operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
        when (moveDestination) {
            is LocalDestinationApi -> {
                moveDestination.pathRepresentation(
                    context = context,
                    includeVolumeName = true
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

@Singleton
internal class MoveDestinationPathConverterImpl @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @GlobalScope(AppDispatcher.Default) scope: CoroutineScope
) : MoveDestinationPathConverter {
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

            else -> throw IllegalArgumentException()
        }
}
