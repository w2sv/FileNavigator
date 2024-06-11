package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.w2sv.common.DocumentUriToPathConverter

val LocalNavHostController =
    staticCompositionLocalOf<NavHostController> { throw UninitializedPropertyAccessException("LocalRootNavHostController not yet provided") }

val LocalDocumentUriToPathConverter =
    staticCompositionLocalOf<DocumentUriToPathConverter> {
        throw UninitializedPropertyAccessException(
            "LocalDocumentUriToPathConverter not yet provided"
        )
    }

val LocalUseDarkTheme =
    compositionLocalOf { false }