package com.w2sv.navigator.notifications.helper

import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.repository.NavigatorConfigFlow
import com.w2sv.navigator.domain.moving.MoveDestination
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class GetQuickMoveDestinations @Inject constructor(private val navigatorConfigFlow: NavigatorConfigFlow) {

    suspend operator fun invoke(fileAndSourceType: FileAndSourceType): List<MoveDestination.Directory> =
        navigatorConfigFlow
            .map {
                it
                    .quickMoveDestinations(
                        fileType = fileAndSourceType.fileType,
                        sourceType = fileAndSourceType.sourceType
                    )
                    .map(MoveDestination::Directory)
            }
            .first()
}
