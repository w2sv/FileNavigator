package com.w2sv.navigator.moving.activity

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.w2sv.core.navigator.R
import com.w2sv.navigator.shared.DialogHostingActivity
import com.w2sv.navigator.shared.setIconHeader
import slimber.log.i

internal class QuickMoveDestinationPermissionQueryOverlayDialogActivity : DialogHostingActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i { "onCreate" }

        showDialog(
            AlertDialog
                .Builder(this, R.style.RoundedCornersAlertDialog)
                .setIconHeader(R.drawable.ic_info_outline_24)
                .setMessage(getString(R.string.quick_move_permission_query_dialog_content))
                .setPositiveButton(getString(R.string.understood)) { _, _ ->
                    setResult(RESULT_OK)
                    finishAndRemoveTask()
                }
                .setCancelable(false)
        )
    }
}
