package com.w2sv.navigator.notifications.helper

import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import com.w2sv.navigator.domain.moving.MoveDestination
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

// TODO: test, make stuff suspending
internal class GetQuickMoveDestination @Inject constructor(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope
) {
    private val map = mutableMapOf<FileAndSourceType, StateFlow<List<MoveDestination.Directory>>>()

    operator fun invoke(fileAndSourceType: FileAndSourceType): List<MoveDestination.Directory> =
        map.getOrPut(
            key = fileAndSourceType,
            defaultValue = {
                navigatorConfigDataSource
                    .quickMoveDestinations(
                        fileType = fileAndSourceType.fileType,
                        sourceType = fileAndSourceType.sourceType
                    )
                    .map { it.map { localDestinationApi -> MoveDestination.Directory(localDestinationApi) } }
                    .stateInWithBlockingInitial(scope)
            }
        )
            .value
}
