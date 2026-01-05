package com.w2sv.navigator.moving.activity

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import com.w2sv.common.logging.LoggingComponentActivity

internal abstract class DialogHostingActivity : LoggingComponentActivity() {

    protected var dialog: Dialog? = null

    protected fun showDialog(builder: AlertDialog.Builder) {
        dialog = builder.create().also { it.show() }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Prevents 'android.view.WindowLeaked: Activity ... has
        // leaked window DecorView@f70b286[QuickMoveDestinationPermissionQueryOverlayDialogActivity] that was originally added here'
        dialog?.dismiss()
    }
}
