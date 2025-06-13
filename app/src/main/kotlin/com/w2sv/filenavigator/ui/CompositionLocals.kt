package com.w2sv.filenavigator.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.domain.usecase.MoveDestinationPathConverter

val LocalDestinationsNavigator =
    staticCompositionLocalOf<DestinationsNavigator> {
        noCompositionLocalProvidedFor("LocalDestinationsNavigator")
    }

val LocalMoveDestinationPathConverter =
    staticCompositionLocalOf<MoveDestinationPathConverter> {
        noCompositionLocalProvidedFor("LocalMoveDestinationPathConverter")
    }

val LocalUseDarkTheme =
    compositionLocalOf { false }

private fun noCompositionLocalProvidedFor(name: String): Nothing {
    throw UninitializedPropertyAccessException("$name not provided")
}
