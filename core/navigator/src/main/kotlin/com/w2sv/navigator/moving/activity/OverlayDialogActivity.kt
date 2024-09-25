package com.w2sv.navigator.moving.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.R

internal class OverlayDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this, R.style.MD_Dark)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage("Some whacko message")
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                finishAndRemoveTask()
            }
            .show()
    }
}