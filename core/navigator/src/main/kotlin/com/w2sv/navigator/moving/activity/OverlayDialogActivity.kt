package com.w2sv.navigator.moving.activity

import android.annotation.SuppressLint
import android.app.Dialog
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
import javax.inject.Inject

@AndroidEntryPoint
internal class OverlayDialogActivity : ComponentActivity() {

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var ioScope: CoroutineScope

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private var dialog: Dialog? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
//                ioScope.launch {
//                    preferencesRepository.showQuickMovePermissionQueryExplanation.save(
//                        false
//                    )
//                }
                dialog.dismiss()
            }
            .setCancelable(false)
            .setOnDismissListener {
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
}