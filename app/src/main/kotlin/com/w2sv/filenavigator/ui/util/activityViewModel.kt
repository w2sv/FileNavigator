package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.w2sv.androidutils.findActivity

@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM =
    hiltViewModel(
        LocalView.current.findViewTreeViewModelStoreOwner()
            ?: LocalContext.current.findActivity() as ViewModelStoreOwner
    )