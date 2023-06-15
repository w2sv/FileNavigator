package com.w2sv.filenavigator.ui.screens.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.PreferencesKey
import com.w2sv.filenavigator.ui.DialogButton
import com.w2sv.filenavigator.ui.RailwayText
import com.w2sv.filenavigator.utils.goToManageExternalStorageSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
@Composable
fun EventualManageExternalStorageRational(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showManageExternalStorageRational by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                ManageExternalStorageRationalDialog(
                    onConfirmation = {
                        goToManageExternalStorageSettings(
                            context
                        )
                    },
                    onDismissRequest = {
                        coroutineScope.launch {
                            mainScreenViewModel.saveToDataStore(
                                PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL,
                                true
                            )
                            value = false
                        }
                    }
                )
            }
        }

    if (!mainScreenViewModel.manageExternalStoragePermissionGranted.collectAsState().value) {
        if (!mainScreenViewModel.repository.showedManageExternalStorageRational.collectAsState(
                initial = false
            ).value
        ) {
            LaunchedEffect(
                key1 = Unit,
                block = {
                    delay(1000L)
                    showManageExternalStorageRational = true
                }
            )
        }
    }
}

@Composable
private fun ManageExternalStorageRationalDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            ElevatedButton(onClick = onDismissRequest) {
                RailwayText(text = stringResource(id = R.string.maybe_later))
            }
        },
        confirmButton = {
            DialogButton(onClick = {
                onDismissRequest()
                onConfirmation()
            }) {
                RailwayText(text = stringResource(id = R.string.alright))
            }
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_info_24),
                modifier = Modifier.size(32.dp),
                contentDescription = null
            )
        },
        text = { RailwayText(text = stringResource(id = R.string.manage_external_storage_permission_rational)) }
    )
}