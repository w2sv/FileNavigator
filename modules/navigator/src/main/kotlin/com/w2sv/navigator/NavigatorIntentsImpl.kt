package com.w2sv.navigator

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.common.uri.DocumentUri
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import com.w2sv.navigator.moving.BatchMoveService
import com.w2sv.navigator.moving.activity.BatchMoveDestinationPickerActivity
import com.w2sv.navigator.moving.activity.DestinationPickerActivityApi
import com.w2sv.navigator.moving.activity.FileDeletionActivity
import com.w2sv.navigator.moving.activity.FileDestinationPickerActivity
import com.w2sv.navigator.moving.activity.QuickMoveDestinationAccessPermissionActivity
import com.w2sv.navigator.moving.activity.ViewFileIfPresentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class NavigatorIntentsImpl @Inject constructor(@ApplicationContext private val context: Context) : NavigatorIntents {

    override fun viewFile(moveFileNotificationData: MoveFileNotificationData): Intent =
        ViewFileIfPresentActivity.intent(moveFileNotificationData, context)

    override fun deleteFile(moveFileNotificationData: MoveFileNotificationData): Intent =
        FileDeletionActivity.intent(moveFileNotificationData, context)

    override fun pickFileDestination(
        file: NavigatableFile,
        startDestination: DocumentUri?,
        cancelNotification: CancelNotificationEvent
    ): Intent =
        DestinationPickerActivityApi.makeRestartActivityIntent<FileDestinationPickerActivity>(
            FileDestinationPickerActivity.Args(
                navigatableFile = file,
                startDestination = startDestination,
                cancelNotification = cancelNotification
            ),
            context
        )

    override fun pickBatchMoveDestination(args: List<MoveFileNotificationData>, startDestination: DocumentUri?): Intent =
        DestinationPickerActivityApi.makeRestartActivityIntent<BatchMoveDestinationPickerActivity>(
            BatchMoveDestinationPickerActivity.Args(
                moveFilesWithNotificationResources = args,
                startDestination = startDestination
            ),
            context
        )

    override fun quickMoveWithPermissionCheck(bundle: MoveOperation.QuickMove): Intent =
        QuickMoveDestinationAccessPermissionActivity.intent(bundle, context)

    override fun startNavigator() =
        FileNavigator.start(context)

    override fun stopNavigator(): Intent =
        FileNavigator.stopIntent(context)

    override fun openMainActivity(): Intent =
        Intent.makeRestartActivityTask(
            ComponentName(context, "com.w2sv.filenavigator.ui.MainActivity")
        )

    override fun openMainActivityPending(): PendingIntent =
        PendingIntent.getActivity(
            context,
            1,
            openMainActivity(),
            PendingIntent.FLAG_IMMUTABLE
        )

    override fun startBatchMove(batchMoveBundles: List<MoveOperation.Batchable>): Intent =
        BatchMoveService.startIntent(context, BatchMoveService.Args(batchMoveBundles))

    override fun cancelBatchMove(): Intent =
        BatchMoveService.cancelIntent(context)
}
