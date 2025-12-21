package com.w2sv.filenavigator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.Keep
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreen

@Keep
class NavigatorSettingsBenchmarkActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            NavigatorSettingsScreen(snackbarHostState = snackbarHostState)
        }
    }
}
