package com.w2sv.filenavigator.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner

@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM =
    hiltViewModel(
        LocalView.current.findViewTreeViewModelStoreOwner()
            ?: LocalContext.current.findActivity() as ViewModelStoreOwner
    )

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Couldn't get Activity")
}