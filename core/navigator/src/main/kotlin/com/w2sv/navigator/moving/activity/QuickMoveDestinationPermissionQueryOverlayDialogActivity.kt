package com.w2sv.navigator.moving.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class QuickMoveDestinationPermissionQueryOverlayDialogActivity : ComponentActivity() {

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var ioScope: CoroutineScope

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

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
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .setOnDismissListener {
                ioScope.launch {
                    preferencesRepository.showQuickMovePermissionQueryExplanation.save(
                        false
                    )
                }
                finishAndRemoveTask()
            }
            .create()
            .also { dialog = it }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()

        dialog?.dismiss()
    }

    companion object {
        fun startIfNotYetShown(context: Context, preferencesRepository: PreferencesRepository) {
            if (preferencesRepository.showQuickMovePermissionQueryExplanation.value) {
                i { "Starting QuickMoveDestinationPermissionQueryOverlayDialogActivity" }
                context.startActivity(
                    Intent(
                        context,
                        QuickMoveDestinationPermissionQueryOverlayDialogActivity::class.java
                    )
                )
            }
        }
    }
}