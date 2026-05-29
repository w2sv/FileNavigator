package com.w2sv.navigator.observing

import android.content.Context
import com.w2sv.common.di.ApplicationIoScope
import com.w2sv.domain.repository.NavigatorConfigFlow
import com.w2sv.navigator.di.SelfCreatedFilesFlow
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import com.w2sv.navigator.moving.FileMover
import com.w2sv.navigator.shared.createdfiles.SelfCreatedFileIdentifiers
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Bundles shared infrastructure needed by all [FileObserver] instances.
 *
 * This avoids large constructor parameter lists and replaces AssistedInject by providing
 * a single, DI-managed environment object that is delegated by each observer.
 */
internal interface FileObserverEnvironment {
    val context: Context
    val scope: CoroutineScope
    val selfCreatedFilesFlow: Flow<SelfCreatedFileIdentifiers>
    val notificationEventHandler: NotificationEventHandler
    val navigatorConfigFlow: NavigatorConfigFlow
    val fileMover: FileMover
}

internal class FileObserverEnvironmentImpl @Inject constructor(
    @ApplicationContext override val context: Context,
    @ApplicationIoScope override val scope: CoroutineScope,
    @SelfCreatedFilesFlow override val selfCreatedFilesFlow: Flow<SelfCreatedFileIdentifiers>,
    override val notificationEventHandler: NotificationEventHandler,
    override val navigatorConfigFlow: NavigatorConfigFlow,
    override val fileMover: FileMover
) : FileObserverEnvironment
