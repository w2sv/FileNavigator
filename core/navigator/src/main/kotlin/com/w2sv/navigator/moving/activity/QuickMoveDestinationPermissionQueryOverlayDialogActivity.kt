package com.w2sv.navigator.moving.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.w2sv.core.navigator.R
import slimber.log.i

internal class QuickMoveDestinationPermissionQueryOverlayDialogActivity : ComponentActivity() {

    private var dialog: Dialog? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i { "onCreate" }

        AlertDialog
            .Builder(this, R.style.RoundedCornersAlertDialog)
            .apply {
                setCustomTitle(
                    LayoutInflater
                        .from(context)
                        .inflate(R.layout.quick_move_permission_query_dialog_title, null)
                )
            }
            .setMessage(getString(R.string.quick_move_permission_query_dialog_content))
            .setPositiveButton(getString(R.string.understood)) { _, _ ->
                setResult(RESULT_OK)
                finishAndRemoveTask()
            }
            .setCancelable(false)
            .create()
            .also { dialog = it }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()  // Prevents 'android.view.WindowLeaked: Activity com.w2sv.navigator.moving.activity.QuickMoveDestinationPermissionQueryOverlayDialogActivity has leaked window DecorView@f70b286[QuickMoveDestinationPermissionQueryOverlayDialogActivity] that was originally added here'
    }
}