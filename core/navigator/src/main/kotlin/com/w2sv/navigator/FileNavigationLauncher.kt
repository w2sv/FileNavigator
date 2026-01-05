package com.w2sv.navigator

import android.content.ContentResolver
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.logging.log
import com.w2sv.navigator.di.MoveOperationSummaryChannel
import com.w2sv.navigator.moving.MoveResultListener
import com.w2sv.navigator.observing.FileObserver
import com.w2sv.navigator.observing.FileObserverFactory
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import slimber.log.i

internal class FileNavigationLauncher @Inject constructor(
    private val fileObserverFactory: FileObserverFactory,
    private val moveResultListener: MoveResultListener,
    private val moveOperationSummaryChannel: MoveOperationSummaryChannel,
    private val contentResolver: ContentResolver,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope
) {
    private var activeFileObservers: List<FileObserver>? = null
    private var moveResultConsumptionJob: Job? = null

    fun launch() {
        i { "Registering file observers" }
        setActiveFileObservers()
        moveResultConsumptionJob = scope.launch {
            moveOperationSummaryChannel
                .consumeAsFlow()
                .collect(moveResultListener::onMoveResult)
        }
    }

    private fun setActiveFileObservers() {
        activeFileObservers = fileObserverFactory()
            .onEach { observer ->
                contentResolver.registerContentObserver(
                    checkNotNull(observer.mediaType.readUri),
                    true,
                    observer
                )
            }
            .log { "Registered ${it.size} FileObserver(s)" }
    }

    private fun unregisterFileObservers() {
        activeFileObservers?.forEach(contentResolver::unregisterContentObserver)
        i { "Unregistered fileObservers" }
    }

    fun reregisterFileObservers() {
        unregisterFileObservers()
        setActiveFileObservers()
    }

    fun tearDown() {
        moveResultConsumptionJob?.cancel()
        unregisterFileObservers()
        fileObserverFactory.onDestroy()
    }
}
