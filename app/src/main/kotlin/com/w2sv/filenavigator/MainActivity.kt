package com.w2sv.filenavigator

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.filenavigator.service.FileNavigator
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileNavigator.startService(this)  // TODO: remove

        setContent {
            FileNavigatorTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (it) {
                FileNavigator.startService(context)
            }
        }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedButton(
            onClick = {
                when (permissionState.status.isGranted) {
                    true -> FileNavigator.startService(context)
                    false -> permissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .width(120.dp)
                .height(80.dp)
        ) {
            Text(text = stringResource(R.string.start_listener), textAlign = TextAlign.Center)
        }
    }
}