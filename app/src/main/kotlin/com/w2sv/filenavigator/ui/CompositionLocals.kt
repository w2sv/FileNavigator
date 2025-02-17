package com.w2sv.filenavigator.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.domain.usecase.MoveDestinationPathConverter

val LocalDestinationsNavigator =
    staticCompositionLocalOf<DestinationsNavigator> {
        throw UninitializedPropertyAccessException(
            "LocalDestinationsNavigator not yet provided"
        )
    }

val LocalMoveDestinationPathConverter =
    staticCompositionLocalOf<MoveDestinationPathConverter> {
        throw UninitializedPropertyAccessException(
            "LocalDocumentUriToPathConverter not yet provided"
        )
    }

val LocalUseDarkTheme =
    compositionLocalOf { false }
