package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import kotlinx.coroutines.CoroutineScope

const val BACK_PRESS_WINDOW_DURATION = 2500L

@Composable
fun rememberBackPressHandler(scope: CoroutineScope = rememberCoroutineScope()): BackPressHandler =
    remember {
        BackPressHandler(scope, BACK_PRESS_WINDOW_DURATION)
    }
