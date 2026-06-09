package com.w2sv.navigator.domain

import android.app.PendingIntent
import android.content.Intent
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import com.w2sv.storage.uri.DocumentUri

interface NavigatorIntents {

    // =====================
    // File viewing / deletion
    // =====================

    fun viewFile(moveFileNotificationData: MoveFileNotificationData): Intent

    fun deleteFile(moveFileNotificationData: MoveFileNotificationData): Intent

    // =====================
    // Destination picking
    // =====================

    fun pickFileDestination(file: NavigatableFile, startDestination: DocumentUri?, cancelNotification: CancelNotificationEvent): Intent

    fun pickBatchMoveDestination(args: List<MoveFileNotificationData>, startDestination: DocumentUri?): Intent

    // =====================
    // Quick move / permissions
    // =====================

    fun quickMoveWithPermissionCheck(bundle: MoveOperation.QuickMove): Intent

    // =====================
    // Navigation / app control
    // =====================

    fun startNavigator()
    fun stopNavigator(): Intent

    fun openMainActivity(): Intent

    fun openMainActivityPending(): PendingIntent

    // =====================
    // Batch move (broadcast)
    // =====================

    fun startBatchMove(batchMoveBundles: List<MoveOperation.Batchable>): Intent

    fun cancelBatchMove(): Intent
}
