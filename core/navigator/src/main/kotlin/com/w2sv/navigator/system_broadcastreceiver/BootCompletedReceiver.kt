package com.w2sv.navigator.system_broadcastreceiver

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.logging.LoggingBroadcastReceiver
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Called on system emission of [Intent.ACTION_BOOT_COMPLETED].
 * Starts the [FileNavigator] if [com.w2sv.domain.model.navigatorconfig.NavigatorConfig.startOnBoot] true and the necessary permissions for
 * running the navigator are granted.
 *
 * Doesn't require any registering via [Context.registerReceiver], as BOOT_COMPLETED receivers are automatically registered via the manifest.
 */
@AndroidEntryPoint
internal class BootCompletedReceiver : LoggingBroadcastReceiver() {

    @Inject
    lateinit var navigatorConfigDataSource: NavigatorConfigDataSource

    @Inject
    @GlobalScope(AppDispatcher.Default)
    internal lateinit var scope: CoroutineScope

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        scope.launch {
            if (navigatorConfigDataSource.navigatorConfig.first().startOnBoot && FileNavigator.necessaryPermissionsGranted(context)) {
                FileNavigator.start(context)
            }
        }
    }
}
