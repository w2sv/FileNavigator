package com.w2sv.navigator.shared

import android.app.Dialog
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog

abstract class DialogHostingActivity: ComponentActivity() {

    protected var dialog: Dialog? = null

    protected fun showDialog(builder: AlertDialog.Builder) {
        dialog = builder.create().also { it.show() }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Prevents 'android.view.WindowLeaked: Activity com.w2sv.navigator.moving.activity.QuickMoveDestinationPermissionQueryOverlayDialogActivity has
        // leaked window DecorView@f70b286[QuickMoveDestinationPermissionQueryOverlayDialogActivity] that was originally added here'
        dialog?.dismiss()
    }
}
