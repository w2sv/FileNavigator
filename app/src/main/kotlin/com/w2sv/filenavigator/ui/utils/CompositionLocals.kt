package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavHostController =
    staticCompositionLocalOf<NavHostController> { throw UninitializedPropertyAccessException("LocalRootNavHostController not yet provided") }

val LocalUseDarkTheme =
    compositionLocalOf { false }