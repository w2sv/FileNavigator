package com.w2sv.domain.usecase

import android.content.Context
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.MoveDestination
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

    // TODO: test
    operator fun invoke(moveDestination: MoveDestination, context: Context): String? =
        moveDestination.documentUri.documentFilePath(context)
            ?.run {
                if (showStorageVolumeNames.value) {
                    this
                } else {
                    substringAfter(":")
                }
            }
}